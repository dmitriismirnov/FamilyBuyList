package dmitrii.smirnov.com.myfamilybuylist;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dmitrii.smirnov.com.myfamilybuylist.database.FirebaseHelper;
import dmitrii.smirnov.com.myfamilybuylist.database.Users;

public class LoginActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "LoginActivity";
    private EditText mEmailField, mPasswordField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        setAuthStateListener();
        setButtonListeners();


    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void init() {
        mEmailField = (EditText) findViewById(R.id.login_et_email);
        mPasswordField = (EditText) findViewById(R.id.login_et_password);
    }


    private void setAuthStateListener() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                    currentUser=user;
                    startMainActivity();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                    currentUser=null;
                }
            }
        };
    }

    private void setButtonListeners() {
        findViewById(R.id.login_btn_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });

        findViewById(R.id.login_btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(mEmailField.getText().toString(), mPasswordField.getText().toString());

            }
        });
    }

    private boolean validateForms() {
        boolean valid = true;
        View focusView = null;

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(email) || !(email.contains("@") && email.contains("."))) {
            mEmailField.setError(getString(R.string.invalid_email));
            focusView = mEmailField;
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(password) || password.length() < 5) {
            mPasswordField.setError(getString(R.string.invalid_password));
            focusView = mPasswordField;
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        if (!valid) focusView.requestFocus();


        return valid;
    }


    private void signIn(String email, String password) {
        if (!validateForms()) {
            return;
        }
        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            makeShortToast(R.string.auth_failed + task.getException().toString());
                        }
                        hideProgressDialog();
                    }
                });

    }

    private void register(String email, String password) {
        if (!validateForms()) {
            return;
        }
        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            makeShortToast(R.string.auth_failed + task.getException().toString());
                        }

                        if (task.isSuccessful()){
                            FirebaseUser fbUser = task.getResult().getUser();
                            Users user = new Users();
                            user.setEmail(fbUser.getEmail());
                            user.setUid(fbUser.getUid());
                            FirebaseHelper.addUser(user);
                        }
                        hideProgressDialog();
                    }
                });


    }

    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
