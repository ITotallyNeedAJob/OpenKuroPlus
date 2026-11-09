package kat.respect.b2.bridge;

import java.util.List;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.setting.BooleanSetting;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.module.setting.ModeSetting;
import kat.respect.b2.module.setting.NumberSetting;
import kat.respect.b2.module.setting.Setting;

public final class NotificationSettings {
   public static final ModeSetting engine = new ModeSetting(
      "Engine", "Rendering engine used to draw notifications", "Sync with ClickGUI", List.of("Sync with ClickGUI", "Mobile (Shaders)", "Blaze3D (Vulkan)")
   );
   public static final ModeSetting style = new ModeSetting(
      "Style", "Visual appearance style of notification cards", "Mobile Glass", List.of("Mobile Glass", "Modern Toast", "Compact Pill", "Minimalist")
   );
   public static final ModeSetting position = new ModeSetting(
      "Position",
      "Screen anchor corner or edge for notifications",
      "Bottom Right",
      List.of("Bottom Right", "Top Right", "Top Left", "Bottom Left", "Top Center", "Bottom Center")
   );
   public static final ModeSetting animation = new ModeSetting(
      "Animation", "Entry and exit animation style", "Smooth Slide", List.of("Smooth Slide", "Pop Bounce", "Fade Only")
   );
   public static final NumberSetting duration = new NumberSetting("Duration", "How many seconds each notification stays on screen", 1.0, 10.0, 3.5, 0.5);
   public static final NumberSetting maxDisplay = new NumberSetting("Max Display", "Maximum notifications visible simultaneously", 1.0, 10.0, 5.0, 1.0);
   public static final NumberSetting width = new NumberSetting("Width", "Width of the notification card in pixels", 160.0, 360.0, 220.0, 5.0);
   public static final ModeSetting heightMode = new ModeSetting(
      "Height Mode", "Height sizing of notification popups", "Standard (46px)", List.of("Standard (46px)", "Compact (34px)", "Auto (Dynamic)")
   );
   public static final BooleanSetting progressBar = new BooleanSetting("Progress Bar", "Show countdown line at the bottom indicating remaining duration", true);
   public static final BooleanSetting showIcons = new BooleanSetting("Show Icons", "Render status icon (check, cross, warning, info)", true);
   public static final BooleanSetting moduleToggles = new BooleanSetting(
      "Module Toggles", "Automatically show a popup when a module is enabled or disabled", true
   );
   public static final BooleanSetting sound = new BooleanSetting("Sound FX", "Play audio feedback when a notification arrives", false);
   public static final BooleanSetting blurGlass = new BooleanSetting("Blur / Glass", "Dual Kawase real-time background blur and liquid refraction", true);
   public static final BooleanSetting rounded = new BooleanSetting("Rounded", "Round the corners of notification popups", true);
   public static final NumberSetting radius = new NumberSetting("Radius", "Corner radius in pixels when rounded", 0.0, 16.0, 8.0, 1.0);
   public static final BooleanSetting shadow = new BooleanSetting("Soft Shadow", "Draw a soft drop shadow behind notifications", true);
   public static final ColorSetting background = new ColorSetting("Background", "Popup card background color", -669839072);
   public static final ColorSetting textColor = new ColorSetting("Title Text", "Title font color", -1);
   public static final ColorSetting subtextColor = new ColorSetting("Body Text", "Body message font color", -7035976);
   public static final ColorSetting infoColor = new ColorSetting("Info Color", "Accent color for Info notifications", -7643914);
   public static final ColorSetting successColor = new ColorSetting("Success Color", "Accent color for Success notifications", -15681151);
   public static final ColorSetting warningColor = new ColorSetting("Warning Color", "Accent color for Warning notifications", -680437);
   public static final ColorSetting errorColor = new ColorSetting("Error Color", "Accent color for Error notifications", -1096636);

   private NotificationSettings() {
   }

   public static List<Setting<?>> getAllSettings() {
      return List.of(
         engine,
         style,
         position,
         animation,
         duration,
         maxDisplay,
         width,
         heightMode,
         progressBar,
         showIcons,
         moduleToggles,
         sound,
         blurGlass,
         rounded,
         radius,
         shadow,
         background,
         textColor,
         subtextColor,
         infoColor,
         successColor,
         warningColor,
         errorColor
      );
   }

   public static void addTo(Module module) {
      module.addPublicSettings(
         engine,
         style,
         position,
         animation,
         duration,
         maxDisplay,
         width,
         heightMode,
         progressBar,
         showIcons,
         moduleToggles,
         sound,
         blurGlass,
         rounded,
         radius,
         shadow,
         background,
         textColor,
         subtextColor,
         infoColor,
         successColor,
         warningColor,
         errorColor
      );
   }

   static {
      radius.setVisibility(() -> rounded.getValue());
   }
}
