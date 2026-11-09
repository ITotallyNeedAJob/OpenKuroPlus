package kat.respect.b2.gui;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.friend.FriendManager;
import kat.respect.b2.gui.entity.UIIcon;
import kat.respect.b2.util.NotificationManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class FriendsView {
   private static final int MAX_NAME_LEN = 16;
   private static final float ROW_H = 30.0F;
   private static final float ROW_GAP = 2.0F;
   private static final float ACTION_FONT = 8.5F;
   private float x;
   private float y;
   private float width;
   private float height;
   private String nameInput = "";
   private boolean inputFocused = false;
   private int scrollOffset = 0;
   private List<String> friends = new ArrayList<>();

   public void activate() {
      this.inputFocused = false;
      this.scrollOffset = 0;
      this.rebuild();
   }

   public void refresh() {
      this.rebuild();
   }

   public void blur() {
      this.inputFocused = false;
   }

   private void rebuild() {
      this.friends = new ArrayList<>(FriendManager.INSTANCE.getFriends());
      this.friends.sort(String.CASE_INSENSITIVE_ORDER);
      this.clampScroll();
   }

   private void layout(float screenW, float screenH) {
      this.width = Math.min(460.0F, screenW - 40.0F);
      this.height = Math.min(380.0F, screenH - 72.0F);
      this.x = (screenW - this.width) * 0.5F;
      this.y = (screenH - this.height) * 0.5F;
   }

   private float contentX() {
      return this.x + 14.0F;
   }

   private float contentW() {
      return this.width - 28.0F;
   }

   private float listTop() {
      return this.y + 84.0F;
   }

   private float listH() {
      return this.height - (this.listTop() - this.y) - 12.0F;
   }

   private float maxScroll() {
      float total = this.friends.size() * 32.0F - 2.0F;
      return Math.max(0.0F, total - this.listH());
   }

   private void clampScroll() {
      int max = (int)this.maxScroll();
      if (this.scrollOffset > max) {
         this.scrollOffset = max;
      }

      if (this.scrollOffset < 0) {
         this.scrollOffset = 0;
      }
   }

   private float rowY(int index) {
      return this.listTop() + index * 32.0F - this.scrollOffset;
   }

   private static float removeX(float rx, float rw) {
      return rx + rw - 8.0F - NvgUtil.textWidth("Remove", 8.5F);
   }

   public void render(float mouseX, float mouseY, int headerColor, int fontColor, int accentColor, String[] hoveredDescriptionOut, float screenW, float screenH) {
      this.layout(screenW, screenH);
      this.clampScroll();
      GuiTheme theme = GuiTheme.current();
      float trans = Math.max(0.1F, Math.min(1.0F, ClickGuiSettings.transparency.getValueFloat()));
      float radius = GuiTheme.radius() > 0.0F ? 8.0F : 0.0F;
      float rowRadius = GuiTheme.radius() > 0.0F ? 4.0F : 0.0F;
      NvgUtil.fillRounded(this.x, this.y, this.width, this.height, radius, NvgUtil.withAlpha(theme.panelBg, Math.max(24, Math.round(248.0F * trans))));
      NvgUtil.strokeRounded(this.x - 0.5F, this.y - 0.5F, this.width + 1.0F, this.height + 1.0F, radius, 1.0F, 587202559);
      float headerH = 36.0F;
      NvgUtil.fillRoundedTop(this.x, this.y, this.width, headerH, radius, NvgUtil.withAlpha(headerColor, 112));
      NvgUtil.fillRect(this.x + 1.0F, this.y + headerH - 1.5F, this.width - 2.0F, 1.5F, accentColor | 0xFF000000);
      NvgUtil.drawText("FRIENDS", this.x + 12.0F, this.y + 13.0F, 10.5F, fontColor | 0xFF000000, 9);
      float closeS = 18.0F;
      float closeX = this.x + this.width - 12.0F - closeS;
      float closeY = this.y + (headerH - closeS) * 0.5F;
      boolean closeHover = mouseX >= closeX && mouseX <= closeX + closeS && mouseY >= closeY && mouseY <= closeY + closeS;
      UIIcon.drawClose(closeX + closeS * 0.5F, closeY + closeS * 0.5F, 8.0F, closeHover ? fontColor | 0xFF000000 : theme.dimText);
      float inputX = this.contentX();
      float inputY = this.y + 50.0F;
      float addW = 54.0F;
      float inputW = this.contentW() - addW - 8.0F;
      boolean inputHover = mouseX >= inputX && mouseX <= inputX + inputW && mouseY >= inputY && mouseY <= inputY + 24.0F;
      int boxBg = this.inputFocused ? theme.textboxFocusBg : (inputHover ? theme.rowHover : theme.textboxBg);
      NvgUtil.fillRounded(inputX, inputY, inputW, 24.0F, rowRadius, boxBg);
      NvgUtil.strokeRounded(
         inputX - 0.5F, inputY - 0.5F, inputW + 1.0F, 25.0F, rowRadius, 1.0F, this.inputFocused ? accentColor | 0xFF000000 : theme.textboxBorder
      );
      String shown = this.inputFocused ? this.nameInput + "_" : this.nameInput;
      if (shown.isEmpty()) {
         shown = "Player name...";
      }

      int textCol = this.nameInput.isEmpty() ? NvgUtil.darken(theme.dimText, 0.4F) : fontColor | 0xFF000000;
      NvgUtil.drawText(shown, inputX + 8.0F, inputY + 7.0F, 9.5F, textCol, 9);
      float addX = inputX + inputW + 8.0F;
      boolean addHover = mouseX >= addX && mouseX <= addX + addW && mouseY >= inputY && mouseY <= inputY + 24.0F;
      NvgUtil.fillRounded(addX, inputY, addW, 24.0F, rowRadius, addHover ? NvgUtil.brighten(accentColor, 0.1F) : accentColor | 0xFF000000);
      NvgUtil.drawText("Add", addX + addW * 0.5F, inputY + 7.5F, 9.0F, -1, 10);
      float lx = this.contentX();
      float ly = this.listTop();
      float lw = this.contentW();
      float lh = this.listH();
      NvgUtil.save();
      NvgUtil.intersectScissor(lx, ly, lw, lh);
      if (this.friends.isEmpty()) {
         NvgUtil.drawText("No friends added", lx + lw * 0.5F, ly + lh * 0.5F - 4.0F, 10.0F, 1442840575, 10);
      }

      for (int i = 0; i < this.friends.size(); i++) {
         String name = this.friends.get(i);
         float ry = this.rowY(i);
         if (!(ry + 30.0F < ly) && !(ry > ly + lh)) {
            boolean hover = mouseX >= lx && mouseX <= lx + lw && mouseY >= ry && mouseY <= ry + 30.0F;
            if (hover) {
               NvgUtil.fillRounded(lx, ry, lw, 30.0F, rowRadius, theme.rowHover);
            }

            NvgUtil.drawText(name, lx + 8.0F, ry + 10.5F, 9.5F, fontColor | 0xFF000000, 9);
            float removeX = removeX(lx, lw);
            boolean removeHover = mouseX >= removeX - 3.0F
               && mouseX <= removeX + NvgUtil.textWidth("Remove", 8.5F) + 3.0F
               && mouseY >= ry + 5.0F
               && mouseY <= ry + 30.0F - 5.0F;
            NvgUtil.drawText("Remove", removeX, ry + 11.0F, 8.5F, removeHover ? -1096636 : theme.dimText, 9);
            if (hover) {
               hoveredDescriptionOut[0] = removeHover ? "Remove '" + name + "'" : "'" + name + "' is ignored by combat modules";
            }
         }
      }

      NvgUtil.restore();
      float max = this.maxScroll();
      if (max > 0.0F) {
         float trackX = lx + lw - 4.0F;
         float trackY = ly + 4.0F;
         float trackH = lh - 8.0F;
         float ratio = lh / (lh + max);
         float thumbH = Math.max(20.0F, trackH * ratio);
         float pct = this.scrollOffset / max;
         float thumbY = trackY + (trackH - thumbH) * pct;
         NvgUtil.fillRounded(trackX, trackY, 2.5F, trackH, 1.25F, 318767103);
         NvgUtil.fillRounded(trackX, thumbY, 2.5F, thumbH, 1.25F, NvgUtil.withAlpha(accentColor, 160));
      }

      if (inputHover) {
         hoveredDescriptionOut[0] = "Type a player name and press Add";
      }

      if (closeHover) {
         hoveredDescriptionOut[0] = "Back to ClickGUI";
      }
   }

   private void addFriend() {
      String name = this.nameInput.trim();
      if (!name.isEmpty()) {
         if (FriendManager.INSTANCE.isFriend(name)) {
            NotificationManager.push("Friends", name + " is already a friend", NotificationManager.Type.WARNING);
         } else {
            FriendManager.INSTANCE.addFriend(name);
            this.nameInput = "";
            this.inputFocused = false;
            this.rebuild();
            NotificationManager.push("Friends", "Added " + name, NotificationManager.Type.SUCCESS);
         }
      }
   }

   private void removeFriend(String name) {
      FriendManager.INSTANCE.removeFriend(name);
      this.rebuild();
      NotificationManager.push("Friends", "Removed " + name, NotificationManager.Type.INFO);
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      float mx = (float)event.x();
      float my = (float)event.y();
      int button = event.buttonInfo().button();
      float closeS = 18.0F;
      float closeX = this.x + this.width - 12.0F - closeS;
      float closeY = this.y + (36.0F - closeS) * 0.5F;
      if (button == 0 && mx >= closeX && mx <= closeX + closeS && my >= closeY && my <= closeY + closeS) {
         ClickGuiScreen.getInstance().showModules();
         return true;
      } else {
         float inputX = this.contentX();
         float inputY = this.y + 50.0F;
         float addW = 54.0F;
         float inputW = this.contentW() - addW - 8.0F;
         if (button == 0 && mx >= inputX && mx <= inputX + inputW && my >= inputY && my <= inputY + 24.0F) {
            this.inputFocused = true;
            return true;
         } else {
            float addX = inputX + inputW + 8.0F;
            if (button == 0 && mx >= addX && mx <= addX + addW && my >= inputY && my <= inputY + 24.0F) {
               this.addFriend();
               return true;
            } else {
               this.inputFocused = false;
               float lx = this.contentX();
               float ly = this.listTop();
               float lw = this.contentW();
               float lh = this.listH();
               if (button == 0 && mx >= lx && mx <= lx + lw && my >= ly && my <= ly + lh) {
                  for (int i = 0; i < this.friends.size(); i++) {
                     float ry = this.rowY(i);
                     if (!(my < ry) && !(my > ry + 30.0F)) {
                        float removeX = removeX(lx, lw);
                        if (mx >= removeX - 3.0F && mx <= removeX + NvgUtil.textWidth("Remove", 8.5F) + 3.0F) {
                           this.removeFriend(this.friends.get(i));
                        }

                        return true;
                     }
                  }

                  return true;
               } else {
                  return mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height;
               }
            }
         }
      }
   }

   public void mouseReleased(MouseButtonEvent event) {
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      return false;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (mouseX >= this.contentX() && mouseX <= this.contentX() + this.contentW() && mouseY >= this.listTop() && mouseY <= this.listTop() + this.listH()) {
         this.scrollOffset -= (int)(amount * 30.0);
         this.clampScroll();
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(KeyEvent event) {
      if (!this.inputFocused) {
         return false;
      } else if (event.isPaste()) {
         String pasted = GuiClipboard.getSanitized(16);
         if (!pasted.isEmpty()) {
            this.nameInput = this.nameInput + pasted;
            if (this.nameInput.length() > 16) {
               this.nameInput = this.nameInput.substring(0, 16);
            }
         }

         return true;
      } else if (!event.isCopy() && !event.isCut()) {
         if (event.key() == 259) {
            if (!this.nameInput.isEmpty()) {
               this.nameInput = this.nameInput.substring(0, this.nameInput.length() - 1);
            }

            return true;
         } else if (event.key() == 257) {
            this.addFriend();
            return true;
         } else if (event.key() == 256) {
            this.inputFocused = false;
            return true;
         } else {
            return true;
         }
      } else {
         if (!this.nameInput.isEmpty()) {
            GuiClipboard.set(this.nameInput);
         }

         if (event.isCut()) {
            this.nameInput = "";
         }

         return true;
      }
   }

   public boolean charTyped(CharacterEvent event) {
      if (this.inputFocused) {
         if (event.isAllowedChatCharacter() && this.nameInput.length() < 16) {
            this.nameInput = this.nameInput + event.codepointAsString();
         }

         return true;
      } else {
         return false;
      }
   }
}
