package com.github.gfx.util.encrypt;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@link android.content.SharedPreferences} implementation where its values are encrypted by
 * {@link com.github.gfx.util.encrypt.Encryption}.
 *
 * @see com.github.gfx.util.encrypt.Encryption
 */
public class EncryptedSharedPreferences implements SharedPreferences {

    @NonNull
    private static Charset CHARSET = Charset.forName("UTF-8");

    /* package */
    @NonNull
    static String getDefaultPreferenceName(@NonNull Context context) {
        return context.getPackageName() + "_preferences_encrypted";
    }

    @NonNull
    private static SharedPreferences getDefaultSharedPreferences(@NonNull Context context) {
        String preferenceName = getDefaultPreferenceName(context);
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    private final SharedPreferences base;

    private final Encryption encryption;

    private final IdentityHashMap<OnSharedPreferenceChangeListener, OnSharedPreferenceChangeListener>
            listenerWrappers = new IdentityHashMap<>();

    /**
     * Creates a default wrapper class for {@link android.content.Context}. The private key for
     * {@link com.github.gfx.util.encrypt.Encryption} is determined by {@code
     * android.provider.Settings.Secure.ANDROID_ID}.
     *
     * @param context - an application context used to get a base {@link android.content.SharedPreferences}
     *                and {@link com.github.gfx.util.encrypt.Encryption}
     */
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
    private String encodeKey(@NonNull String value) {
        return Base64.encodeToString(value.getBytes(CHARSET), Base64.NO_WRAP);
    }

    @NonNull
    private String decodeKey(@NonNull String value) {
        return new String(Base64.decode(value.getBytes(CHARSET), Base64.NO_WRAP), CHARSET);
    }

    @NonNull
    private String encodeValue(@NonNull String value) {
        return encryption.encrypt(value);
    }

    @NonNull
    private String decodeValue(@NonNull String value) {
        return encryption.decrypt(value);
    }

    @Override
    public synchronized Map<String, ?> getAll() {
        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, ?> entry : base.getAll().entrySet()) {
            String realKey = decodeKey(entry.getKey());
            if (entry.getValue() != null) {
                String encrypted = (String) entry.getValue();
                newMap.put(realKey, decodeValue(encrypted));
            } else {
                newMap.put(realKey, null);
            }
        }
        return newMap;
    }

    @Override
    @Nullable
    public synchronized  String getString(@NonNull String key, @Nullable String defValue) {
        String realKey = encodeKey(key);
        String encoded = base.getString(realKey, null);
        return encoded != null ? decodeValue(encoded) : defValue;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public synchronized Set<String> getStringSet(@NonNull String key, Set<String> defValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized int getInt(@NonNull String key, int defValue) {
        String value = getString(key, null);
        return value != null ? Integer.parseInt(value) : defValue;
    }

    @Override
    public synchronized long getLong(@NonNull String key, long defValue) {
        String value = getString(key, null);
        return value != null ? Long.parseLong(value) : defValue;
    }

    @Override
    public synchronized float getFloat(@NonNull String key, float defValue) {
        String value = getString(key, null);
        return value != null ? Float.parseFloat(value) : defValue;
    }

    @Override
    public synchronized boolean getBoolean(@NonNull String key, boolean defValue) {
        String value = getString(key, null);
        return value != null ? Boolean.parseBoolean(value) : defValue;
    }

    @Override
    public synchronized boolean contains(@NonNull String key) {
        String realKey = encodeKey(key);
        return base.contains(realKey);
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
                listener.onSharedPreferenceChanged(EncryptedSharedPreferences.this, decodeKey(key));
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
        public synchronized Editor putString(@NonNull String key, @Nullable String value) {
            String realKey = encodeKey(key);
            editor.putString(realKey, value != null ? encodeValue(value) : null);
            return this;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public synchronized Editor putStringSet(String key, Set<String> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized Editor putInt(String key, int value) {
            return putString(key, String.valueOf(value));
        }

        @Override
        public synchronized Editor putLong(String key, long value) {
            return putString(key, String.valueOf(value));
        }

        @Override
        public synchronized Editor putFloat(String key, float value) {
            return putString(key, String.valueOf(value));
        }

        @Override
        public synchronized Editor putBoolean(String key, boolean value) {
            return putString(key, String.valueOf(value));
        }

        @Override
        public synchronized  Editor remove(String key) {
            String realKey = encodeKey(key);
            editor.remove(realKey);
            return this;
        }

        @Override
        public synchronized Editor clear() {
            editor.clear();
            return this;
        }

        @Override
        public synchronized boolean commit() {
            return editor.commit();
        }

        @Override
        public synchronized void apply() {
            editor.apply();
        }
    }
}
