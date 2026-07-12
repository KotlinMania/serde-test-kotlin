# Serde Test in Kotlin

Kotlin Multiplatform port of [`serde_test`](https://github.com/serde-rs/test), the token serializer and deserializer used to verify Serde implementations.

## Installation

```kotlin
dependencies {
    testImplementation("io.github.kotlinmania:serde-test-kotlin:0.1.1")
}
```

## Build

```bash
./gradlew build
./gradlew test
```

See [AGENTS.md](AGENTS.md) for translation and test-parity requirements.
