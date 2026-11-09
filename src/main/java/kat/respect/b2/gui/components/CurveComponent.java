package kat.respect.b2.gui.components;

import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.CurveSetting;
import net.minecraft.client.input.MouseButtonEvent;

public class CurveComponent extends SettingComponent {
   private static final float PAD = 10.0F;
   private static final float LABEL_H = 14.0F;
   private static final int STEPS = 40;
   private static final int GRAPH_MAX = 150;
   private int dragging = -1;

   public CurveComponent(CurveSetting setting, int x, int y, int width, int height) {
      super(setting, x, y, width, height);
   }

   public CurveSetting getSetting() {
      return (CurveSetting)super.getSetting();
   }

   private float graphSize() {
      return Math.max(80.0F, Math.min(150.0F, this.width - 4.0F));
   }

   @Override
   public int getHeight() {
      return (int)(20.0F + this.graphSize() + 6.0F);
   }

   private float graphX() {
      return this.x + (this.width - this.graphSize()) * 0.5F;
   }

   private float graphY() {
      return this.y + 14.0F + 6.0F;
   }

   private float inner() {
      return this.graphSize() - 20.0F;
   }

   private float handleX(double cx) {
      return this.graphX() + 10.0F + (float)cx * this.inner();
   }

   private float handleY(double cy) {
      return this.graphY() + 10.0F + this.inner() - (float)cy * this.inner();
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      CurveSetting setting = this.getSetting();
      float size = this.graphSize();
      float gx = this.graphX();
      float gy = this.graphY();
      float in = this.inner();
      NvgUtil.drawText(setting.getName(), this.x + 2.0F, this.y + 2.0F, 9.0F, fontColor, 9);
      String reset = "Reset";
      float resetW = NvgUtil.textWidth(reset, 8.5F);
      boolean resetHover = this.overReset(mouseX, mouseY);
      NvgUtil.drawText(reset, this.x + this.width - resetW - 2.0F, this.y + 2.0F, 8.5F, resetHover ? accentColor : -7697770, 9);
      NvgUtil.fillRounded(gx, gy, size, size, 7.0F, -1726737382);
      NvgUtil.strokeRounded(gx, gy, size, size, 7.0F, 1.0F, 587202559);

      for (int i = 1; i < 4; i++) {
         float t = 10.0F + in * i / 4.0F;
         NvgUtil.drawLine(gx + t, gy + 10.0F, gx + t, gy + 10.0F + in, 1.0F, 251658239);
         NvgUtil.drawLine(gx + 10.0F, gy + t, gx + 10.0F + in, gy + t, 1.0F, 251658239);
      }

      float p1x = this.handleX(setting.controlX1());
      float p1y = this.handleY(setting.controlY1());
      float p2x = this.handleX(setting.controlX2());
      float p2y = this.handleY(setting.controlY2());
      float ox = gx + 10.0F;
      float oy = gy + 10.0F + in;
      float ex = gx + 10.0F + in;
      float ey = gy + 10.0F;
      NvgUtil.drawLine(ox, oy, p1x, p1y, 1.0F, 1023410175);
      NvgUtil.drawLine(ex, ey, p2x, p2y, 1.0F, 1023410175);
      float prevX = ox;
      float prevY = oy;

      for (int i = 1; i <= 40; i++) {
         double t = i / 40.0;
         double u = 1.0 - t;
         double bx = 3.0 * u * u * t * setting.controlX1() + 3.0 * u * t * t * setting.controlX2() + t * t * t;
         double by = 3.0 * u * u * t * setting.controlY1() + 3.0 * u * t * t * setting.controlY2() + t * t * t;
         float px = gx + 10.0F + (float)bx * in;
         float py = gy + 10.0F + in - (float)by * in;
         NvgUtil.drawLine(prevX, prevY, px, py, 2.2F, accentColor);
         prevX = px;
         prevY = py;
      }

      NvgUtil.fillCircle(p1x, p1y, 5.5F, -1);
      NvgUtil.fillCircle(p1x, p1y, 3.0F, accentColor);
      NvgUtil.fillCircle(p2x, p2y, 5.5F, -1);
      NvgUtil.fillCircle(p2x, p2y, 3.0F, accentColor);
      NvgUtil.drawText("distance", gx + 6.0F, gy + size - 12.0F, 8.0F, -9539974, 9);
      float speedW = NvgUtil.textWidth("speed", 8.0F);
      NvgUtil.drawText("speed", gx + size - speedW - 6.0F, gy + 4.0F, 8.0F, -9539974, 9);
   }

   private boolean overReset(float mouseX, float mouseY) {
      float resetW = NvgUtil.textWidth("Reset", 8.5F);
      return mouseX >= this.x + this.width - resetW - 8.0F && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + 14.0F;
   }

   private boolean overGraph(float mouseX, float mouseY) {
      float size = this.graphSize();
      return mouseX >= this.graphX() && mouseX <= this.graphX() + size && mouseY >= this.graphY() && mouseY <= this.graphY() + size;
   }

   private void applyDrag(float mouseX, float mouseY) {
      if (this.dragging >= 0) {
         CurveSetting setting = this.getSetting();
         double nx = (mouseX - this.graphX() - 10.0F) / this.inner();
         double ny = 1.0 - (mouseY - this.graphY() - 10.0F) / this.inner();
         nx = Math.max(0.0, Math.min(1.0, nx));
         ny = Math.max(-0.25, Math.min(1.5, ny));
         if (this.dragging == 0) {
            setting.setCurve(nx, ny, setting.controlX2(), setting.controlY2());
         } else {
            setting.setCurve(setting.controlX1(), setting.controlY1(), nx, ny);
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      float mx = (float)event.x();
      float my = (float)event.y();
      if (this.overReset(mx, my)) {
         this.getSetting().reset();
         this.dragging = -1;
         return true;
      } else if (!this.overGraph(mx, my)) {
         this.dragging = -1;
         return false;
      } else {
         CurveSetting setting = this.getSetting();
         float dx1 = mx - this.handleX(setting.controlX1());
         float dy1 = my - this.handleY(setting.controlY1());
         float dx2 = mx - this.handleX(setting.controlX2());
         float dy2 = my - this.handleY(setting.controlY2());
         this.dragging = dx1 * dx1 + dy1 * dy1 <= dx2 * dx2 + dy2 * dy2 ? 0 : 1;
         this.applyDrag(mx, my);
         return true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (this.dragging < 0) {
         return false;
      } else {
         this.applyDrag((float)event.x(), (float)event.y());
         return true;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.dragging = -1;
      return false;
   }
}
