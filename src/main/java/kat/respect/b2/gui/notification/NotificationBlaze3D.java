package kat.respect.b2.gui.notification;

import java.util.List;
import kat.respect.b2.bridge.NotificationSettings;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.RenderUtil;
import kat.respect.b2.util.NotificationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NotificationBlaze3D {
   private NotificationBlaze3D() {
   }

   public static void renderToast(
      GuiGraphicsExtractor graphics,
      NotificationManager.Notification n,
      float x,
      float y,
      float w,
      float h,
      float alpha,
      long age,
      int accent,
      List<String> lines
   ) {
      Font font = Minecraft.getInstance().font;
      int bgCol = NotificationSettings.background.getValue();
      boolean rounded = NotificationSettings.rounded.getValue();
      int radius = rounded ? Math.min(NotificationSettings.radius.getValueInt(), (int)Math.min(w / 2.0F, h / 2.0F)) : 0;
      String style = NotificationSettings.style.getValue();
      if ("Compact Pill".equalsIgnoreCase(style)) {
         radius = (int)(h / 2.0F);
      }

      int ix = (int)x;
      int iy = (int)y;
      int iw = (int)w;
      int ih = (int)h;
      if (NotificationSettings.shadow.getValue()) {
         int shadowCol = NvgUtil.withAlpha(-16777216, Math.round(75.0F * alpha));
         RenderUtil.drawShadow(graphics, ix, iy, iw, ih, Math.max(1, radius), shadowCol);
      }

      int cardBg = NvgUtil.withAlpha(bgCol, Math.round(240.0F * alpha));
      RenderUtil.fillRounded(graphics, ix, iy, iw, ih, radius, cardBg);
      int stripCol = NvgUtil.withAlpha(accent, Math.round(255.0F * alpha));
      RenderUtil.fillRounded(graphics, ix, iy, 4, ih, Math.min(2, radius), stripCol);
      boolean showIcons = NotificationSettings.showIcons.getValue();
      int textX = ix + (showIcons ? 32 : 12);
      if (showIcons) {
         String symbol = switch (n.type()) {
            case SUCCESS -> "✓";
            case ERROR -> "✕";
            case WARNING -> "⚠";
            case INFO -> "ℹ";
         };
         graphics.text(font, symbol, ix + 12, iy + (ih - 8) / 2, stripCol, false);
      }

      int titleCol = NvgUtil.withAlpha(NotificationSettings.textColor.getValue(), Math.round(255.0F * alpha));
      if (ih <= 34) {
         graphics.text(font, n.title() + " • " + n.message(), textX, iy + (ih - 8) / 2, titleCol, false);
      } else {
         graphics.text(font, n.title(), textX, iy + 8, titleCol, false);
         int subCol = NvgUtil.withAlpha(NotificationSettings.subtextColor.getValue(), Math.round(225.0F * alpha));
         int msgY = iy + 22;

         for (String line : lines) {
            graphics.text(font, line, textX, msgY, subCol, false);
            msgY += 11;
         }
      }

      if (NotificationSettings.progressBar.getValue()) {
         float progress = Math.max(0.0F, Math.min(1.0F, 1.0F - (float)age / (float)n.durationMs()));
         int barW = (int)((iw - 8) * progress);
         if (barW > 0) {
            int barCol = NvgUtil.withAlpha(accent, Math.round(200.0F * alpha));
            RenderUtil.fillRounded(graphics, ix + 4, iy + ih - 3, barW, 2, 1, barCol);
         }
      }
   }
}
