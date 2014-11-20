package com.github.gfx.util.encrypt;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import android.util.Log;

import java.security.Provider;
import java.util.Arrays;

import javax.crypto.Cipher;

@SuppressWarnings("Assert")
public class EncryptionTest extends AndroidTestCase {

    public void testDefaultPrivateKeyForContext() throws Exception {
        final Context context = getContext();

        byte[] k1 = Encryption.getDefaultPrivateKey(new MockContext() {
            @Override
            public String getPackageName() {
                return "a";
            }

            @Override
            public ContentResolver getContentResolver() {
                return context.getContentResolver();
            }
        });
        byte[] k2 = Encryption.getDefaultPrivateKey(new MockContext() {
            @Override
            public String getPackageName() {
                return "b";
            }

            @Override
            public ContentResolver getContentResolver() {
                return context.getContentResolver();
            }
        });

        assert !Arrays.equals(k1, k2);
    }

    public void testTooShortPrivateKey() throws Exception {
        try {
            new Encryption(Encryption.getDefaultCipher(), "?");
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testTooLongPrivateKey() throws Exception {
        try {
            new Encryption(Encryption.getDefaultCipher(), StringUtils.repeat(".", Encryption.KEY_LENGTH+1));
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }


    public void testEncryptDecrypt() throws Exception {
        for (int privateKeyPattern = 0; privateKeyPattern < 10; privateKeyPattern++) {
            String privateKey = RandomStringUtils.randomAscii(16);
            Encryption encryption = new Encryption(Encryption.getDefaultCipher(), privateKey);

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
        Encryption encryption = new Encryption(Encryption.getDefaultCipher(), privateKey);

        String s = "日本語の混じった文字列。 Hello, world!";
        String encrypted = encryption.encrypt(s);
        String decrypted = encryption.decrypt(encrypted);

        assert !s.equals(encrypted);
        assert decrypted.equals(s);

        String decrypted2nd = encryption.decrypt(encrypted);

        assert decrypted2nd.equals(decrypted);
    }

    public void testUsingDefaultPrivateKey() throws Exception {
        Encryption encryption = new Encryption(Encryption.getDefaultCipher(), getContext());

        String s = "Hello, world!";
        String encrypted = encryption.encrypt(s);
        String decrypted = encryption.decrypt(encrypted);

        assert !s.equals(encrypted);
        assert decrypted.equals(s);

        String decrypted2nd = new Encryption(Encryption.getDefaultCipher(), getContext()).decrypt(encrypted);

        assert decrypted2nd.equals(decrypted);
    }

    public void testBadEncryption() throws Exception {
        Encryption encryption = new Encryption(Encryption.getDefaultCipher(), getContext());

        try {
            encryption.decrypt("foo");
            fail();
        } catch (Encryption.UnexpectedDecryptionStateException e) {
            // ok
        }
    }
}

