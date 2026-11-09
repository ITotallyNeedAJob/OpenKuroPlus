package kat.respect.b2.ct;

import java.util.Set;
import kat.respect.b2.bridge.EspOutlineBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CRedirect;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;

@CTransformer({LevelRenderer.class})
public class LevelRendererCT {
   @CInline
   @CRedirect(
      method = {"renderLevel"},
      target = @CTarget(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;",
         optional = true
      )
   )
   public PostChain respectB2$espOutlineChain(ShaderManager shaderManager, Identifier original, Set<Identifier> set) {
      Identifier override = EspOutlineBridge.postChainId();
      return shaderManager.getPostChain(override != null ? override : original, set);
   }
}
