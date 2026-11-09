package kat.respect.b2.gui.entity;

import java.util.function.Consumer;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.module.Module;
import net.minecraft.client.input.MouseButtonEvent;

public class UIModuleCard extends UIEntity {
   private final Module module;
   private Consumer<Module> openSettingsListener;
   private String cachedTruncatedDesc = "";
   private String lastDescInput = "";
   private float lastDescW = -1.0F;

   public UIModuleCard(Module module) {
      this.module = module;
      this.height = 46.0F;
      this.radius = 8.0F;
   }

   public Module getModule() {
      return this.module;
   }

   public UIModuleCard onOpenSettings(Consumer<Module> listener) {
      this.openSettingsListener = listener;
      return this;
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      boolean enabled = this.module.isEnabled();
      float activeP = this.anim("active", enabled ? 1.0F : 0.0F, 22.0F);
      float hoverP = this.anim("hover", this.hovered ? 1.0F : 0.0F, 20.0F);
      GuiTheme theme = GuiTheme.current();
      int accentColor = theme.accentColor | 0xFF000000;
      int idleBg = NvgUtil.lerpColor(-15264476, -14869715, hoverP);
      int activeBg = NvgUtil.lerpColor(-14804939, -14147769, hoverP);
      int curBg = NvgUtil.lerpColor(idleBg, activeBg, activeP);
      NvgUtil.fillRounded(this.x, this.y, this.width, this.height, this.radius, curBg);
      int idleBorder = NvgUtil.lerpColor(419430399, 905969663, hoverP);
      int activeBorder = NvgUtil.withAlpha(accentColor, Math.round(180.0F + 75.0F * hoverP));
      int curBorder = NvgUtil.lerpColor(idleBorder, activeBorder, activeP);
      NvgUtil.strokeRounded(this.x + 0.5F, this.y + 0.5F, this.width - 1.0F, this.height - 1.0F, this.radius, 1.0F, curBorder);
      if (activeP > 0.01F) {
         float barH = 16.0F * activeP;
         float barY = this.y + (this.height - barH) * 0.5F;
         int barCol = NvgUtil.withAlpha(accentColor, activeP * 0.95F);
         NvgUtil.fillRounded(this.x + 3.5F, barY, 2.5F, barH, 1.25F, barCol);
      }

      float rightEdge = this.x + this.width - 12.0F;
      boolean hasSettings = !this.module.getSettings().isEmpty();
      if (hasSettings) {
         int chevronCol = NvgUtil.lerpColor(1090519039, -1862270977, hoverP);
         chevronCol = NvgUtil.lerpColor(chevronCol, accentColor, activeP * 0.5F);
         NvgUtil.drawChevronRight(rightEdge - 2.0F, this.y + this.height * 0.5F, 2.5F, chevronCol);
         rightEdge -= 14.0F;
      }

      if (this.module.getKey() > 0) {
         String keyName = UIKeybind.getKeyName(this.module.getKey());
         float keyW = NvgUtil.textWidth(keyName, 8.0F) + 8.0F;
         float keyH = 14.0F;
         float keyX = rightEdge - keyW;
         float keyY = this.y + (this.height - keyH) * 0.5F;
         NvgUtil.fillRounded(keyX, keyY, keyW, keyH, 3.0F, 419430399);
         NvgUtil.drawText(keyName, keyX + keyW * 0.5F, keyY + 3.0F, 8.0F, 1979711487, 10);
         rightEdge -= keyW + 6.0F;
      }

      float textX = this.x + 12.0F;
      float nameY = this.y + 9.5F;
      float descY = this.y + 25.0F;
      float maxTextW = Math.max(20.0F, rightEdge - textX);
      int idleNameCol = NvgUtil.lerpColor(-920071, -1, hoverP);
      int activeNameCol = NvgUtil.brighten(accentColor, 0.25F);
      int nameColor = NvgUtil.lerpColor(idleNameCol, activeNameCol, activeP);
      NvgUtil.drawText(this.module.getName(), textX, nameY, 11.0F, nameColor, 9);
      String desc = this.module.getDescription();
      if (desc != null && !desc.isEmpty()) {
         if (!desc.equals(this.lastDescInput) || Math.abs(maxTextW - this.lastDescW) > 1.0F) {
            this.lastDescInput = desc;
            this.lastDescW = maxTextW;
            float currentW = NvgUtil.textWidth(desc, 8.5F);
            if (!(currentW > maxTextW)) {
               this.cachedTruncatedDesc = desc;
            } else {
               String el = "...";
               float elW = NvgUtil.textWidth(el, 8.5F);
               StringBuilder sb = new StringBuilder();

               for (int i = 0; i < desc.length(); i++) {
                  sb.append(desc.charAt(i));
                  if (NvgUtil.textWidth(sb.toString(), 8.5F) + elW > maxTextW) {
                     if (sb.length() > 1) {
                        sb.setLength(sb.length() - 1);
                     }

                     sb.append(el);
                     break;
                  }
               }

               this.cachedTruncatedDesc = sb.toString();
            }
         }

         int descCol = NvgUtil.lerpColor(-7434849, -6250827, hoverP);
         NvgUtil.drawText(this.cachedTruncatedDesc, textX, descY, 8.5F, descCol, 9);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         float mx = (float)event.x();
         float my = (float)event.y();
         int btn = event.buttonInfo().button();
         if (this.contains(mx, my)) {
            if (btn == 0) {
               if ("ClickGUI".equalsIgnoreCase(this.module.getName())) {
                  if (this.openSettingsListener != null) {
                     this.openSettingsListener.accept(this.module);
                  }

                  return true;
               }

               this.module.toggle();
               return true;
            }

            if (btn == 1) {
               if (this.openSettingsListener != null) {
                  this.openSettingsListener.accept(this.module);
               }

               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
