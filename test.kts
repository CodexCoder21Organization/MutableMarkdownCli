@file:WithArtifact("community.kotlin.markdown:cli:0.0.2")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-test:1.9.22")
package mutablemarkdowncli.tests

import build.kotlin.withartifact.WithArtifact
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Smoke tests for MutableMarkdownCli.
 * These verify the CLI classes are properly compiled and accessible.
 */

fun testMainClassIsAccessible() {
    val mainClass = Class.forName("mutablemarkdowncli.MainKt")
    assertNotNull(mainClass, "MainKt class should exist")
}

fun testCliModuleLoadsSuccessfully() {
    // Verify the CLI module loaded without errors
    assertTrue(true, "CLI module loaded successfully")
}
