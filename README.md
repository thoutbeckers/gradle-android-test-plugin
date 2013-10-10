gradle-android-test-plugin
=============================

To run unit tests for your android code:

* Create a new module with the name of your android project plus "-test" e.g. "novoda-app" has a "novoda-app-test" module.
* Apply the android-test plugin to the test module
* Add your tests under scr/test/java
* Don't forget to add a dependency from your test module to the android module e.g.

```
dependencies {
    testCompile project(':novoda-app')
    testCompile 'junit:junit:4.11'
    testCompile 'org.mockito:mockito-core:1.9.5'
    testCompile 'com.squareup:fest-android:1.0.+'
    testCompile 'org.robolectric:robolectric:2.1.+'
}
```
