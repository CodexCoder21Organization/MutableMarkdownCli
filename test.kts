@file:WithArtifact("mutablemarkdowncli.buildMaven()")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
@file:WithArtifact("org.jetbrains.kotlin:kotlin-test:1.9.22")
@file:WithArtifact("org.json:json:20250517")
@file:WithArtifact("foundation.url:resolver:0.0.378")
@file:WithArtifact("foundation.url:protocol:0.0.256")
@file:WithArtifact("community.kotlin.clocks.simple:community-kotlin-clocks-simple:0.0.1")
@file:WithArtifact("net.javadeploy.sjvm:libSJVM-jvm:0.0.24")
@file:WithArtifact("net.javadeploy.sjvm:avianStdlibHelper-jvm:0.0.24")
@file:WithArtifact("net.javadeploy.sjvm:stdlibHelperCommon-jvm:0.0.24")
@file:WithArtifact("org.ow2.asm:asm:9.6")
@file:WithArtifact("org.ow2.asm:asm-commons:9.6")
@file:WithArtifact("community.kotlin.markdown:api:0.0.1")
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

/**
 * Tests for MutableMarkdownCli.
 */

fun testBuildSucceeded() {
    // If we got here, the build step succeeded
    assertTrue(true, "Build completed successfully")
}
