package kat.respect.b2.gui.hud;

import java.util.List;
import kat.respect.b2.event.EventHandler;
import kat.respect.b2.event.events.Render2DEvent;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.module.Category;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.setting.BooleanSetting;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.module.setting.ModeSetting;
import kat.respect.b2.module.setting.NumberSetting;
import kat.respect.b2.module.setting.Setting;
import kat.respect.b2.util.ScreenUtil;

public abstract class HudModule extends Module {
   private static final int BACKGROUND = 1342769170;
   private static final int OUTLINE = 587202559;
   private static final int INFO_COLOR = -3552806;
   private static final int TEXT_COLOR = -1;
   private static final int DEFAULT_ACCENT = -1952175;
   private static final int SHADOW = -1342177280;
   private final String layoutId;
   private final ModeSetting position;
   private final NumberSetting marginX;
   private final NumberSetting marginY;
   private final NumberSetting scale;
   private final BooleanSetting background;
   private final BooleanSetting outline;
   private final BooleanSetting accentBar;
   private final ColorSetting accent;
   private final BooleanSetting textShadow;

   protected HudModule(String name, String description, String layoutId, String defaultPosition, double defaultMarginY) {
      super(name, description, Category.INTERFACE);
      this.layoutId = layoutId;
      this.position = new ModeSetting("Position", "Screen anchor", defaultPosition, List.of("Top Left", "Top Right", "Bottom Left", "Bottom Right"));
      this.marginX = new NumberSetting("Margin X", "Distance from the screen edge", 2.0, 0.0, 100.0, 1.0);
      this.marginY = new NumberSetting("Margin Y", "Distance from the top/bottom edge", defaultMarginY, 0.0, 200.0, 1.0);
      this.scale = new NumberSetting("Scale", "HUD scale multiplier", 0.8, 0.5, 2.0, 0.05);
      this.background = new BooleanSetting("Background", "Translucent background plate", true);
      this.outline = new BooleanSetting("Outline", "Subtle outline around the element", true);
      this.accentBar = new BooleanSetting("Accent Bar", "Coloured bar on the outer edge", true);
      this.accent = new ColorSetting("Accent", "Accent colour", -1952175);
      this.textShadow = new BooleanSetting("Text Shadow", "Draw a shadow behind the text", false);
      this.addSettings(
         new Setting[]{this.position, this.marginX, this.marginY, this.scale, this.background, this.outline, this.accentBar, this.accent, this.textShadow}
      );
   }

   protected abstract String label();

   protected abstract String value();

   protected boolean accentLabel() {
      return false;
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (this.mc.getWindow() != null) {
         if (!ScreenUtil.hasScreen(this.mc) || HudEditorScreen.isOpen()) {
            this.renderHud(this.mc.getWindow().getGuiScaledWidth(), this.mc.getWindow().getGuiScaledHeight());
         }
      }
   }

   @Override
   protected void onDisable() {
      HudLayout.clearBounds(this.layoutId);
   }

   private void renderHud(float screenW, float screenH) {
      HudLayout.Entry layout = HudLayout.of(this.layoutId);
      if (!layout.enabled) {
         HudLayout.clearBounds(this.layoutId);
      } else {
         String label = this.label();
         String value = this.value();
         if (label != null && value != null) {
            float s = this.scale.getValueFloat() * layout.scale;
            float labelSize = 10.0F * s;
            float valueSize = 8.5F * s;
            float pad = 5.0F * s;
            float barW = this.accentBar.getValue() ? 2.5F * s : 0.0F;
            float h = 15.0F * s;
            float gap = value.isEmpty() ? 0.0F : 4.0F * s;
            float labelW = NvgUtil.textWidth(label, labelSize);
            float valueW = value.isEmpty() ? 0.0F : NvgUtil.textWidth(value, valueSize);
            float w = pad + labelW + gap + valueW + pad + barW;
            boolean right = this.position.getValue().endsWith("Right");
            boolean bottom = this.position.getValue().startsWith("Bottom");
            float margin = this.marginX.getValueFloat() * s;
            float edge = this.marginY.getValueFloat() * s;
            float anchorX = (right ? screenW - margin : margin) + layout.offsetX;
            float y = (bottom ? screenH - edge - h : edge) + layout.offsetY;
            float x = right ? anchorX - w : anchorX;
            int accentColor = this.accent.getValue();
            if (this.background.getValue()) {
               NvgUtil.fillRect(x, y, w, h, 1342769170);
            }

            if (this.outline.getValue()) {
               NvgUtil.strokeRect(x - 0.5F, y - 0.5F, w + 1.0F, h + 1.0F, 1.0F, 587202559);
            }

            if (barW > 0.0F) {
               NvgUtil.fillRect(right ? x + w - barW : x, y, barW, h, accentColor);
            }

            float centerY = y + h * 0.5F;
            float textX = x + (right ? 0.0F : barW) + pad;
            int labelColor = this.accentLabel() ? accentColor : -1;
            if (this.textShadow.getValue()) {
               NvgUtil.drawText(label, textX + 1.0F, centerY + 1.0F, labelSize, -1342177280, 17);
               if (!value.isEmpty()) {
                  NvgUtil.drawText(value, textX + labelW + gap + 1.0F, centerY + 1.0F, valueSize, -1342177280, 17);
               }
            }

            NvgUtil.drawText(label, textX, centerY, labelSize, labelColor, 17);
            if (!value.isEmpty()) {
               NvgUtil.drawText(value, textX + labelW + gap, centerY, valueSize, -3552806, 17);
            }

            HudLayout.reportBounds(this.layoutId, x, y, w, h);
         } else {
            HudLayout.clearBounds(this.layoutId);
         }
      }
   }
}
