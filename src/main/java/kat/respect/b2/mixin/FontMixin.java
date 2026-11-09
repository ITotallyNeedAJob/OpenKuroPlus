package kat.respect.b2.mixin;

import kat.respect.b2.bridge.NameProtectBridge;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({Font.class})
public class FontMixin {
   @ModifyVariable(
      method = {"prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private FormattedCharSequence respectB2$protectNameSequence(FormattedCharSequence sequence) {
      return NameProtectBridge.transformSequence(sequence);
   }

   @ModifyVariable(
      method = {"prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private String respectB2$protectNameString(String text) {
      return NameProtectBridge.transformString(text);
   }
}
