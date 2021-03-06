package com.github.gfx.util.encrypt;

import org.apache.commons.io.FileUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SuppressLint("Assert")
public class EncryptedSharedPreferencesTest extends AndroidTestCase {
    private boolean defaultCipherNotAvailable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    private SharedPreferences prefs;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (defaultCipherNotAvailable()) return;

        Context context = getContext();
        assert context != null;
        prefs = new EncryptedSharedPreferences(Encryption.getDefaultCipher(), context);
        //prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void tearDown() throws Exception {
        if (defaultCipherNotAvailable()) return;

        if (false) {
            // dump the file content
            String sharedPrefsContent = slurpSharedPrefsFile(
                    EncryptedSharedPreferences.getDefaultPreferenceName(getContext()));
            Log.d("TEST", sharedPrefsContent);
        }
        prefs.edit()
                .clear()
                .apply();
        prefs = null;

        super.tearDown();
        System.gc();
    }

    private String slurpSharedPrefsFile(String name) throws IOException {
        Context context = getContext();
        assert context != null;
        File appDir = context.getFilesDir().getParentFile();
        File sharedPrefsDir = new File(appDir, "shared_prefs");
        File sharedPrefsFile = new File(sharedPrefsDir, name + ".xml");
        return FileUtils.readFileToString(sharedPrefsFile, "UTF-8");
    }

    public void testConstructorInterfaces() throws Exception {
        if (defaultCipherNotAvailable()) return;

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
        new EncryptedSharedPreferences(Encryption.getDefaultCipher(), getContext());
        new EncryptedSharedPreferences(Encryption.getDefaultCipher(), p, getContext());
        new EncryptedSharedPreferences(Encryption.getDefaultCipher(), p, "0123456789abcdef");
        new EncryptedSharedPreferences(p, new Encryption(Encryption.getDefaultCipher(), "0123456789abcdef"));
    }

    public void testString() throws Exception {
        if (defaultCipherNotAvailable()) return;

        prefs.edit().putString("foo", "bar").apply();

        assert prefs.getString("foo", "*").equals("bar");
    }

    public void testStringDefaultValue() throws Exception {
        if (defaultCipherNotAvailable()) return;

        assert prefs.getString("foo", "*").equals("*");
    }

    public void testInt() throws Exception {
        if (defaultCipherNotAvailable()) return;

        prefs.edit().putInt("foo", 42).apply();

        assert prefs.getInt("foo", 10) == 42;
    }

    public void testIntDefaultValue() throws Exception {
        if (defaultCipherNotAvailable()) return;

        assert prefs.getInt("foo", 10) == 10;
    }

    public void testLong() throws Exception {
        if (defaultCipherNotAvailable()) return;

        prefs.edit().putLong("foo", 42L).apply();

        assert prefs.getLong("foo", 10L) == 42L;
    }

    public void testLongDefaultValue() throws Exception {
        if (defaultCipherNotAvailable()) return;

        assert prefs.getLong("foo", 10L) == 10L;
    }

    public void testFloat() throws Exception {
        if (defaultCipherNotAvailable()) return;

        prefs.edit().putFloat("foo", 42.1f).apply();

        assert prefs.getFloat("foo", 10.1f) == 42.1f;
    }

    public void testFloatDefaultValue() throws Exception {
        if (defaultCipherNotAvailable()) return;

        assert prefs.getFloat("foo", 10.1f) == 10.1f;
    }

    public void testBoolean() throws Exception {
        if (defaultCipherNotAvailable()) return;

        prefs.edit().putBoolean("foo", true).apply();

        assert prefs.getBoolean("foo", false);
    }

    public void testBooleanDefaultValue() throws Exception {
        if (defaultCipherNotAvailable()) return;

        assert prefs.getBoolean("foo", true);
    }

    public void testContains() throws Exception {
        if (defaultCipherNotAvailable()) return;

        prefs.edit().putString("foo", "bar").apply();

        assert prefs.contains("foo");
        assert !prefs.contains("bar");
    }

    public void testAll() throws Exception {
        if (defaultCipherNotAvailable()) return;

        prefs.edit()
                .putString("foo", "aaa")
                .putString("bar", "bbb")
                .apply();

        Map<String, ?> map = prefs.getAll();
        assert map.size() == 2;
        assert map.get("foo").equals("aaa");
        assert map.get("bar").equals("bbb");
        // TODO: other vale types?
    }

    public void testCommit() throws Exception {
        if (defaultCipherNotAvailable()) return;

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("foo", "bar");

        assert prefs.getString("foo", "*").equals("*");

        assert editor.commit();

        assert prefs.getString("foo", "*").equals("bar");
    }

    public void testRemove() throws Exception {
        if (defaultCipherNotAvailable()) return;

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("foo", "aaa");
        editor.putString("bar", "bbb");
        assert editor.commit();

        assert prefs.edit()
                .remove("bar")
                .commit();

        assert prefs.getString("foo", "*").equals("aaa");
        assert prefs.getString("bar", "*").equals("*");
    }

    public void testClear() throws Exception {
        if (defaultCipherNotAvailable()) return;

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("foo", "aaa");
        editor.putString("bar", "bbb");
        assert editor.commit();

        assert prefs.edit()
                .clear()
                .commit();

        assert prefs.getString("foo", "*").equals("*");
        assert prefs.getString("bar", "*").equals("*");
    }

    public void testFileEncrypted() throws Exception {
        if (defaultCipherNotAvailable()) return;

        final CountDownLatch latch = new CountDownLatch(1);
        prefs.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                            String key) {
                        latch.countDown();
                    }
                }
        );
        prefs.edit()
                .putString("foo", "xyzzy")
                .apply();
        assert latch.await(10, TimeUnit.SECONDS);

        String sharedPrefsContent = slurpSharedPrefsFile(
                EncryptedSharedPreferences.getDefaultPreferenceName(getContext()));

        assert !sharedPrefsContent.contains("xyzzy");
    }

    public void testRegisterOnSharedPreferenceChangeListenerForPut() throws Exception {
        if (defaultCipherNotAvailable()) return;

        String key = "testRegisterOnSharedPreferenceChangeListenerForPut";

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> events = new ArrayList<>();

        prefs.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                            String key) {
                        // prefs might be null depending on timing
                        // assert sharedPreferences == prefs;
                        events.add(key);
                        latch.countDown();
                    }
                }
        );

        assert prefs.edit().putString(key, "bar").commit();
        assert latch.await(10, TimeUnit.SECONDS);

        assert events.size() == 1;
        assert events.contains(key);
    }

    public void testUnregisterOnSharedPreferenceChangeListener() throws Exception {
        if (defaultCipherNotAvailable()) return;

        final CountDownLatch latch = new CountDownLatch(1);
        SharedPreferences.OnSharedPreferenceChangeListener listener
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                latch.countDown();
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
        prefs.unregisterOnSharedPreferenceChangeListener(listener);

        assert prefs.edit()
                .putString("testUnregisterOnSharedPreferenceChangeListener", "bar")
                .commit();
        assert !latch.await(10, TimeUnit.SECONDS) : "successfully timed-out!";
    }

    public void testDifferentPrivateKeys() throws Exception {
        if (defaultCipherNotAvailable()) return;

        SharedPreferences base = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        SharedPreferences prefs1 = new EncryptedSharedPreferences(Encryption.getDefaultCipher(), base, "012345678912345a");
        SharedPreferences prefs2 = new EncryptedSharedPreferences(Encryption.getDefaultCipher(), base, "012345678912345b");

        final CountDownLatch latch = new CountDownLatch(2);
        SharedPreferences.OnSharedPreferenceChangeListener listener
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                    String key) {
                latch.countDown();
            }
        };

        base.registerOnSharedPreferenceChangeListener(listener);

        prefs1.edit()
                .putString("foo", "1")
                .apply();
        prefs2.edit()
                .putString("bar", "2")
                .apply();

        assert latch.await(10, TimeUnit.SECONDS);

        assert prefs1.getString("foo", "*").equals("1");

        try {
            assert !prefs1.getString("bar", "*").equals("*");
            assert !prefs1.getString("bar", "1").equals("2");
        } catch (Encryption.UnexpectedDecryptionStateException e) {
            // ignore
        }

        base.unregisterOnSharedPreferenceChangeListener(listener);
        base.edit().clear().apply();
    }
}
