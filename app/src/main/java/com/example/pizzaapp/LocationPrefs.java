package com.example.pizzaapp;

import android.content.Context;
import android.content.SharedPreferences;

public final class LocationPrefs {

    // Pref file + keys
    private static final String FILE = "loc_prefs";
    private static final String K_ADDR = "addr";
    private static final String K_LAT  = "lat";
    private static final String K_LNG  = "lng";

    /* ---------------- Instance API ---------------- */

    private final SharedPreferences sp;

    public LocationPrefs(Context context) {
        this.sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Save address and coordinates in one call. */
    public void save(String address, double lat, double lng) {
        sp.edit()
                .putString(K_ADDR, address)
                .putLong(K_LAT, Double.doubleToRawLongBits(lat))
                .putLong(K_LNG, Double.doubleToRawLongBits(lng))
                .apply();
    }

    /** Update only the address (keeps existing lat/lng). */
    public void setAddress(String address) {
        sp.edit().putString(K_ADDR, address).apply();
    }

    /** Update only the coordinates (keeps existing address). */
    public void setLatLng(double lat, double lng) {
        sp.edit()
                .putLong(K_LAT, Double.doubleToRawLongBits(lat))
                .putLong(K_LNG, Double.doubleToRawLongBits(lng))
                .apply();
    }

    /** @return true if we have at least an address saved. */
    public boolean has() {
        return sp.contains(K_ADDR);
    }

    public String address() {
        return sp.getString(K_ADDR, null);
    }

    public Double lat() {
        if (!sp.contains(K_LAT)) return null;
        long bits = sp.getLong(K_LAT, 0L);
        return Double.longBitsToDouble(bits);
    }

    public Double lng() {
        if (!sp.contains(K_LNG)) return null;
        long bits = sp.getLong(K_LNG, 0L);
        return Double.longBitsToDouble(bits);
    }

    public void clear() {
        sp.edit().clear().apply();
    }

    /* ---------------- Static helpers (backward compatible) ---------------- */

    public static void save(Context c, String address, double lat, double lng) {
        new LocationPrefs(c).save(address, lat, lng);
    }

    public static String getAddress(Context c) {
        return new LocationPrefs(c).address();
    }

    public static Double getLat(Context c) {
        return new LocationPrefs(c).lat();
    }

    public static Double getLng(Context c) {
        return new LocationPrefs(c).lng();
    }

    public static boolean has(Context c) {
        return new LocationPrefs(c).has();
    }

    public static void clear(Context c) {
        new LocationPrefs(c).clear();
    }
}
