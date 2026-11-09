package kat.respect.b2.gui.entity;

public class UIContainer extends UIEntity {
   public UIContainer() {
      this.layoutMode = UIEntity.LayoutMode.NONE;
   }

   public static UIContainer column(float gap) {
      UIContainer c = new UIContainer();
      c.layoutMode = UIEntity.LayoutMode.COLUMN;
      c.gapY = gap;
      return c;
   }

   public static UIContainer row(float gap) {
      UIContainer c = new UIContainer();
      c.layoutMode = UIEntity.LayoutMode.ROW;
      c.gapX = gap;
      return c;
   }

   public static UIContainer grid(int columns, float gapX, float gapY) {
      UIContainer c = new UIContainer();
      c.layoutMode = UIEntity.LayoutMode.GRID;
      c.gridColumns = columns;
      c.gapX = gapX;
      c.gapY = gapY;
      return c;
   }
}
