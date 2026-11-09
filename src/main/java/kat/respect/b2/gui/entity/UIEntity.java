package kat.respect.b2.gui.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.NvgUtil;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class UIEntity {
   protected String id = "";
   protected UIEntity parent;
   protected final List<UIEntity> children = new ArrayList<>();
   protected float x = 0.0F;
   protected float y = 0.0F;
   protected float width = 0.0F;
   protected float height = 0.0F;
   protected float padTop = 0.0F;
   protected float padRight = 0.0F;
   protected float padBottom = 0.0F;
   protected float padLeft = 0.0F;
   protected float marginTop = 0.0F;
   protected float marginRight = 0.0F;
   protected float marginBottom = 0.0F;
   protected float marginLeft = 0.0F;
   protected boolean fillParentWidth = false;
   protected boolean fillParentHeight = false;
   protected UIEntity.LayoutMode layoutMode = UIEntity.LayoutMode.NONE;
   protected float gapX = 0.0F;
   protected float gapY = 0.0F;
   protected int gridColumns = 2;
   protected int bg = 0;
   protected int border = 0;
   protected float borderWidth = 0.0F;
   protected float radius = 0.0F;
   protected int shadowColor = 0;
   protected float shadowRadius = 0.0F;
   protected float opacity = 1.0F;
   protected boolean clipToBounds = false;
   protected boolean visible = true;
   protected boolean enabled = true;
   protected boolean hovered = false;
   protected boolean focused = false;
   protected boolean active = false;
   protected final Map<String, Float> animMap = new HashMap<>();
   protected Consumer<MouseButtonEvent> clickListener;
   protected Consumer<MouseButtonEvent> rightClickListener;
   protected Consumer<Boolean> hoverListener;

   public UIEntity id(String id) {
      this.id = id;
      return this;
   }

   public String getId() {
      return this.id;
   }

   public UIEntity pos(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   public UIEntity size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }

   public UIEntity bounds(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      return this;
   }

   public UIEntity bg(int argb) {
      this.bg = argb;
      return this;
   }

   public UIEntity border(int argb, float width) {
      this.border = argb;
      this.borderWidth = width;
      return this;
   }

   public UIEntity radius(float r) {
      this.radius = r;
      return this;
   }

   public UIEntity shadow(int argb, float r) {
      this.shadowColor = argb;
      this.shadowRadius = r;
      return this;
   }

   public UIEntity padding(float all) {
      this.padTop = this.padRight = this.padBottom = this.padLeft = all;
      return this;
   }

   public UIEntity padding(float vertical, float horizontal) {
      this.padTop = this.padBottom = vertical;
      this.padRight = this.padLeft = horizontal;
      return this;
   }

   public UIEntity padding(float top, float right, float bottom, float left) {
      this.padTop = top;
      this.padRight = right;
      this.padBottom = bottom;
      this.padLeft = left;
      return this;
   }

   public UIEntity margin(float all) {
      this.marginTop = this.marginRight = this.marginBottom = this.marginLeft = all;
      return this;
   }

   public UIEntity gap(float gap) {
      this.gapX = this.gapY = gap;
      return this;
   }

   public UIEntity gap(float gapX, float gapY) {
      this.gapX = gapX;
      this.gapY = gapY;
      return this;
   }

   public UIEntity layout(UIEntity.LayoutMode mode) {
      this.layoutMode = mode;
      return this;
   }

   public UIEntity gridCols(int cols) {
      this.gridColumns = cols;
      return this;
   }

   public UIEntity fillWidth(boolean fill) {
      this.fillParentWidth = fill;
      return this;
   }

   public UIEntity fillHeight(boolean fill) {
      this.fillParentHeight = fill;
      return this;
   }

   public UIEntity clip(boolean clip) {
      this.clipToBounds = clip;
      return this;
   }

   public UIEntity visible(boolean visible) {
      this.visible = visible;
      return this;
   }

   public UIEntity enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
   }

   public UIEntity onClick(Consumer<MouseButtonEvent> listener) {
      this.clickListener = listener;
      return this;
   }

   public UIEntity onRightClick(Consumer<MouseButtonEvent> listener) {
      this.rightClickListener = listener;
      return this;
   }

   public UIEntity onHover(Consumer<Boolean> listener) {
      this.hoverListener = listener;
      return this;
   }

   public UIEntity add(UIEntity child) {
      if (child != null) {
         child.parent = this;
         this.children.add(child);
      }

      return this;
   }

   public UIEntity addAll(UIEntity... children) {
      for (UIEntity c : children) {
         this.add(c);
      }

      return this;
   }

   public UIEntity addAll(Collection<? extends UIEntity> children) {
      for (UIEntity c : children) {
         this.add(c);
      }

      return this;
   }

   public boolean remove(UIEntity child) {
      if (child != null && this.children.remove(child)) {
         child.parent = null;
         return true;
      } else {
         return false;
      }
   }

   public void clear() {
      for (UIEntity c : this.children) {
         c.parent = null;
      }

      this.children.clear();
   }

   public List<UIEntity> getChildren() {
      return Collections.unmodifiableList(this.children);
   }

   public UIEntity getParent() {
      return this.parent;
   }

   public UIEntity find(String targetId) {
      if (this.id.equals(targetId)) {
         return this;
      } else {
         for (UIEntity child : this.children) {
            UIEntity found = child.find(targetId);
            if (found != null) {
               return found;
            }
         }

         return null;
      }
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   public void setX(float x) {
      this.x = x;
   }

   public void setY(float y) {
      this.y = y;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isHovered() {
      return this.hovered;
   }

   public boolean isFocused() {
      return this.focused;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setFocused(boolean focused) {
      this.focused = focused;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public boolean contains(float px, float py) {
      return px >= this.x && px <= this.x + this.width && py >= this.y && py <= this.y + this.height;
   }

   public float anim(String key, float target, float speed) {
      float current = this.animMap.getOrDefault(key, target);
      float dt = ClickGuiScreen.lastDt;
      float factor = 1.0F - (float)Math.exp(-speed * dt);
      current += (target - current) * factor;
      if (Math.abs(target - current) < 0.001F) {
         current = target;
      }

      this.animMap.put(key, current);
      return current;
   }

   public void setAnim(String key, float value) {
      this.animMap.put(key, value);
   }

   public float getAnim(String key) {
      return this.animMap.getOrDefault(key, 0.0F);
   }

   public void layout() {
      if (this.visible) {
         float availW = this.width - this.padLeft - this.padRight;
         float curX = this.x + this.padLeft;
         float curY = this.y + this.padTop;
         switch (this.layoutMode) {
            case NONE:
               for (UIEntity child : this.children) {
                  if (child.visible) {
                     child.layout();
                  }
               }
               break;
            case ROW:
               float innerW = this.width - this.padLeft - this.padRight;
               float innerH = this.height - this.padTop - this.padBottom;
               int visCount = 0;
               int fillerCount = 0;
               float fixedSpan = 0.0F;
               float fillerMargins = 0.0F;

               for (UIEntity childxx : this.children) {
                  if (childxx.visible) {
                     visCount++;
                     if (childxx.fillParentWidth) {
                        fillerCount++;
                        fillerMargins += childxx.marginLeft + childxx.marginRight;
                     } else {
                        fixedSpan += childxx.width + childxx.marginLeft + childxx.marginRight;
                     }
                  }
               }

               float gapTotal = this.gapX * Math.max(0, visCount - 1);
               float fillW = fillerCount > 0 ? Math.max(0.0F, (innerW - fixedSpan - fillerMargins - gapTotal) / fillerCount) : 0.0F;
               float rowX = this.x + this.padLeft;

               for (UIEntity childxxx : this.children) {
                  if (childxxx.visible) {
                     rowX += childxxx.marginLeft;
                     if (childxxx.fillParentWidth) {
                        childxxx.width = fillW;
                     }

                     childxxx.x = rowX;
                     float childY = this.y + this.padTop + childxxx.marginTop;
                     if (childxxx.fillParentHeight) {
                        childxxx.height = Math.max(0.0F, innerH - childxxx.marginTop - childxxx.marginBottom);
                     } else if (childxxx.height > 0.0F) {
                        childY = this.y
                           + this.padTop
                           + childxxx.marginTop
                           + Math.max(0.0F, (innerH - childxxx.height - childxxx.marginTop - childxxx.marginBottom) * 0.5F);
                     }

                     childxxx.y = childY;
                     childxxx.layout();
                     rowX += childxxx.width + childxxx.marginRight + this.gapX;
                  }
               }
               break;
            case COLUMN:
               for (UIEntity childxxxx : this.children) {
                  if (childxxxx.visible) {
                     curY += childxxxx.marginTop;
                     childxxxx.x = curX + childxxxx.marginLeft;
                     childxxxx.y = curY;
                     if (childxxxx.fillParentWidth) {
                        childxxxx.width = Math.max(0.0F, availW - childxxxx.marginLeft - childxxxx.marginRight);
                     }

                     childxxxx.layout();
                     curY += childxxxx.height + childxxxx.marginBottom + this.gapY;
                  }
               }
               break;
            case GRID:
               int cols = Math.max(1, this.gridColumns);
               float colW = (availW - (cols - 1) * this.gapX) / cols;
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
         }
      }
   }

   public float computeContentHeight() {
      if (this.children.isEmpty()) {
         return this.height;
      } else {
         float maxBottom = 0.0F;

         for (UIEntity child : this.children) {
            if (child.visible) {
               float b = child.y - this.y + child.height + child.marginBottom;
               if (b > maxBottom) {
                  maxBottom = b;
               }
            }
         }

         return maxBottom + this.padBottom;
      }
   }

   public void render(float mouseX, float mouseY) {
      if (this.visible) {
         boolean prevHover = this.hovered;
         this.hovered = this.contains(mouseX, mouseY);
         if (this.hovered != prevHover && this.hoverListener != null) {
            this.hoverListener.accept(this.hovered);
         }

         if (this.shadowColor != 0 && this.shadowRadius > 0.0F) {
            NvgUtil.drawShadow(this.x, this.y, this.width, this.height, this.radius, this.shadowColor);
         }

         if (this.bg != 0) {
            if (this.radius > 0.0F) {
               NvgUtil.fillRounded(this.x, this.y, this.width, this.height, this.radius, this.bg);
            } else {
               NvgUtil.fillRect(this.x, this.y, this.width, this.height, this.bg);
            }
         }

         if (this.border != 0 && this.borderWidth > 0.0F) {
            if (this.radius > 0.0F) {
               NvgUtil.strokeRounded(
                  this.x - this.borderWidth * 0.5F,
                  this.y - this.borderWidth * 0.5F,
                  this.width + this.borderWidth,
                  this.height + this.borderWidth,
                  this.radius,
                  this.borderWidth,
                  this.border
               );
            } else {
               NvgUtil.strokeRect(this.x, this.y, this.width, this.height, this.borderWidth, this.border);
            }
         }

         this.renderSelf(mouseX, mouseY);
         if (!this.children.isEmpty()) {
            if (this.clipToBounds) {
               NvgUtil.save();
               NvgUtil.intersectScissor(this.x, this.y, this.width, this.height);
            }

            for (UIEntity child : this.children) {
               if (child.visible) {
                  child.render(mouseX, mouseY);
               }
            }

            if (this.clipToBounds) {
               NvgUtil.restore();
            }
         }
      }
   }

   protected void renderSelf(float mouseX, float mouseY) {
   }

   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         for (int i = this.children.size() - 1; i >= 0; i--) {
            UIEntity child = this.children.get(i);
            if (child.visible && child.enabled && child.mouseClicked(event)) {
               return true;
            }
         }

         if (this.contains((float)event.x(), (float)event.y())) {
            int btn = event.buttonInfo().button();
            if (btn == 0 && this.clickListener != null) {
               this.clickListener.accept(event);
               return true;
            } else if (btn == 1 && this.rightClickListener != null) {
               this.rightClickListener.accept(event);
               return true;
            } else {
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         boolean handled = false;

         for (int i = this.children.size() - 1; i >= 0; i--) {
            if (this.children.get(i).mouseReleased(event)) {
               handled = true;
            }
         }

         return handled;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (this.visible && this.enabled) {
         for (int i = this.children.size() - 1; i >= 0; i--) {
            if (this.children.get(i).mouseDragged(event, dragX, dragY)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.visible && this.enabled) {
         for (int i = this.children.size() - 1; i >= 0; i--) {
            if (this.children.get(i).mouseScrolled(mouseX, mouseY, amount)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean keyPressed(KeyEvent event) {
      if (this.visible && this.enabled) {
         for (int i = this.children.size() - 1; i >= 0; i--) {
            if (this.children.get(i).keyPressed(event)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean charTyped(CharacterEvent event) {
      if (this.visible && this.enabled) {
         for (int i = this.children.size() - 1; i >= 0; i--) {
            if (this.children.get(i).charTyped(event)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static enum LayoutMode {
      NONE,
      ROW,
      COLUMN,
      GRID;
   }
}
