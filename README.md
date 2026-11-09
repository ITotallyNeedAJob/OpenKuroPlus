# OpenKuro+

**OpenKuro+** is a community source rebuild of the **Kuro+** client (by kat) for Minecraft **26.1.2** — decompiled, cleaned up, and made fully buildable with Fabric Loom.

## 💬 Discord

Join the server: **[https://discord.gg/34Ck8RhEvt](https://discord.gg/34Ck8RhEvt)**

## About

- Fabric client mod, MC **26.1.x** (declared `>=26.1.2 <26.2`), Fabric Loader **0.19.2+**, Java **25**
- Framework (GUI, mixins, bridges, config, command system) is full source in `src/main/java`
- The 75 gameplay modules (KillAura, CrystalAura, ESP, Scaffold, ...) ship as pre-built nested jars under `assets/kuroplus/modules/` and are loaded at runtime by the built-in `ModuleClassLoader` — same architecture as the original
- Built against unobfuscated MC 26.x, so Loom runs mapping-less (no yarn/mojmap needed)

## Building

Requires **JDK 25** (the original is compiled at class version 69). `gradle.properties` points
`org.gradle.java.home` at `C:/Program Files/Zulu/zulu-25` — adjust that path to your JDK 25
install if it differs, or delete the line and let Gradle's toolchain find one.

```bash
./gradlew build
```

Output: `build/libs/openkuroplus-mc26.1.2-1.0.0.jar`

## Installing

1. Fabric Loader **0.19.2 or newer** for MC 26.1.2
2. Fabric API (any recent 26.x build)
3. Drop the jar into `mods/`

## Credits

- **kat** — original Kuro+ client (this repo is a rebuild of its jar)
- **Lenni0451** — ClassTransform (bundled as `META-INF/jars/*-1.15.1.jar`)
- Fabric / Loom teams

License follows the original mod metadata: **CC0-1.0**.
