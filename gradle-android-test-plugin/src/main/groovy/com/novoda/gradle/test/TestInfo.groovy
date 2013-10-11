package com.novoda.gradle.test

import org.gradle.api.Task
import org.gradle.api.file.FileCollection

class TestInfo {

    private static final String TEST_CLASSES_DIR = AndroidTestPluginExtension.TEST_CLASSES_DIR

    Task androidCompile
    File testDestinationDir
    FileCollection testCompileClasspath
    FileCollection testRunClasspath

    TestInfo(project, testConfiguration, variant) {
        androidCompile = variant.javaCompile;

        FileCollection testDestinationDirCollection = project.files(
                "$project.buildDir/$TEST_CLASSES_DIR/$variant.dirName")

        testDestinationDir = testDestinationDirCollection.getSingleFile()

        // Add the corresponding java compilation output to the 'testCompile' configuration to
        // create the classpath for the test file compilation.
        testCompileClasspath = testConfiguration.plus project.files(androidCompile.destinationDir, androidCompile.classpath)

        testRunClasspath = testCompileClasspath.plus testDestinationDirCollection
    }
}
