package kat.respect.b2.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.gui.entity.UIEntity;
import kat.respect.b2.gui.entity.UIIcon;
import kat.respect.b2.gui.entity.UIScrollContainer;
import kat.respect.b2.gui.entity.UISettingFactory;
import kat.respect.b2.module.Category;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.module.setting.KeySetting;
import kat.respect.b2.render.IRenderer2D;
import kat.respect.b2.render.Renderer2D;
import kat.respect.b2.render.mobile.MobileDraw2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
   private static ClickGuiScreen instance;
   private static ClickGuiScreen.View activeView = ClickGuiScreen.View.MODULES;
   private static Category activeCategory = null;
   private static Module inspectedModule = null;
   private static Module bindingModule = null;
   private static String searchQuery = "";
   private static float modulesScroll = 0.0F;
   private final List<CategoryPanel> panels = new ArrayList<>();
   private ConfigPanel configPanel;
   private final ConfigDockView configDockView = new ConfigDockView();
   private final FriendsView friendsView = new FriendsView();
   private final GuiSettingsView guiSettingsView = new GuiSettingsView();
   private final UIScrollContainer inspectorScrollContainer = new UIScrollContainer();
   private static final int WINDOW_PANEL_WIDTH = 148;
   public static final int PANELS_TOP = 36;
   public static final int PANELS_BOTTOM = 30;
   private boolean searchFocused = false;
   private float lastScaleW = 1280.0F;
   private float lastScaleH = 720.0F;
   private int lastMouseX;
   private int lastMouseY;
   private float openProgress = 0.0F;
   private long lastFrameTime = 0L;
   private long openedAtMs = 0L;
   private static final long OPEN_GRACE_MS = 350L;
   public static float lastDt = 0.016F;
   private int logoTexId = -1;
   private final Map<String, Float> animMap = new HashMap<>();
   private String hoveredDescription = "";
   private static final Category[] DISPLAY_CATEGORIES = Category.DISPLAY_CATEGORIES;

   private ClickGuiScreen() {
      super(Component.literal("ClickGUI"));
      this.inspectorScrollContainer.layout(UIEntity.LayoutMode.COLUMN);
      this.inspectorScrollContainer.gap(0.0F, 4.0F);
      this.inspectorScrollContainer.padding(2.0F, 4.0F, 8.0F, 2.0F);
      this.initPanels();
   }

   @Override
   protected void init() {
      super.init();
      this.resetOpenProgress();
   }

   public void resetOpenProgress() {
      this.openProgress = 0.0F;
      this.lastFrameTime = 0L;
      this.openedAtMs = System.currentTimeMillis();
      this.searchFocused = false;
      bindingModule = null;
      this.guiSettingsView.reset();
      if (inspectedModule == null) {
         List<Module> mods = this.getFilteredModules(activeCategory, searchQuery);
         if (!mods.isEmpty()) {
            this.setInspectedModule(mods.get(0));
         } else {
            List<Module> all = ModuleManager.INSTANCE.getModules();
            if (!all.isEmpty()) {
               this.setInspectedModule(all.get(0));
            }
         }
      } else {
         this.setInspectedModule(inspectedModule);
      }
   }

   public static ClickGuiScreen getInstance() {
      if (instance == null) {
         instance = new ClickGuiScreen();
      }

      return instance;
   }

   public List<CategoryPanel> getPanels() {
      return this.panels;
   }

   public ConfigPanel getConfigPanel() {
      return this.configPanel;
   }

   public void showModules() {
      activeView = ClickGuiScreen.View.MODULES;
      this.searchFocused = false;
      this.configDockView.blur();
      this.friendsView.blur();
   }

   public void initPanels() {
      this.panels.clear();
      int startX = 15;
      int startY = 36;
      int panelWidth = 148;
      int spacing = 156;

      for (int i = 0; i < DISPLAY_CATEGORIES.length; i++) {
         this.panels.add(new CategoryPanel(DISPLAY_CATEGORIES[i], startX + i * spacing, startY, panelWidth));
      }

      this.configPanel = new ConfigPanel(startX + DISPLAY_CATEGORIES.length * spacing, startY, panelWidth);
      this.configPanel.refresh();
   }

   public void resetPanelPositions() {
      modulesScroll = 0.0F;
      searchQuery = "";
      this.searchFocused = false;
      this.inspectorScrollContainer.resetScroll();
      int startX = 15;
      int startY = 36;
      int spacing = 156;

      for (CategoryPanel panel : this.panels) {
         int idx = -1;

         for (int i = 0; i < DISPLAY_CATEGORIES.length; i++) {
            if (DISPLAY_CATEGORIES[i] == panel.getCategory()) {
               idx = i;
               break;
            }
         }

         if (idx >= 0) {
            panel.setX(startX + idx * spacing);
            panel.setY(startY);
         }
      }

      if (this.configPanel != null) {
         this.configPanel.setEmbedded(false, 200);
         this.configPanel.setX(startX + DISPLAY_CATEGORIES.length * spacing);
         this.configPanel.setY(startY);
         this.configPanel.setWidth(148);
      }
   }

   public int getHeaderColor() {
      return GuiTheme.current().headerColor;
   }

   public int getFontColor() {
      return GuiTheme.current().fontColor;
   }

   public int getAccentColor() {
      return GuiTheme.current().accentColor;
   }

   public float getScale() {
      float rawScale = ClickGuiSettings.scale.getValue().floatValue();
      return Math.max(0.7F, Math.min(1.3F, rawScale));
   }

   private static float easeOutCubic(float x) {
      return 1.0F - (float)Math.pow(1.0F - x, 3.0);
   }

   public float getTotalScale() {
      float eased = easeOutCubic(this.openProgress);
      return this.getScale() * (0.88F + 0.12F * eased);
   }

   private int sx(double screenX) {
      return (int)(screenX / this.getTotalScale());
   }

   private int sy(double screenY) {
      return (int)(screenY / this.getTotalScale());
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      this.lastMouseX = mouseX;
      this.lastMouseY = mouseY;
      if (Renderer2D.isForceVulkan()) {
         this.updateFrameTime();
         Renderer2D.getBlaze3D().setGraphics(graphics);
         this.renderGuiInternal(this.width, this.height);
      }
   }

   private void updateFrameTime() {
      long now = System.nanoTime();
      if (this.lastFrameTime == 0L) {
         lastDt = 0.016F;
      } else {
         float dt = (float)(now - this.lastFrameTime) / 1.0E9F;
         lastDt = Math.max(0.001F, Math.min(0.05F, dt));
      }

      this.lastFrameTime = now;
      if (this.openProgress < 1.0F) {
         this.openProgress = this.openProgress + (1.0F - this.openProgress) * (1.0F - (float)Math.exp(-20.0 * lastDt));
         if (this.openProgress > 0.995F) {
            this.openProgress = 1.0F;
         }
      }
   }

   public void renderMobile() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getWindow() != null) {
         float screenW = mc.getWindow().getGuiScaledWidth();
         float screenH = mc.getWindow().getGuiScaledHeight();
         this.updateFrameTime();
         if (mc.mouseHandler != null && mc.getWindow().getScreenWidth() > 0 && mc.getWindow().getScreenHeight() > 0) {
            this.lastMouseX = (int)(mc.mouseHandler.xpos() * screenW / mc.getWindow().getScreenWidth());
            this.lastMouseY = (int)(mc.mouseHandler.ypos() * screenH / mc.getWindow().getScreenHeight());
         }

         MobileDraw2D draw2D = MobileDraw2D.getInstance();
         if (ClickGuiSettings.background.isMode("Blur") || ClickGuiSettings.background.isMode("Liquid Glass") || draw2D.needsBlur()) {
            try {
               draw2D.computeBlur();
            } catch (Throwable var6) {
            }
         }

         this.renderGuiInternal(screenW, screenH);
      }
   }

   private float anim(String key, float target, float speed) {
      float current = this.animMap.getOrDefault(key, target);
      float dt = lastDt;
      float factor = 1.0F - (float)Math.exp(-speed * dt);
      current += (target - current) * factor;
      if (Math.abs(target - current) < 0.002F) {
         current = target;
      }

      this.animMap.put(key, current);
      return current;
   }

   private void renderGuiInternal(float screenW, float screenH) {
      float totalScale = this.getTotalScale();
      IRenderer2D r2d = Renderer2D.get();
      r2d.begin(screenW, screenH, totalScale, this.openProgress);
      float w = screenW / totalScale;
      float h = screenH / totalScale;
      float mX = this.lastMouseX / totalScale;
      float mY = this.lastMouseY / totalScale;
      this.lastScaleW = w;
      this.lastScaleH = h;
      this.renderBackground(w, h);
      this.renderDecorations(w, h);
      this.renderSidebar(mX, mY, w, h);
      this.renderTopBar(mX, mY, w, h);
      String[] descOut = new String[]{""};
      int headerColor = this.getHeaderColor();
      int fontColor = this.getFontColor();
      int accentColor = this.getAccentColor();
      switch (activeView) {
         case MODULES:
            this.renderModulesView(mX, mY, descOut, w, h);
            break;
         case CONFIG:
            this.configDockView.render(mX, mY, headerColor, fontColor, accentColor, descOut, w, h);
            break;
         case FRIENDS:
            this.friendsView.render(mX, mY, headerColor, fontColor, accentColor, descOut, w, h);
            break;
         case SETTINGS:
            this.guiSettingsView.render(mX, mY, headerColor, fontColor, accentColor, descOut, w, h);
      }

      this.hoveredDescription = descOut[0];
      this.renderTooltip(mX, mY, headerColor, fontColor, w, h);
      r2d.end();
   }

   private void renderBackground(float w, float h) {
      String bgMode = ClickGuiSettings.background.getValue();
      if ("Dim".equalsIgnoreCase(bgMode)) {
         NvgUtil.fillRect(0.0F, 0.0F, w, h, 1711276032);
      }
   }

   private void renderDecorations(float w, float h) {
      GuiTheme theme = GuiTheme.current();
      int accent = theme.accentColor | 0xFF000000;
      float arcEndX = w - 18.0F;
      float arcEndY = h - 42.0F;
      NvgUtil.drawLine(w - 75.0F, h, arcEndX, arcEndY, 1.2F, 587202559);
      UIIcon.drawStar(arcEndX, arcEndY, 12.0F, NvgUtil.withAlpha(accent, 128));
   }

   private void renderSidebar(float mx, float my, float w, float h) {
      GuiTheme theme = GuiTheme.current();
      float trans = Math.max(0.1F, Math.min(1.0F, ClickGuiSettings.transparency.getValueFloat()));
      int fontColor = theme.fontColor | 0xFF000000;
      int accent = theme.accentColor | 0xFF000000;
      float sx = 10.0F;
      float sy = 10.0F;
      float sw = 108.0F;
      float sh = h - 20.0F;
      float r = GuiTheme.radius() > 0.0F ? 12.0F : 0.0F;
      int sidebarBg = NvgUtil.withAlpha(theme.sidebarBg, Math.round(242.0F * trans));
      NvgUtil.fillRounded(sx, sy, sw, sh, r, sidebarBg);
      NvgUtil.strokeRounded(sx - 0.5F, sy - 0.5F, sw + 1.0F, sh + 1.0F, r, 1.0F, 452984831);
      if (this.logoTexId <= 0) {
         this.logoTexId = NvgUtil.loadTexture("/assets/kuroplus/textures/logo.png");
      }

      float logoSize = 34.0F;
      float logoX = sx + (sw - logoSize) * 0.5F;
      float logoY = sy + 14.0F;
      if (this.logoTexId > 0) {
         NvgUtil.drawImage(this.logoTexId, logoX, logoY, logoSize, logoSize, this.openProgress);
      } else {
         NvgUtil.drawText("K", logoX + 5.0F, logoY + 2.0F, 24.0F, fontColor, 9);
         UIIcon.drawPlus(logoX + 24.0F, logoY + 12.0F, 9.0F, accent);
      }

      NvgUtil.drawText("Kuro+", sx + sw * 0.5F, logoY + logoSize + 4.0F, 10.5F, fontColor, 10);
      String[] tabLabels = new String[]{"Modules", "Combat", "Movement", "Render", "World", "Misc", "HUD", "Configs", "Friends"};
      float tabW = 92.0F;
      float tabH = 22.0F;
      float tabX = sx + (sw - tabW) * 0.5F;
      float startTabY = sy + 68.0F;
      float tabGap = 3.0F;

      for (int i = 0; i < tabLabels.length; i++) {
         float ty = startTabY + i * (tabH + tabGap);
         boolean active = this.isSidebarTabActive(i);
         boolean hovered = mx >= tabX && mx <= tabX + tabW && my >= ty && my <= ty + tabH;
         if (active) {
            NvgUtil.fillRounded(tabX, ty, tabW, tabH, 5.0F, NvgUtil.withAlpha(accent, 42));
            NvgUtil.strokeRounded(tabX - 0.5F, ty - 0.5F, tabW + 1.0F, tabH + 1.0F, 5.0F, 1.0F, NvgUtil.withAlpha(accent, 96));
            NvgUtil.fillRounded(tabX + 2.0F, ty + 4.0F, 2.5F, tabH - 8.0F, 1.25F, accent);
         } else if (hovered) {
            int hoverBg = theme.rowHover != 0 ? theme.rowHover : 352321535;
            NvgUtil.fillRounded(tabX, ty, tabW, tabH, 5.0F, hoverBg);
         }

         int iconCol = active ? accent : (hovered ? fontColor : theme.dimText);
         float iconCx = tabX + 12.0F;
         float iconCy = ty + tabH * 0.5F;
         switch (i) {
            case 0:
               UIIcon.drawCube(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 1:
               UIIcon.drawSword(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 2:
               UIIcon.drawMovement(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 3:
               UIIcon.drawMonitor(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 4:
               UIIcon.drawGlobe(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 5:
               UIIcon.drawSliders(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 6:
               UIIcon.drawBarChart(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 7:
               UIIcon.drawFolder(iconCx, iconCy, 10.0F, iconCol);
               break;
            case 8:
               UIIcon.drawPerson(iconCx, iconCy, 10.0F, iconCol);
         }

         int textCol = active ? fontColor : (hovered ? fontColor : theme.dimText);
         NvgUtil.drawText(tabLabels[i], tabX + 25.0F, ty + 5.5F, 9.0F, textCol, 9);
      }

      float statusY = sy + sh - 48.0F;
      NvgUtil.fillCircle(sx + 20.0F, statusY + 4.0F, 2.5F, -11870592);
      NvgUtil.drawText("Ready.", sx + 27.0F, statusY, 8.5F, theme.dimText, 9);
      float gearX = sx + 24.0F;
      float gearY = sy + sh - 20.0F;
      boolean gearHover = Math.hypot(mx - gearX, my - gearY) <= 10.0;
      boolean gearActive = activeView == ClickGuiScreen.View.SETTINGS;
      UIIcon.drawGear(gearX, gearY, 12.0F, !gearActive && !gearHover ? theme.dimText : accent);
      float exitX = sx + sw - 24.0F;
      float exitY = sy + sh - 20.0F;
      boolean exitHover = Math.hypot(mx - exitX, my - exitY) <= 10.0;
      UIIcon.drawExit(exitX, exitY, 12.0F, exitHover ? fontColor : theme.dimText);
      NvgUtil.drawLine(sx + 4.0F, sy + sh - 14.0F, sx + sw * 0.45F, sy + sh - 26.0F, 1.0F, NvgUtil.withAlpha(accent, 36));
      NvgUtil.drawLine(sx + sw * 0.45F, sy + sh - 26.0F, sx + sw - 4.0F, sy + sh - 10.0F, 1.0F, 352321535);
   }

   private boolean isSidebarTabActive(int index) {
      if (activeView == ClickGuiScreen.View.CONFIG) {
         return index == 7;
      } else if (activeView == ClickGuiScreen.View.FRIENDS) {
         return index == 8;
      } else if (activeView == ClickGuiScreen.View.SETTINGS) {
         return false;
      } else if (activeView == ClickGuiScreen.View.MODULES) {
         if (activeCategory == null) {
            return index == 0;
         } else {
            return switch (activeCategory) {
               case COMBAT, BLATANT -> index == 1;
               case MOVEMENT -> index == 2;
               case RENDER -> index == 3;
               case WORLD -> index == 4;
               case MISC -> index == 5;
               case INTERFACE -> index == 6;
               default -> index == 0;
            };
         }
      } else {
         return false;
      }
   }

   private void renderTopBar(float mx, float my, float w, float h) {
      GuiTheme theme = GuiTheme.current();
      float trans = Math.max(0.1F, Math.min(1.0F, ClickGuiSettings.transparency.getValueFloat()));
      int fontColor = theme.fontColor | 0xFF000000;
      int accent = theme.accentColor | 0xFF000000;
      float barX = 126.0F;
      float barY = 10.0F;
      float barW = w - barX - 10.0F;
      float barH = 26.0F;
      float r = GuiTheme.radius() > 0.0F ? 7.0F : 0.0F;
      int barBg = NvgUtil.withAlpha(theme.sidebarBg, Math.round(222.0F * trans));
      NvgUtil.fillRounded(barX, barY, barW, barH, r, barBg);
      NvgUtil.strokeRounded(barX - 0.5F, barY - 0.5F, barW + 1.0F, barH + 1.0F, r, 1.0F, 419430399);
      UIIcon.drawStar(barX + 16.0F, barY + 13.0F, 9.0F, accent);
      NvgUtil.drawText("Kuro+", barX + 26.0F, barY + 7.5F, 10.0F, fontColor, 9);
      NvgUtil.drawText("|", barX + 58.0F, barY + 7.5F, 9.5F, theme.dimText, 9);
      NvgUtil.drawText("play better.", barX + 68.0F, barY + 8.0F, 9.0F, theme.dimText, 9);
      float minusX = barX + barW - 54.0F;
      float maxSquareX = barX + barW - 36.0F;
      float closeX = barX + barW - 18.0F;
      float btnY = barY + 13.0F;
      boolean minusHover = Math.hypot(mx - minusX, my - btnY) <= 8.0;
      boolean maxHover = Math.hypot(mx - maxSquareX, my - btnY) <= 8.0;
      boolean closeHover = Math.hypot(mx - closeX, my - btnY) <= 8.0;
      if (minusHover) {
         NvgUtil.fillRounded(minusX - 7.0F, barY + 5.0F, 14.0F, 16.0F, 3.5F, 486539263);
      }

      if (maxHover) {
         NvgUtil.fillRounded(maxSquareX - 7.0F, barY + 5.0F, 14.0F, 16.0F, 3.5F, 486539263);
      }

      if (closeHover) {
         NvgUtil.fillRounded(closeX - 7.0F, barY + 5.0F, 14.0F, 16.0F, 3.5F, 486539263);
      }

      UIIcon.drawMinus(minusX, btnY, 8.0F, minusHover ? fontColor : theme.dimText);
      UIIcon.drawMaximize(maxSquareX, btnY, 8.0F, maxHover ? fontColor : theme.dimText);
      UIIcon.drawClose(closeX, btnY, 8.0F, closeHover ? fontColor : theme.dimText);
   }

   private void renderModulesView(float mx, float my, String[] descOut, float w, float h) {
      GuiTheme theme = GuiTheme.current();
      float trans = Math.max(0.1F, Math.min(1.0F, ClickGuiSettings.transparency.getValueFloat()));
      int fontColor = theme.fontColor | 0xFF000000;
      int accent = theme.accentColor | 0xFF000000;
      float r = GuiTheme.radius() > 0.0F ? 10.0F : 0.0F;
      float contentX = 126.0F;
      float contentY = 44.0F;
      float contentW = w - contentX - 10.0F;
      float contentH = h - contentY - 10.0F;
      float modulesPanelW = Math.max(260.0F, (contentW - 12.0F) * 0.48F);
      float inspPanelW = contentW - modulesPanelW - 12.0F;
      float inspPanelX = contentX + modulesPanelW + 12.0F;
      int panelBg = NvgUtil.withAlpha(theme.panelBg, Math.round(236.0F * trans));
      int borderCol = NvgUtil.withAlpha(16777215, Math.round(24.0F * trans));
      NvgUtil.fillRounded(contentX, contentY, modulesPanelW, contentH, r, panelBg);
      NvgUtil.strokeRounded(contentX - 0.5F, contentY - 0.5F, modulesPanelW + 1.0F, contentH + 1.0F, r, 1.0F, borderCol);
      float catIconCx = contentX + 18.0F;
      float catIconCy = contentY + 16.0F;
      UIIcon.drawCube(catIconCx, catIconCy, 11.0F, accent);
      String catTitle = activeCategory == null
         ? "All Modules"
         : (activeCategory == Category.INTERFACE ? "HUD Modules" : activeCategory.getDisplayName() + " Modules");
      NvgUtil.drawText(catTitle, contentX + 32.0F, contentY + 10.0F, 11.0F, fontColor, 9);
      float searchW = Math.min(160.0F, modulesPanelW * 0.42F);
      float searchH = 20.0F;
      float searchX = contentX + modulesPanelW - searchW - 12.0F;
      float searchY = contentY + 8.0F;
      int searchBg = NvgUtil.withAlpha(theme.boxBg, Math.round(208.0F * trans));
      NvgUtil.fillRounded(searchX, searchY, searchW, searchH, 4.0F, searchBg);
      NvgUtil.strokeRounded(
         searchX - 0.5F, searchY - 0.5F, searchW + 1.0F, searchH + 1.0F, 4.0F, 1.0F, this.searchFocused ? NvgUtil.withAlpha(accent, 128) : 452984831
      );
      UIIcon.drawSearch(searchX + 9.0F, searchY + 10.0F, 7.5F, theme.dimText);
      if (searchQuery.isEmpty()) {
         NvgUtil.drawText("Search modules...", searchX + 20.0F, searchY + 5.5F, 8.5F, theme.dimText, 9);
      } else {
         NvgUtil.drawText(
            searchQuery + (this.searchFocused && System.currentTimeMillis() % 1000L < 500L ? "|" : ""), searchX + 20.0F, searchY + 5.5F, 8.5F, fontColor, 9
         );
         UIIcon.drawClose(searchX + searchW - 9.0F, searchY + 10.0F, 6.5F, theme.dimText);
      }

      String[] chipLabels = new String[]{"All", "Combat", "Movement", "Render", "World", "Misc", "HUD"};
      Category[] chipCats = new Category[]{null, Category.COMBAT, Category.MOVEMENT, Category.RENDER, Category.WORLD, Category.MISC, Category.INTERFACE};
      float chipY = contentY + 34.0F;
      float chipH = 17.0F;
      float curChipX = contentX + 12.0F;

      for (int i = 0; i < chipLabels.length; i++) {
         String label = chipLabels[i];
         float txtW = NvgUtil.textWidth(label, 8.5F);
         float chipW = txtW + 12.0F;
         boolean chipActive = activeCategory == chipCats[i];
         boolean chipHover = mx >= curChipX && mx <= curChipX + chipW && my >= chipY && my <= chipY + chipH;
         if (chipActive) {
            NvgUtil.fillRounded(curChipX, chipY, chipW, chipH, 4.0F, theme.activePillBg);
            NvgUtil.drawText(label, curChipX + chipW * 0.5F, chipY + 3.5F, 8.5F, theme.activePillText, 10);
         } else {
            int cBg = chipHover ? (theme.rowHover != 0 ? theme.rowHover : 352321535) : NvgUtil.withAlpha(theme.boxBg, 112);
            NvgUtil.fillRounded(curChipX, chipY, chipW, chipH, 4.0F, cBg);
            NvgUtil.drawText(label, curChipX + chipW * 0.5F, chipY + 3.5F, 8.5F, chipHover ? fontColor : theme.dimText, 10);
         }

         curChipX += chipW + 4.0F;
      }

      List<Module> filtered = this.getFilteredModules(activeCategory, searchQuery);
      float listX = contentX + 10.0F;
      float listY = contentY + 58.0F;
      float listW = modulesPanelW - 20.0F;
      float listH = contentH - 68.0F;
      float itemH = 34.0F;
      float itemGap = 3.5F;
      float totalListH = filtered.size() * (itemH + itemGap);
      float maxScroll = Math.max(0.0F, totalListH - listH);
      if (modulesScroll > maxScroll) {
         modulesScroll = maxScroll;
      }

      if (modulesScroll < 0.0F) {
         modulesScroll = 0.0F;
      }

      NvgUtil.scissor(listX, listY, listW, listH);

      for (int i = 0; i < filtered.size(); i++) {
         Module m = filtered.get(i);
         float iy = listY + i * (itemH + itemGap) - modulesScroll;
         if (!(iy + itemH < listY) && !(iy > listY + listH)) {
            boolean isInspected = m == inspectedModule;
            boolean isHovered = mx >= listX && mx <= listX + listW && my >= iy && my <= iy + itemH;
            if (isInspected) {
               NvgUtil.fillRounded(listX, iy, listW, itemH, 5.0F, NvgUtil.withAlpha(accent, 34));
               NvgUtil.strokeRounded(listX - 0.5F, iy - 0.5F, listW + 1.0F, itemH + 1.0F, 5.0F, 1.0F, NvgUtil.withAlpha(accent, 96));
            } else if (isHovered) {
               int hovBg = theme.rowHover != 0 ? theme.rowHover : 352321535;
               NvgUtil.fillRounded(listX, iy, listW, itemH, 5.0F, hovBg);
            } else {
               NvgUtil.fillRounded(listX, iy, listW, itemH, 5.0F, NvgUtil.withAlpha(theme.boxBg, 80));
            }

            NvgUtil.drawText(m.getName(), listX + 10.0F, iy + 6.0F, 10.0F, fontColor, 9);
            String desc = truncate(m.getDescription(), 8.0F, listW - 74.0F);
            NvgUtil.drawText(desc, listX + 10.0F, iy + 19.0F, 8.0F, theme.dimText, 9);
            float swW = 26.0F;
            float swH = 14.0F;
            float swX = listX + listW - swW - 8.0F;
            float swY = iy + (itemH - swH) * 0.5F;
            float swAnim = this.anim("sw_" + m.getName(), m.isEnabled() ? 1.0F : 0.0F, 18.0F);
            int trackCol = NvgUtil.lerpColor(NvgUtil.withAlpha(theme.boxBg, 144), accent, swAnim);
            NvgUtil.fillRounded(swX, swY, swW, swH, 7.0F, trackCol);
            NvgUtil.strokeRounded(swX - 0.5F, swY - 0.5F, swW + 1.0F, swH + 1.0F, 7.0F, 1.0F, NvgUtil.withAlpha(16777215, 32));
            float knobMinX = swX + 7.0F;
            float knobMaxX = swX + swW - 7.0F;
            float knobX = knobMinX + (knobMaxX - knobMinX) * swAnim;
            NvgUtil.fillCircle(knobX, swY + 7.0F, 5.0F, -1);
            if (m.getKey() > 0) {
               String keyStr = KeySetting.keyName(m.getKey());
               float kw = NvgUtil.textWidth(keyStr, 7.5F) + 8.0F;
               float kx = swX - kw - 6.0F;
               float ky = iy + (itemH - 13.0F) * 0.5F;
               NvgUtil.fillRounded(kx, ky, kw, 13.0F, 3.0F, NvgUtil.withAlpha(theme.boxBg, 128));
               NvgUtil.drawText(keyStr, kx + kw * 0.5F, ky + 2.0F, 7.5F, theme.dimText, 10);
            }

            if (isHovered && m.getDescription() != null && !m.getDescription().isEmpty()) {
               descOut[0] = m.getDescription();
            }
         }
      }

      NvgUtil.resetScissor();
      if (totalListH > listH) {
         float ratio = Math.min(1.0F, listH / totalListH);
         float thumbH = Math.max(18.0F, listH * ratio);
         float scrollPct = maxScroll > 0.0F ? modulesScroll / maxScroll : 0.0F;
         float thumbY = listY + (listH - thumbH) * scrollPct;
         NvgUtil.fillRounded(listX + listW - 3.0F, thumbY, 3.0F, thumbH, 1.5F, NvgUtil.withAlpha(accent, 128));
      }

      NvgUtil.fillRounded(inspPanelX, contentY, inspPanelW, contentH, r, panelBg);
      NvgUtil.strokeRounded(inspPanelX - 0.5F, contentY - 0.5F, inspPanelW + 1.0F, contentH + 1.0F, r, 1.0F, borderCol);
      if (inspectedModule != null) {
         NvgUtil.drawText(inspectedModule.getName(), inspPanelX + 16.0F, contentY + 11.0F, 12.5F, fontColor, 9);
         float nameW = NvgUtil.textWidth(inspectedModule.getName(), 12.5F);
         float badgeX = inspPanelX + 16.0F + nameW + 8.0F;
         float badgeY = contentY + 12.0F;
         String catBadge = inspectedModule.getCategory().getDisplayName().toUpperCase(Locale.ROOT);
         float catW = NvgUtil.textWidth(catBadge, 7.5F) + 8.0F;
         NvgUtil.fillRounded(badgeX, badgeY, catW, 13.0F, 3.0F, NvgUtil.withAlpha(accent, 37));
         NvgUtil.strokeRounded(badgeX, badgeY, catW, 13.0F, 3.0F, 1.0F, NvgUtil.withAlpha(accent, 96));
         NvgUtil.drawText(catBadge, badgeX + catW * 0.5F, badgeY + 1.5F, 7.5F, accent, 10);
         String fullDesc = inspectedModule.getDescription();
         if (fullDesc != null && !fullDesc.isEmpty()) {
            NvgUtil.drawText(fullDesc, inspPanelX + 16.0F, contentY + 28.0F, 8.5F, theme.dimText, 9);
         }

         float mswW = 30.0F;
         float mswH = 15.0F;
         float mswX = inspPanelX + inspPanelW - mswW - 14.0F;
         float mswY = contentY + 12.0F;
         float mSwAnim = this.anim("msw_" + inspectedModule.getName(), inspectedModule.isEnabled() ? 1.0F : 0.0F, 18.0F);
         int mTrackCol = NvgUtil.lerpColor(NvgUtil.withAlpha(theme.boxBg, 144), accent, mSwAnim);
         NvgUtil.fillRounded(mswX, mswY, mswW, mswH, 7.5F, mTrackCol);
         NvgUtil.strokeRounded(mswX - 0.5F, mswY - 0.5F, mswW + 1.0F, mswH + 1.0F, 7.5F, 1.0F, NvgUtil.withAlpha(16777215, 32));
         float mKnobMinX = mswX + 7.5F;
         float mKnobMaxX = mswX + mswW - 7.5F;
         float mKnobX = mKnobMinX + (mKnobMaxX - mKnobMinX) * mSwAnim;
         NvgUtil.fillCircle(mKnobX, mswY + 7.5F, 5.5F, -1);
         String kbText = bindingModule == inspectedModule
            ? "Press key..."
            : (inspectedModule.getKey() > 0 ? "Key: " + KeySetting.keyName(inspectedModule.getKey()) : "Bind: None");
         float kbW = NvgUtil.textWidth(kbText, 8.0F) + 12.0F;
         float kbH = 15.0F;
         float kbX = mswX - kbW - 8.0F;
         float kbY = contentY + 12.0F;
         int kbBg = bindingModule == inspectedModule ? NvgUtil.withAlpha(accent, 56) : NvgUtil.withAlpha(theme.boxBg, 144);
         int kbBorder = bindingModule == inspectedModule ? accent : 553648127;
         NvgUtil.fillRounded(kbX, kbY, kbW, kbH, 3.5F, kbBg);
         NvgUtil.strokeRounded(kbX - 0.5F, kbY - 0.5F, kbW + 1.0F, kbH + 1.0F, 3.5F, 1.0F, kbBorder);
         NvgUtil.drawText(kbText, kbX + kbW * 0.5F, kbY + 2.5F, 8.0F, bindingModule == inspectedModule ? accent : fontColor, 10);
         NvgUtil.drawLine(inspPanelX + 12.0F, contentY + 44.0F, inspPanelX + inspPanelW - 12.0F, contentY + 44.0F, 1.0F, 318767103);
         this.inspectorScrollContainer.setX(inspPanelX + 10.0F);
         this.inspectorScrollContainer.setY(contentY + 48.0F);
         this.inspectorScrollContainer.setWidth(inspPanelW - 20.0F);
         this.inspectorScrollContainer.setHeight(contentH - 56.0F);
         this.inspectorScrollContainer.layout();
         this.inspectorScrollContainer.render(mx, my);
         if (this.inspectorScrollContainer.getChildren().isEmpty()) {
            NvgUtil.drawText("No additional settings for this module.", inspPanelX + 16.0F, contentY + 70.0F, 9.0F, theme.dimText, 9);
         }
      } else {
         NvgUtil.drawText("Select a module to view its settings.", inspPanelX + 16.0F, contentY + 24.0F, 9.5F, theme.dimText, 9);
      }
   }

   private List<Module> getFilteredModules(Category category, String query) {
      List<Module> all = ModuleManager.INSTANCE.getModules();
      List<Module> result = new ArrayList<>();
      String cleanQ = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

      for (Module m : all) {
         if (category == null
            || (category == Category.COMBAT ? m.getCategory() == Category.COMBAT || m.getCategory() == Category.BLATANT : m.getCategory() == category)) {
            if (!cleanQ.isEmpty()) {
               String name = m.getName().toLowerCase(Locale.ROOT);
               String desc = m.getDescription() != null ? m.getDescription().toLowerCase(Locale.ROOT) : "";
               if (!name.contains(cleanQ) && !desc.contains(cleanQ)) {
                  continue;
               }
            }

            result.add(m);
         }
      }

      return result;
   }

   private void setInspectedModule(Module mod) {
      inspectedModule = mod;
      this.inspectorScrollContainer.clear();
      if (mod != null) {
         for (UIEntity entity : UISettingFactory.createEntitiesForModule(mod)) {
            entity.fillWidth(true);
            entity.margin(2.0F);
            this.inspectorScrollContainer.add(entity);
         }
      }

      this.inspectorScrollContainer.resetScroll();
   }

   private static String truncate(String text, float fontSize, float maxWidth) {
      if (text == null) {
         return "";
      } else if (NvgUtil.textWidth(text, fontSize) <= maxWidth) {
         return text;
      } else {
         String cur = text;

         while (cur.length() > 3 && NvgUtil.textWidth(cur + "...", fontSize) > maxWidth) {
            cur = cur.substring(0, cur.length() - 1);
         }

         return cur + "...";
      }
   }

   private void renderTooltip(float mX, float mY, int headerColor, int fontColor, float w, float h) {
      if (this.hoveredDescription != null && !this.hoveredDescription.isEmpty()) {
         String mode = ClickGuiSettings.descriptions.getValue();
         if (!"Off".equalsIgnoreCase(mode)) {
            float size = 9.0F;
            float textW = NvgUtil.textWidth(this.hoveredDescription, size);
            float pad = 4.0F;
            float boxW = textW + pad * 2.0F;
            float boxH = size + pad * 1.5F;
            float x = mX + 10.0F;
            float y = mY + 10.0F;
            if (x + boxW > w - 4.0F) {
               x = mX - boxW - 4.0F;
            }

            if (y + boxH > h - 4.0F) {
               y = mY - boxH - 4.0F;
            }

            GuiTheme theme = GuiTheme.current();
            int tipBg = NvgUtil.withAlpha(theme.boxBg, 238);
            NvgUtil.fillRounded(x, y, boxW, boxH, 3.0F, tipBg);
            NvgUtil.strokeRounded(x, y, boxW, boxH, 3.0F, 1.0F, 587202559);
            NvgUtil.drawText(this.hoveredDescription, x + pad, y + pad * 0.75F, size, fontColor | 0xFF000000, 9);
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent scaled = this.scaledEvent(event);
      float mx = (float)scaled.x();
      float my = (float)scaled.y();
      int btn = event.buttonInfo().button();
      float barX = 126.0F;
      float barY = 10.0F;
      float barW = this.lastScaleW - barX - 10.0F;
      if (my >= barY && my <= barY + 26.0F) {
         if (mx >= barX + barW - 24.0F && mx <= barX + barW) {
            this.onClose();
            return true;
         }

         if (mx >= barX + barW - 44.0F && mx <= barX + barW - 26.0F) {
            this.resetPanelPositions();
            return true;
         }

         if (mx >= barX + barW - 64.0F && mx <= barX + barW - 46.0F) {
            this.onClose();
            return true;
         }
      }

      float sx = 10.0F;
      float sy = 10.0F;
      float sw = 108.0F;
      float sh = this.lastScaleH - 20.0F;
      if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + sh) {
         float startTabY = sy + 68.0F;
         float tabH = 22.0F;
         float tabGap = 3.0F;

         for (int i = 0; i < 9; i++) {
            float ty = startTabY + i * (tabH + tabGap);
            if (my >= ty && my <= ty + tabH) {
               this.handleSidebarTabClick(i);
               return true;
            }
         }

         if (Math.hypot(mx - (sx + 24.0F), my - (sy + sh - 20.0F)) <= 12.0) {
            activeView = ClickGuiScreen.View.SETTINGS;
            this.searchFocused = false;
            return true;
         } else if (Math.hypot(mx - (sx + sw - 24.0F), my - (sy + sh - 20.0F)) <= 12.0) {
            this.onClose();
            return true;
         } else {
            return true;
         }
      } else if (activeView == ClickGuiScreen.View.CONFIG) {
         return this.configDockView.mouseClicked(scaled, doubleClick) || super.mouseClicked(event, doubleClick);
      } else if (activeView == ClickGuiScreen.View.FRIENDS) {
         return this.friendsView.mouseClicked(scaled, doubleClick) || super.mouseClicked(event, doubleClick);
      } else if (activeView != ClickGuiScreen.View.SETTINGS) {
         return activeView == ClickGuiScreen.View.MODULES && this.handleModulesViewClick(mx, my, btn, scaled) ? true : super.mouseClicked(event, doubleClick);
      } else {
         return this.guiSettingsView.mouseClicked(scaled, doubleClick) || super.mouseClicked(event, doubleClick);
      }
   }

   private void handleSidebarTabClick(int idx) {
      this.searchFocused = false;
      this.configDockView.blur();
      this.friendsView.blur();
      switch (idx) {
         case 0:
            this.showModules();
            activeCategory = null;
            modulesScroll = 0.0F;
            this.autoSelectFirstModule();
            break;
         case 1:
            this.showModules();
            activeCategory = Category.COMBAT;
            modulesScroll = 0.0F;
            this.autoSelectFirstModule();
            break;
         case 2:
            this.showModules();
            activeCategory = Category.MOVEMENT;
            modulesScroll = 0.0F;
            this.autoSelectFirstModule();
            break;
         case 3:
            this.showModules();
            activeCategory = Category.RENDER;
            modulesScroll = 0.0F;
            this.autoSelectFirstModule();
            break;
         case 4:
            this.showModules();
            activeCategory = Category.WORLD;
            modulesScroll = 0.0F;
            this.autoSelectFirstModule();
            break;
         case 5:
            this.showModules();
            activeCategory = Category.MISC;
            modulesScroll = 0.0F;
            this.autoSelectFirstModule();
            break;
         case 6:
            this.showModules();
            activeCategory = Category.INTERFACE;
            modulesScroll = 0.0F;
            this.autoSelectFirstModule();
            break;
         case 7:
            activeView = ClickGuiScreen.View.CONFIG;
            this.configDockView.activate();
            break;
         case 8:
            activeView = ClickGuiScreen.View.FRIENDS;
            this.friendsView.activate();
      }
   }

   private void autoSelectFirstModule() {
      List<Module> mods = this.getFilteredModules(activeCategory, searchQuery);
      if (!mods.isEmpty() && (inspectedModule == null || !mods.contains(inspectedModule))) {
         this.setInspectedModule(mods.get(0));
      }
   }

   private boolean handleModulesViewClick(float mx, float my, int btn, MouseButtonEvent scaled) {
      float contentX = 126.0F;
      float contentY = 44.0F;
      float contentW = this.lastScaleW - contentX - 10.0F;
      float contentH = this.lastScaleH - contentY - 10.0F;
      float modulesPanelW = Math.max(260.0F, (contentW - 12.0F) * 0.48F);
      float inspPanelW = contentW - modulesPanelW - 12.0F;
      float inspPanelX = contentX + modulesPanelW + 12.0F;
      float searchW = Math.min(160.0F, modulesPanelW * 0.42F);
      float searchH = 20.0F;
      float searchX = contentX + modulesPanelW - searchW - 12.0F;
      float searchY = contentY + 8.0F;
      if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
         if (!searchQuery.isEmpty() && mx >= searchX + searchW - 18.0F) {
            searchQuery = "";
            modulesScroll = 0.0F;
            this.searchFocused = false;
            this.autoSelectFirstModule();
         } else {
            this.searchFocused = true;
         }

         return true;
      } else {
         this.searchFocused = false;
         String[] chipLabels = new String[]{"All", "Combat", "Movement", "Render", "World", "Misc", "HUD"};
         Category[] chipCats = new Category[]{null, Category.COMBAT, Category.MOVEMENT, Category.RENDER, Category.WORLD, Category.MISC, Category.INTERFACE};
         float chipY = contentY + 34.0F;
         float chipH = 17.0F;
         float curChipX = contentX + 12.0F;

         for (int i = 0; i < chipLabels.length; i++) {
            String label = chipLabels[i];
            float txtW = NvgUtil.textWidth(label, 8.5F);
            float chipW = txtW + 12.0F;
            if (mx >= curChipX && mx <= curChipX + chipW && my >= chipY && my <= chipY + chipH) {
               activeCategory = chipCats[i];
               modulesScroll = 0.0F;
               this.autoSelectFirstModule();
               return true;
            }

            curChipX += chipW + 4.0F;
         }

         List<Module> filtered = this.getFilteredModules(activeCategory, searchQuery);
         float listX = contentX + 10.0F;
         float listY = contentY + 58.0F;
         float listW = modulesPanelW - 20.0F;
         float listH = contentH - 68.0F;
         float itemH = 34.0F;
         float itemGap = 3.5F;
         if (mx >= listX && mx <= listX + listW && my >= listY && my <= listY + listH) {
            for (int i = 0; i < filtered.size(); i++) {
               Module m = filtered.get(i);
               float iy = listY + i * (itemH + itemGap) - modulesScroll;
               if (!(iy + itemH < listY) && !(iy > listY + listH) && my >= iy && my <= iy + itemH) {
                  float swW = 26.0F;
                  float swX = listX + listW - swW - 8.0F;
                  if (mx >= swX - 4.0F && mx <= swX + swW + 4.0F) {
                     m.toggle();
                     return true;
                  }

                  this.setInspectedModule(m);
                  return true;
               }
            }

            return true;
         } else if (mx >= inspPanelX && mx <= inspPanelX + inspPanelW && my >= contentY && my <= contentY + contentH) {
            if (inspectedModule != null) {
               float mswW = 30.0F;
               float mswH = 15.0F;
               float mswX = inspPanelX + inspPanelW - mswW - 14.0F;
               float mswY = contentY + 12.0F;
               if (mx >= mswX - 4.0F && mx <= mswX + mswW + 4.0F && my >= mswY - 2.0F && my <= mswY + mswH + 2.0F) {
                  inspectedModule.toggle();
                  return true;
               }

               float kbW = 60.0F;
               float kbX = mswX - kbW - 8.0F;
               float kbY = contentY + 12.0F;
               if (mx >= kbX && mx <= mswX - 8.0F && my >= kbY && my <= kbY + 15.0F) {
                  bindingModule = bindingModule == inspectedModule ? null : inspectedModule;
                  return true;
               }

               if (this.inspectorScrollContainer.mouseClicked(scaled)) {
                  return true;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      MouseButtonEvent scaled = this.scaledEvent(event);
      if (activeView == ClickGuiScreen.View.CONFIG) {
         this.configDockView.mouseReleased(scaled);
         return super.mouseReleased(event);
      } else if (activeView == ClickGuiScreen.View.FRIENDS) {
         this.friendsView.mouseReleased(scaled);
         return super.mouseReleased(event);
      } else if (activeView == ClickGuiScreen.View.SETTINGS) {
         this.guiSettingsView.mouseReleased(scaled);
         return super.mouseReleased(event);
      } else {
         if (activeView == ClickGuiScreen.View.MODULES) {
            this.inspectorScrollContainer.mouseReleased(scaled);
         }

         return super.mouseReleased(event);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      MouseButtonEvent scaled = this.scaledEvent(event);
      if (activeView == ClickGuiScreen.View.CONFIG) {
         return this.configDockView.mouseDragged(scaled, dragX, dragY) || super.mouseDragged(event, dragX, dragY);
      } else if (activeView == ClickGuiScreen.View.FRIENDS) {
         return this.friendsView.mouseDragged(scaled, dragX, dragY) || super.mouseDragged(event, dragX, dragY);
      } else if (activeView != ClickGuiScreen.View.SETTINGS) {
         return activeView == ClickGuiScreen.View.MODULES && this.inspectorScrollContainer.mouseDragged(scaled, dragX, dragY)
            ? true
            : super.mouseDragged(event, dragX, dragY);
      } else {
         return this.guiSettingsView.mouseDragged(scaled, dragX, dragY) || super.mouseDragged(event, dragX, dragY);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      float mx = (float)(mouseX / this.getTotalScale());
      float my = (float)(mouseY / this.getTotalScale());
      if (activeView == ClickGuiScreen.View.CONFIG) {
         return this.configDockView.mouseScrolled(mx, my, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      } else if (activeView == ClickGuiScreen.View.FRIENDS) {
         return this.friendsView.mouseScrolled(mx, my, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      } else if (activeView != ClickGuiScreen.View.SETTINGS) {
         if (activeView == ClickGuiScreen.View.MODULES) {
            float contentX = 126.0F;
            float contentY = 44.0F;
            float contentW = this.lastScaleW - contentX - 10.0F;
            float contentH = this.lastScaleH - contentY - 10.0F;
            float modulesPanelW = Math.max(260.0F, (contentW - 12.0F) * 0.48F);
            if (mx >= contentX && mx <= contentX + modulesPanelW && my >= contentY + 58.0F && my <= contentY + contentH) {
               float speed = ClickGuiSettings.scrollSpeed.getValue().floatValue() * 3.0F;
               modulesScroll -= (float)scrollY * speed;
               return true;
            }

            if (this.inspectorScrollContainer.contains(mx, my)) {
               return this.inspectorScrollContainer.mouseScrolled(mx, my, scrollY);
            }
         }

         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      } else {
         return this.guiSettingsView.mouseScrolled(mx, my, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (bindingModule == null) {
         if (activeView == ClickGuiScreen.View.CONFIG && this.configDockView.keyPressed(event)) {
            return true;
         } else if (activeView == ClickGuiScreen.View.FRIENDS && this.friendsView.keyPressed(event)) {
            return true;
         } else if (activeView == ClickGuiScreen.View.SETTINGS && this.guiSettingsView.keyPressed(event)) {
            return true;
         } else {
            if (activeView == ClickGuiScreen.View.MODULES) {
               if (this.searchFocused) {
                  if (event.isPaste()) {
                     String pasted = GuiClipboard.getSanitized(30);
                     if (!pasted.isEmpty()) {
                        searchQuery = searchQuery + pasted;
                        modulesScroll = 0.0F;
                        this.autoSelectFirstModule();
                     }

                     return true;
                  }

                  if (event.key() == 259) {
                     if (!searchQuery.isEmpty()) {
                        searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                        modulesScroll = 0.0F;
                        this.autoSelectFirstModule();
                     }

                     return true;
                  }

                  if (event.key() != 256 && event.key() != 257) {
                     return true;
                  }

                  this.searchFocused = false;
                  return true;
               }

               if (this.inspectorScrollContainer.keyPressed(event)) {
                  return true;
               }
            }

            return super.keyPressed(event);
         }
      } else {
         if (event.key() != 256 && event.key() != 261 && event.key() != 259) {
            bindingModule.setKey(event.key());
         } else {
            bindingModule.setKey(0);
         }

         bindingModule = null;
         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      if (activeView == ClickGuiScreen.View.CONFIG && this.configDockView.charTyped(event)) {
         return true;
      } else if (activeView == ClickGuiScreen.View.FRIENDS && this.friendsView.charTyped(event)) {
         return true;
      } else if (activeView == ClickGuiScreen.View.SETTINGS && this.guiSettingsView.charTyped(event)) {
         return true;
      } else {
         if (activeView == ClickGuiScreen.View.MODULES) {
            if (this.searchFocused && event.isAllowedChatCharacter() && searchQuery.length() < 30) {
               searchQuery = searchQuery + event.codepointAsString();
               modulesScroll = 0.0F;
               this.autoSelectFirstModule();
               return true;
            }

            if (this.inspectorScrollContainer.charTyped(event)) {
               return true;
            }
         }

         return super.charTyped(event);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return ClickGuiSettings.pauseGame.isMode("Pause");
   }

   private MouseButtonEvent scaledEvent(MouseButtonEvent event) {
      return new MouseButtonEvent(event.x() / this.getTotalScale(), event.y() / this.getTotalScale(), event.buttonInfo());
   }

   public static enum View {
      MODULES,
      CONFIG,
      FRIENDS,
      SETTINGS;
   }
}
