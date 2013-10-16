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

    private final Project project

    AndroidTestPluginExtension(project) {
        this.project = project
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public void projectUnderTest(Project projectUnderTest) {

        def hasAppPlugin = projectUnderTest.plugins.hasPlugin "android"
        def hasLibraryPlugin = projectUnderTest.plugins.hasPlugin "android-library"

        if (!hasAppPlugin && !hasLibraryPlugin) {
            throw new IllegalStateException("The 'android' or 'android-library' plugin is required.")
        } else if (hasAppPlugin && hasLibraryPlugin) {
            throw new IllegalStateException(
                    "Having both 'android' and 'android-library' plugin is not supported.")
        }
        // Get the 'test' configuration for test-only dependencies.
        Configuration testConfiguration = project.configurations.getByName('testCompile')

        // Replace the root 'test' task for running all unit tests.
        Task testTask = project.tasks.replace(TEST_TASK_NAME, TestReport)
        testTask.destinationDir = project.file("$project.buildDir/$TEST_REPORT_DIR")
        testTask.description = 'Runs all unit tests.'
        testTask.group = JavaBasePlugin.VERIFICATION_GROUP
        // Add our new task to Gradle's standard "check" task.
        project.tasks.check.dependsOn testTask


        def androidPlugin = projectUnderTest.plugins.getPlugin(hasAppPlugin ? "android" : "android-library");

        project.dependencies {
            testCompile project.files(androidPlugin.runtimeJarList)
            testCompile projectUnderTest
        }

        def variants = hasAppPlugin ? projectUnderTest.android.applicationVariants :
                projectUnderTest.android.libraryVariants

        VariationConfigurator variantConfigurator = new VariationConfigurator(project, testConfiguration, testTask)
        variants.all { variant ->
            variantConfigurator.configure(variant)
        }
    }
}

