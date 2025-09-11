package com.example.pizzaapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pizza_mania.db";
    // Bump version so onUpgrade runs for older installs
    private static final int DB_VERSION = 2;

    // ---- users ----
    private static final String T_USERS = "users";
    private static final String C_ID = "id";
    private static final String C_NAME = "full_name";
    private static final String C_EMAIL = "email";
    private static final String C_PW_HASH = "password_hash";
    private static final String C_CREATED_AT = "created_at";

    // ---- menu_items ----
    private static final String T_MENU = "menu_items";
    private static final String M_ID = "id";
    private static final String M_NAME = "name";
    private static final String M_SUB = "subtitle";
    private static final String M_CAT = "category";
    private static final String M_PRICE = "price";
    private static final String M_RATING = "rating";
    private static final String M_IMG = "image_name"; // drawable name, e.g., pizza_margherita

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // users
        db.execSQL(
                "CREATE TABLE " + T_USERS + " (" +
                        C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        C_NAME + " TEXT NOT NULL, " +
                        C_EMAIL + " TEXT NOT NULL UNIQUE, " +
                        C_PW_HASH + " TEXT NOT NULL, " +
                        C_CREATED_AT + " INTEGER DEFAULT (strftime('%s','now'))" +
                        ");"
        );

        // menu_items
        db.execSQL(
                "CREATE TABLE " + T_MENU + " (" +
                        M_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        M_NAME + " TEXT NOT NULL, " +
                        M_SUB + " TEXT, " +
                        M_CAT + " TEXT, " +
                        M_PRICE + " REAL, " +
                        M_RATING + " REAL DEFAULT 0, " +
                        M_IMG + " TEXT" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Add menu table when upgrading from v1
        if (oldVersion < 2) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + T_MENU + " (" +
                            M_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            M_NAME + " TEXT NOT NULL, " +
                            M_SUB + " TEXT, " +
                            M_CAT + " TEXT, " +
                            M_PRICE + " REAL, " +
                            M_RATING + " REAL DEFAULT 0, " +
                            M_IMG + " TEXT" +
                            ");"
            );
        }
    }

    // ---------- USERS API ----------
    public boolean registerUser(String fullName, String email, String plainPassword) {
        if (emailExists(email)) return false;
        String hash = sha256(plainPassword);
        ContentValues cv = new ContentValues();
        cv.put(C_NAME, fullName);
        cv.put(C_EMAIL, email);
        cv.put(C_PW_HASH, hash);
        long rowId = getWritableDatabase().insert(T_USERS, null, cv);
        return rowId != -1;
    }

    public boolean checkLogin(String email, String plainPassword) {
        String hash = sha256(plainPassword);
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + C_ID + " FROM " + T_USERS + " WHERE " + C_EMAIL + "=? AND " + C_PW_HASH + "=?",
                new String[]{email, hash}
        );
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public boolean emailExists(String email) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + T_USERS + " WHERE " + C_EMAIL + "=?",
                new String[]{email}
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ---------- MENU API ----------
    public long insertMenuItem(String name, String subtitle, String category,
                               double price, float rating, String imageName) {
        ContentValues cv = new ContentValues();
        cv.put(M_NAME, name);
        cv.put(M_SUB, subtitle);
        cv.put(M_CAT, category);
        cv.put(M_PRICE, price);
        cv.put(M_RATING, rating);
        cv.put(M_IMG, imageName);
        return getWritableDatabase().insert(T_MENU, null, cv);
    }

    public int countMenuItems() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T_MENU, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    /** Seed a small starter menu only once */
    public void seedMenuIfEmpty() {
        if (countMenuItems() > 0) return;
        insertMenuItem("Margherita",   "Classic Pizza", "Pizza", 1590, 4.8f, "pizza_margherita");
        insertMenuItem("Pepperoni",    "Double Cheese", "Pizza", 1790, 4.7f, "pizza_pepperoni");
        insertMenuItem("Veggie",       "Fresh Garden",  "Pizza", 1490, 4.6f, "pizza_veggie");
        insertMenuItem("BBQ Chicken",  "Smoky & Sweet", "Pizza", 1890, 4.5f, "pizza_bbq");
        insertMenuItem("Hawaiian",     "Pineapple Hit", "Pizza", 1690, 4.2f, "pizza_hawaiian");
        insertMenuItem("Meat Lovers",  "Loaded Feast",  "Pizza", 1990, 4.9f, "pizza_meatlovers");
        // Add more as you like
    }

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + M_ID + "," + M_NAME + "," + M_SUB + "," + M_CAT + "," + M_PRICE + "," + M_RATING + "," + M_IMG +
                        " FROM " + T_MENU + " ORDER BY " + M_RATING + " DESC", null);
        while (c.moveToNext()) {
            out.add(new MenuItem(
                    c.getLong(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getDouble(4),
                    c.getFloat(5),
                    c.getString(6)
            ));
        }
        c.close();
        return out;
    }

    public List<MenuItem> searchMenuItems(String query) {
        List<MenuItem> out = new ArrayList<>();
        String q = "%" + query + "%";
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + M_ID + "," + M_NAME + "," + M_SUB + "," + M_CAT + "," + M_PRICE + "," + M_RATING + "," + M_IMG +
                        " FROM " + T_MENU +
                        " WHERE " + M_NAME + " LIKE ? OR " + M_SUB + " LIKE ? OR " + M_CAT + " LIKE ?" +
                        " ORDER BY " + M_RATING + " DESC",
                new String[]{q, q, q}
        );
        while (c.moveToNext()) {
            out.add(new MenuItem(
                    c.getLong(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getDouble(4), c.getFloat(5), c.getString(6)
            ));
        }
        c.close();
        return out;
    }
}
