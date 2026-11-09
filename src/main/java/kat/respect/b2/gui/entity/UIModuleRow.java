package kat.respect.b2.gui.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.setting.Setting;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class UIModuleRow extends UIEntity {
   public static final float BASE_HEIGHT = 36.0F;
   private static final float SETTINGS_TOP = 4.0F;
   private static final float SETTINGS_BOTTOM = 8.0F;
   private static final float SETTINGS_INDENT = 10.0F;
   private static final float CHEVRON_INSET = 15.0F;
   private static final float TOGGLE_W = 26.0F;
   private static final float TOGGLE_H = 14.0F;
   private final Module module;
   private final UIContainer settings;
   private final List<UIEntity> settingEntities = new ArrayList<>();
   private final List<Setting<?>> settingSources = new ArrayList<>();
   private boolean built = false;
   private boolean expanded = false;
   private float expandAnim = 0.0F;
   private float settingsContentHeight = 0.0F;
   private Consumer<Boolean> expandListener;

   public UIModuleRow(Module module) {
      this.module = module;
      this.radius = 7.0F;
      this.clipToBounds = true;
      this.settings = UIContainer.column(6.0F);
      this.settings.visible(false);
      this.add(this.settings);
   }

   public Module getModule() {
      return this.module;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public void setExpanded(boolean expanded) {
      this.expanded = expanded;
   }

   public void onExpand(Consumer<Boolean> listener) {
      this.expandListener = listener;
   }

   private void toggleExpanded() {
      this.expanded = !this.expanded;
      if (this.expandListener != null) {
         this.expandListener.accept(this.expanded);
      }
   }

   private void buildSettings() {
      if (!this.built) {
         this.built = true;
         UIKeybind keybind = new UIKeybind("Keybind", this.module::getKey);
         keybind.onBind(this.module::setKey);
         keybind.fillWidth(true);
         this.addSettingEntity(null, keybind);

         for (Setting<?> setting : this.module.getSettings()) {
            UIEntity entity = UISettingFactory.createForSetting(setting);
            if (entity != null) {
               entity.fillWidth(true);
               this.addSettingEntity(setting, entity);
            }
         }
      }
   }

   private void addSettingEntity(Setting<?> source, UIEntity entity) {
      this.settingSources.add(source);
      this.settingEntities.add(entity);
      this.settings.add(entity);
   }

   private void refreshVisibility() {
      for (int i = 0; i < this.settingEntities.size(); i++) {
         Setting<?> source = this.settingSources.get(i);
         this.settingEntities.get(i).visible(source == null || source.isVisible());
      }
   }

   @Override
   public void layout() {
      if (this.visible) {
         if (!this.built) {
            this.buildSettings();
         }

         float expand = this.anim("expand", this.expanded ? 1.0F : 0.0F, 24.0F);
         this.expandAnim = expand;
         this.refreshVisibility();
         float innerW = Math.max(0.0F, this.width - 20.0F);
         this.settings.bounds(this.x + 10.0F, this.y + 36.0F + 4.0F, innerW, 0.0F);
         this.settings.visible(true);
         this.settings.layout();
         this.settingsContentHeight = this.settings.computeContentHeight();
         this.settings.visible(expand > 0.001F);
         float settingsH = 4.0F + this.settingsContentHeight + 8.0F;
         this.height = 36.0F + expand * settingsH;
      }
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      GuiTheme theme = GuiTheme.current();
      int accent = theme.accentColor | 0xFF000000;
      boolean enabled = this.module.isEnabled();
      float activeP = this.anim("active", enabled ? 1.0F : 0.0F, 24.0F);
      float hoverP = this.anim("hover", this.hovered ? 1.0F : 0.0F, 20.0F);
      float expand = this.expandAnim;
      int idleBg = NvgUtil.lerpColor(-15396064, -15001303, hoverP);
      int activeBg = NvgUtil.lerpColor(-14935760, -14278592, hoverP);
      int rowBg = NvgUtil.lerpColor(idleBg, activeBg, activeP);
      NvgUtil.fillRounded(this.x, this.y, this.width, this.height, this.radius, rowBg);
      int idleBorder = NvgUtil.lerpColor(352321535, 788529151, hoverP);
      int activeBorder = NvgUtil.withAlpha(accent, Math.round(130.0F + 90.0F * activeP));
      int border = NvgUtil.lerpColor(idleBorder, activeBorder, activeP);
      NvgUtil.strokeRounded(this.x + 0.5F, this.y + 0.5F, this.width - 1.0F, this.height - 1.0F, this.radius, 1.0F, border);
      if (activeP > 0.01F) {
         float barH = 18.0F * activeP;
         int barCol = NvgUtil.withAlpha(accent, activeP);
         NvgUtil.fillRounded(this.x + 4.5F, this.y + (36.0F - barH) * 0.5F, 2.5F, barH, 1.25F, barCol);
      }

      if (expand > 0.01F) {
         int bgCol = NvgUtil.withAlpha(201326592, expand);
         NvgUtil.fillRounded(
            this.x + 4.0F, this.y + 36.0F, this.width - 8.0F, Math.max(0.0F, this.height - 36.0F - 4.0F), Math.max(0.0F, this.radius - 2.0F), bgCol
         );
         int lineCol = NvgUtil.withAlpha(352321535, expand);
         NvgUtil.drawLine(this.x + 10.0F, this.y + 36.0F, this.x + this.width - 10.0F, this.y + 36.0F, 1.0F, lineCol);
      }

      float centerY = this.y + 18.0F;
      float chevronCx = this.x + this.width - 15.0F;
      float toggleX = chevronCx - 18.0F - 26.0F;
      this.drawToggle(toggleX, centerY - 7.0F, activeP, hoverP);
      float rightEdge = toggleX - 8.0F;
      if (this.module.getKey() > 0) {
         String keyName = UIKeybind.getKeyName(this.module.getKey());
         float keyW = Math.min(72.0F, NvgUtil.textWidth(keyName, 8.0F) + 10.0F);
         float keyX = rightEdge - keyW;
         NvgUtil.fillRounded(keyX, centerY - 7.0F, keyW, 14.0F, 3.0F, 385875967);
         NvgUtil.drawText(keyName, keyX + keyW * 0.5F, centerY - 4.0F, 8.0F, 2030043135, 10);
         rightEdge = keyX - 8.0F;
      }

      float textX = this.x + 13.0F;
      float maxTextW = Math.max(24.0F, rightEdge - textX);
      String desc = this.module.getDescription();
      boolean hasDesc = desc != null && !desc.isEmpty();
      float nameY = hasDesc ? this.y + 7.5F : centerY - 5.0F;
      int nameIdle = NvgUtil.lerpColor(-920071, -1, hoverP);
      int nameColor = NvgUtil.lerpColor(nameIdle, NvgUtil.brighten(accent, 0.2F), activeP);
      NvgUtil.drawText(this.module.getName(), textX, nameY, 10.5F, nameColor, 9);
      if (hasDesc) {
         NvgUtil.drawText(truncate(desc, 8.2F, maxTextW), textX, this.y + 21.0F, 8.2F, -8619374, 9);
      }

      int chevronCol = NvgUtil.lerpColor(1174405119, -1426063361, hoverP);
      if (expand < 1.0F) {
         int c = NvgUtil.withAlpha(chevronCol, 1.0F - expand);
         NvgUtil.drawChevronRight(chevronCx - 1.0F, centerY, 2.6F, c);
      }

      if (expand > 0.0F) {
         int c = NvgUtil.withAlpha(chevronCol, expand);
         NvgUtil.drawChevronDown(chevronCx, centerY - 1.0F, 2.6F, c);
      }
   }

   private void drawToggle(float tx, float ty, float activeP, float hoverP) {
      int accent = GuiTheme.current().accentColor | 0xFF000000;
      int offBg = NvgUtil.lerpColor(-14014663, -13356982, hoverP);
      int track = NvgUtil.lerpColor(offBg, NvgUtil.brighten(accent, 0.1F), activeP);
      NvgUtil.fillRounded(tx, ty, 26.0F, 14.0F, 7.0F, track);
      int strokeCol = NvgUtil.lerpColor(419430399, NvgUtil.withAlpha(accent, 102), activeP);
      NvgUtil.strokeRounded(tx - 0.5F, ty - 0.5F, 27.0F, 15.0F, 7.0F, 1.0F, strokeCol);
      float knobCx = tx + 7.0F + 12.0F * activeP;
      NvgUtil.fillCircle(knobCx, ty + 7.0F + 1.0F, 5.0F, 1073741824);
      NvgUtil.fillCircle(knobCx, ty + 7.0F, 5.0F, -1);
   }

   private static String truncate(String text, float size, float maxWidth) {
      if (text != null && !(maxWidth <= 0.0F)) {
         if (NvgUtil.textWidth(text, size) <= maxWidth) {
            return text;
         } else {
            String ellipsis = "...";
            float ellipsisW = NvgUtil.textWidth(ellipsis, size);
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
               sb.append(text.charAt(i));
               if (NvgUtil.textWidth(sb.toString(), size) + ellipsisW > maxWidth) {
                  if (sb.length() > 1) {
                     sb.setLength(sb.length() - 1);
                  }

                  sb.append(ellipsis);
                  break;
               }
            }

            return sb.toString();
         }
      } else {
         return "";
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         float mx = (float)event.x();
         float my = (float)event.y();
         if (this.expanded && my > this.y + 36.0F && this.settings.mouseClicked(event)) {
            return true;
         } else if (!this.contains(mx, my)) {
            return false;
         } else {
            int btn = event.buttonInfo().button();
            float centerY = this.y + 18.0F;
            float chevronCx = this.x + this.width - 15.0F;
            boolean onChevron = mx >= chevronCx - 11.0F && mx <= chevronCx + 11.0F && my >= centerY - 11.0F && my <= centerY + 11.0F;
            if (onChevron && btn == 0) {
               this.toggleExpanded();
               return true;
            } else {
               if (my <= this.y + 36.0F) {
                  if (btn == 1) {
                     this.toggleExpanded();
                     return true;
                  }

                  if (btn == 0) {
                     if ("ClickGUI".equalsIgnoreCase(this.module.getName())) {
                        this.toggleExpanded();
                     } else {
                        this.module.toggle();
                     }

                     return true;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      return this.settings.mouseDragged(event, dragX, dragY);
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      return this.settings.keyPressed(event);
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      return this.settings.charTyped(event);
   }
}
