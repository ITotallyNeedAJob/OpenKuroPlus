package kat.respect.b2.gui;

import kat.respect.b2.module.setting.Setting;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class SettingComponent {
   protected final Setting<?> setting;
   protected int x;
   protected int y;
   protected int width;
   protected int height;

   public SettingComponent(Setting<?> setting, int x, int y, int width, int height) {
      this.setting = setting;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public Setting<?> getSetting() {
      return this.setting;
   }

   public void setPosition(int x, int y, int width) {
      this.x = x;
      this.y = y;
      this.width = width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isHovered(float mouseX, float mouseY) {
      return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.getHeight();
   }

   public abstract void render(float var1, float var2, int var3, int var4);

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      return false;
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      return false;
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      return false;
   }

   public boolean keyPressed(KeyEvent event) {
      return false;
   }

   public boolean charTyped(CharacterEvent event) {
      return false;
   }
}
