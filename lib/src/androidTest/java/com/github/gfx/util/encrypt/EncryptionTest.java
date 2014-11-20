package com.github.gfx.util.encrypt;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import java.util.Arrays;

@SuppressWarnings("Assert")
public class EncryptionTest extends AndroidTestCase {
    private boolean defaultCipherNotAvailable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

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
        if (defaultCipherNotAvailable()) return;

        try {
            new Encryption(Encryption.getDefaultCipher(), "?");
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testTooLongPrivateKey() throws Exception {
        if (defaultCipherNotAvailable()) return;

        try {
            new Encryption(Encryption.getDefaultCipher(), StringUtils.repeat(".", Encryption.KEY_LENGTH+1));
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }


    public void testEncryptDecrypt() throws Exception {
        if (defaultCipherNotAvailable()) return;

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
        if (defaultCipherNotAvailable()) return;

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
        if (defaultCipherNotAvailable()) return;

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
        if (defaultCipherNotAvailable()) return;

        Encryption encryption = new Encryption(Encryption.getDefaultCipher(), getContext());

        try {
            encryption.decrypt("foo");
            fail();
        } catch (Encryption.UnexpectedDecryptionStateException e) {
            // ok
        }
    }
}

