package kat.respect.b2.module.setting;

import kat.respect.b2.util.PacketUtils;

public class KeySetting extends Setting<Integer> {
   private int keyCode;
   private boolean listening;

   public KeySetting(String name, int keyCode) {
      super(name);
      this.keyCode = keyCode;
   }

   public KeySetting(String name, String description, int keyCode) {
      super(name, description);
      this.keyCode = keyCode;
   }

   public Integer getValue() {
      return this.keyCode;
   }

   public void setValue(Integer value) {
      this.keyCode = value;
      this.notifyChange(value);
   }

   public boolean isPressed() {
      return PacketUtils.isKeyPressed(this.getValue());
   }

   public boolean onKey(int key) {
      if (!this.listening) {
         return false;
      } else if (key == 256) {
         this.listening = false;
         return true;
      } else if (key == 259) {
         this.setValue(-1);
         this.listening = false;
         return true;
      } else {
         this.setValue(key);
         this.listening = false;
         return true;
      }
   }

   public boolean isListening() {
      return this.listening;
   }

   public void setListening(boolean listening) {
      this.listening = listening;
   }

   public void toggleListening() {
      this.listening = !this.listening;
   }

   public String getKeyName() {
      return keyName(this.getValue());
   }

   public static String keyName(int key) {
      if (key == -1 || key == -1) {
         return "None";
      } else if (key >= 1000 && key < 1008) {
         return switch (key - 1000) {
            case 0 -> "LClick";
            case 1 -> "RClick";
            case 2 -> "MClick";
            case 3 -> "M4";
            case 4 -> "M5";
            case 5 -> "M6";
            case 6 -> "M7";
            case 7 -> "M8";
            default -> "Mouse" + (key - 1000 + 1);
         };
      } else if (key >= 0 && key < 8) {
         return switch (key) {
            case 0 -> "LClick";
            case 1 -> "RClick";
            case 2 -> "MClick";
            case 3 -> "M4";
            case 4 -> "M5";
            case 5 -> "M6";
            case 6 -> "M7";
            case 7 -> "M8";
            default -> "Mouse" + (key + 1);
         };
      } else if (key >= 65 && key <= 90) {
         return String.valueOf((char)(65 + (key - 65)));
      } else if (key >= 48 && key <= 57) {
         return String.valueOf((char)(48 + (key - 48)));
      } else if (key >= 290 && key <= 314) {
         return "F" + (key - 290 + 1);
      } else {
         return switch (key) {
            case 32 -> "SPACE";
            case 39 -> "'";
            case 44 -> ",";
            case 45 -> "-";
            case 46 -> ".";
            case 47 -> "/";
            case 59 -> ";";
            case 61 -> "=";
            case 91 -> "[";
            case 92 -> "\\";
            case 93 -> "]";
            case 96 -> "`";
            case 256 -> "ESC";
            case 257 -> "ENTER";
            case 258 -> "TAB";
            case 259 -> "BACKSPACE";
            case 260 -> "INS";
            case 261 -> "DELETE";
            case 262 -> "RIGHT";
            case 263 -> "LEFT";
            case 264 -> "DOWN";
            case 265 -> "UP";
            case 266 -> "PGUP";
            case 267 -> "PGDN";
            case 268 -> "HOME";
            case 269 -> "END";
            case 280 -> "CAPS";
            case 340 -> "L-SHIFT";
            case 341 -> "L-CTRL";
            case 342 -> "L-ALT";
            case 344 -> "R-SHIFT";
            case 345 -> "R-CTRL";
            case 346 -> "R-ALT";
            default -> "KEY-" + key;
         };
      }
   }
}
