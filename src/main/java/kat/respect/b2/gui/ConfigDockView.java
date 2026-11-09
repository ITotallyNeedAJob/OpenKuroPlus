package kat.respect.b2.gui;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.config.ConfigManager;
import kat.respect.b2.gui.entity.UIIcon;
import kat.respect.b2.util.NotificationManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class ConfigDockView {
   private static final int MAX_NAME_LEN = 24;
   private static final float ROW_H = 30.0F;
   private static final float ROW_GAP = 2.0F;
   private static final float ACTION_FONT = 8.5F;
   private float x;
   private float y;
   private float width;
   private float height;
   private final List<String> configs = new ArrayList<>();
   private boolean listDirty = true;
   private int scrollOffset = 0;
   private String newConfigName = "";
   private boolean newFocused = false;

   public void refresh() {
      this.listDirty = true;
   }

   public void blur() {
      this.newFocused = false;
   }

   public void activate() {
      this.listDirty = true;
      this.scrollOffset = 0;
      this.newFocused = false;
   }

   private void rebuildList() {
      this.configs.clear();
      this.configs.addAll(ConfigManager.getInstance().listProfiles());
      this.listDirty = false;
      this.clampScroll();
   }

   private void layout(float screenW, float screenH) {
      this.width = Math.min(540.0F, screenW - 40.0F);
      this.height = Math.min(440.0F, screenH - 72.0F);
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
      float total = this.configs.size() * 32.0F - 2.0F;
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

   private static float loadX(float rx, float rw) {
      float delW = NvgUtil.textWidth("Delete", 8.5F);
      float saveW = NvgUtil.textWidth("Save", 8.5F);
      float loadW = NvgUtil.textWidth("Load", 8.5F);
      return rx + rw - 8.0F - delW - 10.0F - saveW - 10.0F - loadW;
   }

   private static float saveX(float rx, float rw) {
      float delW = NvgUtil.textWidth("Delete", 8.5F);
      float saveW = NvgUtil.textWidth("Save", 8.5F);
      return rx + rw - 8.0F - delW - 10.0F - saveW;
   }

   private static float deleteX(float rx, float rw) {
      return rx + rw - 8.0F - NvgUtil.textWidth("Delete", 8.5F);
   }

   public void render(float mouseX, float mouseY, int headerColor, int fontColor, int accentColor, String[] hoveredDescriptionOut, float screenW, float screenH) {
      this.layout(screenW, screenH);
      if (this.listDirty) {
         this.rebuildList();
      }

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
      NvgUtil.drawText("CONFIG", this.x + 12.0F, this.y + 13.0F, 10.5F, fontColor | 0xFF000000, 9);
      float closeS = 18.0F;
      float closeX = this.x + this.width - 12.0F - closeS;
      float closeY = this.y + (headerH - closeS) * 0.5F;
      boolean closeHover = mouseX >= closeX && mouseX <= closeX + closeS && mouseY >= closeY && mouseY <= closeY + closeS;
      UIIcon.drawClose(closeX + closeS * 0.5F, closeY + closeS * 0.5F, 8.0F, closeHover ? fontColor | 0xFF000000 : theme.dimText);
      float inputX = this.contentX();
      float inputY = this.y + 50.0F;
      float createW = 60.0F;
      float inputW = this.contentW() - createW - 8.0F;
      boolean inputHover = mouseX >= inputX && mouseX <= inputX + inputW && mouseY >= inputY && mouseY <= inputY + 24.0F;
      int boxBg = this.newFocused ? theme.textboxFocusBg : (inputHover ? theme.rowHover : theme.textboxBg);
      NvgUtil.fillRounded(inputX, inputY, inputW, 24.0F, rowRadius, boxBg);
      NvgUtil.strokeRounded(
         inputX - 0.5F, inputY - 0.5F, inputW + 1.0F, 25.0F, rowRadius, 1.0F, this.newFocused ? accentColor | 0xFF000000 : theme.textboxBorder
      );
      String shown = this.newFocused ? this.newConfigName + "_" : this.newConfigName;
      if (shown.isEmpty()) {
         shown = "New profile name...";
      }

      int textCol = this.newConfigName.isEmpty() ? NvgUtil.darken(theme.dimText, 0.4F) : fontColor | 0xFF000000;
      NvgUtil.drawText(shown, inputX + 8.0F, inputY + 7.0F, 9.5F, textCol, 9);
      float createX = inputX + inputW + 8.0F;
      boolean createHover = mouseX >= createX && mouseX <= createX + createW && mouseY >= inputY && mouseY <= inputY + 24.0F;
      NvgUtil.fillRounded(createX, inputY, createW, 24.0F, rowRadius, createHover ? NvgUtil.brighten(accentColor, 0.1F) : accentColor | 0xFF000000);
      NvgUtil.drawText("Create", createX + createW * 0.5F, inputY + 7.5F, 9.0F, -1, 10);
      float lx = this.contentX();
      float ly = this.listTop();
      float lw = this.contentW();
      float lh = this.listH();
      NvgUtil.save();
      NvgUtil.intersectScissor(lx, ly, lw, lh);
      if (this.configs.isEmpty()) {
         NvgUtil.drawText("No profiles saved", lx + lw * 0.5F, ly + lh * 0.5F - 4.0F, 10.0F, 1442840575, 10);
      }

      String active = ConfigManager.getInstance().getActiveProfile();

      for (int i = 0; i < this.configs.size(); i++) {
         String name = this.configs.get(i);
         float ry = this.rowY(i);
         if (!(ry + 30.0F < ly) && !(ry > ly + lh)) {
            boolean hover = mouseX >= lx && mouseX <= lx + lw && mouseY >= ry && mouseY <= ry + 30.0F;
            if (hover) {
               NvgUtil.fillRounded(lx, ry, lw, 30.0F, rowRadius, theme.rowHover);
            }

            boolean isActive = name.equalsIgnoreCase(active);
            float nameMax = loadX(lx, lw) - 16.0F - (lx + 8.0F);
            NvgUtil.drawText(this.clip(name, nameMax, 9.5F), lx + 8.0F, ry + 10.5F, 9.5F, isActive ? accentColor | 0xFF000000 : fontColor | 0xFF000000, 9);
            float textY = ry + 11.0F;
            boolean loadHover = this.actionHover(mouseX, mouseY, loadX(lx, lw), ry, "Load");
            boolean saveHover = this.actionHover(mouseX, mouseY, saveX(lx, lw), ry, "Save");
            boolean deleteHover = this.actionHover(mouseX, mouseY, deleteX(lx, lw), ry, "Delete");
            NvgUtil.drawText("Load", loadX(lx, lw), textY, 8.5F, loadHover ? accentColor | 0xFF000000 : theme.dimText, 9);
            NvgUtil.drawText("Save", saveX(lx, lw), textY, 8.5F, saveHover ? accentColor | 0xFF000000 : theme.dimText, 9);
            NvgUtil.drawText("Delete", deleteX(lx, lw), textY, 8.5F, deleteHover ? -1096636 : theme.dimText, 9);
            if (hover) {
               hoveredDescriptionOut[0] = deleteHover ? "Delete '" + name + "'" : (saveHover ? "Overwrite '" + name + "'" : "Load '" + name + "'");
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
         hoveredDescriptionOut[0] = "Type a name and press Create";
      }

      if (closeHover) {
         hoveredDescriptionOut[0] = "Back to ClickGUI";
      }
   }

   private boolean actionHover(float mouseX, float mouseY, float actionX, float rowY, String label) {
      float w = NvgUtil.textWidth(label, 8.5F);
      return mouseX >= actionX - 3.0F && mouseX <= actionX + w + 3.0F && mouseY >= rowY + 5.0F && mouseY <= rowY + 30.0F - 5.0F;
   }

   private String clip(String text, float maxWidth, float size) {
      if (NvgUtil.textWidth(text, size) <= maxWidth) {
         return text;
      } else {
         String clipped = text;

         while (!clipped.isEmpty() && NvgUtil.textWidth(clipped + "...", size) > maxWidth) {
            clipped = clipped.substring(0, clipped.length() - 1);
         }

         return clipped + "...";
      }
   }

   private void createProfile() {
      String name = this.newConfigName.trim();
      if (!name.isEmpty()) {
         ConfigManager.getInstance().saveProfile(name);
         ConfigManager.getInstance().setActiveProfile(name);
         ConfigManager.getInstance().saveClient();
         this.newConfigName = "";
         this.newFocused = false;
         this.listDirty = true;
         NotificationManager.push("Config", "Created profile '" + name + "'", NotificationManager.Type.SUCCESS);
      }
   }

   private void loadProfile(String name) {
      if (ConfigManager.getInstance().loadProfile(name)) {
         NotificationManager.push("Config", "Loaded '" + name + "'", NotificationManager.Type.SUCCESS);
      } else {
         NotificationManager.push("Config", "Failed to load '" + name + "'", NotificationManager.Type.ERROR);
      }
   }

   private void saveProfile(String name) {
      ConfigManager.getInstance().saveProfile(name);
      ConfigManager.getInstance().saveClient();
      this.listDirty = true;
      NotificationManager.push("Config", "Saved to '" + name + "'", NotificationManager.Type.INFO);
   }

   private void deleteProfile(String name) {
      ConfigManager.getInstance().deleteProfile(name);
      this.listDirty = true;
      NotificationManager.push("Config", "Deleted '" + name + "'", NotificationManager.Type.WARNING);
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
         float createW = 60.0F;
         float inputW = this.contentW() - createW - 8.0F;
         if (button == 0 && mx >= inputX && mx <= inputX + inputW && my >= inputY && my <= inputY + 24.0F) {
            this.newFocused = true;
            return true;
         } else {
            float createX = inputX + inputW + 8.0F;
            if (button == 0 && mx >= createX && mx <= createX + createW && my >= inputY && my <= inputY + 24.0F) {
               this.createProfile();
               return true;
            } else {
               this.newFocused = false;
               float lx = this.contentX();
               float ly = this.listTop();
               float lw = this.contentW();
               float lh = this.listH();
               if (button == 0 && mx >= lx && mx <= lx + lw && my >= ly && my <= ly + lh) {
                  for (int i = 0; i < this.configs.size(); i++) {
                     float ry = this.rowY(i);
                     if (!(my < ry) && !(my > ry + 30.0F)) {
                        String name = this.configs.get(i);
                        if (this.actionHover(mx, my, deleteX(lx, lw), ry, "Delete")) {
                           this.deleteProfile(name);
                        } else if (this.actionHover(mx, my, saveX(lx, lw), ry, "Save")) {
                           this.saveProfile(name);
                        } else {
                           this.loadProfile(name);
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
      if (!this.newFocused) {
         return false;
      } else if (event.isPaste()) {
         String pasted = GuiClipboard.getSanitized(24);
         if (!pasted.isEmpty()) {
            this.newConfigName = this.newConfigName + pasted;
            if (this.newConfigName.length() > 24) {
               this.newConfigName = this.newConfigName.substring(0, 24);
            }
         }

         return true;
      } else if (!event.isCopy() && !event.isCut()) {
         if (event.key() == 259) {
            if (!this.newConfigName.isEmpty()) {
               this.newConfigName = this.newConfigName.substring(0, this.newConfigName.length() - 1);
            }

            return true;
         } else if (event.key() == 257) {
            this.createProfile();
            return true;
         } else if (event.key() == 256) {
            this.newFocused = false;
            return true;
         } else {
            return true;
         }
      } else {
         if (!this.newConfigName.isEmpty()) {
            GuiClipboard.set(this.newConfigName);
         }

         if (event.isCut()) {
            this.newConfigName = "";
         }

         return true;
      }
   }

   public boolean charTyped(CharacterEvent event) {
      if (this.newFocused) {
         if (event.isAllowedChatCharacter() && this.newConfigName.length() < 24) {
            this.newConfigName = this.newConfigName + event.codepointAsString();
         }

         return true;
      } else {
         return false;
      }
   }
}
