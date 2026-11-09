package kat.respect.b2.event;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import kat.respect.b2.event.events.AttackEvent;
import kat.respect.b2.event.events.ClickAttackEvent;
import kat.respect.b2.event.events.ClientReadyEvent;
import kat.respect.b2.event.events.ClientShutdownEvent;
import kat.respect.b2.event.events.HandleInputEarlyEvent;
import kat.respect.b2.event.events.ItemUseEvent;
import kat.respect.b2.event.events.KeyEvent;
import kat.respect.b2.event.events.KineticHitEvent;
import kat.respect.b2.event.events.ModuleStateChangeEvent;
import kat.respect.b2.event.events.MotionEvent;
import kat.respect.b2.event.events.MouseButtonEvent;
import kat.respect.b2.event.events.MovementInputEvent;
import kat.respect.b2.event.events.PacketEvent;
import kat.respect.b2.event.events.PlayerTickEvent;
import kat.respect.b2.event.events.Render2DEvent;
import kat.respect.b2.event.events.Render3DEvent;
import kat.respect.b2.event.events.RenderGui2DEvent;
import kat.respect.b2.event.events.TickEvent;
import kat.respect.b2.event.events.WorldLeaveEvent;
import kat.respect.b2.event.events.WorldLoadEvent;
import kat.respect.b2.module.Module;
import kat.respect.b2.util.Log;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;

public final class ClientEventDispatcher {
   private static final AtomicBoolean render2DPosted = new AtomicBoolean();
   private static final AtomicBoolean renderGui2DPosted = new AtomicBoolean();
   private static final AtomicBoolean render3DPosted = new AtomicBoolean();
   private static volatile long lastPlayerTick = Long.MIN_VALUE;
   private static volatile long lastMotionPre = Long.MIN_VALUE;
   private static volatile long lastMotionPost = Long.MIN_VALUE;
   private static volatile long clientTick;
   private static volatile String activeTickSource = null;
   private static final AtomicLong clientTickCounter = new AtomicLong();
   private static final AtomicLong playerTickCounter = new AtomicLong();
   private static final AtomicLong motionPreCounter = new AtomicLong();
   private static final AtomicLong motionPostCounter = new AtomicLong();
   private static final AtomicLong render2DCounter = new AtomicLong();
   private static final AtomicLong renderGui2DCounter = new AtomicLong();
   private static final AtomicLong render3DCounter = new AtomicLong();

   private ClientEventDispatcher() {
   }

   public static void beginFrame() {
      render2DPosted.set(false);
      renderGui2DPosted.set(false);
      render3DPosted.set(false);
   }

   public static void onClientTickStart() {
      onClientTickStart("fabric");
   }

   public static void onClientTickStart(String source) {
      if (activeTickSource == null) {
         synchronized (ClientEventDispatcher.class) {
            if (activeTickSource == null) {
               activeTickSource = source;
               Log.info("events", "Tick source selected: " + source);
            }
         }
      }

      if (activeTickSource.equals(source)) {
         clientTick++;
         clientTickCounter.incrementAndGet();
         lastPlayerTick = Long.MIN_VALUE;
         lastMotionPre = Long.MIN_VALUE;
         lastMotionPost = Long.MIN_VALUE;
         EventBus.INSTANCE.post(new TickEvent(TickEvent.Phase.PRE));
      }
   }

   public static void onClientTickEnd() {
      onClientTickEnd("fabric");
   }

   public static void onClientTickEnd(String source) {
      if (activeTickSource == null || activeTickSource.equals(source)) {
         EventBus.INSTANCE.post(new TickEvent(TickEvent.Phase.POST));
      }
   }

   public static void onHandleKeybindsEarly() {
      EventBus.INSTANCE.post(new HandleInputEarlyEvent());
   }

   public static MotionEvent onPlayerAiStepHead(LocalPlayer player) {
      long tick = clientTick;
      if (lastPlayerTick == tick) {
         return null;
      } else {
         lastPlayerTick = tick;
         lastMotionPre = tick;
         playerTickCounter.incrementAndGet();
         motionPreCounter.incrementAndGet();
         EventBus.INSTANCE.post(new PlayerTickEvent());
         MotionEvent event = new MotionEvent(
            MotionEvent.State.PRE, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround()
         );
         return EventBus.INSTANCE.post(event);
      }
   }

   public static MotionEvent onPlayerAiStepPost(LocalPlayer player) {
      long tick = clientTick;
      if (lastMotionPost == tick) {
         return null;
      } else {
         lastMotionPost = tick;
         motionPostCounter.incrementAndGet();
         MotionEvent event = new MotionEvent(
            MotionEvent.State.POST, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround()
         );
         return EventBus.INSTANCE.post(event);
      }
   }

   public static void onRenderLevelTail(float partialTick) {
      if (render3DPosted.compareAndSet(false, true)) {
         render3DCounter.incrementAndGet();
         EventBus.INSTANCE.post(new Render3DEvent(partialTick));
      }
   }

   public static void onRender2D(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
      if (render2DPosted.compareAndSet(false, true)) {
         render2DCounter.incrementAndGet();
         EventBus.INSTANCE.post(new Render2DEvent(graphics, deltaTracker));
      }
   }

   public static void onRenderGui2D(GuiGraphicsExtractor graphics) {
      if (graphics != null) {
         if (renderGui2DPosted.compareAndSet(false, true)) {
            renderGui2DCounter.incrementAndGet();
            EventBus.INSTANCE.post(new RenderGui2DEvent(graphics));
         }
      }
   }

   public static KeyEvent onKey(int key, int action) {
      return EventBus.INSTANCE.post(new KeyEvent(key, action));
   }

   public static MouseButtonEvent onMouseButton(int button, int action) {
      return EventBus.INSTANCE.post(new MouseButtonEvent(button, action));
   }

   public static AttackEvent onAttack(LivingEntity target) {
      return EventBus.INSTANCE.post(new AttackEvent(target));
   }

   public static ItemUseEvent onItemUse() {
      return EventBus.INSTANCE.post(new ItemUseEvent());
   }

   public static ClickAttackEvent onClickAttack() {
      return EventBus.INSTANCE.post(new ClickAttackEvent());
   }

   public static Input onModifyInput(Input input) {
      MovementInputEvent event = new MovementInputEvent(input);
      EventBus.INSTANCE.post(event);
      return event.getInput();
   }

   public static KineticHitEvent onKineticHit() {
      return EventBus.INSTANCE.post(new KineticHitEvent());
   }

   public static PacketEvent onPacketReceive(Packet<?> packet) {
      return EventBus.INSTANCE.post(new PacketEvent(PacketEvent.Direction.RECEIVE, packet));
   }

   public static PacketEvent onPacketSend(Packet<?> packet) {
      return EventBus.INSTANCE.post(new PacketEvent(PacketEvent.Direction.SEND, packet));
   }

   public static ClientReadyEvent onClientReady() {
      return EventBus.INSTANCE.post(new ClientReadyEvent());
   }

   public static ClientShutdownEvent onClientShutdown() {
      return EventBus.INSTANCE.post(new ClientShutdownEvent());
   }

   public static WorldLoadEvent onWorldLoad() {
      return EventBus.INSTANCE.post(new WorldLoadEvent());
   }

   public static WorldLeaveEvent onWorldLeave() {
      return EventBus.INSTANCE.post(new WorldLeaveEvent());
   }

   public static ModuleStateChangeEvent onModuleStateChange(Module module, boolean enabled) {
      return EventBus.INSTANCE.post(new ModuleStateChangeEvent(module, enabled));
   }

   public static long getClientTickCount() {
      return clientTickCounter.get();
   }

   public static long getPlayerTickCount() {
      return playerTickCounter.get();
   }

   public static long getMotionPreCount() {
      return motionPreCounter.get();
   }

   public static long getMotionPostCount() {
      return motionPostCounter.get();
   }

   public static long getRender2DCount() {
      return render2DCounter.get();
   }

   public static long getRender3DCount() {
      return render3DCounter.get();
   }

   public static void resetMetrics() {
      clientTickCounter.set(0L);
      playerTickCounter.set(0L);
      motionPreCounter.set(0L);
      motionPostCounter.set(0L);
      render2DCounter.set(0L);
      render3DCounter.set(0L);
   }
}
