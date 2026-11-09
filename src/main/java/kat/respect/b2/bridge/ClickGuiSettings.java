package kat.respect.b2.bridge;

import java.util.List;
import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.hud.HudEditorScreen;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.setting.BooleanSetting;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.module.setting.ModeSetting;
import kat.respect.b2.module.setting.NumberSetting;
import kat.respect.b2.module.setting.Setting;
import net.minecraft.client.Minecraft;

public final class ClickGuiSettings {
   public static final ModeSetting renderEngine = new ModeSetting(
      "Render Engine", "Controls whether ClickGUI renders with Mobile shaders or Blaze3D", "Mobile (Shaders)", List.of("Mobile (Shaders)", "Blaze3D (Vulkan)")
   );
   public static final ModeSetting theme = new ModeSetting(
      "Theme",
      "GUI color theme",
      "Larp",
      List.of("Larp", "Purple", "Classic", "Midnight", "Emerald", "Crimson", "Cyan", "Sakura", "Cyberpunk", "Rainbow", "Custom")
   );
   public static final ModeSetting background = new ModeSetting(
      "Background", "Background effect behind ClickGUI", "Dim", List.of("Dim", "Blur", "Liquid Glass", "None")
   );
   public static final ModeSetting configScreen = new ModeSetting(
      "Config Screen", "Side keeps the quick panel beside modules; Dock uses only the large config screen", "Side", List.of("Side", "Dock")
   );
   public static final ModeSetting corners = new ModeSetting("Corners", "Panel corner rounding", "Rounded", List.of("Rounded", "Sharp"));
   public static final NumberSetting transparency = new NumberSetting("Transparency", "How see-through the panels are", 0.1, 1.0, 0.85, 0.05);
   public static final ModeSetting headerHeight = new ModeSetting("Header Height", "Slim (15px)", "Slim (15px)", "Standard (18px)", "Tall (22px)");
   public static final ModeSetting moduleHeight = new ModeSetting("Module Height", "Slim (14px)", "Slim (14px)", "Standard (16px)", "Tall (18px)");
   public static final BooleanSetting showCounters = new BooleanSetting("Show Counters", true);
   public static final NumberSetting scrollSpeed = new NumberSetting("Scroll Speed", 1.0, 30.0, 10.0, 1.0);
   public static final ModeSetting descriptions = new ModeSetting("Descriptions", "Shrink", "Shrink", "Full", "Off");
   public static final ModeSetting pauseGame = new ModeSetting("Pause Game", "Continue", "Continue", "Pause");
   public static final ColorSetting clientColour = new ColorSetting("Header Colour", -15461346);
   public static final ColorSetting fontColour = new ColorSetting("Font Colour", -1);
   public static final ColorSetting accentColour = new ColorSetting("Accent Colour", -50373);
   public static final BooleanSetting drawn = new BooleanSetting("Drawn", true);
   public static final NumberSetting scale = new NumberSetting("Scale", "GUI scale multiplier", 0.7, 1.3, 1.0, 0.05);
   public static final BooleanSetting rangeSplitClick = new BooleanSetting(
      "Range Split Click", "Left click = Min, Right click = Max on range sliders (off = drags nearest knob)", false
   );
   public static final BooleanSetting resetPositions = new BooleanSetting(
      "Reset Positions", "Puts all panels back to their default spots (auto-disables)", false
   );
   public static final BooleanSetting hudEditor = new BooleanSetting("HUD Editor", "Opens the drag and drop HUD editor (auto-disables)", false);

   private ClickGuiSettings() {
   }

   public static List<Setting<?>> getAllSettings() {
      return List.of(
         renderEngine,
         theme,
         background,
         configScreen,
         corners,
         transparency,
         headerHeight,
         moduleHeight,
         showCounters,
         scrollSpeed,
         descriptions,
         pauseGame,
         clientColour,
         fontColour,
         accentColour,
         drawn,
         scale,
         rangeSplitClick,
         resetPositions,
         hudEditor
      );
   }

   public static void addTo(Module module) {
      module.addPublicSettings(
         renderEngine,
         theme,
         background,
         configScreen,
         corners,
         transparency,
         headerHeight,
         moduleHeight,
         showCounters,
         scrollSpeed,
         descriptions,
         pauseGame,
         clientColour,
         fontColour,
         accentColour,
         drawn,
         scale,
         rangeSplitClick,
         resetPositions,
         hudEditor
      );
   }

   static {
      clientColour.setVisibility(() -> theme.isMode("Custom"));
      fontColour.setVisibility(() -> theme.isMode("Custom"));
      accentColour.setVisibility(() -> theme.isMode("Custom"));
      resetPositions.onChange(v -> {
         if (Boolean.TRUE.equals(v)) {
            ClickGuiScreen.getInstance().resetPanelPositions();
            resetPositions.setValue(false);
         }
      });
      hudEditor.onChange(v -> {
         if (Boolean.TRUE.equals(v)) {
            Minecraft.getInstance().setScreenAndShow(new HudEditorScreen());
            hudEditor.setValue(false);
         }
      });
   }
}
