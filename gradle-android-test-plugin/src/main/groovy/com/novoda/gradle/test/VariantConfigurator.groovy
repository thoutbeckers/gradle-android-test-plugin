package com.novoda.gradle.test

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test

class VariantConfigurator {

    public static final String TEST_DIR = 'test'
    private static final String TEST_TASK_NAME = 'test'
    private static final String TEST_CLASSES_DIR = 'test-classes'
    private static final String TEST_REPORT_DIR = 'test-report'

    private final Project project
    private final Object testConfiguration
    private final Object androidRuntime
    private final Object testTask

    VariantConfigurator(project, testConfiguration, androidRuntime, testTask) {
        this.project = project
        this.testConfiguration = testConfiguration
        this.androidRuntime = androidRuntime
        this.testTask = testTask
    }

    public void configure(variant) {
        // Get the build type name (e.g., "Debug", "Release").
        def buildTypeName = variant.buildType.name.capitalize()
        def projectFlavorNames = variant.productFlavors.collect { it.name.capitalize() }
        def projectFlavorName = projectFlavorNames.join()
        // The combination of flavor and type yield a unique "variation". This value is used for
        // looking up existing associated tasks as well as naming the task we are about to create.
        def variationName = "$projectFlavorName$buildTypeName"
        // Grab the task which outputs the merged manifest, resources, and assets for this flavor.
        def processedManifestPath = variant.processManifest.manifestOutputFile
        def processedResourcesPath = variant.mergeResources.outputDir
        def processedAssetsPath = variant.mergeAssets.outputDir

        ArrayList testSrcDirs = testSrcDir(buildTypeName, projectFlavorName, projectFlavorNames, 'java')
        ArrayList groovyTestSrcDirs = testSrcDir(buildTypeName, projectFlavorName, projectFlavorNames, 'groovy')

        JavaPluginConvention javaConvention = project.convention.getPlugin JavaPluginConvention
        SourceSet variationSources = javaConvention.sourceSets.create "test$variationName"
        variationSources.resources.srcDirs file("src/$TEST_DIR/resources")
        variationSources.java.setSrcDirs testSrcDirs
        variationSources.groovy.setSrcDirs groovyTestSrcDirs

        log("----------------------------------------")
        log("build type name: $buildTypeName")
        log("project flavor name: $projectFlavorName")
        log("variation name: $variationName")
        log("manifest: $processedManifestPath")
        log("resources: $processedResourcesPath")
        log("assets: $processedAssetsPath")
        log("test sources: $variationSources.java.asPath")
        log("test resources: $variationSources.resources.asPath")
        log("----------------------------------------")

        def javaCompile = variant.javaCompile;

        // Add the corresponding java compilation output to the 'testCompile' configuration to
        // create the classpath for the test file compilation.
        def testCompileClasspath = testConfiguration.plus files(javaCompile.destinationDir, javaCompile.classpath)

        def testDestinationDir = files(
                "$project.buildDir/$TEST_CLASSES_DIR/$variant.dirName")

        // Create a task which compiles the test sources.
        def testCompileTask = project.tasks.getByName variationSources.getCompileTaskName('java')
        // Depend on the project compilation (which itself depends on the manifest processing task).
        testCompileTask.dependsOn javaCompile
        testCompileTask.group = null
        testCompileTask.description = null
        testCompileTask.classpath = testCompileClasspath
        testCompileTask.source = variationSources.java
        testCompileTask.destinationDir = testDestinationDir.getSingleFile()
        testCompileTask.doFirst {
            testCompileTask.options.bootClasspath = androidRuntime
        }

        // Create a task which compiles the test sources.
        def groovyTestCompileTask = project.tasks.getByName variationSources.getCompileTaskName('groovy')
        // Depend on the project compilation (which itself depends on the manifest processing task).
        groovyTestCompileTask.dependsOn testCompileTask
        groovyTestCompileTask.group = null
        groovyTestCompileTask.description = null
        groovyTestCompileTask.classpath = testCompileClasspath
        groovyTestCompileTask.source = variationSources.groovy
        groovyTestCompileTask.destinationDir = testDestinationDir.getSingleFile()
        groovyTestCompileTask.doFirst {
            groovyTestCompileTask.options.bootClasspath = androidRuntime
        }

        // Clear out the group/description of the classes plugin so it's not top-level.
        def testClassesTask = project.tasks.getByName variationSources.classesTaskName
        testClassesTask.group = null
        testClassesTask.description = null

        // Add the output of the test file compilation to the existing test classpath to create
        // the runtime classpath for test execution.
        def testRunClasspath = testCompileClasspath.plus testDestinationDir
        testRunClasspath.add files("$project.buildDir/resources/test$variationName")

        // Create a task which runs the compiled test classes.
        def taskRunName = "$TEST_TASK_NAME$variationName"
        def testRunTask = project.tasks.create(taskRunName, Test)
        testRunTask.dependsOn testClassesTask
        testRunTask.inputs.sourceFiles.from.clear()
        testRunTask.classpath = testRunClasspath
        testRunTask.testClassesDir = testDestinationDir.getSingleFile()
        testRunTask.group = JavaBasePlugin.VERIFICATION_GROUP
        testRunTask.description = "Run unit tests for Build '$variationName'."
        // TODO Gradle 1.7: testRunTask.reports.html.destination =
        testRunTask.testReportDir =
                file("$project.buildDir/$TEST_REPORT_DIR/$variant.dirName")
        testRunTask.doFirst {
            // Prepend the Android runtime onto the classpath.
            testRunTask.classpath = files(androidRuntime).plus testRunClasspath
        }

        // Work around http://issues.gradle.org/browse/GRADLE-1682
        testRunTask.scanForTestClasses = false
        testRunTask.include '**/*Test.class'
        testRunTask.include '**/*Spec.class'

        // Add the path to the correct manifest, resources, assets as a system property.
        testRunTask.systemProperties.put('android.manifest', processedManifestPath)
        testRunTask.systemProperties.put('android.resources', processedResourcesPath)
        testRunTask.systemProperties.put('android.assets', processedAssetsPath)

        testTask.reportOn testRunTask
    }

    private ArrayList testSrcDir(buildTypeName, String projectFlavorName, List projectFlavorNames, String language) {
        def testSrcDirs = []
        testSrcDirs.add(file("src/$TEST_DIR/$language"))
        testSrcDirs.add(file("src/$TEST_DIR$buildTypeName/$language"))
        testSrcDirs.add(file("src/$TEST_DIR$projectFlavorName/$language"))
        projectFlavorNames.each { flavor ->
            testSrcDirs.add file("src/$TEST_DIR$flavor/$language")
        }
        testSrcDirs
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
