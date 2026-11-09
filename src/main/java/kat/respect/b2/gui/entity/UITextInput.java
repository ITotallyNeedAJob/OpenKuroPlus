package kat.respect.b2.gui.entity;

import java.util.function.Consumer;
import kat.respect.b2.gui.GuiClipboard;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class UITextInput extends UIEntity {
   private String text = "";
   private String placeholder = "";
   private boolean hasSearchIcon = false;
   private Consumer<String> changeListener;
   private long lastBlinkTime = 0L;

   public UITextInput(String placeholder) {
      this.placeholder = placeholder != null ? placeholder : "";
      this.height = 32.0F;
      this.radius = 8.0F;
      this.bg = -15330012;
      this.border = 419430399;
      this.borderWidth = 1.0F;
   }

   public UITextInput text(String text) {
      this.text = text != null ? text : "";
      return this;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text != null ? text : "";
   }

   public UITextInput searchIcon(boolean searchIcon) {
      this.hasSearchIcon = searchIcon;
      return this;
   }

   public UITextInput onChange(Consumer<String> listener) {
      this.changeListener = listener;
      return this;
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      float focusP = this.anim("focus", this.focused ? 1.0F : 0.0F, 22.0F);
      GuiTheme theme = GuiTheme.current();
      int accentColor = theme.accentColor | 0xFF000000;
      int curBg = NvgUtil.lerpColor(this.bg, -14803922, focusP);
      NvgUtil.fillRounded(this.x, this.y, this.width, this.height, this.radius, curBg);
      int curBorder = NvgUtil.lerpColor(this.border, accentColor, focusP);
      NvgUtil.strokeRounded(this.x - 0.5F, this.y - 0.5F, this.width + 1.0F, this.height + 1.0F, this.radius, 1.0F, curBorder);
      float textX = this.x + 12.0F;
      if (this.hasSearchIcon) {
         float iconCx = this.x + 16.0F;
         float iconCy = this.y + this.height * 0.5F;
         int iconColor = NvgUtil.lerpColor(1627389951, -1, focusP);
         UIIcon.drawSearch(iconCx, iconCy, 12.0F, iconColor);
         textX = this.x + 32.0F;
      }

      float textY = this.y + (this.height - 10.0F) * 0.5F - 0.5F;
      if (this.text.isEmpty() && !this.focused) {
         NvgUtil.drawText(this.placeholder, textX, textY, 10.0F, 1358954495, 9);
      } else {
         NvgUtil.drawText(this.text, textX, textY, 10.0F, -1, 9);
         if (this.focused) {
            long now = System.currentTimeMillis();
            boolean showCaret = now % 1000L < 500L;
            if (showCaret) {
               float textW = NvgUtil.textWidth(this.text, 10.0F);
               float caretX = textX + textW + 1.5F;
               NvgUtil.fillRect(caretX, textY, 1.2F, 11.0F, accentColor);
            }
         }
      }

      if (!this.text.isEmpty()) {
         float clearCx = this.x + this.width - 16.0F;
         float clearCy = this.y + this.height * 0.5F;
         boolean clearHover = mouseX >= clearCx - 8.0F && mouseX <= clearCx + 8.0F && mouseY >= clearCy - 8.0F && mouseY <= clearCy + 8.0F;
         int clearColor = clearHover ? -1 : 1627389951;
         UIIcon.drawClose(clearCx, clearCy, 9.0F, clearColor);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         int btn = event.buttonInfo().button();
         float mx = (float)event.x();
         float my = (float)event.y();
         if (this.contains(mx, my) && btn == 0) {
            if (!this.text.isEmpty()) {
               float clearCx = this.x + this.width - 16.0F;
               float clearCy = this.y + this.height * 0.5F;
               if (mx >= clearCx - 8.0F && mx <= clearCx + 8.0F && my >= clearCy - 8.0F && my <= clearCy + 8.0F) {
                  this.text = "";
                  if (this.changeListener != null) {
                     this.changeListener.accept(this.text);
                  }

                  return true;
               }
            }

            this.focused = true;
            return true;
         } else {
            this.focused = false;
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.focused && this.visible && this.enabled) {
         int key = event.key();
         if (event.isPaste()) {
            String pasted = GuiClipboard.getSanitized(256);
            if (!pasted.isEmpty()) {
               this.text = this.text + pasted;
               if (this.changeListener != null) {
                  this.changeListener.accept(this.text);
               }
            }

            return true;
         }

         if (event.isCopy() || event.isCut()) {
            if (!this.text.isEmpty()) {
               GuiClipboard.set(this.text);
            }

            if (event.isCut() && !this.text.isEmpty()) {
               this.text = "";
               if (this.changeListener != null) {
                  this.changeListener.accept(this.text);
               }
            }

            return true;
         }

         if (key == 259) {
            if (!this.text.isEmpty()) {
               this.text = this.text.substring(0, this.text.length() - 1);
               if (this.changeListener != null) {
                  this.changeListener.accept(this.text);
               }
            }

            return true;
         }

         if (key == 257 || key == 256) {
            this.focused = false;
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      if (this.focused && this.visible && this.enabled) {
         int codepoint = event.codepoint();
         if (codepoint >= 32 && codepoint != 127) {
            this.text = this.text + Character.toString(codepoint);
            if (this.changeListener != null) {
               this.changeListener.accept(this.text);
            }

            return true;
         }
      }

      return false;
   }
}
