package com.github.gfx.util.encrypt;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import java.io.File;

public class EncryptedSharedPreferencesTest extends AndroidTestCase {

    private SharedPreferences prefs;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Context context = getContext();
        assert context != null;
        prefs = new EncryptedSharedPreferences(context);
        //prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void tearDown() throws Exception {
        prefs.edit()
                .clear()
                .apply();

        super.tearDown();
    }

    public void testString() throws Exception {
        prefs.edit().putString("foo", "bar").apply();

        assert prefs.getString("foo", "*").equals("bar");
    }

    public void testStringDefaultValue() throws Exception {
        assert prefs.getString("foo", "*").equals("*");
    }

    public void testInt() throws Exception {
        prefs.edit().putInt("foo", 42).apply();

        assert prefs.getInt("foo", 10) == 42;
    }

    public void testIntDefaultValue() throws Exception {
        assert prefs.getInt("foo", 10) == 10;
    }

    public void testLong() throws Exception {
        prefs.edit().putLong("foo", 42L).apply();

        assert prefs.getLong("foo", 10L) == 42L;
    }

    public void testLongDefaultValue() throws Exception {
        assert prefs.getLong("foo", 10L) == 10L;
    }

    public void testFloat() throws Exception {
        prefs.edit().putFloat("foo", 42.1f).apply();

        assert prefs.getFloat("foo", 10.1f) == 42.1f;
    }

    public void testFloatDefaultValue() throws Exception {
        assert prefs.getFloat("foo", 10.1f) == 10.1f;
    }

    public void testBoolean() throws Exception {
        prefs.edit().putBoolean("foo", true).apply();

        assert prefs.getBoolean("foo", false);
    }

    public void testBooleanDefaultValue() throws Exception {
        assert prefs.getBoolean("foo", true);
    }


    public void testCommit() throws Exception {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("foo", "bar");

        assert prefs.getString("foo", "*").equals("*");

        assert editor.commit();

        assert prefs.getString("foo", "*").equals("bar");
    }

    public void testRemove() throws Exception {
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

    private File getSharedPrefsFile(String name) {
        Context context = getContext();
        assert context != null;
        File appDir = context.getFilesDir().getParentFile();
        File sharedPrefsDir = new File(appDir, "shared_prefs");
        return new File(sharedPrefsDir, name + ".xml");
    }

    public void testFileEncrypted() throws Exception {
        prefs.edit()
                .putString("foo", "xyzzy")
                .commit();

        File sharedPrefsFile = getSharedPrefsFile(
                EncryptedSharedPreferences.getDefaultPreferenceName(getContext()));
        assert sharedPrefsFile.exists();

        String content = FileUtils.readFileToString(sharedPrefsFile, "UTF-8");
        assert !content.contains("xyzzy");
    }
}
