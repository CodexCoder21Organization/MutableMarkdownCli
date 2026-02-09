@file:WithArtifact("mutablemarkdowncli.buildMaven()")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-test:1.9.22")
@file:WithArtifact("org.json:json:20250517")
@file:WithArtifact("foundation.url:resolver:0.0.293")
@file:WithArtifact("foundation.url:protocol:0.0.154")
@file:WithArtifact("community.kotlin.clocks.simple:community-kotlin-clocks-simple:0.0.1")
@file:WithArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.0")
@file:WithArtifact("io.libp2p:jvm-libp2p:1.2.2-RELEASE")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
@file:WithArtifact("community.kotlin.rpc:protocol-api:0.0.2")
@file:WithArtifact("community.kotlin.rpc:protocol-impl:0.0.11")
@file:WithArtifact("com.google.protobuf:protobuf-java:3.25.1")
@file:WithArtifact("tech.pegasys:noise-java:22.1.0")
@file:WithArtifact("io.netty:netty-buffer:4.1.101.Final")
@file:WithArtifact("io.netty:netty-codec:4.1.101.Final")
@file:WithArtifact("io.netty:netty-common:4.1.101.Final")
@file:WithArtifact("io.netty:netty-handler:4.1.101.Final")
@file:WithArtifact("io.netty:netty-resolver:4.1.101.Final")
@file:WithArtifact("io.netty:netty-transport:4.1.101.Final")
@file:WithArtifact("io.netty:netty-transport-native-unix-common:4.1.101.Final")
@file:WithArtifact("org.bouncycastle:bcpkix-jdk18on:1.78.1")
@file:WithArtifact("org.bouncycastle:bcprov-jdk18on:1.78.1")
@file:WithArtifact("org.bouncycastle:bcutil-jdk18on:1.78.1")
@file:WithArtifact("com.squareup.okio:okio-jvm:3.4.0")
@file:WithArtifact("org.slf4j:slf4j-api:1.7.36")
@file:WithArtifact("org.slf4j:slf4j-simple:1.7.36")
@file:WithArtifact("com.google.guava:guava:33.2.0-jre")
@file:WithArtifact("com.google.guava:failureaccess:1.0.2")
package mutablemarkdowncli.tests

import build.kotlin.withartifact.WithArtifact
import kotlin.test.assertTrue
import kotlin.test.assertFails
import mutablemarkdowncli.UrlProtocolClient

/**
 * Tests for MutableMarkdownCli.
 */

fun testBuildSucceeded() {
    // If we got here, the build step succeeded
    assertTrue(true, "Build completed successfully")
}

/**
 * Tests that UrlProtocolClient can be instantiated quickly.
 *
 * Previously the client had hardcoded sleeps totaling 13+ seconds in the init block:
 * - Thread.sleep(3000) waiting for gossip
 * - Retry loop with Thread.sleep(2000) per attempt
 *
 * After fixing, the client should instantiate in under 100ms because:
 * - No manual joinNetwork()/discoverPeers() calls
 * - No Thread.sleep() calls
 * - Network operations happen lazily on first RPC call
 */
fun testUrlProtocolClientInstantiatesQuickly() {
    val startTime = System.currentTimeMillis()

    // Create the client - this should be fast now
    val client = UrlProtocolClient("url://markdown/")

    val instantiationTime = System.currentTimeMillis() - startTime

    println("[CLIENT-INIT-TEST] UrlProtocolClient instantiated in ${instantiationTime}ms")

    // The client should instantiate in under 500ms (plenty of margin)
    // Previously this could take 13+ seconds due to hardcoded sleeps
    assertTrue(instantiationTime < 500,
        "UrlProtocolClient should instantiate in under 500ms, but took ${instantiationTime}ms. " +
        "Check that there are no Thread.sleep() calls or blocking operations in the init block.")

    client.close()

    println("[CLIENT-INIT-TEST] SUCCESS: Client instantiated quickly at ${instantiationTime}ms")
}

/**
 * Tests that UrlProtocolClient properly defers network operations.
 *
 * The client should not make any network calls during construction.
 * Network operations should only happen when an actual RPC method is called.
 */
fun testUrlProtocolClientDefersNetworkOperations() {
    val startTime = System.currentTimeMillis()

    // Create multiple clients quickly - this should work without network delays
    val clients = mutableListOf<UrlProtocolClient>()
    for (i in 1..5) {
        clients.add(UrlProtocolClient("url://markdown/"))
    }

    val totalTime = System.currentTimeMillis() - startTime

    println("[CLIENT-BATCH-TEST] Created 5 UrlProtocolClients in ${totalTime}ms")

    // 5 clients should still be fast (under 1 second total)
    assertTrue(totalTime < 1000,
        "Creating 5 UrlProtocolClients should take under 1 second, but took ${totalTime}ms. " +
        "Network operations should be deferred to the first RPC call.")

    // Clean up
    clients.forEach { it.close() }

    println("[CLIENT-BATCH-TEST] SUCCESS: Batch creation was fast at ${totalTime}ms")
}
