package kat.respect.b2.gui;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.config.ConfigManager;
import kat.respect.b2.util.NotificationManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class ConfigPanel {
   private static final int MAX_NAME_LEN = 24;
   private int x;
   private int y;
   private int width;
   private boolean collapsed = false;
   private boolean dragging = false;
   private float dragOffsetX;
   private float dragOffsetY;
   private int scrollOffset = 0;
   private float lastScreenH = 720.0F;
   private String newConfigName = "";
   private boolean textboxFocused = false;
   private final List<String> configs = new ArrayList<>();
   private boolean listDirty = true;
   private boolean embedded = false;
   private int embeddedVisibleH = 200;
   private int standaloneX = 0;
   private int standaloneY = 0;
   private int standaloneWidth = 135;
   private final String titleText = "CONFIG";
   private long highlightUntil = 0L;

   public ConfigPanel(int x, int y, int width) {
      this.x = x;
      this.y = y;
      this.width = width;
   }

   public void refresh() {
      this.listDirty = true;
   }

   public void flash() {
      this.highlightUntil = System.currentTimeMillis() + 900L;
   }

   public void setEmbedded(boolean embedded, int visibleH) {
      if (embedded && !this.embedded) {
         this.standaloneX = this.x;
         this.standaloneY = this.y;
         this.standaloneWidth = this.width;
      }

      boolean wasEmbedded = this.embedded;
      this.embedded = embedded;
      this.embeddedVisibleH = visibleH;
      if (!embedded && wasEmbedded) {
         this.x = this.standaloneX;
         this.y = this.standaloneY;
         this.width = this.standaloneWidth;
      }
   }

   private void rebuildList() {
      this.configs.clear();
      this.configs.addAll(ConfigManager.getInstance().listProfiles());
      this.listDirty = false;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getSavedX() {
      return this.embedded ? this.standaloneX : this.x;
   }

   public int getSavedY() {
      return this.embedded ? this.standaloneY : this.y;
   }

   public void setX(int x) {
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   public boolean isCollapsed() {
      return this.collapsed;
   }

   public void setCollapsed(boolean collapsed) {
      this.collapsed = collapsed;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public int getHeaderHeight() {
      String hMode = ClickGuiSettings.headerHeight.getValue();
      if ("Standard (18px)".equals(hMode)) {
         return 18;
      } else {
         return "Tall (22px)".equals(hMode) ? 22 : 15;
      }
   }

   public int getRowHeight() {
      if (ClickGuiSettings.moduleHeight != null) {
         String mMode = ClickGuiSettings.moduleHeight.getValue();
         if ("Slim (14px)".equals(mMode)) {
            return 14;
         } else {
            return "Tall (18px)".equals(mMode) ? 18 : 16;
         }
      } else {
         return 16;
      }
   }

   private int getIconBtnSize() {
      return this.getRowHeight();
   }

   private boolean isHeaderHovered(int mouseX, int mouseY) {
      return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.getHeaderHeight();
   }

   public int getTotalContentHeight() {
      if (this.collapsed && !this.embedded) {
         return 0;
      } else {
         int rowH = this.getRowHeight();
         return 4 + rowH + 2 + this.configs.size() * rowH + 4;
      }
   }

   public void clampPosition(float screenW, float screenH) {
      this.lastScreenH = screenH;
      int headerH = this.getHeaderHeight();
      if (this.x < 4) {
         this.x = 4;
      }

      if (this.y < 36) {
         this.y = 36;
      }

      if (this.x > screenW - this.width - 4.0F) {
         this.x = Math.max(4, (int)(screenW - this.width - 4.0F));
      }

      if (this.y > screenH - headerH - 30.0F) {
         this.y = Math.max(36, (int)(screenH - headerH - 30.0F));
      }
   }

   private float contentTop() {
      return this.embedded ? this.y : this.y + this.getHeaderHeight();
   }

   private float newRowY() {
      return this.contentTop() + this.scrollOffset + 4.0F;
   }

   private float textboxX() {
      return this.x + 4;
   }

   private float textboxW() {
      return this.width - 8 - this.getIconBtnSize() - 4;
   }

   private float newSaveBtnX() {
      return this.x + 4 + this.textboxW() + 4.0F;
   }

   private float rowY(int index) {
      int rowH = this.getRowHeight();
      return this.contentTop() + this.scrollOffset + 4.0F + rowH + 2.0F + index * rowH;
   }

   private float iconsStartX() {
      return this.x + this.width - 4 - this.getIconBtnSize() * 3;
   }

   private float iconCenterX(int iconIndex) {
      int iconBtn = this.getIconBtnSize();
      return this.iconsStartX() + iconIndex * iconBtn + iconBtn / 2.0F;
   }

   private void saveNewConfig() {
      String name = this.newConfigName.trim();
      if (!name.isEmpty()) {
         ConfigManager.getInstance().saveProfile(name);
         ConfigManager.getInstance().setActiveProfile(name);
         ConfigManager.getInstance().saveClient();
         this.newConfigName = "";
         this.textboxFocused = false;
         this.refresh();
         NotificationManager.push("Config", "Saved config: " + name, NotificationManager.Type.SUCCESS);
      }
   }

   private void saveConfig(String name) {
      ConfigManager.getInstance().saveProfile(name);
      ConfigManager.getInstance().saveClient();
      this.refresh();
      NotificationManager.push("Config", "Saved config: " + name, NotificationManager.Type.SUCCESS);
   }

   private void loadConfig(String name) {
      if (ConfigManager.getInstance().loadProfile(name)) {
         NotificationManager.push("Config", "Loaded config: " + name, NotificationManager.Type.SUCCESS);
      } else {
         NotificationManager.push("Config", "Config not found: " + name, NotificationManager.Type.WARNING);
      }

      this.refresh();
   }

   private void deleteConfig(String name) {
      ConfigManager.getInstance().deleteProfile(name);
      this.refresh();
      NotificationManager.push("Config", "Deleted config: " + name, NotificationManager.Type.INFO);
   }

   public void render(float mouseX, float mouseY, int headerColor, int fontColor, int accentColor, String[] hoveredDescriptionOut, float screenW, float screenH) {
      if (!this.embedded) {
         if (this.dragging) {
            this.x = (int)(mouseX - this.dragOffsetX);
            this.y = (int)(mouseY - this.dragOffsetY);
         }

         this.clampPosition(screenW, screenH);
      }

      if (this.listDirty) {
         this.rebuildList();
      }

      int headerH = this.embedded ? 0 : this.getHeaderHeight();
      boolean headerHovered = !this.embedded && this.isHeaderHovered((int)mouseX, (int)mouseY);
      if (!this.embedded) {
         float contentH = this.getTotalContentHeight();
         float visibleContentH = this.collapsed ? 0.0F : Math.max(0.0F, screenH - this.y - headerH - 30.0F);
         float panelH = headerH + Math.min(contentH, visibleContentH);
         NvgUtil.save();
         GuiTheme theme = GuiTheme.current();
         float radius = GuiTheme.radius();
         float trans = Math.max(0.1F, Math.min(1.0F, ClickGuiSettings.transparency.getValueFloat()));
         if (ClickGuiSettings.background.isMode("Liquid Glass")) {
            int baseTint = theme.panelBg & 16777215;
            int glassAlpha = Math.max(16, Math.min(255, Math.round(117.0F * trans)));
            NvgUtil.liquidGlass(this.x, this.y, this.width, panelH, radius, glassAlpha << 24 | baseTint, 14.0F);
         } else {
            int panelBg = NvgUtil.withAlpha(theme.panelBg, Math.max(24, Math.round(248.0F * trans)));
            NvgUtil.fillRounded(this.x, this.y, this.width, panelH, radius, panelBg);
         }

         NvgUtil.strokeRounded(this.x - 0.5F, this.y - 0.5F, this.width + 1.0F, panelH + 1.0F, radius, 1.0F, 587202559);
         long now = System.currentTimeMillis();
         if (now < this.highlightUntil) {
            float remain = (float)(this.highlightUntil - now) / 900.0F;
            int glowAlpha = Math.max(0, Math.round(192.0F * remain));
            NvgUtil.strokeRounded(
               this.x - 1.5F, this.y - 1.5F, this.width + 3.0F, panelH + 3.0F, radius + 1.0F, 1.5F + remain, NvgUtil.withAlpha(accentColor, glowAlpha)
            );
         }

         int headerTint = NvgUtil.withAlpha(headerColor, headerHovered ? 116 : 80);
         NvgUtil.fillRoundedTop(this.x, this.y, this.width, headerH, radius, headerTint);
         NvgUtil.fillRect(this.x + 1.0F, this.y + headerH - 1.5F, this.width - 2.0F, 1.5F, accentColor | 0xFF000000);
         float fontSize = headerH >= 22 ? 11.0F : (headerH >= 18 ? 10.2F : 9.5F);
         float textOffsetY = (headerH - fontSize) / 2.0F - 0.5F;
         NvgUtil.drawText("CONFIG", this.x + 7.0F, this.y + textOffsetY, fontSize, fontColor | 0xFF000000, 9);
         if (this.collapsed) {
            NvgUtil.drawChevronRight(this.x + this.width - 7.0F, this.y + headerH / 2.0F, 3.0F, theme.dimText);
         } else {
            NvgUtil.drawChevronDown(this.x + this.width - 7.0F, this.y + headerH / 2.0F, 3.0F, theme.dimText);
         }

         if (!this.collapsed && panelH > headerH) {
            float maxVisibleH = Math.min(contentH, visibleContentH);
            if (maxVisibleH > 0.0F) {
               NvgUtil.intersectScissor(this.x, this.contentTop(), this.width, maxVisibleH);
            }

            this.renderRows(mouseX, mouseY, fontColor, accentColor, hoveredDescriptionOut);
         }

         NvgUtil.restore();
      } else {
         if (!this.collapsed || this.embedded) {
            NvgUtil.save();
            if (this.embeddedVisibleH > 0) {
               NvgUtil.intersectScissor(this.x, this.contentTop(), this.width, this.embeddedVisibleH);
            }

            this.renderRows(mouseX, mouseY, fontColor, accentColor, hoveredDescriptionOut);
            NvgUtil.restore();
         }
      }
   }

   private void renderRows(float mouseX, float mouseY, int fontColor, int accentColor, String[] hoveredDescriptionOut) {
      this.renderNewConfigRow(mouseX, mouseY, fontColor, accentColor, hoveredDescriptionOut);
      int rowCount = this.configs.size();
      if (rowCount == 0) {
         int noConfCol = NvgUtil.darken(GuiTheme.current().dimText, 0.3F);
         NvgUtil.drawText("No configs yet", this.x + 5.0F, this.rowY(0) + 4.0F, 9.5F, noConfCol, 9);
      }

      for (int i = 0; i < rowCount; i++) {
         this.renderConfigRow(mouseX, mouseY, this.configs.get(i), i, fontColor, accentColor, hoveredDescriptionOut);
      }
   }

   private void renderNewConfigRow(float mouseX, float mouseY, int fontColor, int accentColor, String[] hoveredDescriptionOut) {
      float ry = this.newRowY();
      float radius = GuiTheme.radius();
      int rowH = this.getRowHeight();
      int iconBtn = this.getIconBtnSize();
      boolean hovered = mouseX >= this.textboxX() && mouseX <= this.newSaveBtnX() + iconBtn && mouseY >= ry && mouseY <= ry + rowH;
      GuiTheme theme = GuiTheme.current();
      int boxBg = this.textboxFocused ? theme.textboxFocusBg : theme.textboxBg;
      NvgUtil.fillRounded(this.textboxX(), ry, this.textboxW(), rowH, radius, boxBg);
      int borderCol = this.textboxFocused ? accentColor | 0xFF000000 : theme.textboxBorder;
      NvgUtil.strokeRounded(this.textboxX() - 0.5F, ry - 0.5F, this.textboxW() + 1.0F, rowH + 1.0F, radius, 1.0F, borderCol);
      float textY = ry + (rowH - 10.0F) / 2.0F - 0.5F;
      String shown = this.textboxFocused ? this.newConfigName + "_" : this.newConfigName;
      if (shown.isEmpty()) {
         shown = "name...";
      }

      int textColor = this.textboxFocused ? -8875 : (this.newConfigName.isEmpty() ? NvgUtil.darken(theme.dimText, 0.45F) : fontColor | 0xFF000000);
      NvgUtil.drawText(shown, this.textboxX() + 4.0F, textY, 9.5F, textColor, 9);
      boolean saveHovered = this.isIconHovered(mouseX, mouseY, this.newSaveBtnX(), ry);
      if (saveHovered) {
         NvgUtil.fillRounded(this.newSaveBtnX(), ry, iconBtn, rowH, radius, theme.rowHover);
      }

      this.drawIconSave(this.newSaveBtnX() + iconBtn / 2.0F, ry + rowH / 2.0F, saveHovered ? accentColor | 0xFF000000 : theme.dimText);
      if (hovered) {
         hoveredDescriptionOut[0] = "Type a name and click the floppy to save a new config";
      }
   }

   private void renderConfigRow(float mouseX, float mouseY, String name, int index, int fontColor, int accentColor, String[] hoveredDescriptionOut) {
      float ry = this.rowY(index);
      float radius = GuiTheme.radius();
      int rowH = this.getRowHeight();
      int iconBtn = this.getIconBtnSize();
      boolean rowHovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= ry && mouseY <= ry + rowH;
      GuiTheme theme = GuiTheme.current();
      if (rowHovered) {
         NvgUtil.fillRounded(this.x + 3.0F, ry + 0.5F, this.width - 6.0F, rowH - 1.0F, radius, theme.rowHover);
      } else if ((theme.rowBg & 0xFF000000) != 0) {
         NvgUtil.fillRounded(this.x + 3.0F, ry + 0.5F, this.width - 6.0F, rowH - 1.0F, radius, theme.rowBg);
      }

      float textY = ry + (rowH - 10.0F) / 2.0F - 0.5F;
      boolean isActive = name.equalsIgnoreCase(ConfigManager.getInstance().getActiveProfile());
      boolean nameHovered = mouseX >= this.x + 4 && mouseX <= this.iconsStartX() && rowHovered;
      int nameColor = isActive ? accentColor | 0xFF000000 : (nameHovered ? accentColor | 0xFF000000 : fontColor | 0xFF000000);
      String displayName = isActive ? name + " *" : name;
      NvgUtil.drawText(this.clipText(displayName, this.iconsStartX() - this.x - 9.0F), this.x + 5.0F, textY, 10.0F, nameColor, 9);
      if (nameHovered) {
         hoveredDescriptionOut[0] = isActive ? "Active profile '" + name + "'" : "Load '" + name + "'";
      }

      boolean saveHovered = this.isIconHovered(mouseX, mouseY, this.iconsStartX() + 0 * iconBtn, ry);
      boolean loadHovered = this.isIconHovered(mouseX, mouseY, this.iconsStartX() + 1 * iconBtn, ry);
      boolean deleteHovered = this.isIconHovered(mouseX, mouseY, this.iconsStartX() + 2 * iconBtn, ry);
      if (saveHovered) {
         NvgUtil.fillRounded(this.iconsStartX() + 0 * iconBtn, ry, iconBtn, rowH, radius, theme.rowHover);
      } else if (loadHovered) {
         NvgUtil.fillRounded(this.iconsStartX() + 1 * iconBtn, ry, iconBtn, rowH, radius, theme.rowHover);
      } else if (deleteHovered) {
         NvgUtil.fillRounded(this.iconsStartX() + 2 * iconBtn, ry, iconBtn, rowH, radius, theme.rowHover);
      }

      int dim = theme.dimText;
      this.drawIconSave(this.iconCenterX(0), ry + rowH / 2.0F, saveHovered ? accentColor | 0xFF000000 : dim);
      this.drawIconLoad(this.iconCenterX(1), ry + rowH / 2.0F, loadHovered ? accentColor | 0xFF000000 : dim);
      this.drawIconDelete(this.iconCenterX(2), ry + rowH / 2.0F, deleteHovered ? accentColor | 0xFF000000 : dim);
      if (saveHovered) {
         hoveredDescriptionOut[0] = "Save current settings as '" + name + "'";
      } else if (loadHovered) {
         hoveredDescriptionOut[0] = "Load '" + name + "'";
      } else if (deleteHovered) {
         hoveredDescriptionOut[0] = "Delete '" + name + "'";
      }
   }

   private boolean isIconHovered(float mouseX, float mouseY, float ix, float iy) {
      int iconBtn = this.getIconBtnSize();
      int rowH = this.getRowHeight();
      return mouseX >= ix && mouseX <= ix + iconBtn && mouseY >= iy && mouseY <= iy + rowH;
   }

   private String clipText(String text, float maxWidth) {
      if (NvgUtil.textWidth(text, 10.0F) <= maxWidth) {
         return text;
      } else {
         String clipped = text;

         while (!clipped.isEmpty() && NvgUtil.textWidth(clipped + "...", 10.0F) > maxWidth) {
            clipped = clipped.substring(0, clipped.length() - 1);
         }

         return clipped + "...";
      }
   }

   private void drawIconSave(float cx, float cy, int color) {
      NvgUtil.fillRounded(cx - 4.5F, cy - 4.0F, 9.0F, 8.0F, 1.2F, color);
      NvgUtil.fillRect(cx - 2.2F, cy - 2.6F, 4.4F, 4.4F, GuiTheme.current().textboxBg);
   }

   private void drawIconLoad(float cx, float cy, int color) {
      NvgUtil.drawLine(cx, cy - 3.6F, cx, cy + 1.2F, 1.3F, color);
      NvgUtil.drawLine(cx - 3.0F, cy - 0.8F, cx, cy + 2.2F, 1.3F, color);
      NvgUtil.drawLine(cx, cy + 2.2F, cx + 3.0F, cy - 0.8F, 1.3F, color);
      NvgUtil.drawLine(cx - 4.2F, cy + 3.6F, cx + 4.2F, cy + 3.6F, 1.3F, color);
   }

   private void drawIconDelete(float cx, float cy, int color) {
      NvgUtil.drawLine(cx - 4.5F, cy - 3.4F, cx + 4.5F, cy - 3.4F, 1.3F, color);
      NvgUtil.drawLine(cx - 1.6F, cy - 4.4F, cx + 1.6F, cy - 4.4F, 1.3F, color);
      NvgUtil.drawLine(cx - 3.1F, cy - 1.9F, cx - 2.5F, cy + 4.2F, 1.3F, color);
      NvgUtil.drawLine(cx - 2.5F, cy + 4.2F, cx + 2.5F, cy + 4.2F, 1.3F, color);
      NvgUtil.drawLine(cx + 2.5F, cy + 4.2F, cx + 3.1F, cy - 1.9F, 1.3F, color);
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int btn = event.buttonInfo().button();
      if (!this.embedded && this.isHeaderHovered(mouseX, mouseY)) {
         if (btn == 0) {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.x;
            this.dragOffsetY = mouseY - this.y;
            return true;
         }

         if (btn == 1) {
            this.collapsed = !this.collapsed;
            return true;
         }
      }

      if (this.collapsed && !this.embedded) {
         return false;
      } else {
         int headerH = this.embedded ? 0 : this.getHeaderHeight();
         int contentH = this.getTotalContentHeight();
         float visibleContentH = this.embedded ? this.embeddedVisibleH : (this.collapsed ? 0.0F : Math.max(0.0F, this.lastScreenH - this.y - headerH - 30.0F));
         float panelH = headerH + Math.min((float)contentH, visibleContentH);
         float bottom = this.y + panelH;
         float top = this.contentTop();
         if (mouseX >= this.x && mouseX <= this.x + this.width && !(mouseY < top) && !(mouseY > bottom)) {
            float ry = this.newRowY();
            int rowH = this.getRowHeight();
            int iconBtn = this.getIconBtnSize();
            if (btn == 0) {
               if (this.isIconHovered(mouseX, mouseY, this.newSaveBtnX(), ry)) {
                  this.saveNewConfig();
                  return true;
               }

               if (mouseX >= this.textboxX() && mouseX <= this.textboxX() + this.textboxW() && mouseY >= ry && mouseY <= ry + rowH) {
                  this.textboxFocused = true;
                  return true;
               }

               this.textboxFocused = false;
               int rowCount = this.configs.size();

               for (int i = 0; i < rowCount; i++) {
                  float rowY = this.rowY(i);
                  if (!(rowY + rowH < top)) {
                     if (rowY > bottom) {
                        break;
                     }

                     if (!(mouseY < rowY) && !(mouseY > rowY + rowH)) {
                        String name = this.configs.get(i);
                        if (mouseX >= this.iconsStartX() && mouseX <= this.iconsStartX() + iconBtn) {
                           this.saveConfig(name);
                        } else if (mouseX >= this.iconsStartX() + iconBtn && mouseX <= this.iconsStartX() + iconBtn * 2) {
                           this.loadConfig(name);
                        } else if (mouseX >= this.iconsStartX() + iconBtn * 2 && mouseX <= this.x + this.width - 4) {
                           this.deleteConfig(name);
                        } else if (mouseX >= this.x && mouseX <= this.iconsStartX()) {
                           this.loadConfig(name);
                        }

                        return true;
                     }
                  }
               }
            }

            return !this.embedded && mouseY >= top && mouseY <= bottom && mouseX >= this.x && mouseX <= this.x + this.width;
         } else {
            return false;
         }
      }
   }

   public void mouseReleased(MouseButtonEvent event) {
      this.dragging = false;
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (this.dragging) {
         this.x = (int)(this.x + dragX);
         this.y = (int)(this.y + dragY);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, double speedMultiplier) {
      int headerH = this.embedded ? 0 : this.getHeaderHeight();
      int contentH = this.getTotalContentHeight();
      float visibleContentH = this.embedded ? this.embeddedVisibleH : (this.collapsed ? 0.0F : Math.max(0.0F, this.lastScreenH - this.y - headerH - 30.0F));
      float panelH = headerH + Math.min((float)contentH, visibleContentH);
      if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + panelH) {
         int maxScroll = Math.max(0, contentH - (this.embedded ? this.embeddedVisibleH : 200));
         this.scrollOffset += (int)(scrollAmount * 15.0 * speedMultiplier);
         if (this.scrollOffset > 0) {
            this.scrollOffset = 0;
         }

         if (this.scrollOffset < -maxScroll) {
            this.scrollOffset = -maxScroll;
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(KeyEvent event) {
      if (!this.textboxFocused) {
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
            this.saveNewConfig();
            return true;
         } else if (event.key() == 256) {
            this.textboxFocused = false;
            return true;
         } else {
            return false;
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
      if (this.textboxFocused && event.isAllowedChatCharacter()) {
         if (this.newConfigName.length() < 24) {
            this.newConfigName = this.newConfigName + event.codepointAsString();
         }

         return true;
      } else {
         return false;
      }
   }
}
