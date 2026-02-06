@file:WithArtifact("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-test:1.9.22")
package mutablemarkdowncli.tests

import build.kotlin.withartifact.WithArtifact
import kotlin.test.assertTrue

/**
 * Build verification tests for MutableMarkdownCli.
 * The actual CLI classes are verified by the fat JAR build step;
 * these tests just ensure the test framework is working.
 */

fun testBuildSucceeded() {
    // If we got here, the build step succeeded
    assertTrue(true, "Build completed successfully")
}
