package com.github.gfx.util.encrypt;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

public class EncryptedSharedPreferencesTest extends AndroidTestCase {

    private SharedPreferences prefs;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Context context = getContext();
        assert context != null;
        prefs = new EncryptedSharedPreferences(context);
    }

    @Override
    public void tearDown() throws Exception {
        for(String key : prefs.getAll().keySet()) {
            prefs.edit()
                    .remove(key)
                    .apply();
        }

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

}
