package com.github.gfx.util.encrypt;

import android.content.ContentResolver;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@SuppressWarnings({"Assert", "Deprecated"})
public class LegacyEncryptionTest extends AndroidTestCase {

    public void testTooShortPrivateKey() throws Exception {
        try {
            new Encryption("?");
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testTooLongPrivateKey() throws Exception {
        try {
            new Encryption(StringUtils.repeat(".", Encryption.KEY_LENGTH+1));
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }


    public void testEncryptDecrypt() throws Exception {
        for (int privateKeyPattern = 0; privateKeyPattern < 10; privateKeyPattern++) {
            String privateKey = RandomStringUtils.randomAscii(16);
            Encryption encryption = new Encryption(privateKey);

            for (int len = 1; len < 10000; len *= 2) {
                for (int i = 0; i < 100; i++) {
                    String s = RandomStringUtils.randomAscii(len);
                    String encrypted = encryption.encrypt(s);
                    String decrypted = encryption.decrypt(encrypted);
                    assert !s.equals(encrypted);
                    assert decrypted.equals(s);
                }
            }
        }
    }

    public void testMultiByteString() throws Exception {
        String privateKey = RandomStringUtils.randomAscii(16);
        Encryption encryption = new Encryption(privateKey);

        String s = "日本語の混じった文字列。 Hello, world!";
        String encrypted = encryption.encrypt(s);
        String decrypted = encryption.decrypt(encrypted);

        assert !s.equals(encrypted);
        assert decrypted.equals(s);

        String decrypted2nd = encryption.decrypt(encrypted);

        assert decrypted2nd.equals(decrypted);
    }

    public void testUsingDefaultPrivateKey() throws Exception {
        Encryption encryption = new Encryption(getContext());

        String s = "Hello, world!";
        String encrypted = encryption.encrypt(s);
        String decrypted = encryption.decrypt(encrypted);

        assert !s.equals(encrypted);
        assert decrypted.equals(s);

        String decrypted2nd = new Encryption(getContext()).decrypt(encrypted);

        assert decrypted2nd.equals(decrypted);
    }

    public void testBadEncryption() throws Exception {
        Encryption encryption = new Encryption(getContext());

        try {
            encryption.decrypt("foo");
            fail();
        } catch (Encryption.UnexpectedDecryptionStateException e) {
            // ok
        }
    }
}

