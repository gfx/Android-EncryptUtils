# Android-EncryptUtils [![Build Status](https://travis-ci.org/gfx/Android-EncryptUtils.svg)](https://travis-ci.org/gfx/Android-EncryptUtils) [![Coverage Status](https://img.shields.io/coveralls/gfx/Android-EncryptUtils.svg)](https://coveralls.io/r/gfx/Android-EncryptUtils?branch=master)

This is a set of class libraries that provides a way to save credentials in Android devices.

Note that this is not perfectly secure because private keys could not be concealed so the attacker
are able to decrypt data if they have the device and enough time. However, this library should
prevent data from 10-munutes cracking.

## Gradle Dependencies

```gradle
dependencies {
    compile 'com.github.gfx.util.encrypt:android-encrypt-utils:1.2.+'
}
```

## Encryption

This is a utility to encrypt and decrypt credentials.
`Encryption` creates a private key from `context`'s
packag name and `ANDROID_ID` by default.

```java
Encryption encryption = new Encryption(context);
String plainText = ...;
String encrypted = encryption.encrypt(plainText);
String decrypted = encryption.decrypt(encrypted);

assert plainText.equals(decrypted);
```

You can also specify a private key.

```java
byte[] privateKey = ...;
assert privateKey.length == 16; // you must ensure!
Encryption encryption = new Encryption(privateKey);
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
