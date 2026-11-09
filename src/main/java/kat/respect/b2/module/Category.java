package kat.respect.b2.module;

public enum Category {
   COMBAT("Combat"),
   BLATANT("Blatant"),
   MOVEMENT("Movement"),
   RENDER("Render"),
   WORLD("World"),
   MISC("Misc"),
   INTERFACE("HUD"),
   EXPLOIT("Exploit"),
   PLAYER("Player"),
   CLIENT("Client");

   private final String displayName;
   public static final Category[] DISPLAY_CATEGORIES = new Category[]{COMBAT, BLATANT, MOVEMENT, RENDER, WORLD, MISC, INTERFACE};

   private Category(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }
}
