package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Render2DEvent extends Event {
   private final GuiGraphicsExtractor graphics;
   private final DeltaTracker deltaTracker;

   public Render2DEvent(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
      this.graphics = graphics;
      this.deltaTracker = deltaTracker;
   }

   public GuiGraphicsExtractor getGraphics() {
      return this.graphics;
   }

   public DeltaTracker getDeltaTracker() {
      return this.deltaTracker;
   }

   public float getTickDelta() {
      return this.deltaTracker.getGameTimeDeltaPartialTick(false);
   }
}
