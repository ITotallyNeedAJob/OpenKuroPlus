package kat.respect.b2.render;

public interface IRenderer2D {
   void begin(float var1, float var2, float var3, float var4);

   void end();

   void save();

   void restore();

   void pushScissor(float var1, float var2, float var3, float var4);

   void popScissor();

   void fillRect(float var1, float var2, float var3, float var4, int var5);

   void fillRounded(float var1, float var2, float var3, float var4, float var5, int var6);

   void fillRoundedTop(float var1, float var2, float var3, float var4, float var5, int var6);

   void fillRoundedVarying(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9);

   void strokeRect(float var1, float var2, float var3, float var4, float var5, int var6);

   void strokeRounded(float var1, float var2, float var3, float var4, float var5, float var6, int var7);

   void fillCircle(float var1, float var2, float var3, int var4);

   void fillGradient(float var1, float var2, float var3, float var4, float var5, int var6, int var7, boolean var8);

   void drawShadow(float var1, float var2, float var3, float var4, float var5, int var6);

   void drawText(String var1, float var2, float var3, float var4, int var5, int var6);

   void drawTextShadow(String var1, float var2, float var3, float var4, int var5, int var6, int var7);

   float textWidth(String var1, float var2);

   void drawChevronDown(float var1, float var2, float var3, int var4);

   void drawChevronRight(float var1, float var2, float var3, int var4);

   void drawLine(float var1, float var2, float var3, float var4, float var5, int var6);

   default void liquidGlass(float x, float y, float w, float h, float radius, int tint, float refractionStrength) {
      this.fillRounded(x, y, w, h, radius, tint);
   }

   int loadTexture(String var1);

   void drawImage(int var1, float var2, float var3, float var4, float var5, float var6);

   default void drawImage(int imgId, float x, float y, float w, float h, float u, float v, float uw, float vh, float radius, float alpha) {
      this.drawImage(imgId, x, y, w, h, alpha);
   }
}
