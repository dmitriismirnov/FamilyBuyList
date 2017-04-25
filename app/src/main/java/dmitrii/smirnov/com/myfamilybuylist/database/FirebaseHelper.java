package dmitrii.smirnov.com.myfamilybuylist.database;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Дмитрий on 23.04.2017.
 */

public class FirebaseHelper {

    final public static String USERS = "users", PURCHASES = "purchases", EVENTS = "events";
    final public static String pNAME = "name", pBOUGHT = "bought", pUSERID = "userId";
    final public static String eFROM = "fromUserId", eTO = "toUserId", eMESSAGE = "message", eTYPE = "typeOfEvent", eSOLVED = "solved";
    final public static String uEMAIL = "email", uID = "uid";

    public static DatabaseReference mDatabaseRows = FirebaseDatabase.getInstance().getReference(PURCHASES);
    public static DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference(USERS);
    public static DatabaseReference mDatabaseEvents = FirebaseDatabase.getInstance().getReference(EVENTS);


    public static void addUser(Users user) {
        DatabaseReference userRef = mDatabaseUsers.getRef();
        String key = userRef.push().getKey();
        userRef.child(key).setValue(user);
    }

    public static void addEvent(Events event) {
        DatabaseReference ref = mDatabaseEvents.getRef();
        String key = ref.push().getKey();
        ref.child(key).setValue(event);
    }

    public static void addPurchase(Purchase purchase) {
        DatabaseReference ref = mDatabaseRows.child(purchase.getUserId()).getRef();
        String key = ref.push().getKey();
        ref.child(key).setValue(purchase);
    }


    public static void updatePurchase(final Purchase oldPurchase, final Purchase newPurchase) {

        mDatabaseRows
                .child(oldPurchase.getUserId())
                .orderByChild(pNAME)
                .equalTo(oldPurchase.getName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = "fuck";

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Purchase compare = childSnapshot.getValue(Purchase.class);

                            if (oldPurchase.getName().equals(compare.getName())
                                    && oldPurchase.isBought() == compare.isBought()) {
                                key = childSnapshot.getKey();
                                Log.d("KEY FROM CHILDREN:", key);
                            }
                        }

                        if (key.equals("fuck")) {
                            Log.d("updatePurchase: ", key);
                        } else {

                            Map updateMap = new HashMap();
                            updateMap.put("/" + pNAME, newPurchase.getName());
                            updateMap.put("/" + pBOUGHT, newPurchase.isBought());
                            updateMap.put("/" + pUSERID, newPurchase.getUserId());
                            mDatabaseRows
                                    .child(oldPurchase.getUserId())
                                    .child(key)
                                    .updateChildren(updateMap);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("updateUser cancel:", databaseError.getMessage());
                    }
                });
    }
/*
    public static void updatePurchase(String key, Purchase purchase) {

        Map updateMap = new HashMap();
        updateMap.put("/" + pNAME, purchase.getName());
        updateMap.put("/" + pBOUGHT, purchase.isBought());
        updateMap.put("/" + pUSERID, purchase.getUserId());

        mDatabaseRows
                .child(purchase.getUserId())
                .child(key)
                .updateChildren(updateMap);
    }
  */

    public static void removePurchase(final Purchase purchase) {
        mDatabaseRows
                .child(purchase.getUserId())
                .orderByChild(pNAME)
                .equalTo(purchase.getName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = "fuck";

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                            key = childSnapshot.getKey();
                            Log.d("KEY FROM CHILDREN:", key);

                        }

                        if (key.equals("fuck")) {
                            Log.d("deleteEvent: ", key);

                        } else {
                            mDatabaseRows
                                    .child(purchase.getUserId())
                                    .child(key)
                                    .removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public static void deleteEvent(final Events event) {
        mDatabaseEvents
                .orderByChild(eFROM)
                .equalTo(event.getFromUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = "fuck";

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Events check = childSnapshot.getValue(Events.class);
                            if (check.getToUserId().equals(event.getToUserId())
                                    && check.getTypeOfEvent() == event.getTypeOfEvent()
                                    && check.isSolved() == event.isSolved()) {
                                key = childSnapshot.getKey();
                                Log.d("KEY FROM CHILDREN:", key);
                            }
                        }

                        if (key.equals("fuck")) {
                            Log.d("deleteEvent: ", key);

                        } else {
                            mDatabaseEvents
                                    .child(key)
                                    .removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static void confirmFriendEvent(final Events event) {
//         Обновить событие (solved = true), создать дублирующие событие с поменянными местами юзверями; создать простое сообщение типа все пляшут
        mDatabaseEvents
                .orderByChild(eFROM)
                .equalTo(event.getFromUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = "fuck";

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Events check = childSnapshot.getValue(Events.class);
                            if (check.getToUserId().equals(event.getToUserId())
                                    && check.getTypeOfEvent() == event.getTypeOfEvent()
                                    && check.isSolved() == event.isSolved()) {
                                key = childSnapshot.getKey();
                                Log.d("KEY FROM CHILDREN:", key);
                            }
                        }

                        if (key.equals("fuck")) {
                            Log.d("eventConfirm: ", key);
                        } else {
                            Map updateMap = new HashMap();
                            updateMap.put("/" + eSOLVED, true);
                            mDatabaseEvents
                                    .child(key)
                                    .updateChildren(updateMap);

//                            добавляем взаимную дружбу
                            addEvent(new Events(event.getToUserId(), event.getFromUserId(), "confirm", Events.FRIEND_INVITE, true));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static void refuseFriendEvent(final Events event) {
//         Поменять местами fromUserId и toUserId; Поменять тип события на простое сообщение, типа отказано ололо
        mDatabaseEvents
                .orderByChild(eFROM)
                .equalTo(event.getFromUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = "fuck";

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Events check = childSnapshot.getValue(Events.class);
                            if (check.getToUserId().equals(event.getToUserId())
                                    && check.getTypeOfEvent() == event.getTypeOfEvent()
                                    && check.isSolved() == event.isSolved()) {
                                key = childSnapshot.getKey();
                                Log.d("KEY FROM CHILDREN:", key);
                            }
                        }

                        if (key.equals("fuck")) {
                            Log.d("eventRefuse: ", key);
                        } else {
                            Map updateMap = new HashMap();
                            updateMap.put("/" + eTYPE, Events.SIMPLE_MESSAGE);
                            updateMap.put("/" + eFROM, event.getToUserId());
                            updateMap.put("/" + eTO, event.getFromUserId());
                            updateMap.put("/" + eMESSAGE, "refuse");
                            mDatabaseEvents
                                    .child(key)
                                    .updateChildren(updateMap);

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public static ArrayList<Purchase> getPurchaseListFromShapshot(DataSnapshot dataSnapshot) {
        ArrayList<Purchase> list = new ArrayList<>();

        for (DataSnapshot iterator : dataSnapshot.getChildren()) {
            Purchase purchase = iterator.getValue(Purchase.class);
            list.add(purchase);

        }
        return list;

    }

    public static ArrayList<FirebaseUser> getUserListFromSnapshot(DataSnapshot dataSnapshot) {
        ArrayList<FirebaseUser> list = new ArrayList<>();

        for (DataSnapshot iterator : dataSnapshot.getChildren()) {
            FirebaseUser user = iterator.getValue(FirebaseUser.class);
            list.add(user);
        }

        return list;
    }


}
