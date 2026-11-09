package kat.respect.b2.gui;

import java.awt.Color;
import kat.respect.b2.bridge.ClickGuiSettings;

public final class GuiTheme {
   public static final float RADIUS = 7.0F;
   public int headerColor;
   public int fontColor;
   public int accentColor;
   public int panelBg;
   public final int rowBg;
   public final int rowHover;
   public final int subHover;
   public final int sidebarBg;
   public final int dimText;
   public final int boxBg;
   public final int sliderTrack;
   public final int textboxBg;
   public final int textboxFocusBg;
   public final int textboxBorder;
   public final int pickerBg;
   public final int pickerBorder;
   public final int activePillBg;
   public final int activePillText;
   private final int basePanelBg;
   public static final int BORDER = 587202559;
   public static final int SURFACE_BORDER = 822083583;
   public static final GuiTheme LARP = new GuiTheme(
      -15198179,
      -460036,
      -10262799,
      -166653675,
      0,
      385875967,
      318767103,
      -133362416,
      -7035976,
      -14935006,
      -15198178,
      -15461352,
      -15066592,
      587202559,
      -15329764,
      788529151,
      -1,
      -15658731
   );
   public static final GuiTheme PURPLE = new GuiTheme(
      -7643914,
      -1,
      -8635667,
      -166654178,
      0,
      385875967,
      318767103,
      -133494252,
      -7434849,
      -14145992,
      -14540749,
      -15330012,
      -14803922,
      553648127,
      -15198680,
      788529151,
      -7643914,
      -1
   );
   public static final GuiTheme CLASSIC = new GuiTheme(
      -1555377,
      -1,
      -38028,
      -166587882,
      0,
      385875967,
      318767103,
      -133494002,
      -7434597,
      -14146000,
      -14540758,
      -15330016,
      -14803926,
      553648127,
      -15198684,
      788529151,
      -1555377,
      -1
   );
   public static final GuiTheme MIDNIGHT = new GuiTheme(
      -10777105,
      -1,
      -13058568,
      -166915550,
      0,
      385875967,
      318767103,
      -133624552,
      -7892570,
      -14670280,
      -14998989,
      -15591384,
      -15195594,
      553648127,
      -15459280,
      788529151,
      -13058568,
      -16052455
   );
   public static final GuiTheme CRIMSON = new GuiTheme(
      -1952180,
      -1,
      -770210,
      -166195694,
      0,
      385875967,
      318767103,
      -133035250,
      -5993584,
      -13360600,
      -13886174,
      -14674920,
      -14018016,
      553648127,
      -14543334,
      788529151,
      -770210,
      -1
   );
   public static final GuiTheme CYAN = new GuiTheme(
      -13841992,
      -1,
      -16337196,
      -167111654,
      0,
      385875967,
      318767103,
      -133755116,
      -8217438,
      -14799308,
      -15062994,
      -15589338,
      -15193550,
      553648127,
      -15457752,
      788529151,
      -16337196,
      -16247785
   );
   public static final GuiTheme EMERALD = new GuiTheme(
      -16411031,
      -1,
      -15681151,
      -167045102,
      0,
      385875967,
      318767103,
      -133754356,
      -7952234,
      -14995677,
      -15325411,
      -15720426,
      -15193313,
      553648127,
      -15588583,
      788529151,
      -1,
      -16050158
   );
   public static final GuiTheme SAKURA = new GuiTheme(
      -2414729,
      -1,
      -757066,
      -166194920,
      0,
      385875967,
      318767103,
      -132903916,
      -5861466,
      -13426637,
      -14018006,
      -14675168,
      -13952725,
      553648127,
      -14543582,
      788529151,
      -1,
      -15199976
   );
   public static final GuiTheme CYBERPUNK = new GuiTheme(
      -2525434,
      -1,
      -278748,
      -166193907,
      0,
      385875967,
      318767103,
      -132968438,
      -5726328,
      -13291748,
      -13883626,
      -14672881,
      -13883884,
      553648127,
      -14541295,
      788529151,
      -1,
      -15198963
   );
   private static final GuiTheme RAINBOW = new GuiTheme(
      -15198179,
      -460036,
      -52378,
      -166653675,
      0,
      385875967,
      318767103,
      -133362416,
      -7035976,
      -14935006,
      -15198178,
      -15461352,
      -15066592,
      587202559,
      -15329764,
      788529151,
      -1,
      -15658731
   );
   private static final GuiTheme CUSTOM = new GuiTheme(
      -7643914,
      -1,
      -8635667,
      -166654178,
      0,
      385875967,
      318767103,
      -133494252,
      -7434849,
      -14145992,
      -14540749,
      -15330012,
      -14803922,
      553648127,
      -15198680,
      788529151,
      -1,
      -15658731
   );

   private GuiTheme(
      int headerColor,
      int fontColor,
      int accentColor,
      int panelBg,
      int rowBg,
      int rowHover,
      int subHover,
      int sidebarBg,
      int dimText,
      int boxBg,
      int sliderTrack,
      int textboxBg,
      int textboxFocusBg,
      int textboxBorder,
      int pickerBg,
      int pickerBorder,
      int activePillBg,
      int activePillText
   ) {
      this.headerColor = headerColor;
      this.fontColor = fontColor;
      this.accentColor = accentColor;
      this.basePanelBg = panelBg;
      this.panelBg = panelBg;
      this.rowBg = rowBg;
      this.rowHover = rowHover;
      this.subHover = subHover;
      this.sidebarBg = sidebarBg;
      this.dimText = dimText;
      this.boxBg = boxBg;
      this.sliderTrack = sliderTrack;
      this.textboxBg = textboxBg;
      this.textboxFocusBg = textboxFocusBg;
      this.textboxBorder = textboxBorder;
      this.pickerBg = pickerBg;
      this.pickerBorder = pickerBorder;
      this.activePillBg = activePillBg;
      this.activePillText = activePillText;
   }

   public static float radius() {
      return ClickGuiSettings.corners.isMode("Rounded") ? 7.0F : 0.0F;
   }

   public static int getRainbowColor() {
      float hue = (float)(System.currentTimeMillis() % 4000L / 4000.0);
      return Color.HSBtoRGB(hue, 0.75F, 1.0F);
   }

   public static GuiTheme current() {
      String trans = ClickGuiSettings.theme.getValue();
      GuiTheme theme;
      switch (trans) {
         case "Larp":
            theme = LARP;
            break;
         case "Classic":
            theme = CLASSIC;
            break;
         case "Midnight":
            theme = MIDNIGHT;
            break;
         case "Emerald":
            theme = EMERALD;
            break;
         case "Crimson":
            theme = CRIMSON;
            break;
         case "Cyan":
            theme = CYAN;
            break;
         case "Sakura":
            theme = SAKURA;
            break;
         case "Cyberpunk":
            theme = CYBERPUNK;
            break;
         case "Rainbow":
            theme = RAINBOW;
            theme.accentColor = getRainbowColor();
            break;
         case "Custom":
            theme = CUSTOM;
            theme.headerColor = ClickGuiSettings.clientColour.getValue();
            theme.fontColor = ClickGuiSettings.fontColour.getValue();
            theme.accentColor = ClickGuiSettings.accentColour.getValue();
            break;
         default:
            theme = PURPLE;
      }

      float transF = Math.max(0.1F, Math.min(1.0F, ClickGuiSettings.transparency.getValueFloat()));
      int baseA = theme.basePanelBg >> 24 & 0xFF;
      int newA = Math.max(24, Math.round(baseA * transF));
      theme.panelBg = newA << 24 | theme.basePanelBg & 16777215;
      return theme;
   }
}
