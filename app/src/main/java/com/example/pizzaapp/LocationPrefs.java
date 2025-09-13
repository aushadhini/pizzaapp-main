package com.example.pizzaapp;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationPrefs {
    private static final String PREF_NAME = "location_prefs";
    private static final String K_ADDR = "addr";
    private static final String K_LAT  = "lat";
    private static final String K_LNG  = "lng";

    private final SharedPreferences sp;

    public LocationPrefs(Context ctx) {
        sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void save(String address, double lat, double lng) {
        sp.edit()
                .putString(K_ADDR, address == null ? "" : address)
                .putString(K_LAT,  String.valueOf(lat))
                .putString(K_LNG,  String.valueOf(lng))
                .apply();
    }

    public boolean has() {
        String a = sp.getString(K_ADDR, "");
        return a != null && !a.trim().isEmpty();
    }

    public String address() { return sp.getString(K_ADDR, ""); }

    public double lat() {
        try { return Double.parseDouble(sp.getString(K_LAT, "0")); } catch (Exception e) { return 0; }
    }

    public double lng() {
        try { return Double.parseDouble(sp.getString(K_LNG, "0")); } catch (Exception e) { return 0; }
    }

    public void clear() { sp.edit().clear().apply(); }
}
