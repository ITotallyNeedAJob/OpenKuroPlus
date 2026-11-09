package kat.respect.b2.gui.components;

import kat.respect.b2.gui.GuiClipboard;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.StringSetting;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class StringComponent extends SettingComponent {
   private boolean focused = false;

   public StringComponent(StringSetting setting, int x, int y, int width, int height) {
      super(setting, x, y, width, 20);
   }

   public StringSetting getSetting() {
      return (StringSetting)super.getSetting();
   }

   @Override
   public int getHeight() {
      return 20;
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      boolean hovered = this.isHovered(mouseX, mouseY);
      StringSetting ss = this.getSetting();
      if (hovered || this.focused) {
         NvgUtil.fillRounded(this.x, this.y, this.width, this.height, GuiTheme.radius(), GuiTheme.current().pickerBg);
      }

      NvgUtil.drawText(ss.getName(), this.x + 14.0F, this.y + 4.5F, 9.5F, fontColor | 0xFF000000, 9);
      String text = ss.getValue() + (this.focused ? "_" : "");
      int valColor = this.focused ? -8875 : accentColor | 0xFF000000;
      NvgUtil.drawText(text, this.x + this.width - 10.0F, this.y + 4.5F, 9.5F, valColor, 12);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.isHovered((float)event.x(), (float)event.y()) && event.buttonInfo().button() == 0) {
         this.focused = !this.focused;
         return true;
      } else {
         this.focused = false;
         return false;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (!this.focused) {
         return false;
      } else {
         StringSetting ss = this.getSetting();
         if (event.isPaste()) {
            String pasted = GuiClipboard.getSanitized(256);
            if (!pasted.isEmpty()) {
               ss.setValue(ss.getValue() + pasted);
            }

            return true;
         } else if (!event.isCopy() && !event.isCut()) {
            if (event.key() == 259) {
               String cur = ss.getValue();
               if (!cur.isEmpty()) {
                  ss.setValue(cur.substring(0, cur.length() - 1));
               }

               return true;
            } else if (event.key() != 257 && event.key() != 256) {
               return false;
            } else {
               this.focused = false;
               return true;
            }
         } else {
            if (!ss.getValue().isEmpty()) {
               GuiClipboard.set(ss.getValue());
            }

            if (event.isCut()) {
               ss.setValue("");
            }

            return true;
         }
      }
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      if (this.focused && event.isAllowedChatCharacter()) {
         StringSetting ss = this.getSetting();
         ss.setValue(ss.getValue() + event.codepointAsString());
         return true;
      } else {
         return false;
      }
   }
}
