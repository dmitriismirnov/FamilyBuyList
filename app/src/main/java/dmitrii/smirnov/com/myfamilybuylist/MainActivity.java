package dmitrii.smirnov.com.myfamilybuylist;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import dmitrii.smirnov.com.myfamilybuylist.database.Events;
import dmitrii.smirnov.com.myfamilybuylist.database.FirebaseHelper;
import dmitrii.smirnov.com.myfamilybuylist.database.Purchase;
import dmitrii.smirnov.com.myfamilybuylist.database.Users;
import dmitrii.smirnov.com.myfamilybuylist.recyclerView.DataManager;
import dmitrii.smirnov.com.myfamilybuylist.recyclerView.RecyclerClickListener;

public class MainActivity extends BaseActivity {

    RecyclerView recyclerView;
    DataManager dataManager;
    ArrayList<Purchase> USER_PURCHASE_LIST = new ArrayList<>();
    ArrayList<ArrayList<Purchase>> FRIENDS_PURCHASE_LIST = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPersistenceEnabled();

        initUI();


        FloatingActionButton fabTest = (FloatingActionButton) findViewById(R.id.fab_test);
        fabTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeShortToast("made for tests");
            }
        });
        fabTest.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserAuthorized();
    }


    void checkUserAuthorized() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startLoginActivity();
        } else {
            checkEvents();
            addPurchaseListener();
            updateAllUsersList();
        }
    }


    void checkPersistenceEnabled() {
//        для оффлайн доступа
        if (!PersistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            PersistenceEnabled = true;
        }
    }

    void initUI() {

        recyclerView = (RecyclerView) findViewById(R.id.main_buylist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        dataManager = new DataManager();
        recyclerView.setAdapter(dataManager);
        recyclerView.addOnItemTouchListener(new RecyclerClickListener(this, new RecyclerClickListener.OnItemMotionEventListener() {
            @Override
            public void onItemClick(View view, int position) {
                changePurchaseBought(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                buildEditOrRemoveDialog(position);
            }

            @Override
            public void onScroll(boolean scrollDown) {
                if (scrollDown) {
                    hideFAB();
                } else {
                    showFAB();
                }
            }
        }));

        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_add_purchase);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildAddPurchaseDialog();
            }
        });

        FloatingActionButton fabErase = (FloatingActionButton) findViewById(R.id.fab_erase_list);
        fabErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildEraseListDialog();
            }
        });

    }


    void updateAllUsersList() {

        Query query = FirebaseHelper.mDatabaseUsers
                .orderByChild(FirebaseHelper.uEMAIL);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allUsers.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Users user = childSnapshot.getValue(Users.class);
                    allUsers.put(user.getUid(), user);
//                    makeShortToast(user.getEmail() + " added!");
                }

                updateFriendsList();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                makeShortToast("update all users: " + databaseError.toString());
            }
        });


    }

    void updateFriendsList() {

        showProgressDialog("friends list");

        FirebaseHelper
                .mDatabaseEvents
                .orderByChild(FirebaseHelper.eTO)
                .equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean hasFriends = false;
                        currentUserFriends.clear();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Events event = childSnapshot.getValue(Events.class);
                            makeShortToast("event: " + event.getMessage());
                            if (event.isSolved() && event.getTypeOfEvent() == Events.FRIEND_INVITE) {
                                currentUserFriends
                                        .add(allUsers.get(event.getFromUserId()));
                                Log.d("ADD FRIEND ", allUsers.get(event.getFromUserId()).getEmail());
//                                makeShortToast("ADD FRIEND " + allUsers.get(event.getFromUserId()).getEmail());
                                hasFriends = true;
                            }
                        }
                        if (hasFriends) {
                            addListenersForFriends();
                        } else {
                            makeShortToast("has no friends");
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        makeShortToast("no friends added, " + databaseError.toString());
                    }
                });
    }

    void addPurchaseListener() {
        FirebaseHelper
                .mDatabaseRows
                .child(currentUser.getUid())
                .orderByChild(FirebaseHelper.pNAME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        USER_PURCHASE_LIST.clear();
                        int i = 0;
                        for (DataSnapshot iterator : dataSnapshot.getChildren()) {
                            Purchase purchase = iterator.getValue(Purchase.class);
                            Log.d("PURCHASE CHECK", "#" + ++i + ": " + purchase.toString());
                            USER_PURCHASE_LIST.add(purchase);
                        }
                        updateRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    void addListenersForFriends() {
        FRIENDS_PURCHASE_LIST.clear();

        for (int i = 0; i < currentUserFriends.size(); i++) {
            ArrayList<Purchase> list = new ArrayList<>();
            FRIENDS_PURCHASE_LIST.add(i, list);
            addPurchaseListener(currentUserFriends.get(i).getUid(), i);
        }
    }

    void addPurchaseListener(String userId, final int friendIndex) {
        FirebaseHelper
                .mDatabaseRows
                .child(userId)
                .orderByChild(FirebaseHelper.pNAME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FRIENDS_PURCHASE_LIST.get(friendIndex).clear();
                        int i = 0;
                        for (DataSnapshot iterator : dataSnapshot.getChildren()) {
                            Purchase purchase = iterator.getValue(Purchase.class);
                            Log.d("PURCHASE CHECK", "#" + ++i + ": " + purchase.toString());
                            FRIENDS_PURCHASE_LIST.get(friendIndex).add(purchase);
                        }
                        updateRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    void updateRecyclerView() {
        hideProgressDialog();
        Purchase.PURCHASE_LIST.clear();
        Purchase.PURCHASE_LIST.addAll(USER_PURCHASE_LIST);
//        Purchase.PURCHASE_LIST.addAll(FRIENDS_PURCHASE_LIST);
        for (int i = 0; i < FRIENDS_PURCHASE_LIST.size(); i++) {
            Purchase.PURCHASE_LIST.addAll(FRIENDS_PURCHASE_LIST.get(i));
        }
        Collections.sort(Purchase.PURCHASE_LIST, new PurchaseComparator());
        dataManager.notifyDataSetChanged();
    }

    private class PurchaseComparator implements Comparator<Purchase> {
        public int compare(Purchase left, Purchase right) {
            return left.getName().compareTo(right.getName());
        }
    }

    private void showFAB() {
        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_add_purchase);
        FloatingActionButton fabErase = (FloatingActionButton) findViewById(R.id.fab_erase_list);
        if (fabAdd.getVisibility()==View.GONE) {
            fabAdd.setVisibility(View.VISIBLE);
            fabErase.setVisibility(View.VISIBLE);
            animateView(fabAdd, R.anim.enlarge);
            animateView(fabErase, R.anim.enlarge);
        }
    }

    private void hideFAB() {
        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_add_purchase);
        FloatingActionButton fabErase = (FloatingActionButton) findViewById(R.id.fab_erase_list);
        if (fabAdd.getVisibility() == View.VISIBLE) {
            animateView(fabAdd, R.anim.shrink);
            animateView(fabErase, R.anim.shrink);
            fabAdd.setVisibility(View.GONE);
            fabErase.setVisibility(View.GONE);
        }
    }

    private void changePurchaseBought(int position) {
        Purchase.PURCHASE_LIST.get(position).setBought(
                !Purchase.PURCHASE_LIST.get(position).isBought());

        dataManager.notifyItemChanged(position);

        FirebaseHelper.updatePurchase(new Purchase(
                        Purchase.PURCHASE_LIST.get(position).getName(),
                        !Purchase.PURCHASE_LIST.get(position).isBought(),
                        Purchase.PURCHASE_LIST.get(position).getUserId()),
                Purchase.PURCHASE_LIST.get(position));
    }

    private void buildAddPurchaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);

        TextView tv = (TextView) layout.findViewById(R.id.dialog_tv);
        tv.setText(R.string.enter_name_of_purchase);

        final EditText et = (EditText) layout.findViewById(R.id.dialog_et);
//        et.requestFocus();
        et.setSelection(0);

        builder
                .setView(layout)
                .setTitle(R.string.add_purchase)
                .setIcon(R.drawable.ic_add)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        TODO: add item here
                        String s = et.getText().toString();
                        if (s.equals("")) {
                            makeShortToast(getString(R.string.invalid_name));
                        } else {
                            Purchase purchase = new Purchase(s);
                            Purchase.PURCHASE_LIST.add(purchase);
                            FirebaseHelper.addPurchase(purchase);
                            animateView(findViewById(R.id.fab_add_purchase), R.anim.rotate);
                            dataManager.notifyItemInserted(Purchase.PURCHASE_LIST.size());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        do nothing
                    }
                })
                .create()
                .show();
    }

    private void buildEditOrRemoveDialog(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);

        final Purchase purchase = Purchase.PURCHASE_LIST.get(position);

        TextView tv = (TextView) layout.findViewById(R.id.dialog_tv);
        tv.setText(purchase.getName()
                + "\n"
                + getString(R.string.edit_or_remove));

        EditText et = (EditText) layout.findViewById(R.id.dialog_et);
        et.setVisibility(View.GONE);

        builder
                .setView(layout)
                .setTitle("")
                .setIcon(R.drawable.ic_question_answer)
                .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        makeShortToast("EDIT" + purchase.getName());
                        buildEditDialog(position);
                    }
                })
                .setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        makeShortToast("REMOVE " + purchase.getName());
                        buildRemoveDialog(position);
                    }
                })
                .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        do nothing
                    }
                })
                .create()
                .show();
    }

    private void buildEditDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);

        final Purchase oldPurchase = Purchase.PURCHASE_LIST.get(position);
        final Purchase purchase = new Purchase(oldPurchase.getName(), oldPurchase.isBought(), oldPurchase.getUserId());

        TextView tv = (TextView) layout.findViewById(R.id.dialog_tv);
        tv.setText(getString(R.string.edit_position));

        final EditText et = (EditText) layout.findViewById(R.id.dialog_et);
        et.setText(purchase.getName());

        builder
                .setView(layout)
                .setTitle(R.string.edit)
                .setIcon(R.drawable.ic_edit)
                .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        TODO: edit here
                        String s = et.getText().toString();
                        if (s.equals("")) {
                            makeShortToast(getString(R.string.invalid_name));
                        } else {
                            purchase.setName(s);
                            Purchase.PURCHASE_LIST.set(position, purchase);
                            FirebaseHelper.updatePurchase(oldPurchase, purchase);
                            dataManager.notifyItemChanged(position);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        do nothing
                    }
                })
                .create()
                .show();
    }

    private void buildRemoveDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);

        final Purchase purchase = Purchase.PURCHASE_LIST.get(position);

        TextView tv = (TextView) layout.findViewById(R.id.dialog_tv);
        tv.setText(purchase.getName()
                + "\n"
                + getString(R.string.sure));

        EditText et = (EditText) layout.findViewById(R.id.dialog_et);
        et.setVisibility(View.GONE);

        builder
                .setView(layout)
                .setTitle(R.string.remove)
                .setIcon(R.drawable.ic_remove)
                .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseHelper.removePurchase(purchase);
                        Purchase.PURCHASE_LIST.remove(position);
                        dataManager.notifyItemRemoved(position);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        do nothing
                    }
                })
                .create()
                .show();
    }

    private void buildEraseListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);

        TextView tv = (TextView) layout.findViewById(R.id.dialog_tv);
        tv.setText(R.string.erase_list_question);

        EditText et = (EditText) layout.findViewById(R.id.dialog_et);
        et.setVisibility(View.GONE);

        builder
                .setView(layout)
                .setTitle(getString(R.string.remove))
                .setIcon(R.drawable.ic_question_answer)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eraseBoughtItems();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        do nothing
                    }
                })
                .create()
                .show();
    }

    private void eraseBoughtItems() {

        ArrayList<Purchase> list = new ArrayList<>();

        for (int i = 0; i < Purchase.PURCHASE_LIST.size(); i++) {
            if (Purchase.PURCHASE_LIST.get(i).isBought()) {
                list.add(Purchase.PURCHASE_LIST.get(i));
            }
        }

        if (list.size() == 0) {
            makeShortToast(getString(R.string.nothing_to_remove));
            animateView(findViewById(R.id.fab_erase_list), R.anim.shake);
        } else {

            for (int i = 0; i < list.size(); i++) {
                FirebaseHelper.removePurchase(list.get(i));
            }

            Purchase.PURCHASE_LIST.removeAll(list);

            animateView(findViewById(R.id.fab_erase_list), R.anim.rotate_back);
            dataManager.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile:
                startUserProfileActivity();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    void startUserProfileActivity() {
        Intent i = new Intent(this, UserProfileActivity.class);
        startActivity(i);
    }

    void startLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    void animateView(View view, int animationId) {
        view.startAnimation(AnimationUtils.loadAnimation(this, animationId));
    }

}
