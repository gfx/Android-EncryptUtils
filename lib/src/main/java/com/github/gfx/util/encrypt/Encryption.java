package com.github.gfx.util.encrypt;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class performs encryption and decryption for plain texts.
 * Note that this class is <strong>not thread-safe</strong> so you have to lock calling methods explicitly.
 */
@SuppressLint("Assert")
public class Encryption {

    private static final String TAG = Encryption.class.getSimpleName();

    private static final String ALGORITHM = "AES";

    private static final String ALGORITHM_MODE = ALGORITHM + "/CTR/PKCS5Padding";

    private static final Charset CHARSET = Charset.forName("UTF-8");

    public static final int KEY_LENGTH = 128 / 8;

    @NonNull
    public static byte[] getDefaultPrivateKey(@NonNull Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        byte[] privateKey = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                .getBytes(CHARSET);
        assert privateKey.length == KEY_LENGTH;

        byte[] packageDigest = md5(context.getPackageName().getBytes(CHARSET));
        assert packageDigest.length == KEY_LENGTH;

        for (int i = 0; i < privateKey.length; i++) {
            privateKey[i] ^= packageDigest[i];
        }
        return privateKey;
    }

    private static byte[] md5(byte[] value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        return md5.digest(value);
    }


    @NonNull
    private static SecretKeySpec createKeySpec(@NonNull byte[] privateKey) {
        if (privateKey.length < KEY_LENGTH) {
            throw new IllegalArgumentException("private key is too short."
                    + " Expected=" + KEY_LENGTH + " but got=" + privateKey.length);
        } else if (privateKey.length > KEY_LENGTH) {
            throw new IllegalArgumentException("private key is too long."
                    + " Expected=" + KEY_LENGTH + " but got=" + privateKey.length);
        }
        return new SecretKeySpec(privateKey, ALGORITHM_MODE);
    }


    private final SecretKeySpec privateKey;

    private final Cipher cipher;

    public Encryption(@NonNull Context context) {
        this(getDefaultPrivateKey(context));
    }

    public Encryption(@NonNull String privateKey) {
        this(privateKey.getBytes(CHARSET));
    }

    public Encryption(@NonNull byte[] privateKey) {
        this.privateKey = createKeySpec(privateKey);

        try {
            cipher = Cipher.getInstance(ALGORITHM_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new AssertionError(e);
        }
    }

    @NonNull
    public String encrypt(@NonNull String plainText) {
        byte[] encrypted;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
        } catch (Exception e) {
            throw new UnexpectedEncryptionStateException(e);
        }
        byte[] iv = cipher.getIV();

        byte[] buffer = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, buffer, 0, iv.length);
        System.arraycopy(encrypted, 0, buffer, iv.length, encrypted.length);
        return Base64.encodeToString(buffer, Base64.NO_WRAP);
    }

    @NonNull
    public String decrypt(@NonNull String encrypted) {
        byte[] buffer = Base64.decode(encrypted.getBytes(CHARSET), Base64.NO_WRAP);
        byte[] decrypted;

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey,
                    new IvParameterSpec(buffer, 0, KEY_LENGTH));
            decrypted = cipher.doFinal(buffer, KEY_LENGTH, buffer.length - KEY_LENGTH);
        } catch (Exception e) {
            throw new UnexpectedDecryptionStateException(e);
        }
        return new String(decrypted, CHARSET);
    }

    public class UnexpectedStateException extends RuntimeException {

        public UnexpectedStateException(Throwable throwable) {
            super(throwable);
        }
    }

    public class UnexpectedEncryptionStateException extends UnexpectedStateException {

        public UnexpectedEncryptionStateException(Throwable throwable) {
            super(throwable);
        }
    }

    public class UnexpectedDecryptionStateException extends UnexpectedStateException {

        public UnexpectedDecryptionStateException(Throwable throwable) {
            super(throwable);
        }
    }

}
