package kat.respect.b2.ct;

public enum GameVersion {
   UNKNOWN("unknown"),
   V1_21_X("1.21"),
   V26_X("26");

   private final String prefix;

   private GameVersion(String prefix) {
      this.prefix = prefix;
   }

   public static GameVersion fromId(String versionId) {
      if (versionId == null) {
         return UNKNOWN;
      } else {
         String v = versionId.trim();
         if (v.startsWith("26.")) {
            return V26_X;
         } else if (v.startsWith("26")) {
            return V26_X;
         } else {
            return v.startsWith("1.21") ? V1_21_X : UNKNOWN;
         }
      }
   }

   public boolean isCurrent() {
      return this == V26_X;
   }

   @Override
   public String toString() {
      return this.prefix;
   }
}
