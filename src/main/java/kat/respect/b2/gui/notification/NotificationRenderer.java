package kat.respect.b2.gui.notification;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import kat.respect.b2.bridge.NotificationSettings;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.EventHandler;
import kat.respect.b2.event.events.Render2DEvent;
import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.entity.UIIcon;
import kat.respect.b2.gui.hud.HudLayout;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.render.Renderer2D;
import kat.respect.b2.util.NotificationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public final class NotificationRenderer {
   public static final NotificationRenderer INSTANCE = new NotificationRenderer();
   private static final float MARGIN = 14.0F;
   private static final float GAP = 8.0F;
   private final Map<NotificationManager.Notification, NotificationRenderer.ToastAnim> anims = new IdentityHashMap<>();

   private NotificationRenderer() {
   }

   public static void init() {
      EventBus.INSTANCE.subscribe(INSTANCE);
   }

   private boolean isModuleEnabled() {
      Module mod = ModuleManager.INSTANCE.getModuleByName("Notifications");
      return mod == null || mod.isEnabled();
   }

   private boolean isMobileActive() {
      String eng = NotificationSettings.engine.getValue();
      if ("Mobile (Shaders)".equalsIgnoreCase(eng)) {
         return true;
      } else {
         return "Blaze3D (Vulkan)".equalsIgnoreCase(eng) ? false : Renderer2D.isMobileActive();
      }
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (this.isModuleEnabled()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.getWindow() != null) {
            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();
            this.render(screenW, screenH, event);
         }
      }
   }

   public void render(float screenW, float screenH, Render2DEvent event) {
      HudLayout.Entry hudLayout = HudLayout.of("notifications");
      if (!hudLayout.enabled) {
         HudLayout.clearBounds("notifications");
      } else {
         List<NotificationManager.Notification> list = NotificationManager.snapshot();
         if (list.isEmpty() && this.anims.isEmpty()) {
            HudLayout.clearBounds("notifications");
         } else {
            long now = System.currentTimeMillis();
            this.anims.keySet().removeIf(nx -> !list.contains(nx));
            int max = NotificationSettings.maxDisplay.getValueInt();
            int count = Math.min(list.size(), max);
            float dt = Math.max(0.001F, Math.min(0.1F, ClickGuiScreen.lastDt));
            String animMode = NotificationSettings.animation.getValue();
            String pos = NotificationSettings.position.getValue();
            float toastW = NotificationSettings.width.getValueFloat();
            boolean fromTop = pos.startsWith("Top");
            boolean fromLeft = pos.endsWith("Left");
            boolean isCenter = pos.endsWith("Center");
            float currentY = (fromTop ? 14.0F : screenH - 14.0F) + hudLayout.offsetY;
            float boundsMinX = Float.MAX_VALUE;
            float boundsMinY = Float.MAX_VALUE;
            float boundsMaxX = -Float.MAX_VALUE;
            float boundsMaxY = -Float.MAX_VALUE;
            boolean useShaders = this.isMobileActive() && !Renderer2D.isForceVulkan();

            for (int i = 0; i < count; i++) {
               NotificationManager.Notification n = list.get(i);
               NotificationRenderer.ToastAnim anim = this.anims.computeIfAbsent(n, k -> new NotificationRenderer.ToastAnim());
               long age = n.getAge(now);
               long remaining = n.durationMs() - age;
               float toastH = this.measureHeight(n, toastW);
               this.updateAnimation(anim, age, remaining, dt, animMode);
               if (!(anim.alpha < 0.01F)) {
                  float drawX = this.calculateX(screenW, toastW, pos, anim, animMode) + hudLayout.offsetX;
                  float drawY = this.calculateY(currentY, toastH, fromTop, isCenter, anim, animMode);
                  if (fromTop) {
                     currentY += toastH + 8.0F;
                  } else {
                     currentY -= toastH + 8.0F;
                  }

                  if (useShaders) {
                     this.renderSingleToastShaders(n, drawX, drawY, toastW, toastH, anim.alpha, anim.scale, age);
                  } else if (event != null && event.getGraphics() != null) {
                     int accent = this.accentFor(n.type());
                     NotificationBlaze3D.renderToast(
                        event.getGraphics(),
                        n,
                        drawX,
                        drawY,
                        toastW,
                        toastH,
                        anim.alpha,
                        age,
                        accent,
                        this.wrapLinesFont(n.message(), (int)(toastW - (NotificationSettings.showIcons.getValue() ? 38 : 16)))
                     );
                  }

                  boundsMinX = Math.min(boundsMinX, drawX);
                  boundsMinY = Math.min(boundsMinY, drawY);
                  boundsMaxX = Math.max(boundsMaxX, drawX + toastW);
                  boundsMaxY = Math.max(boundsMaxY, drawY + toastH);
               }
            }

            if (boundsMaxX > boundsMinX && boundsMaxY > boundsMinY) {
               HudLayout.reportBounds("notifications", boundsMinX, boundsMinY, boundsMaxX - boundsMinX, boundsMaxY - boundsMinY);
            } else {
               HudLayout.clearBounds("notifications");
            }
         }
      }
   }

   private void updateAnimation(NotificationRenderer.ToastAnim anim, long age, long remaining, float dt, String mode) {
      if ("Fade Only".equalsIgnoreCase(mode)) {
         anim.slide = 1.0F;
         anim.scale = 1.0F;
         float inAlpha = age < 250L ? (float)age / 250.0F : 1.0F;
         float outAlpha = remaining < 250L ? Math.max(0.0F, (float)remaining / 250.0F) : 1.0F;
         anim.alpha = Math.max(0.0F, Math.min(1.0F, Math.min(inAlpha, outAlpha)));
      } else if ("Pop Bounce".equalsIgnoreCase(mode)) {
         float inAlpha = age < 200L ? (float)age / 200.0F : 1.0F;
         float outAlpha = remaining < 250L ? Math.max(0.0F, (float)remaining / 250.0F) : 1.0F;
         anim.alpha = Math.max(0.0F, Math.min(1.0F, Math.min(inAlpha, outAlpha)));
         anim.slide = 1.0F;
         if (age < 350L) {
            float t = (float)age / 350.0F;
            float overshoot = (float)(1.0 + 0.15 * Math.sin(t * Math.PI) * (1.0 - t));
            anim.scale = Math.max(0.6F, Math.min(1.1F, t * overshoot));
         } else if (remaining < 250L) {
            float t = Math.max(0.0F, (float)remaining / 250.0F);
            anim.scale = 0.85F + 0.15F * t;
         } else {
            anim.scale = 1.0F;
         }
      } else {
         anim.scale = 1.0F;
         float targetSlide = remaining > 300L ? 1.0F : Math.max(0.0F, (float)remaining / 300.0F);
         anim.slide = anim.slide + (targetSlide - anim.slide) * (1.0F - (float)Math.exp(-18.0 * dt));
         anim.alpha = Math.max(0.0F, Math.min(1.0F, anim.slide));
      }
   }

   private float calculateX(float screenW, float toastW, String pos, NotificationRenderer.ToastAnim anim, String animMode) {
      float travel = (1.0F - anim.slide) * (toastW + 24.0F);
      if ("Fade Only".equalsIgnoreCase(animMode) || "Pop Bounce".equalsIgnoreCase(animMode)) {
         travel = 0.0F;
      }

      if (pos.endsWith("Center")) {
         return (screenW - toastW) / 2.0F;
      } else {
         return pos.endsWith("Left") ? 14.0F - travel : screenW - 14.0F - toastW + travel;
      }
   }

   private float calculateY(float stackY, float toastH, boolean fromTop, boolean isCenter, NotificationRenderer.ToastAnim anim, String animMode) {
      float baseY = fromTop ? stackY : stackY - toastH;
      if (isCenter && "Smooth Slide".equalsIgnoreCase(animMode)) {
         float travelY = (1.0F - anim.slide) * (toastH + 20.0F);
         return fromTop ? baseY - travelY : baseY + travelY;
      } else {
         return baseY;
      }
   }

   private float measureHeight(NotificationManager.Notification n, float toastW) {
      String mode = NotificationSettings.heightMode.getValue();
      if ("Standard (46px)".equalsIgnoreCase(mode)) {
         return 46.0F;
      } else if ("Compact (34px)".equalsIgnoreCase(mode)) {
         return 34.0F;
      } else {
         boolean hasIcon = NotificationSettings.showIcons.getValue();
         float textW = toastW - (hasIcon ? 42.0F : 16.0F);
         int lines = Math.max(1, this.wrapLines(n.message(), textW).size());
         return Math.max(38.0F, 20.0F + lines * 13.0F);
      }
   }

   private List<String> wrapLines(String text, float maxW) {
      List<String> lines = new ArrayList<>();
      if (text != null && !text.isEmpty()) {
         StringBuilder current = new StringBuilder();

         for (String word : text.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (NvgUtil.textWidth(candidate, 8.5F) > maxW && !current.isEmpty()) {
               lines.add(current.toString());
               current.setLength(0);
            }

            if (!current.isEmpty()) {
               current.append(' ');
            }

            current.append(word);
         }

         if (!current.isEmpty()) {
            lines.add(current.toString());
         }

         return lines;
      } else {
         return lines;
      }
   }

   private List<String> wrapLinesFont(String text, float maxW) {
      List<String> lines = new ArrayList<>();
      if (text != null && !text.isEmpty()) {
         Font font = Minecraft.getInstance().font;
         StringBuilder current = new StringBuilder();

         for (String word : text.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (font.width(candidate) > maxW && !current.isEmpty()) {
               lines.add(current.toString());
               current.setLength(0);
            }

            if (!current.isEmpty()) {
               current.append(' ');
            }

            current.append(word);
         }

         if (!current.isEmpty()) {
            lines.add(current.toString());
         }

         return lines;
      } else {
         return lines;
      }
   }

   private int accentFor(NotificationManager.Type type) {
      return switch (type) {
         case INFO -> NotificationSettings.infoColor.getValue();
         case SUCCESS -> NotificationSettings.successColor.getValue();
         case WARNING -> NotificationSettings.warningColor.getValue();
         case ERROR -> NotificationSettings.errorColor.getValue();
      };
   }

   private void renderSingleToastShaders(NotificationManager.Notification n, float x, float y, float w, float h, float alpha, float scale, long age) {
      int accent = this.accentFor(n.type());
      String style = NotificationSettings.style.getValue();
      boolean isMobile = this.isMobileActive();
      boolean rounded = NotificationSettings.rounded.getValue();
      float radius = rounded ? Math.min(NotificationSettings.radius.getValueFloat(), Math.min(w / 2.0F, h / 2.0F)) : 0.0F;
      if ("Compact Pill".equalsIgnoreCase(style)) {
         radius = h / 2.0F;
      }

      if (NotificationSettings.shadow.getValue()) {
         int shadowCol = NvgUtil.withAlpha(-16777216, Math.round(75.0F * alpha));
         NvgUtil.drawShadow(x, y, w, h, radius, shadowCol);
      }

      int bgCol = NotificationSettings.background.getValue();
      if ("Mobile Glass".equalsIgnoreCase(style)) {
         if (!isMobile && !NotificationSettings.blurGlass.getValue()) {
            NvgUtil.fillRounded(x, y, w, h, radius, NvgUtil.withAlpha(bgCol, Math.round(200.0F * alpha)));
         } else {
            NvgUtil.liquidGlass(x, y, w, h, radius, NvgUtil.withAlpha(bgCol, Math.round(70.0F * alpha)), 14.0F);
         }

         NvgUtil.strokeRounded(x - 0.5F, y - 0.5F, w + 1.0F, h + 1.0F, radius, 1.0F, NvgUtil.withAlpha(1174405119, Math.round(65.0F * alpha)));
         float barX = x + (radius > 0.0F ? Math.min(radius, 6.0F) : 3.0F);
         NvgUtil.fillRounded(barX, y + 6.0F, 3.0F, h - 12.0F, 1.5F, NvgUtil.withAlpha(accent, Math.round(255.0F * alpha)));
      } else if ("Modern Toast".equalsIgnoreCase(style)) {
         NvgUtil.fillRounded(x, y, w, h, radius, NvgUtil.withAlpha(bgCol, Math.round(245.0F * alpha)));
         NvgUtil.strokeRounded(x - 0.5F, y - 0.5F, w + 1.0F, h + 1.0F, radius, 1.0F, NvgUtil.withAlpha(687865855, Math.round(50.0F * alpha)));
         if (radius > 0.0F) {
            NvgUtil.fillRoundedVarying(x, y, 4.0F, h, radius, 0.0F, 0.0F, radius, NvgUtil.withAlpha(accent, Math.round(255.0F * alpha)));
         } else {
            NvgUtil.fillRect(x, y, 4.0F, h, NvgUtil.withAlpha(accent, Math.round(255.0F * alpha)));
         }
      } else if ("Compact Pill".equalsIgnoreCase(style)) {
         if (!isMobile && !NotificationSettings.blurGlass.getValue()) {
            NvgUtil.fillRounded(x, y, w, h, radius, NvgUtil.withAlpha(bgCol, Math.round(230.0F * alpha)));
         } else {
            NvgUtil.liquidGlass(x, y, w, h, radius, NvgUtil.withAlpha(bgCol, Math.round(110.0F * alpha)), 12.0F);
         }

         NvgUtil.strokeRounded(x - 0.5F, y - 0.5F, w + 1.0F, h + 1.0F, radius, 1.0F, NvgUtil.withAlpha(905969663, Math.round(60.0F * alpha)));
         float badgeR = (h - 10.0F) / 2.0F;
         float badgeCx = x + 5.0F + badgeR;
         float badgeCy = y + h / 2.0F;
         NvgUtil.fillCircle(badgeCx, badgeCy, badgeR, NvgUtil.withAlpha(accent, Math.round(45.0F * alpha)));
         NvgUtil.strokeCircle(badgeCx, badgeCy, badgeR, 1.0F, NvgUtil.withAlpha(accent, Math.round(180.0F * alpha)));
      } else {
         NvgUtil.fillRounded(x, y, w, h, radius, NvgUtil.withAlpha(1427181336, Math.round(160.0F * alpha)));
         NvgUtil.strokeRounded(x - 0.5F, y - 0.5F, w + 1.0F, h + 1.0F, radius, 1.0F, NvgUtil.withAlpha(553648127, Math.round(40.0F * alpha)));
         NvgUtil.fillRounded(x + 2.5F, y + 4.0F, 2.0F, h - 8.0F, 1.0F, NvgUtil.withAlpha(accent, Math.round(240.0F * alpha)));
      }

      boolean showIcons = NotificationSettings.showIcons.getValue();
      float textX = x + (showIcons ? 34.0F : 14.0F);
      if (showIcons) {
         float iconCx = "Compact Pill".equalsIgnoreCase(style) ? x + 5.0F + (h - 10.0F) / 2.0F : x + 18.0F;
         float iconCy = y + (h >= 44.0F ? 20.0F : h / 2.0F);
         int iconCol = NvgUtil.withAlpha(accent, Math.round(255.0F * alpha));
         switch (n.type()) {
            case INFO:
               UIIcon.drawInfo(iconCx, iconCy, 12.0F, iconCol);
               break;
            case SUCCESS:
               UIIcon.drawCheck(iconCx, iconCy, 12.0F, iconCol);
               break;
            case WARNING:
               UIIcon.drawWarning(iconCx, iconCy, 12.0F, iconCol);
               break;
            case ERROR:
               UIIcon.drawClose(iconCx, iconCy, 11.0F, iconCol);
         }
      }

      int titleCol = NvgUtil.withAlpha(NotificationSettings.textColor.getValue(), Math.round(255.0F * alpha));
      if (h <= 34.0F) {
         float titleY = y + (h - 10.0F) / 2.0F - 0.5F;
         NvgUtil.drawText(n.title() + " • " + n.message(), textX, titleY, 9.5F, titleCol, 9);
      } else {
         float titleY = y + 8.0F;
         NvgUtil.drawText(n.title(), textX, titleY, 10.5F, titleCol, 9);
         int subCol = NvgUtil.withAlpha(NotificationSettings.subtextColor.getValue(), Math.round(225.0F * alpha));
         float msgY = y + 23.0F;
         float maxTextW = w - (showIcons ? 40.0F : 20.0F);

         for (String line : this.wrapLines(n.message(), maxTextW)) {
            NvgUtil.drawText(line, textX, msgY, 8.5F, subCol, 9);
            msgY += 12.0F;
         }
      }

      if (NotificationSettings.progressBar.getValue()) {
         float progress = Math.max(0.0F, Math.min(1.0F, 1.0F - (float)age / (float)n.durationMs()));
         float barPad = "Compact Pill".equalsIgnoreCase(style) ? 14.0F : 4.0F;
         float barW = (w - barPad * 2.0F) * progress;
         if (barW > 0.0F) {
            float barY = y + h - 2.5F;
            int barCol = NvgUtil.withAlpha(accent, Math.round(200.0F * alpha));
            NvgUtil.fillRounded(x + barPad, barY, barW, 1.5F, 1.0F, barCol);
         }
      }
   }

   private static class ToastAnim {
      float slide = 0.0F;
      float alpha = 0.0F;
      float scale = 1.0F;
   }
}
