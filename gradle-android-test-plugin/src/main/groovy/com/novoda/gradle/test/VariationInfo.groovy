package com.novoda.gradle.test

class VariationInfo {

    String buildTypeName
    List projectFlavorNames
    String projectFlavorName
    String variationName

    def processedManifestPath
    def processedResourcesPath
    def processedAssetsPath

    VariationInfo(variant) {
        // Get the build type name (e.g., "Debug", "Release").
        buildTypeName = variant.buildType.name.capitalize()
        projectFlavorNames = variant.productFlavors.collect { it.name.capitalize() }
        projectFlavorName = projectFlavorNames.join()
        // The combination of flavor and type yield a unique "variation". This value is used for
        // looking up existing associated tasks as well as naming the task we are about to create.
        variationName = "$projectFlavorName$buildTypeName"
        // Grab the task which outputs the merged manifest, resources, and assets for this flavor.
        processedManifestPath = variant.outputs[0].processManifest.manifestOutputFile
        processedResourcesPath = variant.mergeResources.outputDir
        processedAssetsPath = variant.mergeAssets.outputDir
    }
}
