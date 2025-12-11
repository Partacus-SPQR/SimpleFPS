package com.simplefps.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback config screen using vanilla Minecraft widgets.
 * Used when Cloth Config is not available.
 * 
 * Features:
 * - Scrollable layout that adapts to screen size
 * - Sliders with labels inside
 * - Tooltips for all options
 * - Reset buttons for all options
 * - Bottom buttons: Save & Close | Key Binds | Cancel
 */
public class SimpleFPSConfigScreen extends Screen {
	private final Screen parent;
	private final SimpleFPSConfig config;
	
	// Layout constants
	private static final int ROW_HEIGHT = 24;
	private static final int WIDGET_WIDTH = 180;
	private static final int RESET_BTN_WIDTH = 40;
	private static final int TOTAL_WIDTH = WIDGET_WIDTH + RESET_BTN_WIDTH + 4;
	
	// Scrolling
	private int scrollOffset = 0;
	private int maxScrollOffset = 0;
	private static final int HEADER_HEIGHT = 28;
	private static final int FOOTER_HEIGHT = 58;
	
	// All config rows for scrolling and rendering
	private final List<ConfigRow> configRows = new ArrayList<>();
	
	// Widget references for saving
	private TextFieldWidget textColorField;
	private TextFieldWidget bgColorField;
	
	public SimpleFPSConfigScreen(Screen parent) {
		super(Text.literal("SimpleFPS Configuration"));
		this.parent = parent;
		this.config = SimpleFPSConfig.getInstance();
	}
	
	@Override
	protected void init() {
		super.init();
		configRows.clear();
		
		int centerX = this.width / 2;
		int widgetX = centerX - TOTAL_WIDTH / 2;
		int resetX = widgetX + WIDGET_WIDTH + 4;
		
		// Calculate visible area
		int visibleHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
		int startY = HEADER_HEIGHT;
		
		// ============ BUILD CONFIG ROWS ============
		int rowIndex = 0;
		
		// === GENERAL SECTION ===
		configRows.add(new ConfigRow("§e§l[ General ]", rowIndex++, true));
		
		// Enabled toggle
		ButtonWidget enabledBtn = ButtonWidget.builder(
			Text.literal("Enabled: " + (config.enabled ? "ON" : "OFF")),
			button -> {
				config.enabled = !config.enabled;
				button.setMessage(Text.literal("Enabled: " + (config.enabled ? "ON" : "OFF")));
			}
		).dimensions(widgetX, 0, WIDGET_WIDTH, 20).build();
		
		ButtonWidget enabledReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.enabled = true;
				enabledBtn.setMessage(Text.literal("Enabled: ON"));
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(enabledBtn, enabledReset, 
			"Toggle the FPS counter visibility. (Default: ON)", rowIndex++));
		
		// Show Label toggle
		ButtonWidget showLabelBtn = ButtonWidget.builder(
			Text.literal("Show Label: " + (config.showLabel ? "ON" : "OFF")),
			button -> {
				config.showLabel = !config.showLabel;
				button.setMessage(Text.literal("Show Label: " + (config.showLabel ? "ON" : "OFF")));
			}
		).dimensions(widgetX, 0, WIDGET_WIDTH, 20).build();
		
		ButtonWidget showLabelReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.showLabel = true;
				showLabelBtn.setMessage(Text.literal("Show Label: ON"));
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(showLabelBtn, showLabelReset,
			"Show 'FPS: ' label before the number. (Default: ON)", rowIndex++));
		
		// === APPEARANCE SECTION ===
		configRows.add(new ConfigRow("§e§l[ Appearance ]", rowIndex++, true));
		
		// Text Color
		textColorField = new TextFieldWidget(textRenderer, widgetX, 0, WIDGET_WIDTH - 55, 20, Text.literal(""));
		textColorField.setText(config.textColor);
		textColorField.setMaxLength(7);
		
		ButtonWidget textColorPick = ButtonWidget.builder(
			Text.literal("Pick"),
			button -> {
				client.setScreen(new ColorPickerScreen(this, config.textColor, color -> {
					config.textColor = color;
					textColorField.setText(color);
				}));
			}
		).dimensions(widgetX + WIDGET_WIDTH - 50, 0, 50, 20).build();
		
		ButtonWidget textColorReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.textColor = "#FFFFFF";
				textColorField.setText("#FFFFFF");
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(textColorField, textColorPick, textColorReset,
			"The color of the FPS text in hex format. (Default: #FFFFFF)", rowIndex++));
		
		// Background toggle
		ButtonWidget showBackgroundBtn = ButtonWidget.builder(
			Text.literal("Background: " + (config.showBackground ? "ON" : "OFF")),
			button -> {
				config.showBackground = !config.showBackground;
				button.setMessage(Text.literal("Background: " + (config.showBackground ? "ON" : "OFF")));
			}
		).dimensions(widgetX, 0, WIDGET_WIDTH, 20).build();
		
		ButtonWidget showBackgroundReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.showBackground = true;
				showBackgroundBtn.setMessage(Text.literal("Background: ON"));
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(showBackgroundBtn, showBackgroundReset,
			"Show a background behind the FPS text. (Default: ON)", rowIndex++));
		
		// Background Color
		bgColorField = new TextFieldWidget(textRenderer, widgetX, 0, WIDGET_WIDTH - 55, 20, Text.literal(""));
		bgColorField.setText(config.backgroundColor);
		bgColorField.setMaxLength(7);
		
		ButtonWidget bgColorPick = ButtonWidget.builder(
			Text.literal("Pick"),
			button -> {
				client.setScreen(new ColorPickerScreen(this, config.backgroundColor, color -> {
					config.backgroundColor = color;
					bgColorField.setText(color);
				}));
			}
		).dimensions(widgetX + WIDGET_WIDTH - 50, 0, 50, 20).build();
		
		ButtonWidget bgColorReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.backgroundColor = "#000000";
				bgColorField.setText("#000000");
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(bgColorField, bgColorPick, bgColorReset,
			"The background color in hex format. (Default: #000000)", rowIndex++));
		
		// Text Size slider (0.5 to 2.0)
		FloatSlider textSizeSlider = new FloatSlider(widgetX, 0, WIDGET_WIDTH, 20, "Text Size", 0.5f, 2.0f, config.textSize, "x");
		ButtonWidget textSizeReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> textSizeSlider.setValue(1.0f)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(textSizeSlider, textSizeReset,
			"Scale of the FPS text. (Default: 1.0x)", rowIndex++));
		
		// Text Opacity slider
		IntSlider textOpacitySlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Text Opacity", 0, 100, config.textOpacity, "%");
		ButtonWidget textOpacityReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> textOpacitySlider.setValue(100, 0, 100)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(textOpacitySlider, textOpacityReset,
			"Opacity of the FPS text. (Default: 100%)", rowIndex++));
		
		// Background Opacity slider
		IntSlider bgOpacitySlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "BG Opacity", 0, 100, config.backgroundOpacity, "%");
		ButtonWidget bgOpacityReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> bgOpacitySlider.setValue(50, 0, 100)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(bgOpacitySlider, bgOpacityReset,
			"Opacity of the background. (Default: 50%)", rowIndex++));
		
		// === POSITION SECTION ===
		configRows.add(new ConfigRow("§e§l[ Position ]", rowIndex++, true));
		
		// Position X slider
		IntSlider posXSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Position X", 0, 500, config.positionX, " px");
		ButtonWidget posXReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> posXSlider.setValue(5, 0, 500)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(posXSlider, posXReset,
			"Horizontal position in pixels. (Default: 5)", rowIndex++));
		
		// Position Y slider
		IntSlider posYSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Position Y", 0, 500, config.positionY, " px");
		ButtonWidget posYReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> posYSlider.setValue(5, 0, 500)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(posYSlider, posYReset,
			"Vertical position in pixels. (Default: 5)", rowIndex++));
		
		// === ADAPTIVE COLORS SECTION ===
		configRows.add(new ConfigRow("§e§l[ Adaptive Colors ]", rowIndex++, true));
		
		// Adaptive toggle
		ButtonWidget adaptiveBtn = ButtonWidget.builder(
			Text.literal("Adaptive Colors: " + (config.adaptiveColorEnabled ? "ON" : "OFF")),
			button -> {
				config.adaptiveColorEnabled = !config.adaptiveColorEnabled;
				button.setMessage(Text.literal("Adaptive Colors: " + (config.adaptiveColorEnabled ? "ON" : "OFF")));
			}
		).dimensions(widgetX, 0, WIDGET_WIDTH, 20).build();
		
		ButtonWidget adaptiveReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.adaptiveColorEnabled = false;
				adaptiveBtn.setMessage(Text.literal("Adaptive Colors: OFF"));
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(adaptiveBtn, adaptiveReset,
			"Change text color based on FPS (red=low, green=high). (Default: OFF)", rowIndex++));
		
		// Low FPS Threshold slider
		IntSlider lowFpsSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Low FPS", 1, 120, config.lowFpsThreshold, " FPS");
		ButtonWidget lowFpsReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> lowFpsSlider.setValue(25, 1, 120)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(lowFpsSlider, lowFpsReset,
			"FPS at or below this shows as red. (Default: 25)", rowIndex++));
		
		// High FPS Threshold slider
		IntSlider highFpsSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "High FPS", 1, 300, config.highFpsThreshold, " FPS");
		ButtonWidget highFpsReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> highFpsSlider.setValue(60, 1, 300)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(highFpsSlider, highFpsReset,
			"FPS at or above this shows as green. (Default: 60)", rowIndex++));
		
		// === FPS GRAPH SECTION ===
		configRows.add(new ConfigRow("§e§l[ FPS Graph ]", rowIndex++, true));
		
		// Graph Enabled toggle
		ButtonWidget graphEnabledBtn = ButtonWidget.builder(
			Text.literal("Graph Enabled: " + (config.graphEnabled ? "ON" : "OFF")),
			button -> {
				config.graphEnabled = !config.graphEnabled;
				button.setMessage(Text.literal("Graph Enabled: " + (config.graphEnabled ? "ON" : "OFF")));
			}
		).dimensions(widgetX, 0, WIDGET_WIDTH, 20).build();
		
		ButtonWidget graphEnabledReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.graphEnabled = false;
				graphEnabledBtn.setMessage(Text.literal("Graph Enabled: OFF"));
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(graphEnabledBtn, graphEnabledReset,
			"Show a real-time FPS graph on screen. (Default: OFF)", rowIndex++));
		
		// Graph Background toggle
		ButtonWidget graphBgBtn = ButtonWidget.builder(
			Text.literal("Graph Background: " + (config.graphShowBackground ? "ON" : "OFF")),
			button -> {
				config.graphShowBackground = !config.graphShowBackground;
				button.setMessage(Text.literal("Graph Background: " + (config.graphShowBackground ? "ON" : "OFF")));
			}
		).dimensions(widgetX, 0, WIDGET_WIDTH, 20).build();
		
		ButtonWidget graphBgReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> {
				config.graphShowBackground = true;
				graphBgBtn.setMessage(Text.literal("Graph Background: ON"));
			}
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(graphBgBtn, graphBgReset,
			"Show a background behind the FPS graph. (Default: ON)", rowIndex++));
		
		// Graph X slider
		IntSlider graphXSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Graph X", 0, 500, config.graphX, " px");
		ButtonWidget graphXReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> graphXSlider.setValue(5, 0, 500)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(graphXSlider, graphXReset,
			"Graph horizontal position in pixels. (Default: 5)", rowIndex++));
		
		// Graph Y slider
		IntSlider graphYSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Graph Y", 0, 500, config.graphY, " px");
		ButtonWidget graphYReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> graphYSlider.setValue(100, 0, 500)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(graphYSlider, graphYReset,
			"Graph vertical position in pixels. (Default: 100)", rowIndex++));
		
		// Graph Scale slider
		IntSlider graphScaleSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Graph Scale", 50, 200, config.graphScale, "%");
		ButtonWidget graphScaleReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> graphScaleSlider.setValue(100, 50, 200)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(graphScaleSlider, graphScaleReset,
			"Scale of the FPS graph. (Default: 100%)", rowIndex++));
		
		// Graph Low FPS slider
		IntSlider graphLowFpsSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Graph Low", 1, 120, config.graphLowFpsThreshold, " FPS");
		ButtonWidget graphLowFpsReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> graphLowFpsSlider.setValue(30, 1, 120)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(graphLowFpsSlider, graphLowFpsReset,
			"Low FPS threshold for graph coloring. (Default: 30)", rowIndex++));
		
		// Graph High FPS slider
		IntSlider graphHighFpsSlider = new IntSlider(widgetX, 0, WIDGET_WIDTH, 20, "Graph High", 1, 300, config.graphHighFpsThreshold, " FPS");
		ButtonWidget graphHighFpsReset = ButtonWidget.builder(
			Text.literal("Reset"),
			button -> graphHighFpsSlider.setValue(60, 1, 300)
		).dimensions(resetX, 0, RESET_BTN_WIDTH, 20).build();
		
		configRows.add(new ConfigRow(graphHighFpsSlider, graphHighFpsReset,
			"High FPS threshold for graph coloring. (Default: 60)", rowIndex++));
		
		// Calculate max scroll offset
		int totalContentHeight = rowIndex * ROW_HEIGHT;
		maxScrollOffset = Math.max(0, totalContentHeight - visibleHeight);
		
		// Add all widgets to the screen
		for (ConfigRow row : configRows) {
			row.addWidgetsToScreen();
		}
		
		// === BOTTOM BUTTONS (Fixed at bottom, never scrolled) ===
		int bottomY = this.height - 28;
		int buttonGap = 5;
		int bottomButtonWidth = 90;
		int totalButtonsWidth = bottomButtonWidth * 3 + buttonGap * 2;
		int buttonStartX = centerX - totalButtonsWidth / 2;
		
		// Save & Close button (leftmost)
		addDrawableChild(ButtonWidget.builder(
			Text.literal("Save & Close"),
			button -> {
				saveSettings();
				close();
			}
		).dimensions(buttonStartX, bottomY, bottomButtonWidth, 20).build());
		
		// Key Binds shortcut button (middle)
		addDrawableChild(ButtonWidget.builder(
			Text.literal("Key Binds"),
			button -> {
				client.setScreen(new KeybindsScreen(this, client.options));
			}
		).dimensions(buttonStartX + bottomButtonWidth + buttonGap, bottomY, bottomButtonWidth, 20).build());
		
		// Cancel button (rightmost)
		addDrawableChild(ButtonWidget.builder(
			Text.literal("Cancel"),
			button -> close()
		).dimensions(buttonStartX + (bottomButtonWidth + buttonGap) * 2, bottomY, bottomButtonWidth, 20).build());
	}
	
	private void saveSettings() {
		// Save all settings from the config rows
		for (ConfigRow row : configRows) {
			row.saveToConfig(config);
		}
		config.save();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		int visibleTop = HEADER_HEIGHT;
		int visibleBottom = this.height - FOOTER_HEIGHT;
		
		// Enable scissor to clip content to visible area
		context.enableScissor(0, visibleTop, this.width, visibleBottom);
		
		// Render config rows with scroll offset
		String hoveredTooltip = null;
		for (ConfigRow row : configRows) {
			int rowY = HEADER_HEIGHT + row.rowIndex * ROW_HEIGHT - scrollOffset;
			
			// Only render if in visible area
			if (rowY + ROW_HEIGHT >= visibleTop && rowY < visibleBottom) {
				row.updatePositions(rowY);
				
				// Check tooltip
				if (mouseY >= visibleTop && mouseY < visibleBottom) {
					String tooltip = row.getTooltipIfHovered(mouseX, mouseY);
					if (tooltip != null) {
						hoveredTooltip = tooltip;
					}
				}
			} else {
				// Hide widgets that are out of view
				row.hide();
			}
			
			// Render section headers
			if (row.isSectionHeader) {
				if (rowY + ROW_HEIGHT >= visibleTop && rowY < visibleBottom) {
					context.drawCenteredTextWithShadow(textRenderer, Text.literal(row.headerText), 
						this.width / 2, rowY + 6, 0xFFFF55);
				}
			}
		}
		
		context.disableScissor();
		
		// Render title (above scissor area)
		context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
		
		// Render footer note
		String footerNote = "§7Using fallback config (install Cloth Config for full experience)";
		context.drawCenteredTextWithShadow(textRenderer, Text.literal(footerNote), this.width / 2, this.height - 48, 0x888888);
		
		// Render scroll indicator if needed
		if (maxScrollOffset > 0) {
			int scrollbarHeight = Math.max(20, (this.height - HEADER_HEIGHT - FOOTER_HEIGHT) * (this.height - HEADER_HEIGHT - FOOTER_HEIGHT) / (maxScrollOffset + this.height - HEADER_HEIGHT - FOOTER_HEIGHT));
			int scrollbarY = HEADER_HEIGHT + (int)((this.height - HEADER_HEIGHT - FOOTER_HEIGHT - scrollbarHeight) * ((float)scrollOffset / maxScrollOffset));
			context.fill(this.width - 6, HEADER_HEIGHT, this.width - 2, this.height - FOOTER_HEIGHT, 0x44FFFFFF);
			context.fill(this.width - 6, scrollbarY, this.width - 2, scrollbarY + scrollbarHeight, 0xAAFFFFFF);
		}
		
		// Render tooltip last (on top of everything)
		if (hoveredTooltip != null && !hoveredTooltip.isEmpty()) {
			context.drawTooltip(textRenderer, Text.literal(hoveredTooltip), mouseX, mouseY);
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScrollOffset > 0) {
			scrollOffset = (int) Math.max(0, Math.min(maxScrollOffset, scrollOffset - verticalAmount * 10));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	@Override
	public void close() {
		client.setScreen(parent);
	}
	
	// =====================================================================
	// CONFIG ROW CLASS
	// =====================================================================
	
	private class ConfigRow {
		final int rowIndex;
		final boolean isSectionHeader;
		final String headerText;
		final String tooltip;
		
		// For widget rows
		ButtonWidget widget1;
		ButtonWidget widget2;
		ButtonWidget widget3;
		TextFieldWidget textField;
		SliderWidget slider;
		
		// Section header constructor
		ConfigRow(String headerText, int rowIndex, boolean isSectionHeader) {
			this.headerText = headerText;
			this.rowIndex = rowIndex;
			this.isSectionHeader = isSectionHeader;
			this.tooltip = null;
		}
		
		// Button + Reset constructor
		ConfigRow(ButtonWidget button, ButtonWidget reset, String tooltip, int rowIndex) {
			this.widget1 = button;
			this.widget2 = reset;
			this.tooltip = tooltip;
			this.rowIndex = rowIndex;
			this.isSectionHeader = false;
			this.headerText = null;
		}
		
		// Slider + Reset constructor
		ConfigRow(SliderWidget slider, ButtonWidget reset, String tooltip, int rowIndex) {
			this.slider = slider;
			this.widget2 = reset;
			this.tooltip = tooltip;
			this.rowIndex = rowIndex;
			this.isSectionHeader = false;
			this.headerText = null;
		}
		
		// TextField + Pick + Reset constructor
		ConfigRow(TextFieldWidget textField, ButtonWidget pick, ButtonWidget reset, String tooltip, int rowIndex) {
			this.textField = textField;
			this.widget1 = pick;
			this.widget2 = reset;
			this.tooltip = tooltip;
			this.rowIndex = rowIndex;
			this.isSectionHeader = false;
			this.headerText = null;
		}
		
		void addWidgetsToScreen() {
			if (widget1 != null) addDrawableChild(widget1);
			if (widget2 != null) addDrawableChild(widget2);
			if (widget3 != null) addDrawableChild(widget3);
			if (textField != null) addDrawableChild(textField);
			if (slider != null) addDrawableChild(slider);
		}
		
		void updatePositions(int y) {
			if (widget1 != null) widget1.setY(y);
			if (widget2 != null) widget2.setY(y);
			if (widget3 != null) widget3.setY(y);
			if (textField != null) textField.setY(y);
			if (slider != null) slider.setY(y);
			
			// Make sure widgets are visible
			if (widget1 != null) widget1.visible = true;
			if (widget2 != null) widget2.visible = true;
			if (widget3 != null) widget3.visible = true;
			if (textField != null) textField.visible = true;
			if (slider != null) slider.visible = true;
		}
		
		void hide() {
			if (widget1 != null) widget1.visible = false;
			if (widget2 != null) widget2.visible = false;
			if (widget3 != null) widget3.visible = false;
			if (textField != null) textField.visible = false;
			if (slider != null) slider.visible = false;
		}
		
		String getTooltipIfHovered(int mouseX, int mouseY) {
			if (tooltip == null) return null;
			
			// Check if mouse is over any of our widgets
			if (isHovered(widget1, mouseX, mouseY)) return tooltip;
			if (isHovered(widget2, mouseX, mouseY)) return tooltip;
			if (isHovered(widget3, mouseX, mouseY)) return tooltip;
			if (isHovered(textField, mouseX, mouseY)) return tooltip;
			if (isHovered(slider, mouseX, mouseY)) return tooltip;
			
			return null;
		}
		
		private boolean isHovered(Object widget, int mouseX, int mouseY) {
			if (widget == null) return false;
			
			if (widget instanceof ButtonWidget btn) {
				return btn.visible && mouseX >= btn.getX() && mouseX < btn.getX() + btn.getWidth()
					&& mouseY >= btn.getY() && mouseY < btn.getY() + btn.getHeight();
			}
			if (widget instanceof TextFieldWidget tf) {
				return tf.visible && mouseX >= tf.getX() && mouseX < tf.getX() + tf.getWidth()
					&& mouseY >= tf.getY() && mouseY < tf.getY() + tf.getHeight();
			}
			if (widget instanceof SliderWidget sl) {
				return sl.visible && mouseX >= sl.getX() && mouseX < sl.getX() + sl.getWidth()
					&& mouseY >= sl.getY() && mouseY < sl.getY() + sl.getHeight();
			}
			return false;
		}
		
		void saveToConfig(SimpleFPSConfig config) {
			// Saving is handled by direct manipulation in button callbacks
			// and text field / slider getters - we just need to collect slider values
			if (slider instanceof IntSlider intSlider) {
				// Map sliders to config fields based on label
				String label = intSlider.getLabel();
				int value = intSlider.getIntValue();
				
				switch (label) {
					case "Text Opacity" -> config.textOpacity = value;
					case "BG Opacity" -> config.backgroundOpacity = value;
					case "Position X" -> config.positionX = value;
					case "Position Y" -> config.positionY = value;
					case "Low FPS" -> config.lowFpsThreshold = value;
					case "High FPS" -> config.highFpsThreshold = value;
					case "Graph X" -> config.graphX = value;
					case "Graph Y" -> config.graphY = value;
					case "Graph Scale" -> config.graphScale = value;
					case "Graph Low" -> config.graphLowFpsThreshold = value;
					case "Graph High" -> config.graphHighFpsThreshold = value;
				}
			} else if (slider instanceof FloatSlider floatSlider) {
				String label = floatSlider.getLabel();
				float value = floatSlider.getFloatValue();
				
				if ("Text Size".equals(label)) {
					config.textSize = value;
				}
			}
			
			if (textField != null) {
				// Map text fields to config fields
				if (textField == textColorField) {
					config.textColor = textField.getText();
				} else if (textField == bgColorField) {
					config.backgroundColor = textField.getText();
				}
			}
		}
	}
	
	// =====================================================================
	// SLIDER WIDGETS
	// =====================================================================
	
	/**
	 * Custom slider widget for integer values.
	 */
	private static class IntSlider extends SliderWidget {
		private final int min;
		private final int max;
		private final String suffix;
		private final String label;
		
		public IntSlider(int x, int y, int width, int height, String label, int min, int max, int value, String suffix) {
			super(x, y, width, height, Text.literal(label + ": " + value + suffix), (double)(value - min) / (max - min));
			this.min = min;
			this.max = max;
			this.suffix = suffix;
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
		
		public int getIntValue() {
			return (int) Math.round(this.value * (max - min) + min);
		}
		
		public void setValue(int newValue, int min, int max) {
			this.value = (double)(newValue - min) / (max - min);
			updateMessage();
		}
		
		@Override
		protected void updateMessage() {
			setMessage(Text.literal(label + ": " + getIntValue() + suffix));
		}
		
		@Override
		protected void applyValue() {
			// Value is applied when getIntValue() is called during save
		}
	}
	
	/**
	 * Custom slider widget for float values.
	 */
	private static class FloatSlider extends SliderWidget {
		private final float min;
		private final float max;
		private final String suffix;
		private final String label;
		
		public FloatSlider(int x, int y, int width, int height, String label, float min, float max, float value, String suffix) {
			super(x, y, width, height, Text.literal(label + ": " + String.format("%.1f", value) + suffix), (double)(value - min) / (max - min));
			this.min = min;
			this.max = max;
			this.suffix = suffix;
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
		
		public float getFloatValue() {
			return (float)(this.value * (max - min) + min);
		}
		
		public void setValue(float newValue) {
			this.value = (double)(newValue - min) / (max - min);
			updateMessage();
		}
		
		@Override
		protected void updateMessage() {
			setMessage(Text.literal(label + ": " + String.format("%.1f", getFloatValue()) + suffix));
		}
		
		@Override
		protected void applyValue() {
			// Value is applied when getFloatValue() is called during save
		}
	}
}
