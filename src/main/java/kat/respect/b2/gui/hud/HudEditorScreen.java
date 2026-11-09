package kat.respect.b2.gui.hud;

import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.EventHandler;
import kat.respect.b2.event.EventPriority;
import kat.respect.b2.event.events.Render2DEvent;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.util.ScreenUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudEditorScreen extends Screen {
   private static final int PANEL_W = 168;
   private static final int PANEL_HEADER = 22;
   private static final int ROW = 20;
   private static final int HANDLE = 9;
   private static final float SNAP = 6.0F;
   private static final float PANEL_TOGGLE_W = 26.0F;
   private static final float PANEL_TOGGLE_H = 13.0F;
   private static final float PANEL_INSET = 12.0F;
   private static final int PANEL_BG = -267514350;
   private static final int PANEL_BORDER = 822083583;
   private static final int TEXT = -1513234;
   private static final int MUTED = -7697770;
   private static final int ACCENT = -16094209;
   private final HudLayout layout = HudLayout.get();
   private String selected = "arraylist";
   private boolean dragging;
   private boolean draggingPanel;
   private double panelGrabX;
   private double panelGrabY;
   private float dragBaseOffsetX;
   private float dragBaseOffsetY;
   private float dragBaseCentreX;
   private float dragBaseCentreY;
   private float dragRawX;
   private float dragRawY;
   private double lastMouseX;
   private double lastMouseY;
   private float panelX = 16.0F;
   private float panelY = 16.0F;
   private float guideX = -1.0F;
   private float guideY = -1.0F;
   private boolean subscribed;

   public HudEditorScreen() {
      super(Component.literal("HUD Editor"));
   }

   public static boolean isOpen() {
      return ScreenUtil.getScreen() instanceof HudEditorScreen;
   }

   @Override
   protected void init() {
      super.init();
      if (!this.subscribed) {
         EventBus.INSTANCE.subscribe(this);
         this.subscribed = true;
      }

      this.layout.load();
   }

   @Override
   public void extractTransparentBackground(GuiGraphicsExtractor graphics) {
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onRender2D(Render2DEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getWindow() != null) {
         float w = mc.getWindow().getGuiScaledWidth();
         float h = mc.getWindow().getGuiScaledHeight();
         this.lastMouseX = mc.mouseHandler.xpos() * w / mc.getWindow().getScreenWidth();
         this.lastMouseY = mc.mouseHandler.ypos() * h / mc.getWindow().getScreenHeight();
         NvgUtil.fillRect(0.0F, 0.0F, w, h, 1073741824);
         NvgUtil.fillRect(w * 0.5F, 0.0F, 1.0F, h, 587202559);
         NvgUtil.fillRect(0.0F, h * 0.5F, w, 1.0F, 587202559);

         for (String id : HudLayout.IDS) {
            HudLayout.Entry entry = HudLayout.of(id);
            float[] bounds = HudLayout.bounds(id);
            if (bounds != null) {
               boolean active = id.equals(this.selected);
               boolean hover = !this.draggingPanel
                  && this.lastMouseX >= bounds[0]
                  && this.lastMouseX <= bounds[0] + bounds[2]
                  && this.lastMouseY >= bounds[1]
                  && this.lastMouseY <= bounds[1] + bounds[3];
               int color = active ? -16094209 : (hover ? -855638017 : (entry.enabled ? 1728053247 : 872415231));
               stroke(bounds[0] - 1.0F, bounds[1] - 1.0F, bounds[2] + 2.0F, bounds[3] + 2.0F, color);
               if ((active || hover) && HudLayout.supportsScale(id)) {
                  float hx = bounds[0] + bounds[2] - 4.5F;
                  float hy = bounds[1] + bounds[3] - 4.5F;
                  NvgUtil.fillRounded(hx, hy, 9.0F, 9.0F, 2.0F, color);
               }
            }
         }

         if (this.guideX >= 0.0F) {
            NvgUtil.fillRect(this.guideX, 0.0F, 1.0F, h, -1442157569);
         }

         if (this.guideY >= 0.0F) {
            NvgUtil.fillRect(0.0F, this.guideY, w, 1.0F, -1442157569);
         }

         this.drawPanel(w, h);
         if (this.dragging) {
            HudLayout.Entry entry = HudLayout.of(this.selected);
            String info = Math.round(entry.offsetX) + ", " + Math.round(entry.offsetY) + "   " + Math.round(entry.scale * 100.0F) + "%";
            float tw = NvgUtil.textWidth(info, 9.0F);
            float tx = Math.min(w - tw - 8.0F, (float)this.lastMouseX + 12.0F);
            float ty = Math.min(h - 16.0F, (float)this.lastMouseY + 12.0F);
            NvgUtil.fillRounded(tx - 5.0F, ty - 4.0F, tw + 10.0F, 16.0F, 4.0F, -267514350);
            NvgUtil.drawText(info, tx, ty, 9.0F, -1513234, 9);
         }

         String hint = "Drag to move   •   Corner or scroll to scale   •   R reset element   •   Esc save and close";
         NvgUtil.drawText(hint, w * 0.5F - NvgUtil.textWidth(hint, 9.0F) * 0.5F, h - 16.0F, 9.0F, -7697770, 9);
      }
   }

   private void beginDrag(String id) {
      HudLayout.Entry entry = HudLayout.of(id);
      this.dragBaseOffsetX = entry.offsetX;
      this.dragBaseOffsetY = entry.offsetY;
      this.dragRawX = 0.0F;
      this.dragRawY = 0.0F;
      float[] b = HudLayout.bounds(id);
      this.dragBaseCentreX = b != null ? b[0] + b[2] * 0.5F : 0.0F;
      this.dragBaseCentreY = b != null ? b[1] + b[3] * 0.5F : 0.0F;
      this.dragging = true;
   }

   private float panelHeight() {
      return 22 + HudLayout.IDS.length * 20 + 20 + 10.0F;
   }

   private float panelRowY(int index) {
      return this.panelY + 22.0F + index * 20;
   }

   private float panelToggleX() {
      return this.panelX + 168.0F - 12.0F - 26.0F;
   }

   private void drawPanel(float w, float h) {
      float ph = this.panelHeight();
      NvgUtil.fillRounded(this.panelX, this.panelY, 168.0F, ph, 8.0F, -267514350);
      NvgUtil.strokeRounded(this.panelX - 0.5F, this.panelY - 0.5F, 169.0F, ph + 1.0F, 8.0F, 1.0F, 822083583);
      NvgUtil.drawText("HUD ELEMENTS", this.panelX + 14.0F, this.panelY + 8.0F, 8.5F, -7697770, 9);
      float toggleX = this.panelToggleX();

      for (int i = 0; i < HudLayout.IDS.length; i++) {
         String id = HudLayout.IDS[i];
         HudLayout.Entry entry = HudLayout.of(id);
         float rowY = this.panelRowY(i);
         if (id.equals(this.selected)) {
            NvgUtil.fillRounded(this.panelX + 6.0F, rowY, 156.0F, 20.0F, 5.0F, 520093695);
         }

         boolean live = HudLayout.bounds(id) != null;
         String hint = entry.enabled && !live ? "idle" : "";
         float hintW = hint.isEmpty() ? 0.0F : NvgUtil.textWidth(hint, 8.0F) + 8.0F;
         float labelMax = toggleX - 8.0F - hintW - (this.panelX + 14.0F);
         int labelColor = !entry.enabled ? -7697770 : (live ? -1513234 : -6250324);
         float textY = rowY + 5.5F;
         NvgUtil.drawText(truncate(HudLayout.label(id), 9.0F, labelMax), this.panelX + 14.0F, textY, 9.0F, labelColor, 9);
         if (!hint.isEmpty()) {
            NvgUtil.drawText(hint, toggleX - 8.0F - NvgUtil.textWidth(hint, 8.0F), textY + 0.5F, 8.0F, -9539974, 9);
         }

         float toggleY = rowY + 3.5F;
         NvgUtil.fillRounded(toggleX, toggleY, 26.0F, 13.0F, 6.5F, entry.enabled ? -16094209 : -14013902);
         float knob = 10.0F;
         float knobX = entry.enabled ? toggleX + 26.0F - knob - 1.5F : toggleX + 1.5F;
         NvgUtil.fillCircle(knobX + knob * 0.5F, toggleY + 6.5F, knob * 0.5F, entry.enabled ? -1 : -10855834);
      }

      float resetY = this.panelRowY(HudLayout.IDS.length);
      NvgUtil.fillRect(this.panelX + 10.0F, resetY + 1.0F, 148.0F, 1.0F, 419430399);
      NvgUtil.drawText("Reset all", this.panelX + 14.0F, resetY + 5.75F, 8.5F, -7697770, 9);
   }

   private static String truncate(String value, float size, float maxWidth) {
      if (maxWidth <= 8.0F) {
         return value;
      } else if (NvgUtil.textWidth(value, size) <= maxWidth) {
         return value;
      } else {
         String out = value;
         int guard = 0;

         while (out.length() > 1 && NvgUtil.textWidth(out + "...", size) > maxWidth && guard++ < 12) {
            out = out.substring(0, out.length() - 1);
         }

         return out + "...";
      }
   }

   private static void stroke(float x, float y, float w, float h, int color) {
      NvgUtil.fillRect(x, y, w, 1.0F, color);
      NvgUtil.fillRect(x, y + h - 1.0F, w, 1.0F, color);
      NvgUtil.fillRect(x, y + 1.0F, 1.0F, h - 2.0F, color);
      NvgUtil.fillRect(x + w - 1.0F, y + 1.0F, 1.0F, h - 2.0F, color);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mx = event.x();
      double my = event.y();
      this.lastMouseX = mx;
      this.lastMouseY = my;
      if (mx >= this.panelX && mx <= this.panelX + 168.0F && my >= this.panelY && my <= this.panelY + this.panelHeight()) {
         if (my <= this.panelY + 22.0F) {
            this.draggingPanel = true;
            this.panelGrabX = mx - this.panelX;
            this.panelGrabY = my - this.panelY;
            return true;
         } else {
            float toggleX = this.panelToggleX();

            for (int i = 0; i < HudLayout.IDS.length; i++) {
               float rowY = this.panelRowY(i);
               if (my >= rowY && my < rowY + 20.0F) {
                  if (mx >= toggleX - 5.0F && mx <= toggleX + 26.0F + 5.0F) {
                     HudLayout.Entry entry = HudLayout.of(HudLayout.IDS[i]);
                     entry.enabled = !entry.enabled;
                     this.layout.save();
                  } else {
                     this.selected = HudLayout.IDS[i];
                  }

                  return true;
               }
            }

            if (my >= this.panelRowY(HudLayout.IDS.length)) {
               this.layout.resetAll();
               this.layout.save();
               return true;
            } else {
               return true;
            }
         }
      } else {
         for (String id : HudLayout.IDS) {
            float[] b = HudLayout.bounds(id);
            if (b != null) {
               if (HudLayout.supportsScale(id)) {
                  float hx = b[0] + b[2] - 4.5F;
                  float hy = b[1] + b[3] - 4.5F;
                  if (mx >= hx - 9.0F && mx <= hx + 9.0F && my >= hy - 9.0F && my <= hy + 9.0F) {
                     this.selected = id;
                     this.beginDrag(id);
                     return true;
                  }
               }

               if (mx >= b[0] && mx <= b[0] + b[2] && my >= b[1] && my <= b[1] + b[3]) {
                  this.selected = id;
                  this.beginDrag(id);
                  return true;
               }
            }
         }

         return super.mouseClicked(event, doubleClick);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      double mx = event.x();
      double my = event.y();
      if (this.draggingPanel) {
         Minecraft mc = Minecraft.getInstance();
         this.panelX = (float)Math.max(0.0, Math.min(mx - this.panelGrabX, (double)(mc.getWindow().getGuiScaledWidth() - 168)));
         this.panelY = (float)Math.max(0.0, Math.min(my - this.panelGrabY, (double)(mc.getWindow().getGuiScaledHeight() - 40)));
         return true;
      } else if (this.dragging) {
         Minecraft mc = Minecraft.getInstance();
         float w = mc.getWindow().getGuiScaledWidth();
         float h = mc.getWindow().getGuiScaledHeight();
         HudLayout.Entry entry = HudLayout.of(this.selected);
         this.dragRawX += (float)dragX;
         this.dragRawY += (float)dragY;
         float rawCentreX = this.dragBaseCentreX + this.dragRawX;
         float rawCentreY = this.dragBaseCentreY + this.dragRawY;
         entry.offsetX = this.dragBaseOffsetX + this.dragRawX;
         entry.offsetY = this.dragBaseOffsetY + this.dragRawY;
         this.guideX = -1.0F;
         this.guideY = -1.0F;
         if (Math.abs(rawCentreX - w * 0.5F) < 6.0F) {
            entry.offsetX += w * 0.5F - rawCentreX;
            this.guideX = w * 0.5F;
         }

         if (Math.abs(rawCentreY - h * 0.5F) < 6.0F) {
            entry.offsetY += h * 0.5F - rawCentreY;
            this.guideY = h * 0.5F;
         }

         this.lastMouseX = mx;
         this.lastMouseY = my;
         return true;
      } else {
         return super.mouseDragged(event, dragX, dragY);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (!this.dragging && !this.draggingPanel) {
         return super.mouseReleased(event);
      } else {
         this.dragging = false;
         this.draggingPanel = false;
         this.guideX = -1.0F;
         this.guideY = -1.0F;
         this.layout.save();
         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      float[] b = HudLayout.bounds(this.selected);
      if (HudLayout.supportsScale(this.selected) && b != null && mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
         HudLayout.Entry entry = HudLayout.of(this.selected);
         entry.scale = HudLayout.clampScale(entry.scale + (float)scrollY * 0.05F);
         this.layout.save();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (event.key() == 256) {
         this.onClose();
         return true;
      } else if (event.key() == 82) {
         this.layout.reset(this.selected);
         this.layout.save();
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      return super.charTyped(event);
   }

   @Override
   public void removed() {
      if (this.subscribed) {
         EventBus.INSTANCE.unsubscribe(this);
         this.subscribed = false;
      }

      this.layout.save();
      super.removed();
   }

   @Override
   public void onClose() {
      this.layout.save();
      ScreenUtil.setScreen(null);
   }
}
