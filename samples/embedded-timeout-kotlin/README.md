# Embedded QUIC Timeout Sample (Kotlin + Gradle)

This sample uses local Maven artifacts of:

- `io.netty.incubator:netty-incubator-codec-classes-quic:0.0.76.Final-SNAPSHOT`
- `io.netty.incubator:netty-incubator-codec-native-quic:0.0.76.Final-SNAPSHOT`

The test creates two `EmbeddedChannel`s (server and client), connects them by pumping QUIC datagrams between outbound/inbound queues, advances embedded time, and verifies both QUIC channels close due to idle timeout.

## Run

```bash
cd samples/embedded-timeout-kotlin
gradle test
```

If your Gradle runtime is old/broken on the current machine, use a recent Gradle 8+ distribution.

If you still see Kotlin/JVM startup issues on JDK 25, run with JDK 21 explicitly:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
gradle test
```
