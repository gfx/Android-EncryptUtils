# Android-EncryptUtils [![Build Status](https://travis-ci.org/gfx/Android-EncryptUtils.svg)](https://travis-ci.org/gfx/Android-EncryptUtils) [![Coverage Status](https://coveralls.io/repos/gfx/Android-EncryptUtils/badge.png)](https://coveralls.io/r/gfx/Android-EncryptUtils)

This is a set of class libraries that provides a way to save credentials in Android devices.

Note that this is not perfectly secure because private keys could not be concealed so the attacker
are able to decrypt data if they have the device and enough time. However, this library should
prevent data from 10-munutes cracking.

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

This is an implementation of SharedPreferences that encrypts data.

```java
SharedPreferences prefs = new EncryptedSharedPreferences(context);

prefs.editor()
    .putString("email", email)
    .putString("password", password)
    .apply();
```

### HOW DATA ARE STORED

As `SharedPreferences` does, `EncryptedSHaredPreferences` saves data in XML and its values
are encrypted in [AES](http://en.wikipedia.org/wiki/Advanced_Encryption_Standard) while
its keys are just encoded in Base64 format.

The following content is an example of shared preferences file:

```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="Zm9v">WvfCT5pyTP9srHQxf5nKXxH7Cw==</string>
    <string name="YmFy">RpLGPJ736a9vctawIz9IbCBYeA==</string>
</map>
```

## AUTHOR

FUJI Goro (gfx) <gfuji@cpan.org>

## LICENSE

This is a free software licensed in Apache License 2.0. See [LICENSE](LICENSE) for details.
