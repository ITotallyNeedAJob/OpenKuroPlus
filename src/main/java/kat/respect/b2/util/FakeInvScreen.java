package kat.respect.b2.util;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Player;

public class FakeInvScreen extends InventoryScreen {
   public FakeInvScreen(Player player) {
      super(player);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      return false;
   }
}
