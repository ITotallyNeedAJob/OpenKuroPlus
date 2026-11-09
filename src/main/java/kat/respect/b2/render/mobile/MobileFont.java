package kat.respect.b2.render.mobile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import kat.respect.b2.KuroPlusMod;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

public class MobileFont extends MobileInstancedShader {
   private final MobileFont.Glyph[] asciiGlyphs = new MobileFont.Glyph[256];
   private final Map<Integer, MobileFont.Glyph> extendedGlyphs = new HashMap<>();
   private final Set<Integer> loggedMissingGlyphs = ConcurrentHashMap.newKeySet();
   private float emSize = 1.0F;
   private float lineHeight = 1.21F;
   private float ascender = 0.96875F;
   private float descender = -0.24121F;
   private float atlasWidth = 828.0F;
   private float atlasHeight = 828.0F;
   private float distanceRange = 8.0F;
   private int fontTexture = 0;
   private boolean metadataLoaded = false;

   public MobileFont() {
      super("gl/2d/font/font.frag", "gl/2d/font/font.vert", 116);
      new MobileAttributeBuilder().stride(116).mat4Attrib(0L).floatAttrib(4, 64L).floatAttrib(4, 80L).intAttrib(4, 96L).floatAttrib(1, 112L);
      this.ensureLoaded();
   }

   public static InputStream getResource(String path) {
      String cleanPath = path.startsWith("/") ? path.substring(1) : path;
      String slashPath = "/" + cleanPath;
      InputStream is = MobileFont.class.getClassLoader().getResourceAsStream(cleanPath);
      if (is != null) {
         return is;
      } else {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (cl != null) {
            is = cl.getResourceAsStream(cleanPath);
            if (is != null) {
               return is;
            }
         }

         is = MobileFont.class.getResourceAsStream(slashPath);
         if (is != null) {
            return is;
         } else {
            try {
               is = KuroPlusMod.class.getClassLoader().getResourceAsStream(cleanPath);
               return is != null ? is : KuroPlusMod.class.getResourceAsStream(slashPath);
            } catch (Throwable var6) {
               return null;
            }
         }
      }
   }

   public synchronized void ensureLoaded() {
      if (!this.metadataLoaded) {
         this.loadMetadata();
      }

      if (this.fontTexture == 0) {
         this.loadTexture();
      }
   }

   private void loadMetadata() {
      String[] candidatePaths = new String[]{"assets/kuroplus/mobile/fonts/inter.json"};
      InputStream is = null;

      for (String path : candidatePaths) {
         is = getResource(path);
         if (is != null) {
            break;
         }
      }

      if (is == null) {
         System.err.println("[kuroplus-mobile] Font json not found in candidates");
      } else {
         try {
            InputStream stream = is;

            try {
               JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
               if (root.has("atlas")) {
                  JsonObject atlas = root.getAsJsonObject("atlas");
                  this.atlasWidth = atlas.get("width").getAsFloat();
                  this.atlasHeight = atlas.get("height").getAsFloat();
                  if (atlas.has("distanceRange")) {
                     this.distanceRange = atlas.get("distanceRange").getAsFloat();
                  }
               }

               if (root.has("metrics")) {
                  JsonObject metrics = root.getAsJsonObject("metrics");
                  if (metrics.has("emSize")) {
                     this.emSize = metrics.get("emSize").getAsFloat();
                  }

                  if (metrics.has("lineHeight")) {
                     this.lineHeight = metrics.get("lineHeight").getAsFloat();
                  }

                  if (metrics.has("ascender")) {
                     this.ascender = metrics.get("ascender").getAsFloat();
                  }

                  if (metrics.has("descender")) {
                     this.descender = metrics.get("descender").getAsFloat();
                  }
               }

               int loadedCount = 0;
               if (root.has("glyphs")) {
                  for (JsonElement el : root.getAsJsonArray("glyphs")) {
                     JsonObject gObj = el.getAsJsonObject();
                     MobileFont.Glyph g = new MobileFont.Glyph();
                     g.unicode = gObj.get("unicode").getAsInt();
                     g.advance = gObj.get("advance").getAsFloat();
                     if (gObj.has("planeBounds") && gObj.has("atlasBounds")) {
                        JsonObject pb = gObj.getAsJsonObject("planeBounds");
                        g.planeLeft = pb.get("left").getAsFloat();
                        g.planeBottom = pb.get("bottom").getAsFloat();
                        g.planeRight = pb.get("right").getAsFloat();
                        g.planeTop = pb.get("top").getAsFloat();
                        JsonObject ab = gObj.getAsJsonObject("atlasBounds");
                        g.atlasLeft = ab.get("left").getAsFloat();
                        g.atlasBottom = ab.get("bottom").getAsFloat();
                        g.atlasRight = ab.get("right").getAsFloat();
                        g.atlasTop = ab.get("top").getAsFloat();
                        g.hasBounds = true;
                     } else {
                        g.hasBounds = false;
                     }

                     if (g.unicode >= 0 && g.unicode < 256) {
                        this.asciiGlyphs[g.unicode] = g;
                     } else {
                        this.extendedGlyphs.put(g.unicode, g);
                     }

                     loadedCount++;
                  }
               }

               this.metadataLoaded = true;
               System.out.println("[Kuro+][mobile] Font metadata loaded: " + loadedCount + " glyphs");
            } catch (Throwable var14) {
               if (is != null) {
                  try {
                     stream.close();
                  } catch (Throwable var13) {
                     var14.addSuppressed(var13);
                  }
               }

               throw var14;
            }

            if (is != null) {
               is.close();
            }
         } catch (Exception var15) {
            System.err.println("[Kuro+][mobile] Failed to load font metadata: " + var15.getMessage());
         }
      }
   }

   private void loadTexture() {
      String[] candidatePaths = new String[]{"assets/kuroplus/mobile/fonts/inter.png"};
      InputStream is = null;

      for (String path : candidatePaths) {
         is = getResource(path);
         if (is != null) {
            break;
         }
      }

      if (is == null) {
         System.err.println("[kuroplus-mobile] Font png not found in candidates");
      } else {
         try {
            InputStream stream = is;

            label164: {
               try {
                  BufferedImage img = ImageIO.read(stream);
                  if (img == null) {
                     System.err.println("[kuroplus-mobile] Failed to decode font png");
                     break label164;
                  }

                  int w = img.getWidth();
                  int h = img.getHeight();
                  int[] pixels = new int[w * h];
                  img.getRGB(0, 0, w, h, pixels, 0, w);
                  ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);

                  for (int p : pixels) {
                     buf.put((byte)(p >> 16 & 0xFF));
                     buf.put((byte)(p >> 8 & 0xFF));
                     buf.put((byte)(p & 0xFF));
                     buf.put((byte)-1);
                  }

                  buf.flip();

                  try (MobileGlState state = new MobileGlState()) {
                     int prevPbo = GL33.glGetInteger(35055);
                     int prevRowLength = GL33.glGetInteger(3314);
                     int prevSkipPixels = GL33.glGetInteger(3316);
                     int prevSkipRows = GL33.glGetInteger(3315);
                     int prevSkipImages = GL33.glGetInteger(32877);
                     int prevImageHeight = GL33.glGetInteger(32878);
                     int prevAlignment = GL33.glGetInteger(3317);

                     try {
                        GL15.glBindBuffer(35052, 0);
                        GL33.glPixelStorei(3314, 0);
                        GL33.glPixelStorei(3316, 0);
                        GL33.glPixelStorei(3315, 0);
                        GL33.glPixelStorei(32877, 0);
                        GL33.glPixelStorei(32878, 0);
                        GL33.glPixelStorei(3317, 1);
                        this.fontTexture = GL33.glGenTextures();
                        if (this.fontTexture == 0) {
                           System.err.println("[Kuro+][mobile] glGenTextures returned 0 (OpenGL context not current?)");
                        } else {
                           GL33.glBindTexture(3553, this.fontTexture);
                           GL33.glTexParameteri(3553, 10241, 9729);
                           GL33.glTexParameteri(3553, 10240, 9729);
                           GL33.glTexParameteri(3553, 10242, 33071);
                           GL33.glTexParameteri(3553, 10243, 33071);
                           GL33.glTexParameteri(3553, 33085, 0);
                           GL33.glTexImage2D(3553, 0, 32856, w, h, 0, 6408, 5121, buf);
                        }
                     } finally {
                        GL15.glBindBuffer(35052, prevPbo);
                        GL33.glPixelStorei(3314, prevRowLength);
                        GL33.glPixelStorei(3316, prevSkipPixels);
                        GL33.glPixelStorei(3315, prevSkipRows);
                        GL33.glPixelStorei(32877, prevSkipImages);
                        GL33.glPixelStorei(32878, prevImageHeight);
                        GL33.glPixelStorei(3317, prevAlignment);
                     }
                  }

                  System.out.println("[Kuro+][mobile] Font texture loaded: id=" + this.fontTexture + ", " + w + "x" + h);
               } catch (Throwable var28) {
                  if (is != null) {
                     try {
                        stream.close();
                     } catch (Throwable var24) {
                        var28.addSuppressed(var24);
                     }
                  }

                  throw var28;
               }

               if (is != null) {
                  is.close();
               }

               return;
            }

            if (is != null) {
               is.close();
            }
         } catch (Exception var29) {
            System.err.println("[Kuro+][mobile] Failed to load font texture: " + var29.getMessage());
         }
      }
   }

   public MobileFont.Glyph getGlyph(int unicode) {
      if (unicode >= 0 && unicode < 256) {
         MobileFont.Glyph g = this.asciiGlyphs[unicode];
         if (g != null) {
            return g;
         }
      }

      MobileFont.Glyph g = this.extendedGlyphs.get(unicode);
      if (g != null) {
         return g;
      } else {
         return switch (unicode) {
            case 8211, 8212, 8213 -> this.getGlyph(45);
            case 8216, 8217, 8218, 8242 -> this.getGlyph(39);
            case 8220, 8221, 8222, 8243 -> this.getGlyph(34);
            case 8226, 8227, 8729 -> this.getGlyph(46);
            case 8230 -> this.getGlyph(46);
            case 8592 -> this.getGlyph(60);
            case 8594 -> this.getGlyph(62);
            default -> null;
         };
      }
   }

   public float getStringWidth(String text, float size) {
      if (text != null && !text.isEmpty()) {
         this.ensureLoaded();
         float w = 0.0F;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            MobileFont.Glyph g = this.getGlyph(c);
            if (g != null) {
               w += g.advance * size;
            } else {
               w += 0.5F * size;
            }
         }

         return w;
      } else {
         return 0.0F;
      }
   }

   public float getStringHeight(float size) {
      return this.lineHeight * size;
   }

   public void drawString(Matrix4f model, String text, float x, float y, float size, int color, int align, float z) {
      if (text != null && !text.isEmpty()) {
         this.ensureLoaded();
         float totalW = this.getStringWidth(text, size);
         float penX = x;
         if ((align & 2) != 0) {
            penX = x - totalW * 0.5F;
         } else if ((align & 4) != 0) {
            penX = x - totalW;
         }

         float baselineY;
         if ((align & 16) != 0) {
            baselineY = y + 0.32F * size;
         } else if ((align & 32) != 0) {
            baselineY = y - this.descender * size;
         } else {
            baselineY = y + this.ascender * size;
         }

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
               MobileFont.Glyph space = this.getGlyph(32);
               penX += (space != null ? space.advance : 0.274F) * size;
            } else {
               MobileFont.Glyph g = this.getGlyph(c);
               if (g == null) {
                  if (this.loggedMissingGlyphs.add(Integer.valueOf(c))) {
                     System.err.println("[kuroplus-mobile] Missing glyph for char '" + c + "' (code " + c + ")");
                  }

                  penX += 0.5F * size;
               } else {
                  if (g.hasBounds) {
                     this.checkFlush();
                     float qx = penX + g.planeLeft * size;
                     float qy = baselineY - g.planeTop * size;
                     float qw = (g.planeRight - g.planeLeft) * size;
                     float qh = (g.planeTop - g.planeBottom) * size;
                     float u = g.atlasLeft / this.atlasWidth;
                     float v = (this.atlasHeight - g.atlasTop) / this.atlasHeight;
                     float uw = (g.atlasRight - g.atlasLeft) / this.atlasWidth;
                     float vh = (g.atlasTop - g.atlasBottom) / this.atlasHeight;
                     int pos = this.buffer.position();
                     model.get(pos, this.buffer);
                     this.buffer.putFloat(pos + 64, qx);
                     this.buffer.putFloat(pos + 68, qy);
                     this.buffer.putFloat(pos + 72, qw);
                     this.buffer.putFloat(pos + 76, qh);
                     this.buffer.putFloat(pos + 80, u);
                     this.buffer.putFloat(pos + 84, v);
                     this.buffer.putFloat(pos + 88, uw);
                     this.buffer.putFloat(pos + 92, vh);
                     this.buffer.putInt(pos + 96, color);
                     this.buffer.putInt(pos + 100, color);
                     this.buffer.putInt(pos + 104, color);
                     this.buffer.putInt(pos + 108, color);
                     this.buffer.putFloat(pos + 112, z);
                     this.buffer.position(pos + 116);
                     this.count++;
                  }

                  penX += g.advance * size;
               }
            }
         }
      }
   }

   @Override
   public void flush() {
      if (this.count != 0) {
         this.ensureLoaded();
         if (this.fontTexture != 0 && this.program > 0) {
            GL33.glUseProgram(this.program);
            GL13.glActiveTexture(33984);
            GL33.glBindTexture(3553, this.fontTexture);
            GL33.glBindSampler(0, 0);
            MobileGLUniforms.uniform1i(this.program, "tex", 0);
            MobileGLUniforms.uniform1f(this.program, "pxRange", this.distanceRange);
            GL33.glBindVertexArray(this.vao);
            GL33.glDrawArraysInstanced(5, 0, 4, this.flushBuffer());
            GL33.glBindTexture(3553, 0);
            GL33.glBindSampler(0, 0);
            GL33.glBindVertexArray(0);
            GL33.glUseProgram(0);
         }
      }
   }

   @Override
   public void cleanup() {
      super.cleanup();
      if (this.fontTexture != 0) {
         GL33.glDeleteTextures(this.fontTexture);
         this.fontTexture = 0;
      }
   }

   public static class Glyph {
      public int unicode;
      public float advance;
      public float planeLeft;
      public float planeBottom;
      public float planeRight;
      public float planeTop;
      public float atlasLeft;
      public float atlasBottom;
      public float atlasRight;
      public float atlasTop;
      public boolean hasBounds;
   }
}
