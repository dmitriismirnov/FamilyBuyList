package dmitrii.smirnov.com.myfamilybuylist.database;

import java.util.ArrayList;

import dmitrii.smirnov.com.myfamilybuylist.BaseActivity;

/**
 * Created by Дмитрий on 23.04.2017.
 */

public class Purchase {
    private String name;
    private boolean bought;
    private String userId;

    public static ArrayList<Purchase> PURCHASE_LIST = new ArrayList<>();


    public Purchase() {
        // empty constructor is needed for firebase
    }

    public Purchase(String name) {
        this.name = name;
        this.bought = false;
        this.userId = BaseActivity.currentUser.getUid();
    }

    public Purchase(String name, boolean bought, String userId) {
        this.name = name;
        this.bought = bought;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "name='" + name + '\'' +
                ", bought=" + bought +
                ", userId='" + userId + '\'' +
                '}';
    }
}
