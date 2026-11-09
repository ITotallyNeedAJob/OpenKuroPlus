package kat.respect.b2.ct;

import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.minecraft.client.Minecraft;

@CTransformer({Minecraft.class})
public class AgentEarlyTransformer {
   @CInline
   @CInject(
      method = {"<init>"},
      target = {@CTarget("HEAD")}
   )
   public void kamiEarlyInit() {
      try {
         AgentRuntime.earlyClientInit();
      } catch (Throwable var2) {
      }
   }
}
