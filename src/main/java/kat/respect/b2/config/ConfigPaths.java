package kat.respect.b2.config;

import java.io.File;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

public final class ConfigPaths {
   private final Path gameDir;

   public ConfigPaths(Path gameDir) {
      this.gameDir = gameDir != null ? gameDir : Path.of(".");
   }

   public static ConfigPaths defaultPaths() {
      return new ConfigPaths(resolveGameDir());
   }

   public static Path resolveGameDir() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            File dir = mc.gameDirectory;
            if (dir != null) {
               return dir.toPath();
            }
         }
      } catch (Throwable var2) {
      }

      return Path.of(".");
   }

   public Path gameDir() {
      return this.gameDir;
   }

   public Path root() {
      return this.gameDir.resolve("kuroplus");
   }

   public Path client() {
      return this.root().resolve("client.json");
   }

   public Path profilesDir() {
      return this.root().resolve("profiles");
   }

   public Path profile(String n) {
      return this.profilesDir().resolve(sanitizeName(n) + ".json");
   }

   public Path friends() {
      return this.root().resolve("friends.json");
   }

   public Path hud() {
      return this.root().resolve("hud.json");
   }

   public Path backups() {
      return this.root().resolve("backups");
   }

   public Path migrationBackup() {
      return this.root().resolve("migration-backup");
   }

   public Path migrationMarker() {
      return this.root().resolve(".migrated-v2-v3");
   }

   public Path legacyRoot() {
      return this.gameDir.resolve("respect");
   }

   public static String sanitizeName(String name) {
      return name != null && !name.isBlank() ? name.replaceAll("[^a-zA-Z0-9._-]", "_") : "default";
   }
}
