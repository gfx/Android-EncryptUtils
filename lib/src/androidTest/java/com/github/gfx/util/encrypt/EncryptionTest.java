package com.github.gfx.util.encrypt;

import org.apache.commons.lang3.RandomStringUtils;

import android.test.AndroidTestCase;

public class EncryptionTest extends AndroidTestCase {
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
    }
}
