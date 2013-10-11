package com.novoda.gradle.test

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidTestPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("android", AndroidTestPluginExtension)
        project.android.testProject = project
    }
}