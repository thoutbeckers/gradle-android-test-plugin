package com.novoda.gradle.test

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet

class SourceSetConfigurator {

    private static final String TEST_DIR = AndroidTestPluginExtension.TEST_DIR

    private final Project project
    private final Object androidRuntime

    SourceSetConfigurator(Project project, androidRuntime) {
        this.project = project
        this.androidRuntime = androidRuntime
    }

    public void configure(SourceSet variationSources, Task compileAndroid, FileCollection testCompileClasspath,
                          def testDestinationDir, String buildTypeName, List projectFlavorNames, String projectFlavorName) {

        variationSources.java.setSrcDirs testSrcDir(buildTypeName, projectFlavorName, projectFlavorNames, 'java')
        Task testCompileTaskJava = compileTestTask(variationSources, variationSources.java, compileAndroid, testCompileClasspath, testDestinationDir, 'java')

        if (project.plugins.hasPlugin('groovy')) {
            variationSources.groovy.setSrcDirs testSrcDir(buildTypeName, projectFlavorName, projectFlavorNames, 'groovy')
            compileTestTask(variationSources, variationSources.groovy, testCompileTaskJava, testCompileClasspath, testDestinationDir, 'groovy')
        }

        if (project.plugins.hasPlugin('scala')) {
            variationSources.scala.setSrcDirs testSrcDir(buildTypeName, projectFlavorName, projectFlavorNames, 'scala')
            compileTestTask(variationSources, variationSources.scala, testCompileTaskJava, testCompileClasspath, testDestinationDir, 'scala')
        }
    }

    private Task compileTestTask(variationSources, compileSourceSet, Task compileAndroid, FileCollection testCompileClasspath, FileCollection testDestinationDir, String language) {
        // Create a task which compiles the test sources.
        def testCompileTask = project.tasks.getByName variationSources.getCompileTaskName(language)
        // Depend on the project compilation (which itself depends on the manifest processing task).
        testCompileTask.dependsOn compileAndroid
        testCompileTask.group = null
        testCompileTask.description = null
        testCompileTask.classpath = testCompileClasspath
        testCompileTask.source = language == compileSourceSet
        testCompileTask.destinationDir = testDestinationDir.getSingleFile()
        testCompileTask.doFirst {
            testCompileTask.options.bootClasspath = androidRuntime
        }
        testCompileTask
    }

    private ArrayList testSrcDir(String buildTypeName, String projectFlavorName, List projectFlavorNames, String language) {
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
}
