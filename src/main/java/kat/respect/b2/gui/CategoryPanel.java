package kat.respect.b2.gui;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.gui.entity.UIIcon;
import kat.respect.b2.module.Category;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class CategoryPanel {
   private final Category category;
   private int x;
   private int y;
   private final int width;
   private boolean collapsed = false;
   private boolean dragging = false;
   private float dragOffsetX;
   private float dragOffsetY;
   private int scrollOffset = 0;
   private float lastScreenH = 720.0F;
   private final List<ModuleButton> allButtons = new ArrayList<>();
   private final List<ModuleButton> buttons = new ArrayList<>();
   private String searchFilter = "";
   private final String titleText;
   private String cachedCounterTag = "[0/0]";

   public CategoryPanel(Category category, int x, int y, int width) {
      this.category = category;
      this.x = x;
      this.y = y;
      this.width = width;
      this.titleText = category.getDisplayName().toUpperCase();
      this.rebuildButtons();
   }

   public void rebuildButtons() {
      this.allButtons.clear();

      for (Module mod : ModuleManager.INSTANCE.getByCategory(this.category)) {
         this.allButtons.add(new ModuleButton(mod));
      }

      this.applySearchFilter();
   }

   public void setSearchFilter(String query) {
      this.searchFilter = query == null ? "" : query.trim().toLowerCase();
      this.applySearchFilter();
   }

   private void applySearchFilter() {
      this.buttons.clear();
      if (this.searchFilter.isEmpty()) {
         this.buttons.addAll(this.allButtons);
      } else {
         for (ModuleButton button : this.allButtons) {
            Module mod = button.getModule();
            if (mod.getName().toLowerCase().contains(this.searchFilter) || mod.getDescription().toLowerCase().contains(this.searchFilter)) {
               this.buttons.add(button);
            }
         }
      }

      this.updateCounterTag();
      this.scrollOffset = 0;
   }

   public boolean hasMatches() {
      return !this.buttons.isEmpty();
   }

   private void updateCounterTag() {
      int enabled = 0;
      int total = this.buttons.size();

      for (int i = 0; i < total; i++) {
         if (this.buttons.get(i).getModule().isEnabled()) {
            enabled++;
         }
      }

      this.cachedCounterTag = "[" + enabled + "/" + total + "]";
   }

   public Category getCategory() {
      return this.category;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public void setX(int x) {
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   public void setCollapsed(boolean collapsed) {
      this.collapsed = collapsed;
   }

   public int getWidth() {
      return this.width;
   }

   public boolean isCollapsed() {
      return this.collapsed;
   }

   public int getHeaderHeight() {
      String hMode = ClickGuiSettings.headerHeight.getValue();
      if ("Standard (18px)".equals(hMode)) {
         return 18;
      } else {
         return "Tall (22px)".equals(hMode) ? 22 : 15;
      }
   }

   public boolean isHeaderHovered(int mouseX, int mouseY) {
      return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.getHeaderHeight();
   }

   public int getTotalContentHeight() {
      if (this.collapsed) {
         return 0;
      } else {
         int h = 0;
         int btnCount = this.buttons.size();

         for (int i = 0; i < btnCount; i++) {
            h += this.buttons.get(i).getHeight();
         }

         return h;
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

   public void render(float mouseX, float mouseY, int headerColor, int fontColor, int accentColor, String[] hoveredDescriptionOut, float screenW, float screenH) {
      if (this.dragging) {
         this.x = (int)(mouseX - this.dragOffsetX);
         this.y = (int)(mouseY - this.dragOffsetY);
      }

      this.clampPosition(screenW, screenH);
      this.updateCounterTag();

      for (int i = 0; i < this.allButtons.size(); i++) {
         this.allButtons.get(i).updateAnimations();
      }

      int headerH = this.getHeaderHeight();
      int contentH = this.getTotalContentHeight();
      float visibleContentH = this.collapsed ? 0.0F : Math.max(0.0F, screenH - this.y - headerH - 30.0F);
      float panelH = headerH + Math.min((float)contentH, visibleContentH);
      boolean headerHovered = this.isHeaderHovered((int)mouseX, (int)mouseY);
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
      int headerTint = NvgUtil.withAlpha(headerColor, headerHovered ? 116 : 80);
      NvgUtil.fillRoundedTop(this.x, this.y, this.width, headerH, radius, headerTint);
      NvgUtil.fillRect(this.x + 1.0F, this.y + headerH - 1.5F, this.width - 2.0F, 1.5F, accentColor | 0xFF000000);
      float fontSize = headerH >= 22 ? 11.0F : (headerH >= 18 ? 10.2F : 9.5F);
      float textOffsetY = (headerH - fontSize) / 2.0F - 0.5F;
      float iconCx = this.x + 12.0F;
      float iconCy = this.y + headerH * 0.5F;
      int iconCol = fontColor | 0xFF000000;
      switch (this.category) {
         case COMBAT:
            UIIcon.drawSword(iconCx, iconCy, 11.0F, iconCol);
            break;
         case MOVEMENT:
            UIIcon.drawMovement(iconCx, iconCy, 11.0F, iconCol);
            break;
         case RENDER:
            UIIcon.drawEye(iconCx, iconCy, 11.0F, iconCol);
            break;
         case WORLD:
            UIIcon.drawCube(iconCx, iconCy, 11.0F, iconCol);
            break;
         case MISC:
            UIIcon.drawSliders(iconCx, iconCy, 11.0F, iconCol);
            break;
         case INTERFACE:
            UIIcon.drawInterface(iconCx, iconCy, 11.0F, iconCol);
      }

      NvgUtil.drawText(this.titleText, this.x + 24.0F, this.y + textOffsetY, fontSize, fontColor | 0xFF000000, 9);
      if (ClickGuiSettings.showCounters.getValue()) {
         float tagW = NvgUtil.textWidth(this.cachedCounterTag, fontSize - 1.0F) + 8.0F;
         float tagH = 12.0F;
         float tagX = this.x + this.width - tagW - 16.0F;
         float tagY = this.y + (headerH - tagH) * 0.5F;
         NvgUtil.fillRounded(tagX, tagY, tagW, tagH, 3.0F, 419430399);
         NvgUtil.drawText(this.cachedCounterTag, tagX + tagW * 0.5F, tagY + 1.5F, fontSize - 1.0F, theme.dimText, 10);
      }

      if (this.collapsed) {
         NvgUtil.drawChevronRight(this.x + this.width - 7.0F, this.y + headerH / 2.0F, 3.0F, theme.dimText);
      } else {
         NvgUtil.drawChevronDown(this.x + this.width - 7.0F, this.y + headerH / 2.0F, 3.0F, theme.dimText);
      }

      if (!this.collapsed && panelH > headerH) {
         float maxVisibleH = Math.min((float)contentH, visibleContentH);
         if (maxVisibleH > 0.0F) {
            NvgUtil.intersectScissor(this.x, this.y + headerH, this.width, maxVisibleH);
         }

         int currentY = this.y + headerH + this.scrollOffset;
         int btnCount = this.buttons.size();

         for (int i = 0; i < btnCount; i++) {
            ModuleButton button = this.buttons.get(i);
            int buttonH = button.getHeight();
            button.render(mouseX, mouseY, this.x, currentY, this.width, fontColor, accentColor);
            if (button.isHeaderHovered((int)mouseX, (int)mouseY, this.x, currentY, this.width)) {
               hoveredDescriptionOut[0] = button.getModule().getDescription();
            }

            currentY += buttonH;
         }
      }

      NvgUtil.restore();
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int btn = event.buttonInfo().button();
      if (this.isHeaderHovered(mouseX, mouseY)) {
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

      if (this.collapsed) {
         return false;
      } else {
         int headerH = this.getHeaderHeight();
         int contentH = this.getTotalContentHeight();
         float visibleContentH = Math.max(0.0F, this.lastScreenH - this.y - headerH - 30.0F);
         float panelH = headerH + Math.min((float)contentH, visibleContentH);
         float visibleBottom = this.y + panelH;
         if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y + headerH && !(mouseY > visibleBottom)) {
            int currentY = this.y + headerH + this.scrollOffset;
            int btnCount = this.buttons.size();

            for (int i = 0; i < btnCount; i++) {
               ModuleButton button = this.buttons.get(i);
               int buttonH = button.getHeight();
               if (currentY >= visibleBottom) {
                  break;
               }

               if (button.mouseClicked(event, this.x, currentY, this.width, visibleBottom)) {
                  return true;
               }

               currentY += buttonH;
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public void mouseReleased(MouseButtonEvent event) {
      this.dragging = false;
      if (!this.collapsed) {
         int currentY = this.y + this.getHeaderHeight() + this.scrollOffset;
         int btnCount = this.buttons.size();

         for (int i = 0; i < btnCount; i++) {
            ModuleButton button = this.buttons.get(i);
            int buttonH = button.getHeight();
            button.mouseReleased(event, this.x, currentY, this.width);
            currentY += buttonH;
         }
      }
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (this.dragging) {
         this.x = (int)(this.x + dragX);
         this.y = (int)(this.y + dragY);
         return true;
      } else {
         if (!this.collapsed) {
            int currentY = this.y + this.getHeaderHeight() + this.scrollOffset;
            int btnCount = this.buttons.size();

            for (int i = 0; i < btnCount; i++) {
               ModuleButton button = this.buttons.get(i);
               int buttonH = button.getHeight();
               if (button.mouseDragged(event, this.x, currentY, this.width, dragX, dragY)) {
                  return true;
               }

               currentY += buttonH;
            }
         }

         return false;
      }
   }

   public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, double speedMultiplier) {
      int headerH = this.getHeaderHeight();
      int contentH = this.getTotalContentHeight();
      float visibleContentH = this.collapsed ? 0.0F : Math.max(0.0F, this.lastScreenH - this.y - headerH - 30.0F);
      float panelH = headerH + Math.min((float)contentH, visibleContentH);
      if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + panelH) {
         int maxScroll = Math.max(0, contentH - 200);
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
      if (!this.collapsed) {
         for (ModuleButton button : this.buttons) {
            if (button.keyPressed(event)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean charTyped(CharacterEvent event) {
      if (!this.collapsed) {
         for (ModuleButton button : this.buttons) {
            if (button.charTyped(event)) {
               return true;
            }
         }
      }

      return false;
   }
}
