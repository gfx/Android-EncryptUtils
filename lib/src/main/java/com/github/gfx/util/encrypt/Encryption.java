package com.github.gfx.util.encrypt;

import android.support.annotation.NonNull;
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_MODE = ALGORITHM + "/CTR/PKCS5Padding";
    private static final int KEY_LENGTH = 128 / 8;

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private final byte[] privateKey;

    public Encryption(@NonNull String privateKey) {
        this.privateKey = privateKey.getBytes(CHARSET);
        assert this.privateKey.length == KEY_LENGTH;
    }

    @NonNull
    private static SecretKeySpec getKey(@NonNull byte[] privateKey) {
        return new SecretKeySpec(privateKey, ALGORITHM_MODE);
    }

    @NonNull
    public String encrypt(@NonNull String plainText)  {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new AssertionError(e);
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getKey(privateKey));
        } catch (InvalidKeyException e) {
            throw new AssertionError(e);
        }
        byte[] encrypted;
        try {
            encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new AssertionError(e);
        }
        byte[] iv = cipher.getIV();

        byte[] buffer = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, buffer, 0, iv.length);
        System.arraycopy(encrypted, 0, buffer, iv.length, encrypted.length);
        return Base64.encodeToString(buffer, Base64.NO_WRAP);
    }

    @NonNull
    public String decrypt(@NonNull String encrypted) throws Exception {
        byte[] buffer = Base64.decode(encrypted.getBytes(CHARSET), Base64.NO_WRAP);

        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE);
        cipher.init(Cipher.DECRYPT_MODE, getKey(privateKey), new IvParameterSpec(buffer, 0, KEY_LENGTH));

        byte[] decrypted = cipher.doFinal(buffer, KEY_LENGTH, buffer.length - KEY_LENGTH);
        return new String(decrypted, CHARSET);
    }
}
