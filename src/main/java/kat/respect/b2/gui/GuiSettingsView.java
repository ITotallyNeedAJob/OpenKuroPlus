package kat.respect.b2.gui;

import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.gui.entity.UIEntity;
import kat.respect.b2.gui.entity.UIIcon;
import kat.respect.b2.gui.entity.UIScrollContainer;
import kat.respect.b2.gui.entity.UISettingFactory;
import kat.respect.b2.module.setting.Setting;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class GuiSettingsView {
   private final UIScrollContainer scroll = new UIScrollContainer();
   private boolean built = false;
   private float x;
   private float y;
   private float width;
   private float height;

   public GuiSettingsView() {
      this.scroll.layout(UIEntity.LayoutMode.COLUMN);
      this.scroll.gap(0.0F, 4.0F);
      this.scroll.padding(0.0F, 0.0F, 8.0F, 0.0F);
   }

   private void build() {
      this.scroll.clear();

      for (Setting<?> setting : ClickGuiSettings.getAllSettings()) {
         UIEntity entity = UISettingFactory.createForSetting(setting);
         if (entity != null) {
            entity.fillWidth(true);
            entity.margin(2.0F);
            this.scroll.add(entity);
         }
      }

      this.built = true;
   }

   public void reset() {
      this.built = false;
      this.scroll.resetScroll();
   }

   private void layout(float screenW, float screenH) {
      this.width = Math.min(600.0F, screenW - 40.0F);
      this.height = Math.min(470.0F, screenH - 72.0F);
      this.x = (screenW - this.width) * 0.5F;
      this.y = (screenH - this.height) * 0.5F;
   }

   public void render(float mouseX, float mouseY, int headerColor, int fontColor, int accentColor, String[] hoveredDescriptionOut, float screenW, float screenH) {
      this.layout(screenW, screenH);
      if (!this.built) {
         this.build();
      }

      GuiTheme theme = GuiTheme.current();
      float trans = Math.max(0.1F, Math.min(1.0F, ClickGuiSettings.transparency.getValueFloat()));
      float radius = GuiTheme.radius();
      NvgUtil.fillRounded(this.x, this.y, this.width, this.height, radius, NvgUtil.withAlpha(theme.panelBg, Math.max(24, Math.round(248.0F * trans))));
      NvgUtil.strokeRounded(this.x - 0.5F, this.y - 0.5F, this.width + 1.0F, this.height + 1.0F, radius, 1.0F, 587202559);
      float headerH = 36.0F;
      NvgUtil.fillRoundedTop(this.x, this.y, this.width, headerH, radius, NvgUtil.withAlpha(headerColor, 112));
      NvgUtil.fillRect(this.x + 1.0F, this.y + headerH - 1.5F, this.width - 2.0F, 1.5F, accentColor | 0xFF000000);
      NvgUtil.drawText("SETTINGS", this.x + 12.0F, this.y + 13.0F, 10.5F, fontColor | 0xFF000000, 9);
      float closeS = 18.0F;
      float closeX = this.x + this.width - 12.0F - closeS;
      float closeY = this.y + (headerH - closeS) * 0.5F;
      boolean closeHover = mouseX >= closeX && mouseX <= closeX + closeS && mouseY >= closeY && mouseY <= closeY + closeS;
      UIIcon.drawClose(closeX + closeS * 0.5F, closeY + closeS * 0.5F, 8.0F, closeHover ? fontColor | 0xFF000000 : theme.dimText);
      float contentX = this.x + 14.0F;
      float contentY = this.y + headerH + 10.0F;
      float contentW = this.width - 28.0F;
      float contentH = this.height - (contentY - this.y) - 12.0F;
      this.scroll.bounds(contentX, contentY, contentW, contentH);
      this.scroll.layout();
      this.scroll.render(mouseX, mouseY);
      if (closeHover) {
         hoveredDescriptionOut[0] = "Back to ClickGUI";
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      float mx = (float)event.x();
      float my = (float)event.y();
      float closeS = 18.0F;
      float closeX = this.x + this.width - 12.0F - closeS;
      float closeY = this.y + (36.0F - closeS) * 0.5F;
      if (event.buttonInfo().button() == 0 && mx >= closeX && mx <= closeX + closeS && my >= closeY && my <= closeY + closeS) {
         ClickGuiScreen.getInstance().showModules();
         return true;
      } else {
         return this.scroll.mouseClicked(event) ? true : mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height;
      }
   }

   public void mouseReleased(MouseButtonEvent event) {
      this.scroll.mouseReleased(event);
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      return this.scroll.mouseDragged(event, dragX, dragY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.scroll.mouseScrolled(mouseX, mouseY, amount);
   }

   public boolean keyPressed(KeyEvent event) {
      return this.scroll.keyPressed(event);
   }

   public boolean charTyped(CharacterEvent event) {
      return this.scroll.charTyped(event);
   }
}
