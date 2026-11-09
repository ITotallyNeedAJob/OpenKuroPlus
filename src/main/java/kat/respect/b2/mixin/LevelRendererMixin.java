package kat.respect.b2.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import kat.respect.b2.bridge.EspOutlineBridge;
import kat.respect.b2.render.WorldGeometry;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class LevelRendererMixin {
   @Redirect(
      method = {"render"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"
      ),
      require = 0
   )
   @CInline
   private PostChain respectB2$espOutlineChain(ShaderManager shaderManager, Identifier original, Set<Identifier> set) {
      Identifier override = EspOutlineBridge.postChainId();
      return shaderManager.getPostChain(override != null ? override : original, set);
   }

   @Inject(
      method = {"submitBlockEntities"},
      at = {@At("TAIL")},
      require = 0
   )
   @CInline
   private void respectB2$submitWorldGeometry(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeStorage storage, CallbackInfo ci) {
      if (WorldGeometry.hasPending()) {
         try {
            WorldGeometry.submitAll(
               storage, levelRenderState != null && levelRenderState.cameraRenderState != null ? levelRenderState.cameraRenderState.pos : null
            );
         } catch (Throwable var6) {
         }
      }
   }
}
