gradle-android-test-plugin
=============================

To run unit tests for your android code:

* Create a new java module outside the android module that you want to test
* Apply the android-test plugin to the test module

```
buildscript {
    repositories {
        mavenCentral()
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots/"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.5.+'
        classpath 'com.novoda:gradle-android-test-plugin:0.9.3-SNAPSHOT'
    }
}
apply plugin: 'java'
apply plugin: 'android-test'
```

* Add your tests under scr/test/java
* Don't forget to add a dependency from your test module to the android module e.g.

```
dependencies {
    testCompile 'junit:junit:4.11'
    testCompile 'org.mockito:mockito-core:1.9.5'
    testCompile 'com.squareup:fest-android:1.0.+'
    testCompile 'org.robolectric:robolectric:2.1.+'
}
```

* Last but not least, tell the plugin which is the android module that you want to test

```
android {
    projectUnderTest ':novoda-app'
}
```

* Congratulation, you can now run unit tests against your android source!
* Known issues: the IDE does not support running a single test in isolation. You have to run the whole suite
* Known boons: plugins respect `testCompile` dependencies and runs JUnit tests, TenstNG, Robolectric and Spock
* Working towards: `scala` and `jacoco` full support

Credits
=============================
The core of this plugin is derived from Jake Wharton's [gradle-android-test-plugin](https://github.com/JakeWharton/gradle-android-test-plugin)
