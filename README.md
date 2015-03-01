# gradle-android-test-plugin [![](http://ci.novoda.com/buildStatus/icon?job=gradle-android-test-plugin)](http://ci.novoda.com/job/gradle-android-test-plugin/lastBuild/console) [![](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)

Runs your Robolectric Android unit tests.


## Description

To run unit tests for your android code:

  * Create a new java module outside the android module that you want to test
  * Apply the android-test plugin to the test module
  * Add your tests under scr/test/java
  * Don't forget to add a dependency from your test module to the android module e.g.

  ```groovy
  dependencies {
      testCompile 'junit:junit:4.11'
      testCompile 'org.mockito:mockito-core:1.9.5'
      testCompile 'com.squareup:fest-android:1.0.+'
      testCompile 'org.robolectric:robolectric:2.1.+'
  }
  ```

  * Last but not least, tell the plugin which is the android module that you want to test

  ```groovy
  android {
      projectUnderTest ':novoda-app'
  }
  ```


## Adding to your project

To start using this library, add these lines to the `build.gradle` of your project:

```groovy
apply plugin: 'java'
apply plugin: 'android-test'

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'com.novoda:gradle-android-test-plugin:0.10.3'
    }
}
```


## Simple usage

These are the commands you can use once it's all set up:

  * Run `./gradlew test` to run all tests
  * Run `./gradlew test{FlavorName}` to run tests for a single flavor
  * Run `./gradlew test -Pmatching=**/*TestClassName*` to run specific a test class
  * Run `./gradlew test -Pmatching=**/special/**/*` to run some tests under a package
  * You can also pass additional JVM params: `./gradlew test -PmaxParallelForks=4 -PmaxHeapSize=4096m -PforkEvery=150 -PtestDebug=true`


## Links

Here are a list of useful links:

 * We always welcome people to contribute new features or bug fixes, [here is how](https://github.com/novoda/novoda/blob/master/CONTRIBUTING.md)
 * If you have a problem check the [Issues Page](https://github.com/novoda/gradle-android-test-plugin/issues) first to see if we are working on it
 * For further usage or to delve more deeply checkout the [Project Wiki](https://github.com/novoda/gradle-android-test-plugin/wiki)
 * Looking for community help, browse the already asked [Stack Overflow Questions](http://stackoverflow.com/questions/tagged/support-gradle-android-test) or use the tag: `support-gradle-android-test` when posting a new question
