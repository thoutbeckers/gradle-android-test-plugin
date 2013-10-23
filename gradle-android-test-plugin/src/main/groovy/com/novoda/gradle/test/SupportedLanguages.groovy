package com.novoda.gradle.test

import org.gradle.api.Project

class SupportedLanguages extends ArrayList {

    SupportedLanguages(Project project) {
        add('java')
        if (project.plugins.hasPlugin('groovy')) {
            add('groovy')
        }
        if (project.plugins.hasPlugin('scala')) {
            add('scala')
        }
    }
}
