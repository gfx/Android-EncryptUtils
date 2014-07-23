# The Revision History of Android-EncryptUtils

## v1.2.1 2014-07-23 23:29:49+0900

* Fix a crash issue where the length of ANDROID_ID < 16 (#2 #3; thanks to tomorrowkey)

## v1.2.0 2014-06-18 07:50:43+0900

* Change the atrifact id from `encrypt-utils` to `android-encrypt-utils`

## v1.1.0 2014-06-18 07:32:18+0900

* Add a constructor `new EncryptedSharedPreferences(SharedPreferences, Context)`,
  where the second argument is used to make a default private key.

## v1.0.0 2014-06-15 11:54:49+0900

* Initial release
