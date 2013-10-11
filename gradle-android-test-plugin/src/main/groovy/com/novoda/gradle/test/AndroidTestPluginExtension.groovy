package com.novoda.gradle.test

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.TestReport

class AndroidTestPluginExtension {

    static final String TEST_DIR = 'test'
    static final String TEST_TASK_NAME = 'test'
    static final String TEST_CLASSES_DIR = 'test-classes'
    static final String TEST_REPORT_DIR = 'test-report'

    Project testProject

    def projectUnderTest(Project androidProject) {

        def hasAppPlugin = androidProject.plugins.hasPlugin "android"
        def hasLibraryPlugin = androidProject.plugins.hasPlugin "android-library"

        if (!hasAppPlugin && !hasLibraryPlugin) {
            throw new IllegalStateException("The 'android' or 'android-library' plugin is required.")
        } else if (hasAppPlugin && hasLibraryPlugin) {
            throw new IllegalStateException(
                    "Having both 'android' and 'android-library' plugin is not supported.")
        }
        // Get the 'test' configuration for test-only dependencies.
        Configuration testConfiguration = testProject.configurations.getByName('testCompile')

        // Replace the root 'test' task for running all unit tests.
        Task testTask = testProject.tasks.replace(TEST_TASK_NAME, TestReport)
        testTask.destinationDir = testProject.file("$testProject.buildDir/$TEST_REPORT_DIR")
        testTask.description = 'Runs all unit tests.'
        testTask.group = JavaBasePlugin.VERIFICATION_GROUP
        // Add our new task to Gradle's standard "check" task.
        testProject.tasks.check.dependsOn testTask

        def androidPlugin = androidProject.plugins.getPlugin(hasAppPlugin ? "android" : "android-library");
        def androidRuntime = androidPlugin.getRuntimeJarList().join(File.pathSeparator)

        def variants = hasAppPlugin ? androidProject.android.applicationVariants :
                androidProject.android.libraryVariants

        VariantConfigurator variantConfigurator = new VariantConfigurator(testProject, androidRuntime, testConfiguration, testTask)
        variants.all { variant ->
            variantConfigurator.configure(variant)
        }
    }
}

