package com.novoda.gradle.test
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.TestReport
import org.gradle.plugins.ide.idea.model.Project

class AndroidTestPluginExtension {

    public static final String TEST_DIR = 'test'
    private static final String TEST_TASK_NAME = 'test'
    private static final String TEST_CLASSES_DIR = 'test-classes'
    private static final String TEST_REPORT_DIR = 'test-report'

    Project testProject

    def projectUnderTest(androidProject) {

        def hasAppPlugin = androidProject.plugins.hasPlugin "android"
        def hasLibraryPlugin = androidProject.plugins.hasPlugin "android-library"

        ensureAndroidPluginIsApplied(hasAppPlugin, hasLibraryPlugin)

        // Get the 'test' configuration for test-only dependencies.
        def testConfiguration = testProject.configurations.getByName('testCompile')

        // Replace the root 'test' task for running all unit tests.
        def testTask = testProject.tasks.replace(TEST_TASK_NAME, TestReport)
        testTask.destinationDir = testProject.file("$testProject.buildDir/$TEST_REPORT_DIR")
        testTask.description = 'Runs all unit tests.'
        testTask.group = JavaBasePlugin.VERIFICATION_GROUP
        // Add our new task to Gradle's standard "check" task.
        testProject.tasks.check.dependsOn testTask

        def androidPlugin = androidProject.plugins.getPlugin(hasAppPlugin ? "android" : "android-library");
        def androidRuntime = androidPlugin.getRuntimeJarList().join(File.pathSeparator)

        def variants = hasAppPlugin ? androidProject.android.applicationVariants :
                androidProject.android.libraryVariants

        VariantConfigurator variantConfigurator = new VariantConfigurator(testProject, testConfiguration, androidRuntime, testTask)
        variants.all { variant ->
            variantConfigurator.configure(variant)
        }
    }

    def ensureAndroidPluginIsApplied(hasAppPlugin, hasLibraryPlugin) {
        if (!hasAppPlugin && !hasLibraryPlugin) {
            throw new IllegalStateException("The 'android' or 'android-library' plugin is required.")
        } else if (hasAppPlugin && hasLibraryPlugin) {
            throw new IllegalStateException(
                    "Having both 'android' and 'android-library' plugin is not supported.")
        }
    }
}

