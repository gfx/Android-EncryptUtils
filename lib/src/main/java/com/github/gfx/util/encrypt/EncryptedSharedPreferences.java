package com.github.gfx.util.encrypt;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class EncryptedSharedPreferences implements SharedPreferences {

    /* package */
    static String getDefaultPreferenceName(@NonNull Context context) {
        return context.getPackageName() + "_preferences_encrypted";
    }

    private static SharedPreferences getDefaultSharedPreferences(@NonNull Context context) {
        String preferenceName = getDefaultPreferenceName(context);
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    private final SharedPreferences base;

    private final Encryption encryption;

    private final IdentityHashMap<OnSharedPreferenceChangeListener, OnSharedPreferenceChangeListener>
            listenerWrappers = new IdentityHashMap<>();

    public EncryptedSharedPreferences(@NonNull Context context) {
        this(getDefaultSharedPreferences(context), new Encryption(context));
    }

    public EncryptedSharedPreferences(@NonNull SharedPreferences sharedPreferences,
            @NonNull String privateKey) {
        this(sharedPreferences, new Encryption(privateKey));
    }

    public EncryptedSharedPreferences(@NonNull SharedPreferences sharedPreferences,
            @NonNull Encryption encryption) {
        base = sharedPreferences;
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
        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, ?> entry : base.getAll().entrySet()) {
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
        String encrypted = base.getString(key, null);
        return encrypted != null ? decrypt(encrypted) : defValue;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(@NonNull String key, Set<String> defValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(@NonNull String key, int defValue) {
        String encrypted = base.getString(key, null);
        return encrypted != null ? Integer.parseInt(decrypt(encrypted)) : defValue;
    }

    @Override
    public long getLong(@NonNull String key, long defValue) {
        String encrypted = base.getString(key, null);
        return encrypted != null ? Long.parseLong(decrypt(encrypted)) : defValue;
    }

    @Override
    public float getFloat(@NonNull String key, float defValue) {
        String encrypted = base.getString(key, null);
        return encrypted != null ? Float.parseFloat(decrypt(encrypted)) : defValue;
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defValue) {
        String encrypted = base.getString(key, null);
        return encrypted != null ? Boolean.parseBoolean(decrypt(encrypted)) : defValue;
    }

    @Override
    public boolean contains(@NonNull String key) {
        return base.contains(key);
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public Editor edit() {
        return new EncryptedEditor(base.edit());
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            @NonNull final OnSharedPreferenceChangeListener listener) {
        OnSharedPreferenceChangeListener wrapper = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                listener.onSharedPreferenceChanged(EncryptedSharedPreferences.this, key);
            }
        };
        listenerWrappers.put(listener, wrapper);
        base.registerOnSharedPreferenceChangeListener(wrapper);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            @NonNull OnSharedPreferenceChangeListener listener) {
        OnSharedPreferenceChangeListener wrapper = listenerWrappers.get(listener);
        if (wrapper != null) {
            listenerWrappers.remove(listener);
            base.unregisterOnSharedPreferenceChangeListener(wrapper);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        for (OnSharedPreferenceChangeListener w : listenerWrappers.values()) {
            base.unregisterOnSharedPreferenceChangeListener(w);
        }
        super.finalize();
    }

    private class EncryptedEditor implements Editor {

        private final Editor editor;

        private EncryptedEditor(@NonNull Editor editor) {
            this.editor = editor;
        }

        @Override
        public Editor putString(String key, String value) {
            editor.putString(key, value != null ? encrypt(value) : null);
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
