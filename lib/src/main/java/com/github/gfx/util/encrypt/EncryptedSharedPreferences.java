package com.github.gfx.util.encrypt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.Map;
import java.util.Set;

public class EncryptedSharedPreferences implements SharedPreferences {

    public static String getDefaultPrivateKey(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private final SharedPreferences prefs;

    private final Encryption encryption;

    public EncryptedSharedPreferences(Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context), getDefaultPrivateKey(context));
    }

    public EncryptedSharedPreferences(SharedPreferences sharedPreferences, String privateKey) {
        prefs = sharedPreferences;
        encryption = new Encryption(privateKey);
    }

    @Override
    public Map<String, ?> getAll() {
        return null;
    }

    @Override
    public String getString(String key, String defValue) {
        return null;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return null;
    }

    @Override
    public int getInt(String key, int defValue) {
        return 0;
    }

    @Override
    public long getLong(String key, long defValue) {
        return 0;
    }

    @Override
    public float getFloat(String key, float defValue) {
        return 0;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return false;
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public Editor edit() {
        return new EncryptedEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {

    }

    private class EncryptedEditor implements Editor {

        @Override
        public Editor putString(String key, String value) {
            return null;
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            return null;
        }

        @Override
        public Editor putInt(String key, int value) {
            return null;
        }

        @Override
        public Editor putLong(String key, long value) {
            return null;
        }

        @Override
        public Editor putFloat(String key, float value) {
            return null;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            return null;
        }

        @Override
        public Editor remove(String key) {
            return null;
        }

        @Override
        public Editor clear() {
            return null;
        }

        @Override
        public boolean commit() {
            return false;
        }

        @Override
        public void apply() {

        }
    }
}
