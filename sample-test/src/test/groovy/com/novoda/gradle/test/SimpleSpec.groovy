package com.novoda.gradle.test

import spock.lang.Specification

class SimpleSpec extends Specification {

    def "it is not null"() {
        expect:
        new Simple() != null
    }
}