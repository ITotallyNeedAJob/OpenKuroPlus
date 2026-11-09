package kat.respect.b2.mixin;

import kat.respect.b2.command.CommandManager;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ClientPacketListenerMixin {
   @Inject(
      method = {"sendChat"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$interceptChatCommand(String message, CallbackInfo ci) {
      if (CommandManager.INSTANCE.isCommand(message)) {
         ci.cancel();
         CommandManager.INSTANCE.handleCommand(message);
      }
   }

   @Inject(
      method = {"sendCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$interceptSlashCommand(String command, CallbackInfo ci) {
      if (CommandManager.INSTANCE.isCommand("/" + command)) {
         ci.cancel();
         CommandManager.INSTANCE.handleCommand("/" + command);
      }
   }

   @Inject(
      method = {"sendUnattendedCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$interceptUnattendedCommand(String command, Screen screen, CallbackInfo ci) {
      if (CommandManager.INSTANCE.isCommand(command)) {
         ci.cancel();
         CommandManager.INSTANCE.handleCommand(command);
      }
   }
}
