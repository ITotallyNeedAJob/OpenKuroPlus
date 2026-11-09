<p align="center">
  <font size="6"><b>OpenKuro+</b></font>
  <br>
  <font size="3">by iqea community</font>
  <br>
  <font size="2">buildable source of the Kuro+ client · MC 26.1.2 · Fabric</font>
</p>

<p align="center">
  <a href="https://discord.gg/34Ck8RhEvt"><b>discord.gg/34Ck8RhEvt</b></a>
</p>

---

## what is this

Kuro+ (by kat), decompiled, cleaned up, and made actually buildable. The
framework — GUI, mixins, bridges, config, commands — is real source in
`src/main/java`. The 75 gameplay modules ship as pre-built nested jars inside
the jar (jars in jars, very normal) and load at runtime through the built-in
`ModuleClassLoader`. Same architecture as the original, byte-identical
modules, we didn't touch them.

**fun facts from the rebuild:**

- the original was compiled at class file version **69**. nice.
- vineflower hit a `StackOverflowError` on `KeyUtil.code()` and left raw
  bytecode as a comment. we reconstructed the entire 61-entry key table from
  `javap -c` like it's 2004.
- recompiled output: **364/364 classes, zero diff** against the original jar.
  we also don't fully believe it, check yourself:
  `unzip -l` both jars and diff.
- `RemoteModuleManager` is not remote. we checked. twice. it just loads jars
  from inside its own jar and someone 3 years ago thought that counted as
  remote.
- is it a virus? no — no URLs, no stealer IOCs, no process launching, no
  natives anywhere in 500+ classes. is it a cheat? yes, obviously.

## how to build

Requires **JDK 25** (see: class version 69, nice). `gradle.properties` points
`org.gradle.java.home` at `C:/Program Files/Zulu/zulu-25` — point it at your
JDK 25 or delete the line and let Gradle's toolchain hunt one down.

```bash
./gradlew build
```

Output: `build/libs/openkuroplus-mc26.1.2-1.0.0.jar`

## how to run

1. Fabric Loader **0.19.2+** for MC 26.1.2
2. Fabric API (any recent 26.x build)
3. jar goes in `mods/`. you know the rest.

## credits

- **kat** — the original Kuro+ client. this is a rebuild of their jar.
- **Lenni0451** — ClassTransform (bundled, `META-INF/jars/*-1.15.1.jar`)
- **iqea community** — the reason this repo exists
- **vineflower** — for the stack overflow. someone had to say it.

license follows the original mod metadata: **CC0-1.0**.
