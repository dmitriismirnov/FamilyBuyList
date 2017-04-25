package dmitrii.smirnov.com.myfamilybuylist;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import dmitrii.smirnov.com.myfamilybuylist.database.Events;
import dmitrii.smirnov.com.myfamilybuylist.database.FirebaseHelper;
import dmitrii.smirnov.com.myfamilybuylist.database.Users;

public class UserProfileActivity extends BaseActivity {

    static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ImageButton photo = (ImageButton) findViewById(R.id.profile_image);
        photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("image/*");
//                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                makeShortToast("not realised yet");
            }
        });
        photo.setVisibility(View.GONE);

        Button btnAddFriend = (Button) findViewById(R.id.profile_add_friend);
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildFriendInviteDialog();
            }
        });

        Button btnSignOut = (Button) findViewById(R.id.profile_sign_out);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startLoginActivity();
            }
        });

//        displayFriends();
    }

    void displayFriends() {
        TextView tv = (TextView) findViewById(R.id.profile_friends_list);
        int friendsCount = currentUserFriends.size();
        if (friendsCount > 0) {
            tv.setText("My friends are:\n");
            for (int i = 0; i < friendsCount; i++) {
                tv.append(currentUserFriends.get(i).toString() + "\n\n");
            }
        } else {
            tv.setText("I have no friends yet :(");
        }
    }

    void startLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

//        Bitmap bitmap = null;
        ImageButton photo = (ImageButton) findViewById(R.id.profile_image);

        switch (requestCode) {
            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    photo.setImageURI(selectedImage);
//                    photo.setImageBitmap(bitmap);
                }
                break;
            default:
                break;
        }
    }

    void buildFriendInviteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.new_event_dialog, null);

        TextView tv = (TextView) layout.findViewById(R.id.new_event_text_view);
        final EditText et = (EditText) layout.findViewById(R.id.new_event_edit_text);

        tv.setText(R.string.enter_friends_email);
        et.setHint("example@gmail.com");
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);


        builder
                .setView(layout)
                .setIcon(R.drawable.ic_add)
                .setTitle("")
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = et.getText().toString();
                        boolean emailFound = false;

                        for (Map.Entry<String, Users> entry : allUsers.entrySet()) {

                            if (entry.getValue().getEmail().equals(email)) {//getValue().getEmail().equals(email)) {
                                emailFound = true;
                                String toId = entry.getKey();
                                String fromId = currentUser.getUid();
                                Events event = new Events(fromId, toId, Events.FRIEND_INVITE);
                                FirebaseHelper.addEvent(event);
                            }
                        }

                        if (!emailFound) {
                            makeShortToast(getString(R.string.user_not_found));
                        }


                    }
                })
                .setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        do nothing
                    }
                })
                .create()
                .show();


    }

}
