# The Revision History of Android-EncryptUtils

## NEXT

* Add new interfaces that takes a `javax.crypto.Cipher` instance and deprecate old ones
  * Deprecated interfaces uses AES/CTR/PKC5Padding algorithm mode with the default provider, which could **break existing data on OS updates**
* Add Encryption.getDefaultCipher() to get a Cipher instance with AES/CTR/PKC5Padding with AndroidOpenSSL provider

## v1.2.0 2014-06-18 07:50:43+0900

* Change the atrifact id from `encrypt-utils` to `android-encrypt-utils`

## v1.1.0 2014-06-18 07:32:18+0900

* Add a constructor `new EncryptedSharedPreferences(SharedPreferences, Context)`,
  where the second argument is used to make a default private key.

## v1.0.0 2014-06-15 11:54:49+0900

* Initial release
