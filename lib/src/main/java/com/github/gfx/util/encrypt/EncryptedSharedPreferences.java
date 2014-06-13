package com.github.gfx.util.encrypt;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EncryptedSharedPreferences implements SharedPreferences {

    /* package */ static String getDefaultPreferenceName(@NonNull Context context) {
        return context.getPackageName() + "_preferences_encrypted";
    }

    private static SharedPreferences getDefaultSharedPreferences(@NonNull Context context) {
        String preferenceName = getDefaultPreferenceName(context);
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    private final SharedPreferences prefs;

    private final Encryption encryption;

    public EncryptedSharedPreferences(@NonNull Context context) {
        this(getDefaultSharedPreferences(context), new Encryption(context));
    }

    public EncryptedSharedPreferences(@NonNull SharedPreferences sharedPreferences,
            @NonNull String privateKey) {
        this(sharedPreferences, new Encryption(privateKey));
    }

    public EncryptedSharedPreferences(@NonNull SharedPreferences sharedPreferences,
            @NonNull Encryption encryption) {
        prefs = sharedPreferences;
        this.encryption = encryption;
    }

    @NonNull
    private String encrypt(@NonNull String value) {
        return encryption.encrypt(value);
    }

    @NonNull
    private String decrypt(@NonNull String value) {
        return encryption.decrypt(value);
    }

    @Override
    public Map<String, ?> getAll() {
        Map<String, ?> map = prefs.getAll();
        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                String encrypted = (String) entry.getValue();
                newMap.put(entry.getKey(), decrypt(encrypted));
            } else {
                newMap.put(entry.getKey(), null);
            }
        }
        return newMap;
    }

    @Override
    @Nullable
    public String getString(@NonNull String key, @Nullable String defValue) {
        String encrypted = prefs.getString(key, null);
        return encrypted != null ? decrypt(encrypted) : defValue;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(@NonNull String key, Set<String> defValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(@NonNull String key, int defValue) {
        String encrypted = prefs.getString(key, null);
        return encrypted != null ? Integer.parseInt(decrypt(encrypted)) : defValue;
    }

    @Override
    public long getLong(@NonNull String key, long defValue) {
        String encrypted = prefs.getString(key, null);
        return encrypted != null ? Long.parseLong(decrypt(encrypted)) : defValue;
    }

    @Override
    public float getFloat(@NonNull String key, float defValue) {
        String encrypted = prefs.getString(key, null);
        return encrypted != null ? Float.parseFloat(decrypt(encrypted)) : defValue;
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defValue) {
        String encrypted = prefs.getString(key, null);
        return encrypted != null ? Boolean.parseBoolean(decrypt(encrypted)) : defValue;
    }

    @Override
    public boolean contains(@NonNull String key) {
        return prefs.contains(key);
    }

    @Override
    public Editor edit() {
        return new EncryptedEditor(prefs.edit());
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            @NonNull OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            @NonNull OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private class EncryptedEditor implements Editor {

        private final Editor editor;

        private EncryptedEditor(@NonNull Editor editor) {
            this.editor = editor;
        }

        @Override
        public Editor putString(String key, String value) {
            editor.putString(key, value != null ? encrypt(value) : value);
            return this;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public Editor putStringSet(String key, Set<String> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Editor putInt(String key, int value) {
            editor.putString(key, encrypt(String.valueOf(value)));
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            editor.putString(key, encrypt(String.valueOf(value)));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            editor.putString(key, encrypt(String.valueOf(value)));
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            editor.putString(key, encrypt(String.valueOf(value)));
            return this;
        }

        @Override
        public Editor remove(String key) {
            editor.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            editor.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return editor.commit();
        }

        @Override
        public void apply() {
            editor.apply();
        }
    }
}
