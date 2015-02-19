package com.novoda.gradle.test

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.TestReport

class AndroidTestPluginExtension {

    static final String ANDROID_APPLICATION_PLUGIN = "com.android.application"
    static final String ANDROID_LIBRARY_PLUGIN = "com.android.library"

    static final String TEST_DIR = 'test'
    static final String TEST_TASK_NAME = 'test'
    static final String TEST_CLASSES_DIR = 'test-classes'
    static final String TEST_REPORT_DIR = 'test-report'

    private final Project project

    public variants

    AndroidTestPluginExtension(project) {
        this.project = project
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public void projectUnderTest(String projectUnderTestName) {
        project.evaluationDependsOn(projectUnderTestName)

        Project projectUnderTest = project.project(projectUnderTestName)
        def hasAppPlugin = hasAppPlugin(projectUnderTest)
        def hasLibraryPlugin = hasLibraryPlugin(projectUnderTest)

        if (!hasAppPlugin && !hasLibraryPlugin) {
            throw new IllegalStateException("The '${ANDROID_APPLICATION_PLUGIN}' or '${ANDROID_LIBRARY_PLUGIN}' plugin is required.")
        } else if (hasAppPlugin && hasLibraryPlugin) {
            throw new IllegalStateException("Having both '${ANDROID_APPLICATION_PLUGIN}' and '${ANDROID_LIBRARY_PLUGIN}' plugin is not supported.")
        }

        Configuration testConfiguration = makeTestConfiguration(projectUnderTest)

        TestReport testTask = makeTestTask()

        variants = hasAppPlugin ? projectUnderTest.android.applicationVariants :
                projectUnderTest.android.libraryVariants

        // Configure every build variant
        VariationConfigurator variantConfigurator = new VariationConfigurator(project, testConfiguration, testTask)
        variants.all { variant ->
            variantConfigurator.configure(variant)
        }

        // We don't need these tasks because we will use their 'variant'
        skipCompileTestTasks()
    }

    private void skipCompileTestTasks() {
        def skipCompileTestTask = new SupportedLanguages(project).collect {
            "compileTest${it.capitalize()}"
        }

        for (String name : skipCompileTestTask) {
            Task task = project.tasks.getByName(name)
            task.deleteAllActions()
            println "Task $task.name is being rendered useless"
        }
    }

    private Configuration makeTestConfiguration(Project projectUnderTest) {
        def androidPlugin = projectUnderTest.plugins.getPlugin(hasAppPlugin(projectUnderTest) ? ANDROID_APPLICATION_PLUGIN : ANDROID_LIBRARY_PLUGIN)

        def androidRuntime = project.files(androidPlugin.bootClasspath)

        // Add additional dependencies such as the project under test and the android runtime
        project.dependencies {
            testCompile androidRuntime
            testCompile projectUnderTest
        }

        // Get the 'test' configuration for test-only dependencies.
        project.configurations.getByName('testCompile')
    }

    private boolean hasLibraryPlugin(Project projectUnderTest) {
        projectUnderTest.plugins.hasPlugin ANDROID_LIBRARY_PLUGIN
    }

    private boolean hasAppPlugin(Project projectUnderTest) {
        projectUnderTest.plugins.hasPlugin ANDROID_APPLICATION_PLUGIN
    }

    private TestReport makeTestTask() {
        // Replace the root 'test' task for running all unit tests.
        Task testTask = project.tasks.replace(TEST_TASK_NAME, TestReport)
        testTask.destinationDir = project.file("$project.buildDir/$TEST_REPORT_DIR")
        testTask.description = 'Runs all unit tests.'
        testTask.group = JavaBasePlugin.VERIFICATION_GROUP
        // Add our new task to Gradle's standard "check" task.
        project.tasks.check.dependsOn testTask
        testTask
    }
}

