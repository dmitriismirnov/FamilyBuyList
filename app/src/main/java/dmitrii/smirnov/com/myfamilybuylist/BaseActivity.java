package dmitrii.smirnov.com.myfamilybuylist;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import dmitrii.smirnov.com.myfamilybuylist.database.Events;
import dmitrii.smirnov.com.myfamilybuylist.database.FirebaseHelper;
import dmitrii.smirnov.com.myfamilybuylist.database.Users;

/**
 * Created by Дмитрий on 23.04.2017.
 */

public class BaseActivity extends AppCompatActivity {

    public static FirebaseUser currentUser = null;
    public static ArrayList<Users> currentUserFriends = new ArrayList<>();
    public static HashMap<String, Users> allUsers = new HashMap<>();
    static boolean PersistenceEnabled = false;
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void showProgressDialog(String message) {
        hideProgressDialog();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading) + " " + message);
        mProgressDialog.setIndeterminate(true);

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    void makeShortToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    void makeLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    void checkEvents() {

        showProgressDialog();
        FirebaseHelper
                .mDatabaseEvents
                .orderByChild(FirebaseHelper.eTO)
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hideProgressDialog();
                        for (DataSnapshot childrenSnapshot : dataSnapshot.getChildren()) {
                            Events event = childrenSnapshot.getValue(Events.class);
                            if (!event.isSolved()) {
                                showEvent(event);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        makeShortToast(databaseError.getMessage());
                        hideProgressDialog();

                    }
                });
    }

    void showEvent(Events event) {
        switch (event.getTypeOfEvent()) {
            case Events.FRIEND_INVITE:
                buildFriendInviteEventDialog(event);
                break;
//            case Events.SIMPLE_MESSAGE:
//                buildMessageEventDialog(event);
            default:
                break;
        }
    }

    void buildFriendInviteEventDialog(final Events event) {
        AlertDialog.Builder dialogEventBuilder = new AlertDialog.Builder(this);
        LinearLayout eventLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.new_event_dialog, null);

        TextView textView = (TextView) eventLayout.findViewById(R.id.new_event_text_view);
        eventLayout.findViewById(R.id.new_event_edit_text).setVisibility(View.GONE);

        Users author = allUsers.get(event.getFromUserId());
        String s = author.getName() + " " + author.getEmail() + " " + getResources().getString(R.string.msg_friend_invite);

        textView.setText(s);


        dialogEventBuilder
                .setView(eventLayout)
                .setIcon(R.drawable.ic_question_answer)
                .setTitle("")
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseHelper.confirmFriendEvent(event);
                    }
                })
                .setNegativeButton(getString(R.string.refuse), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseHelper.refuseFriendEvent(event);
                    }
                })
                .create()
                .show();

    }

    static String getDisplayNameOrEmail(String userId) {
        String s = allUsers.get(userId).getName();
        if (s.isEmpty()) {
            s = allUsers.get(userId).getEmail();
        }
        return s;
    }


}
