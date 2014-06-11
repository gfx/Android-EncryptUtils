package com.github.gfx.util.encrypt;

import android.support.annotation.NonNull;
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private static final String ALGORITHM = "AES";

    private static final String ALGORITHM_MODE = ALGORITHM + "/CTR/PKCS5Padding";

    private static final int KEY_LENGTH = 128 / 8;

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final SecretKeySpec privateKey;

    private final Cipher cipher;

    public Encryption(@NonNull String privateKey) {
        this.privateKey = getKey(privateKey);

        try {
            cipher = Cipher.getInstance(ALGORITHM_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new AssertionError(e);
        }
    }

    @NonNull
    private static SecretKeySpec getKey(@NonNull String privateKey) {
        if (privateKey.length() < KEY_LENGTH) {
            for (int i = privateKey.length(); i < 16; i++) {
                privateKey += ".";
            }
        } else if (privateKey.length() > KEY_LENGTH) {
            privateKey = privateKey.substring(0, 16);
        }

        return new SecretKeySpec(privateKey.getBytes(CHARSET), ALGORITHM_MODE);
    }

    @NonNull
    public synchronized String encrypt(@NonNull String plainText) {
        byte[] encrypted;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        byte[] iv = cipher.getIV();

        byte[] buffer = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, buffer, 0, iv.length);
        System.arraycopy(encrypted, 0, buffer, iv.length, encrypted.length);
        return Base64.encodeToString(buffer, Base64.NO_WRAP);
    }

    @NonNull
    public synchronized String decrypt(@NonNull String encrypted) {
        byte[] buffer = Base64.decode(encrypted.getBytes(CHARSET), Base64.NO_WRAP);
        byte[] decrypted;

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey,
                    new IvParameterSpec(buffer, 0, KEY_LENGTH));
            decrypted = cipher.doFinal(buffer, KEY_LENGTH, buffer.length - KEY_LENGTH);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return new String(decrypted, CHARSET);
    }
}
