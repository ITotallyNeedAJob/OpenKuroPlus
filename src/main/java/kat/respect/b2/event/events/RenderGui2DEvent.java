package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class RenderGui2DEvent extends Event {
   private final GuiGraphicsExtractor graphics;

   public RenderGui2DEvent(GuiGraphicsExtractor graphics) {
      this.graphics = graphics;
   }

   public GuiGraphicsExtractor getGraphics() {
      return this.graphics;
   }
}
