package kat.respect.b2.gui.entity;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.module.setting.KeySetting;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class UIKeybind extends UIEntity {
   private static UIKeybind activeListening = null;
   private String label = "Keybind";
   private int keyCode = -1;
   private IntSupplier keySupplier = null;
   private boolean listening = false;
   private Consumer<Integer> bindListener;
   private long listenStartTime = 0L;

   public UIKeybind(String label, int initialKey) {
      this.label = label != null ? label : "Keybind";
      this.keyCode = initialKey;
      this.height = 20.0F;
   }

   public UIKeybind(String label, IntSupplier keySupplier) {
      this.label = label != null ? label : "Keybind";
      this.keySupplier = keySupplier;
      this.keyCode = keySupplier != null ? keySupplier.getAsInt() : -1;
      this.height = 20.0F;
   }

   public UIKeybind onBind(Consumer<Integer> listener) {
      this.bindListener = listener;
      return this;
   }

   public void setKey(int key) {
      this.keyCode = key;
   }

   public int getKey() {
      return this.keySupplier != null ? this.keySupplier.getAsInt() : this.keyCode;
   }

   public String getKeyName() {
      return getKeyName(this.getKey());
   }

   public static String getKeyName(int keyCode) {
      return KeySetting.keyName(keyCode);
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      GuiTheme theme = GuiTheme.current();
      int fontColor = theme.fontColor | 0xFF000000;
      int accentColor = theme.accentColor | 0xFF000000;
      NvgUtil.drawText(this.label, this.x, this.y + 4.0F, 9.5F, fontColor, 9);
      String text = this.listening ? "Listening..." : this.getKeyName();
      float textW = NvgUtil.textWidth(text, 9.0F);
      float pillW = Math.max(45.0F, textW + 16.0F);
      float pillH = 16.0F;
      float pillX = this.x + this.width - pillW;
      float pillY = this.y + 2.0F;
      float pillR = 4.0F;
      boolean pillHover = mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH;
      float hoverP = this.anim("hover", pillHover ? 1.0F : 0.0F, 20.0F);
      int pillBg = this.listening ? -13292723 : NvgUtil.lerpColor(-14540749, -14014400, hoverP);
      NvgUtil.fillRounded(pillX, pillY, pillW, pillH, pillR, pillBg);
      int borderCol = this.listening ? accentColor : NvgUtil.lerpColor(553648127, NvgUtil.withAlpha(accentColor, 136), hoverP);
      NvgUtil.strokeRounded(pillX - 0.5F, pillY - 0.5F, pillW + 1.0F, pillH + 1.0F, pillR, 1.0F, borderCol);
      int textColor = this.listening ? -208051 : (this.keyCode != -1 ? accentColor : -1996488705);
      NvgUtil.drawText(text, pillX + pillW * 0.5F, pillY + 3.5F, 9.0F, textColor, 10);
   }

   private void stopListening() {
      if (activeListening == this) {
         activeListening = null;
      }

      this.listening = false;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (!this.visible || !this.enabled) {
         return false;
      } else if (this.listening) {
         if (System.currentTimeMillis() - this.listenStartTime < 60L) {
            return true;
         } else {
            int btn = event.buttonInfo().button();
            this.keyCode = btn;
            this.stopListening();
            if (this.bindListener != null) {
               this.bindListener.accept(this.keyCode);
            }

            return true;
         }
      } else if (this.contains((float)event.x(), (float)event.y())) {
         if (activeListening != null) {
            activeListening.stopListening();
         }

         activeListening = this;
         this.listening = true;
         this.listenStartTime = System.currentTimeMillis();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (!this.listening) {
         return false;
      } else {
         int key = event.key();
         if (key == 256) {
            this.stopListening();
         } else if (key != 259 && key != 261) {
            this.keyCode = key;
            this.stopListening();
            if (this.bindListener != null) {
               this.bindListener.accept(this.keyCode);
            }
         } else {
            this.keyCode = -1;
            this.stopListening();
            if (this.bindListener != null) {
               this.bindListener.accept(this.keyCode);
            }
         }

         return true;
      }
   }
}
