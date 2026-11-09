package kat.respect.b2;

import kat.respect.b2.client.ClickGuiKeybinds;
import kat.respect.b2.client.ClickGuiOpener;
import kat.respect.b2.config.ConfigManager;
import kat.respect.b2.event.ClientEventDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;

public final class KuroPlusMod implements ClientModInitializer {
   public void onInitializeClient() {
      try {
         ClickGuiKeybinds.register();
      } catch (Throwable var7) {
         System.err.println("[Kuro+] ClickGuiKeybinds.register failed: " + var7);
      }

      try {
         KuroPlusClient.getInstance().init();
      } catch (Throwable var6) {
         System.err.println("[Kuro+] KuroPlusClient.getInstance().init() failed: " + var6);
         var6.printStackTrace();
      }

      try {
         ClientTickEvents.START_CLIENT_TICK.register((StartTick)client -> ClientEventDispatcher.onClientTickStart());
         ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
            ClientEventDispatcher.onClientTickEnd();
            KuroPlusClient.getInstance().onUpdate();
         });
      } catch (Throwable var5) {
         System.err.println("[Kuro+] ClientTickEvents register failed: " + var5);
      }

      try {
         ClickGuiKeybinds.syncFromModule();
      } catch (Throwable var4) {
         System.err.println("[Kuro+] ClickGuiKeybinds.syncFromModule failed: " + var4);
      }

      try {
         ClickGuiOpener.register();
      } catch (Throwable var3) {
         System.err.println("[Kuro+] ClickGuiOpener.register failed: " + var3);
      }

      try {
         ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> ConfigManager.getInstance().saveActive());
      } catch (Throwable var2) {
         System.err.println("[Kuro+] CLIENT_STOPPING register failed: " + var2);
      }
   }
}
