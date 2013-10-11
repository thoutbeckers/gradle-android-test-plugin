package com.novoda.gradle.test
import org.gradle.api.Plugin

class AndroidTestPlugin implements Plugin<org.gradle.api.Project> {

    @Override
    void apply(org.gradle.api.Project project) {
        project.extensions.create("android", AndroidTestPluginExtension)
        project.android.testProject = project
    }
}