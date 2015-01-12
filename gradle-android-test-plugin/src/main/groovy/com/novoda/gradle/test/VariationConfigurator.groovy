package com.novoda.gradle.test

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test

class VariationConfigurator {

    private static final String TEST_DIR = AndroidTestPluginExtension.TEST_DIR
    private static final String TEST_TASK_NAME = AndroidTestPluginExtension.TEST_TASK_NAME
    private static final String TEST_REPORT_DIR = AndroidTestPluginExtension.TEST_REPORT_DIR

    private final Project project
    private final Configuration testConfiguration
    private final Task testTask

    VariationConfigurator(Project project, Configuration testConfiguration, Task testTask) {
        this.project = project
        this.testConfiguration = testConfiguration
        this.testTask = testTask
    }

    public void configure(variant) {
        VariationInfo variationInfo = new VariationInfo(variant)
        TestInfo testInfo = new TestInfo(project, testConfiguration, variant)

        JavaPluginConvention javaConvention = project.convention.getPlugin JavaPluginConvention
        SourceSet variationSources = javaConvention.sourceSets.create "test$variationInfo.variationName"
        variationSources.resources.srcDirs file("src/$TEST_DIR/resources")

        // Configure the compile task for every language supported
        SourceSetConfigurator configurator = new SourceSetConfigurator(project)
        new SupportedLanguages(project).each { String language ->
            configurator.configureCompileTestTask(language, variationSources, testInfo, variationInfo)
        }

        // Clear out the group/description of the classes plugin so it's not top-level.
        def testClassesTask = project.tasks.getByName variationSources.classesTaskName
        testClassesTask.group = null
        testClassesTask.description = null

        // Add the output of the test file compilation to the existing test classpath to create
        // the runtime classpath for test execution.
        testInfo.testRunClasspath.add files("$project.buildDir/resources/test$variationInfo.variationName")

        // Create a task which runs the compiled test classes.
        def taskRunName = "$TEST_TASK_NAME$variationInfo.variationName"
        def testRunTask = project.tasks.create(taskRunName, Test)
        testRunTask.dependsOn testClassesTask
        testRunTask.inputs.sourceFiles.from.clear()
        testRunTask.classpath = testInfo.testRunClasspath
        testRunTask.testClassesDir = testInfo.testDestinationDir
        testRunTask.group = JavaBasePlugin.VERIFICATION_GROUP
        testRunTask.description = "Run unit tests for Build '$variationInfo.variationName'."
        testRunTask.reports.html.destination = file("$project.buildDir/$TEST_REPORT_DIR/$variant.dirName")
        testRunTask.doFirst {
            // Prepend the Android runtime onto the classpath.
            testRunTask.classpath = testInfo.testRunClasspath
        }

        // Work around http://issues.gradle.org/browse/GRADLE-1682
        testRunTask.scanForTestClasses = false


        if (project.hasProperty("matching")) {
          testRunTask.include project.matching
        } else {
          testRunTask.include '**/*Test.class'
          testRunTask.include '**/*Spec.class'
        }

        if (project.hasProperty("maxParallelForks")) {
            testRunTask.setMaxParallelForks(project.maxParallelForks.toInteger())
        }

        if (project.hasProperty("maxHeapSize")) {
            testRunTask.setMaxHeapSize(project.maxHeapSize)
        }

        if (project.hasProperty("forkEvery")) {
            testRunTask.setForkEvery(project.maxParallelForks.toInteger())
        }

        // Add the path to the correct manifest, resources, assets as a system property.
        testRunTask.systemProperties.put('android.manifest', variationInfo.processedManifestPath)
        testRunTask.systemProperties.put('android.resources', variationInfo.processedResourcesPath)
        testRunTask.systemProperties.put('android.assets', variationInfo.processedAssetsPath)

        testTask.reportOn testRunTask

        log("----------------------------------------")
        log("build type name: $variationInfo.buildTypeName")
        log("project flavor name: $variationInfo.projectFlavorName")
        log("variation name: $variationInfo.variationName")
        log("manifest: $variationInfo.processedManifestPath")
        log("resources: $variationInfo.processedResourcesPath")
        log("assets: $variationInfo.processedAssetsPath")
        log("test sources: $variationSources.java.asPath")
        log("test resources: $variationSources.resources.asPath")
        log("----------------------------------------")
    }

    private File file(Object path) {
        project.file(path)
    }

    private FileCollection files(Object... files) {
        project.files(files)
    }

    private void log(String message) {
        project.logger.debug(message)
    }
}
