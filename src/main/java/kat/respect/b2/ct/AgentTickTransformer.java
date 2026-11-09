package kat.respect.b2.ct;

import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.minecraft.client.Minecraft;

@CTransformer({Minecraft.class})
public class AgentTickTransformer {
   @CInline
   @CInject(
      method = {"tick"},
      target = {@CTarget("HEAD")}
   )
   public void kamiTickPre() {
      try {
         AgentTickHook.postPre();
      } catch (Throwable var2) {
      }
   }

   @CInline
   @CInject(
      method = {"tick"},
      target = {@CTarget("RETURN")}
   )
   public void kamiTickPost() {
      try {
         AgentTickHook.postPost();
      } catch (Throwable var2) {
      }
   }
}
