@KotlinBuildScript("https://tools.kotlin.build/")
@file:WithArtifact("kompile:build-kotlin-jvm:0.0.1")
package mutablemarkdowncli

import build.kotlin.withartifact.WithArtifact
import java.io.File
import build.kotlin.jvm.*
import build.kotlin.annotations.MavenArtifactCoordinates

val dependencies = resolveDependencies(
    // Kotlin stdlib
    MavenPrebuilt("org.jetbrains.kotlin:kotlin-stdlib:1.9.22"),
    MavenPrebuilt("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22"),
    MavenPrebuilt("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22"),
    // JSON
    MavenPrebuilt("org.json:json:20250517"),
    // CLI parsing
    MavenPrebuilt("commons-cli:commons-cli:1.9.0"),
    // UrlResolver and UrlProtocol for url:// protocol support
    MavenPrebuilt("foundation.url:resolver:0.0.293"),
    MavenPrebuilt("foundation.url:protocol:0.0.165"),
    // Clock abstraction (required by UrlProtocol)
    MavenPrebuilt("community.kotlin.clocks.simple:community-kotlin-clocks-simple:0.0.1"),
    // SJVM for sandboxed execution (required by UrlResolver)
    MavenPrebuilt("net.javadeploy.sjvm:libSJVM-jvm:0.0.24"),
    MavenPrebuilt("net.javadeploy.sjvm:avianStdlibHelper-jvm:0.0.24"),
    MavenPrebuilt("net.javadeploy.sjvm:stdlibHelperCommon-jvm:0.0.24"),
    // ASM for bytecode manipulation (required by UrlResolver)
    MavenPrebuilt("org.ow2.asm:asm:9.6"),
    MavenPrebuilt("org.ow2.asm:asm-commons:9.6"),
    // Markdown API (typed interface for sandboxed client)
    MavenPrebuilt("community.kotlin.markdown:api:0.0.1"),
    // Coroutines
    MavenPrebuilt("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.0"),
    // libp2p dependencies (required by UrlResolver)
    MavenPrebuilt("io.libp2p:jvm-libp2p:1.2.2-RELEASE"),
    MavenPrebuilt("org.jetbrains.kotlin:kotlin-reflect:1.9.22"),
    MavenPrebuilt("community.kotlin.rpc:protocol-api:0.0.2"),
    MavenPrebuilt("community.kotlin.rpc:protocol-impl:0.0.11"),
    MavenPrebuilt("com.google.protobuf:protobuf-java:3.25.1"),
    MavenPrebuilt("tech.pegasys:noise-java:22.1.0"),
    // Netty (for libp2p)
    MavenPrebuilt("io.netty:netty-buffer:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-codec:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-codec-http:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-codec-http2:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-common:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-handler:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-resolver:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-transport:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-transport-classes-epoll:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-transport-classes-kqueue:4.1.101.Final"),
    MavenPrebuilt("io.netty:netty-transport-native-unix-common:4.1.101.Final"),
    // BouncyCastle
    MavenPrebuilt("org.bouncycastle:bcpkix-jdk18on:1.78.1"),
    MavenPrebuilt("org.bouncycastle:bcprov-jdk18on:1.78.1"),
    MavenPrebuilt("org.bouncycastle:bcutil-jdk18on:1.78.1"),
    // Guava (required by libp2p)
    MavenPrebuilt("com.google.guava:guava:33.2.0-jre"),
    MavenPrebuilt("com.google.guava:failureaccess:1.0.2"),
    // Logging
    MavenPrebuilt("org.slf4j:slf4j-api:1.7.36"),
    MavenPrebuilt("org.slf4j:slf4j-simple:1.7.36"),
    // Okio
    MavenPrebuilt("com.squareup.okio:okio-jvm:3.4.0"),
)

@MavenArtifactCoordinates("community.kotlin.markdown:cli:")
fun buildMaven(): File {
    return buildSimpleKotlinMavenArtifact(
        // 0.0.5: Use typed SJVM client instead of raw sendServiceRpcRequest
        // 0.0.4: Fix UrlProtocol version mismatch (0.0.154 → 0.0.165)
        //        - UrlResolver 0.0.293 requires UrlProtocol 0.0.165
        // 0.0.3: Update foundation.url:resolver to 0.0.293
        // 0.0.2: Added URL protocol support
        //        - CLI can now connect to url://markdown/ services
        //        - Use --server url://markdown/ to connect via URL protocol
        //        - HTTP client remains for local testing
        // 0.0.1: Initial release
        //        - upload/download/edit/list/delete commands
        //        - Uses vim for editing (configurable via EDITOR env var)
        //        - HTTP client for server communication
        coordinates = "community.kotlin.markdown:cli:0.0.5",
        src = File("src"),
        compileDependencies = dependencies
    )
}

fun buildJar(): File {
    return buildMaven().jar
}

fun buildFatJar(): File {
    val manifest = Manifest("mutablemarkdowncli.MainKt")
    return BuildJar(manifest, dependencies.map { it.jar } + buildJar())
}
