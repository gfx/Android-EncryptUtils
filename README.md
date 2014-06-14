# Android-EncryptUtils [![Build Status](https://travis-ci.org/gfx/Android-EncryptUtils.svg)](https://travis-ci.org/gfx/Android-EncryptUtils) [![Coverage Status](https://coveralls.io/repos/gfx/Android-EncryptUtils/badge.png)](https://coveralls.io/r/gfx/Android-EncryptUtils)

This is a set of class libraries that provides a way to save credentials in Android devices.

## Encryption

This is a utility to encrypt and decrypt credentials.

```java
Encryption encryption = new Encryption(context);
String plainText = ...;
String encrypted = encrypt.encrypt(plainText);
String decrypted = encrypt.decrypt(encrypted);

assert plainText.equals(decrypted);
```

## EncrptedSharedPreferences

This is an implementation of SharedPreferences that encrypts data automatically.


```java
SharedPreferences prefs = new EncryptedSharedPreferences(context);

prefs.editor()
    .putString("email", email)
    .putString("password", password)
    .apply();
```

## AUTHOR

FUJI Goro (gfx) <gfuji@cpan.org>

## LICENSE

This is a free software licensed in Apache License 2.0. See [LICENSE](LICENSE) for details.
