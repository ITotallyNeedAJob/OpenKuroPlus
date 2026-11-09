package kat.respect.b2.gui.entity;

import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import net.minecraft.client.input.MouseButtonEvent;

public class UIScrollContainer extends UIEntity {
   private float scrollY = 0.0F;
   private float targetScrollY = 0.0F;
   private float scrollSpeed = 26.0F;
   private float lastScrollActivity = 0.0F;

   public UIScrollContainer() {
      this.clipToBounds = true;
   }

   public void resetScroll() {
      this.scrollY = 0.0F;
      this.targetScrollY = 0.0F;
   }

   public float getScrollY() {
      return this.scrollY;
   }

   @Override
   public void layout() {
      if (this.visible) {
         float contentH = this.computeInnerHeight();
         float maxScroll = Math.min(0.0F, this.height - contentH);
         if (this.targetScrollY < maxScroll) {
            this.targetScrollY = maxScroll;
         }

         if (this.targetScrollY > 0.0F) {
            this.targetScrollY = 0.0F;
         }

         float dt = ClickGuiScreen.lastDt;
         float factor = 1.0F - (float)Math.exp(-18.0 * dt);
         this.scrollY = this.scrollY + (this.targetScrollY - this.scrollY) * factor;
         if (Math.abs(this.targetScrollY - this.scrollY) < 0.2F) {
            this.scrollY = this.targetScrollY;
         }

         float availW = Math.max(0.0F, this.width - this.padLeft - this.padRight - 6.0F);
         float curX = this.x + this.padLeft;
         float curY = this.y + this.padTop + this.scrollY;
         switch (this.layoutMode) {
            case COLUMN:
               for (UIEntity childxx : this.children) {
                  if (childxx.visible) {
                     curY += childxx.marginTop;
                     childxx.x = curX + childxx.marginLeft;
                     childxx.y = curY;
                     if (childxx.fillParentWidth) {
                        childxx.width = Math.max(0.0F, availW - childxx.marginLeft - childxx.marginRight);
                     }

                     childxx.layout();
                     curY += childxx.height + childxx.marginBottom + this.gapY;
                  }
               }
               break;
            case GRID:
               int cols = Math.max(1, this.gridColumns);
               float colW = Math.max(0.0F, (availW - (cols - 1) * this.gapX) / cols);
               int colIdx = 0;
               float rowMaxH = 0.0F;
               float rowStartX = this.x + this.padLeft;

               for (UIEntity childx : this.children) {
                  if (childx.visible) {
                     childx.width = colW;
                     childx.x = rowStartX + colIdx * (colW + this.gapX);
                     childx.y = curY;
                     childx.layout();
                     rowMaxH = Math.max(rowMaxH, childx.height);
                     if (++colIdx >= cols) {
                        colIdx = 0;
                        curY += rowMaxH + this.gapY;
                        rowMaxH = 0.0F;
                     }
                  }
               }
               break;
            case NONE:
            case ROW:
               for (UIEntity child : this.children) {
                  if (child.visible) {
                     child.y = child.y + this.scrollY;
                     child.layout();
                  }
               }
         }
      }
   }

   private float computeInnerHeight() {
      if (this.children.isEmpty()) {
         return 0.0F;
      } else {
         switch (this.layoutMode) {
            case COLUMN:
               float totalH = this.padTop;

               for (UIEntity childx : this.children) {
                  if (childx.visible) {
                     totalH += childx.marginTop + childx.height + childx.marginBottom + this.gapY;
                  }
               }

               if (!this.children.isEmpty()) {
                  totalH -= this.gapY;
               }

               return totalH + this.padBottom;
            case GRID:
               int cols = Math.max(1, this.gridColumns);
               float gridH = this.padTop;
               float rowMaxH = 0.0F;
               int colIdx = 0;

               for (UIEntity child : this.children) {
                  if (child.visible) {
                     rowMaxH = Math.max(rowMaxH, child.height);
                     if (++colIdx >= cols) {
                        gridH += rowMaxH + this.gapY;
                        rowMaxH = 0.0F;
                        colIdx = 0;
                     }
                  }
               }

               if (colIdx > 0) {
                  gridH += rowMaxH + this.gapY;
               }

               if (gridH > this.padTop) {
                  gridH -= this.gapY;
               }

               return gridH + this.padBottom;
            default:
               float maxBottom = 0.0F;

               for (UIEntity childxx : this.children) {
                  if (childxx.visible) {
                     float b = childxx.y - (this.y + this.scrollY) + childxx.height + childxx.marginBottom;
                     if (b > maxBottom) {
                        maxBottom = b;
                     }
                  }
               }

               return maxBottom + this.padBottom;
         }
      }
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      float contentH = this.computeInnerHeight();
      if (!(contentH <= this.height)) {
         long now = System.currentTimeMillis();
         boolean active = now - (long)this.lastScrollActivity < 1500L || this.hovered;
         float scrollAlpha = this.anim("scrollbarAlpha", active ? 1.0F : 0.0F, 8.0F);
         if (!(scrollAlpha < 0.01F)) {
            float trackX = this.x + this.width - 5.0F;
            float trackY = this.y + 4.0F;
            float trackH = this.height - 8.0F;
            float trackW = 3.0F;
            float ratio = Math.min(1.0F, this.height / contentH);
            float thumbH = Math.max(20.0F, trackH * ratio);
            float maxScroll = this.height - contentH;
            float scrollPct = maxScroll != 0.0F ? this.scrollY / maxScroll : 0.0F;
            float thumbY = trackY + (trackH - thumbH) * scrollPct;
            int trackCol = NvgUtil.withAlpha(285212671, scrollAlpha);
            NvgUtil.fillRounded(trackX, trackY, trackW, trackH, 1.5F, trackCol);
            GuiTheme theme = GuiTheme.current();
            int thumbColor = theme.accentColor | 0xFF000000;
            int thumbCol = NvgUtil.withAlpha(thumbColor, scrollAlpha * 0.75F);
            NvgUtil.fillRounded(trackX, thumbY, trackW, thumbH, 1.5F, thumbCol);
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.visible && this.enabled) {
         if (this.contains((float)mouseX, (float)mouseY)) {
            float contentH = this.computeInnerHeight();
            if (contentH > this.height) {
               this.targetScrollY = this.targetScrollY + (float)amount * this.scrollSpeed;
               float maxScroll = Math.min(0.0F, this.height - contentH);
               if (this.targetScrollY < maxScroll) {
                  this.targetScrollY = maxScroll;
               }

               if (this.targetScrollY > 0.0F) {
                  this.targetScrollY = 0.0F;
               }

               this.lastScrollActivity = (float)System.currentTimeMillis();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         return !this.contains((float)event.x(), (float)event.y()) ? false : super.mouseClicked(event);
      } else {
         return false;
      }
   }
}
