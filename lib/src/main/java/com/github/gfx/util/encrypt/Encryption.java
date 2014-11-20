package com.github.gfx.util.encrypt;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class performs encryption and decryption for plain texts.
 * Note that this class is <strong>not thread-safe</strong> so you have to lock calling methods
 * explicitly.
 */
@SuppressLint("Assert")
public class Encryption {

    private static final String TAG = Encryption.class.getSimpleName();

    /**
     * The default security provider, "AndroidOpenSSL", which is not available on Android 2.3.x.
     */
    public static final String DEFAULT_PROVIDER = "AndroidOpenSSL";

    /**
     * The default algorithm mode, "AES/CBC/PKCS5Padding".
     */
    public static final String DEFAULT_ALGORITHM_MODE =  "AES/CBC/PKCS5Padding";

    private static final String LEGACY_ALGORITHM_MODE  =  "AES/CTR/PKCS5Padding"; // CTR/PKCS5Padding makes no sense

    private static final Charset CHARSET = Charset.forName("UTF-8");

    public static final int KEY_LENGTH = 128 / 8;

    /**
     * @return A {@link javax.crypto.Cipher} instance with "AES/CBC/PKC5Padding" transformation.
     */
    @NonNull
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static Cipher getDefaultCipher() {
        try {
            return Cipher.getInstance(DEFAULT_ALGORITHM_MODE, DEFAULT_PROVIDER);
        } catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new AssertionError(e);
        }
    }

    @Deprecated
    @NonNull
    public static Cipher getLegacyDefaultCipher() {
        try {
            return Cipher.getInstance(LEGACY_ALGORITHM_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new AssertionError(e);
        }
    }

    @NonNull
    public static byte[] getDefaultPrivateKey(@NonNull Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        byte[] androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                .getBytes(CHARSET);
        assert androidId.length == KEY_LENGTH;

        byte[] packageDigest = md5(context.getPackageName().getBytes(CHARSET));
        assert packageDigest.length == KEY_LENGTH;

        for (int i = 0; i < androidId.length; i++) {
            packageDigest[i] ^= androidId[i];
        }
        return packageDigest; // mix of androidId and packageDigest
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
    private static SecretKeySpec createKeySpec(@NonNull Cipher cipher, @NonNull byte[] privateKey) {
        if (privateKey.length < KEY_LENGTH) {
            throw new IllegalArgumentException("private key is too short."
                    + " Expected=" + KEY_LENGTH + " but got=" + privateKey.length);
        } else if (privateKey.length > KEY_LENGTH) {
            throw new IllegalArgumentException("private key is too long."
                    + " Expected=" + KEY_LENGTH + " but got=" + privateKey.length);
        }
        return new SecretKeySpec(privateKey, cipher.getAlgorithm());
    }


    private final SecretKeySpec secretKeySpec;

    private final Cipher cipher;

    @Deprecated
    public Encryption(@NonNull Context context) {
        this(getLegacyDefaultCipher(), getDefaultPrivateKey(context));
    }

    @Deprecated
    public Encryption(@NonNull String privateKey) {
        this(getLegacyDefaultCipher(), privateKey.getBytes(CHARSET));
    }

    @Deprecated
    public Encryption(@NonNull byte[] privateKey) {
        this(getLegacyDefaultCipher(), privateKey);
    }

    public Encryption(@NonNull Cipher cipher, @NonNull Context context) {
        this(cipher, getDefaultPrivateKey(context));
    }

    public Encryption(@NonNull Cipher cipher, @NonNull String privateKey) {
        this(cipher, privateKey.getBytes(CHARSET));
    }

    public Encryption(@NonNull Cipher cipher, @NonNull byte[] privateKey) {
        this(cipher, createKeySpec(cipher, privateKey));
    }

    public Encryption(@NonNull Cipher cipher, @NonNull SecretKeySpec secretKeySpec) {
        this.cipher = cipher;
        this.secretKeySpec = secretKeySpec;
    }

    @NonNull
    public String encrypt(@NonNull String plainText) {
        byte[] encrypted;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
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
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,
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
