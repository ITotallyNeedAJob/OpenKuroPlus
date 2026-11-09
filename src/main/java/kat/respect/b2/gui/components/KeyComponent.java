package kat.respect.b2.gui.components;

import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.KeySetting;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class KeyComponent extends SettingComponent {
   private static KeyComponent activeListener = null;
   private boolean listening = false;
   private long listenStartTime = 0L;

   public KeyComponent(KeySetting setting, int x, int y, int width, int height) {
      super(setting, x, y, width, 15);
   }

   public KeySetting getSetting() {
      return (KeySetting)super.getSetting();
   }

   @Override
   public int getHeight() {
      return 15;
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      boolean hovered = this.isHovered(mouseX, mouseY);
      KeySetting ks = this.getSetting();
      if (hovered || this.listening) {
         NvgUtil.fillRounded(this.x, this.y, this.width, this.height, GuiTheme.radius(), GuiTheme.current().subHover);
      }

      NvgUtil.drawText(ks.getName(), this.x + 10.0F, this.y + 2.5F, 9.0F, fontColor | 0xFF000000, 9);
      String keyStr = this.listening ? "Listening..." : ks.getKeyName();
      float textW = NvgUtil.textWidth(keyStr, 8.5F);
      float pillW = Math.max(34.0F, textW + 10.0F);
      float pillH = 12.0F;
      float pillX = this.x + this.width - pillW - 8.0F;
      float pillY = this.y + (this.height - pillH) * 0.5F;
      int pillBg = this.listening ? -13292723 : -14540749;
      NvgUtil.fillRounded(pillX, pillY, pillW, pillH, 3.0F, pillBg);
      int borderCol = this.listening ? accentColor | 0xFF000000 : 419430399;
      NvgUtil.strokeRounded(pillX - 0.5F, pillY - 0.5F, pillW + 1.0F, pillH + 1.0F, 3.0F, 1.0F, borderCol);
      int valColor = this.listening ? -208051 : accentColor | 0xFF000000;
      NvgUtil.drawText(keyStr, pillX + pillW * 0.5F, pillY + 1.5F, 8.5F, valColor, 10);
   }

   public static void cancelCurrentListening() {
      if (activeListener != null) {
         activeListener.endListening();
      }
   }

   private void beginListening() {
      if (activeListener != null && activeListener != this) {
         activeListener.listening = false;
         activeListener.getSetting().setListening(false);
      }

      activeListener = this;
      this.listening = true;
      this.listenStartTime = System.currentTimeMillis();
      this.getSetting().setListening(true);
   }

   private void endListening() {
      if (activeListener == this) {
         activeListener = null;
      }

      this.listening = false;
      this.getSetting().setListening(false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.listening) {
         if (System.currentTimeMillis() - this.listenStartTime < 60L) {
            return true;
         } else {
            int button = event.buttonInfo().button();
            this.getSetting().setValue(button);
            this.endListening();
            return true;
         }
      } else if (this.isHovered((float)event.x(), (float)event.y())) {
         this.beginListening();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.listening) {
         int key = event.key();
         if (key == 256) {
            this.endListening();
         } else if (key == 259) {
            this.getSetting().setValue(-1);
            this.endListening();
         } else {
            this.getSetting().setValue(key);
            this.endListening();
         }

         return true;
      } else {
         return false;
      }
   }
}
