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
 * Features: centered layout, sliders with labels inside, tooltips, reset buttons.
 */
public class SimpleFPSConfigScreen extends Screen {
	private final Screen parent;
	private final SimpleFPSConfig config;
	
	// Layout constants
	private static final int ROW_HEIGHT = 22;
	private static final int WIDGET_WIDTH = 180;
	private static final int RESET_BTN_WIDTH = 40;
	private static final int TOTAL_WIDTH = WIDGET_WIDTH + RESET_BTN_WIDTH + 4;
	
	// Scrolling
	private int scrollOffset = 0;
	private static final int VISIBLE_HEIGHT = 350;
	private int contentHeight = 0;
	
	// Tooltip tracking
	private final List<TooltipEntry> tooltips = new ArrayList<>();
	private String currentTooltip = null;
	
	// Widget references for saving
	private TextFieldWidget textColorField;
	private TextFieldWidget bgColorField;
	private IntSlider textSizeSlider;
	private IntSlider textOpacitySlider;
	private IntSlider bgOpacitySlider;
	private IntSlider posXSlider;
	private IntSlider posYSlider;
	private IntSlider lowFpsSlider;
	private IntSlider highFpsSlider;
	private IntSlider graphXSlider;
	private IntSlider graphYSlider;
	private IntSlider graphScaleSlider;
	private IntSlider graphLowFpsSlider;
	private IntSlider graphHighFpsSlider;
	
	// Toggle button references for reset
	private ButtonWidget enabledBtn;
	private ButtonWidget showLabelBtn;
	private ButtonWidget showBackgroundBtn;
	private ButtonWidget adaptiveBtn;
	private ButtonWidget graphEnabledBtn;
	private ButtonWidget graphBgBtn;
	
	public SimpleFPSConfigScreen(Screen parent) {
		super(Text.literal("SimpleFPS Configuration"));
		this.parent = parent;
		this.config = SimpleFPSConfig.getInstance();
	}
	
	@Override
	protected void init() {
		super.init();
		tooltips.clear();
		
		int centerX = this.width / 2;
		int widgetX = centerX - TOTAL_WIDTH / 2;
		int resetX = widgetX + WIDGET_WIDTH + 4;
		int y = 32;
		
		// === GENERAL SECTION ===
		y += ROW_HEIGHT; // Section header space
		
		// Enable FPS Counter
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Toggle the FPS counter on or off.");
		enabledBtn = addDrawableChild(ButtonWidget.builder(
			Text.literal(config.enabled ? "Enabled: ON" : "Enabled: OFF"),
			button -> {
				config.enabled = !config.enabled;
				button.setMessage(Text.literal(config.enabled ? "Enabled: ON" : "Enabled: OFF"));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build());
		addResetButton(resetX, y, () -> {
			config.enabled = true;
			enabledBtn.setMessage(Text.literal("Enabled: ON"));
		});
		y += ROW_HEIGHT;
		
		// Show Label
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Display 'FPS' text after the number.");
		showLabelBtn = addDrawableChild(ButtonWidget.builder(
			Text.literal(config.showLabel ? "Show Label: ON" : "Show Label: OFF"),
			button -> {
				config.showLabel = !config.showLabel;
				button.setMessage(Text.literal(config.showLabel ? "Show Label: ON" : "Show Label: OFF"));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build());
		addResetButton(resetX, y, () -> {
			config.showLabel = true;
			showLabelBtn.setMessage(Text.literal("Show Label: ON"));
		});
		y += ROW_HEIGHT;
		
		// Text Color
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Set the text color using a hex code (e.g., #FFFFFF).");
		textColorField = new TextFieldWidget(textRenderer, widgetX, y, WIDGET_WIDTH - 55, 18, Text.literal("Color"));
		textColorField.setText(config.textColor);
		textColorField.setMaxLength(7);
		addDrawableChild(textColorField);
		addDrawableChild(ButtonWidget.builder(Text.literal("Pick"), button -> {
			saveAllFields();
			MinecraftClient.getInstance().setScreen(new ColorPickerScreen(this, config.textColor, color -> {
				config.textColor = color;
				textColorField.setText(color);
			}));
		}).dimensions(widgetX + WIDGET_WIDTH - 50, y, 50, 18).build());
		addResetButton(resetX, y, () -> {
			config.textColor = "#FFFFFF";
			textColorField.setText("#FFFFFF");
		});
		y += ROW_HEIGHT;
		
		// Text Size Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Adjust the size of the FPS text (50% to 300%).");
		textSizeSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Size", 50, 300, (int)(config.textSize * 100), "%");
		addDrawableChild(textSizeSlider);
		addResetButton(resetX, y, () -> textSizeSlider.setValue(100, 50, 300));
		y += ROW_HEIGHT;
		
		// Text Opacity Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Adjust the visibility/opacity of the FPS text (0-100%).");
		textOpacitySlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Opacity", 0, 100, config.textOpacity, "%");
		addDrawableChild(textOpacitySlider);
		addResetButton(resetX, y, () -> textOpacitySlider.setValue(100, 0, 100));
		y += ROW_HEIGHT;
		
		// Show Background
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Display a background behind the FPS text.");
		showBackgroundBtn = addDrawableChild(ButtonWidget.builder(
			Text.literal(config.showBackground ? "Background: ON" : "Background: OFF"),
			button -> {
				config.showBackground = !config.showBackground;
				button.setMessage(Text.literal(config.showBackground ? "Background: ON" : "Background: OFF"));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build());
		addResetButton(resetX, y, () -> {
			config.showBackground = true;
			showBackgroundBtn.setMessage(Text.literal("Background: ON"));
		});
		y += ROW_HEIGHT;
		
		// Background Color
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Set background color using hex code (e.g., #000000).");
		bgColorField = new TextFieldWidget(textRenderer, widgetX, y, WIDGET_WIDTH - 55, 18, Text.literal("BG Color"));
		bgColorField.setText(config.backgroundColor);
		bgColorField.setMaxLength(7);
		addDrawableChild(bgColorField);
		addDrawableChild(ButtonWidget.builder(Text.literal("Pick"), button -> {
			saveAllFields();
			MinecraftClient.getInstance().setScreen(new ColorPickerScreen(this, config.backgroundColor, color -> {
				config.backgroundColor = color;
				bgColorField.setText(color);
			}));
		}).dimensions(widgetX + WIDGET_WIDTH - 50, y, 50, 18).build());
		addResetButton(resetX, y, () -> {
			config.backgroundColor = "#000000";
			bgColorField.setText("#000000");
		});
		y += ROW_HEIGHT;
		
		// Background Opacity Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Adjust the opacity of the background (0-100%).");
		bgOpacitySlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "BG Opacity", 0, 100, config.backgroundOpacity, "%");
		addDrawableChild(bgOpacitySlider);
		addResetButton(resetX, y, () -> bgOpacitySlider.setValue(50, 0, 100));
		y += ROW_HEIGHT;
		
		// Position X Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Horizontal position of the FPS counter on screen.");
		posXSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "X", 0, Math.max(1920, this.width), config.positionX, "");
		addDrawableChild(posXSlider);
		addResetButton(resetX, y, () -> posXSlider.setValue(5, 0, Math.max(1920, this.width)));
		y += ROW_HEIGHT;
		
		// Position Y Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Vertical position of the FPS counter on screen.");
		posYSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Y", 0, Math.max(1080, this.height), config.positionY, "");
		addDrawableChild(posYSlider);
		addResetButton(resetX, y, () -> posYSlider.setValue(5, 0, Math.max(1080, this.height)));
		y += ROW_HEIGHT + 8;
		
		// === ADAPTIVE COLORS SECTION ===
		y += ROW_HEIGHT; // Section header space
		
		// Enable Adaptive Colors
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Color text based on FPS: Red (low), Yellow (medium), Green (high).");
		adaptiveBtn = addDrawableChild(ButtonWidget.builder(
			Text.literal(config.adaptiveColorEnabled ? "Adaptive: ON" : "Adaptive: OFF"),
			button -> {
				config.adaptiveColorEnabled = !config.adaptiveColorEnabled;
				button.setMessage(Text.literal(config.adaptiveColorEnabled ? "Adaptive: ON" : "Adaptive: OFF"));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build());
		addResetButton(resetX, y, () -> {
			config.adaptiveColorEnabled = false;
			adaptiveBtn.setMessage(Text.literal("Adaptive: OFF"));
		});
		y += ROW_HEIGHT;
		
		// Low FPS Threshold Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "FPS at or below this value will be shown in Red.");
		lowFpsSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Low FPS", 1, 120, config.lowFpsThreshold, " fps");
		addDrawableChild(lowFpsSlider);
		addResetButton(resetX, y, () -> lowFpsSlider.setValue(25, 1, 120));
		y += ROW_HEIGHT;
		
		// High FPS Threshold Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "FPS at or above this value will be shown in Green.");
		highFpsSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "High FPS", 1, 240, config.highFpsThreshold, " fps");
		addDrawableChild(highFpsSlider);
		addResetButton(resetX, y, () -> highFpsSlider.setValue(60, 1, 240));
		y += ROW_HEIGHT + 8;
		
		// === FPS GRAPH SECTION ===
		y += ROW_HEIGHT; // Section header space
		
		// Enable Graph
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Show a visual graph of FPS history with Min/Max/Avg stats.");
		graphEnabledBtn = addDrawableChild(ButtonWidget.builder(
			Text.literal(config.graphEnabled ? "Graph: ON" : "Graph: OFF"),
			button -> {
				config.graphEnabled = !config.graphEnabled;
				button.setMessage(Text.literal(config.graphEnabled ? "Graph: ON" : "Graph: OFF"));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build());
		addResetButton(resetX, y, () -> {
			config.graphEnabled = false;
			graphEnabledBtn.setMessage(Text.literal("Graph: OFF"));
		});
		y += ROW_HEIGHT;
		
		// Show Graph Background
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Show background behind the graph.");
		graphBgBtn = addDrawableChild(ButtonWidget.builder(
			Text.literal(config.graphShowBackground ? "Graph BG: ON" : "Graph BG: OFF"),
			button -> {
				config.graphShowBackground = !config.graphShowBackground;
				button.setMessage(Text.literal(config.graphShowBackground ? "Graph BG: ON" : "Graph BG: OFF"));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build());
		addResetButton(resetX, y, () -> {
			config.graphShowBackground = true;
			graphBgBtn.setMessage(Text.literal("Graph BG: ON"));
		});
		y += ROW_HEIGHT;
		
		// Graph X Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Horizontal position of the FPS graph on screen.");
		graphXSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Graph X", 0, Math.max(1920, this.width), config.graphX, "");
		addDrawableChild(graphXSlider);
		addResetButton(resetX, y, () -> graphXSlider.setValue(5, 0, Math.max(1920, this.width)));
		y += ROW_HEIGHT;
		
		// Graph Y Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Vertical position of the FPS graph on screen.");
		graphYSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Graph Y", 0, Math.max(1080, this.height), config.graphY, "");
		addDrawableChild(graphYSlider);
		addResetButton(resetX, y, () -> graphYSlider.setValue(100, 0, Math.max(1080, this.height)));
		y += ROW_HEIGHT;
		
		// Graph Scale Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Adjust the size of the FPS graph (50% to 200%).");
		graphScaleSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Scale", 50, 200, config.graphScale, "%");
		addDrawableChild(graphScaleSlider);
		addResetButton(resetX, y, () -> graphScaleSlider.setValue(100, 50, 200));
		y += ROW_HEIGHT;
		
		// Graph Low FPS Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Low FPS threshold for graph coloring.");
		graphLowFpsSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Graph Low", 1, 120, config.graphLowFpsThreshold, " fps");
		addDrawableChild(graphLowFpsSlider);
		addResetButton(resetX, y, () -> graphLowFpsSlider.setValue(30, 1, 120));
		y += ROW_HEIGHT;
		
		// Graph High FPS Slider
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, "High FPS threshold for graph coloring.");
		graphHighFpsSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20, "Graph High", 1, 240, config.graphHighFpsThreshold, " fps");
		addDrawableChild(graphHighFpsSlider);
		addResetButton(resetX, y, () -> graphHighFpsSlider.setValue(60, 1, 240));
		
		contentHeight = y + ROW_HEIGHT;
		
		// === BOTTOM BUTTONS ===
		int bottomY = this.height - 28;
		int buttonGap = 5;
		int bottomButtonWidth = 90;
		int totalButtonsWidth = bottomButtonWidth * 3 + buttonGap * 2;
		int buttonStartX = centerX - totalButtonsWidth / 2;
		
		// Save button
		addDrawableChild(ButtonWidget.builder(
			Text.literal("Save & Close"),
			button -> {
				saveAllFields();
				config.save();
				close();
			}
		).dimensions(buttonStartX, bottomY, bottomButtonWidth, 20).build());
		
		// Key Binds button - opens directly to keybindings screen
		addDrawableChild(ButtonWidget.builder(
			Text.literal("Key Binds"),
			button -> {
				saveAllFields();
				config.save();
				MinecraftClient.getInstance().setScreen(new KeybindsScreen(this, MinecraftClient.getInstance().options));
			}
		).dimensions(buttonStartX + bottomButtonWidth + buttonGap, bottomY, bottomButtonWidth, 20).build());
		
		// Cancel button
		addDrawableChild(ButtonWidget.builder(
			Text.literal("Cancel"),
			button -> close()
		).dimensions(buttonStartX + (bottomButtonWidth + buttonGap) * 2, bottomY, bottomButtonWidth, 20).build());
	}
	
	private void addResetButton(int x, int y, Runnable onReset) {
		addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> onReset.run())
			.dimensions(x, y, RESET_BTN_WIDTH, 20).build());
	}
	
	private void addTooltip(int x, int y, int width, int height, String tooltip) {
		tooltips.add(new TooltipEntry(x, y, width, height, tooltip));
	}
	
	private void saveAllFields() {
		// Text Color
		if (textColorField.getText().matches("#[0-9A-Fa-f]{6}")) {
			config.textColor = textColorField.getText();
		}
		
		// Background Color
		if (bgColorField.getText().matches("#[0-9A-Fa-f]{6}")) {
			config.backgroundColor = bgColorField.getText();
		}
		
		// Sliders
		config.textSize = textSizeSlider.getIntValue() / 100.0f;
		config.textOpacity = textOpacitySlider.getIntValue();
		config.backgroundOpacity = bgOpacitySlider.getIntValue();
		config.positionX = posXSlider.getIntValue();
		config.positionY = posYSlider.getIntValue();
		config.lowFpsThreshold = lowFpsSlider.getIntValue();
		config.highFpsThreshold = highFpsSlider.getIntValue();
		config.graphX = graphXSlider.getIntValue();
		config.graphY = graphYSlider.getIntValue();
		config.graphScale = graphScaleSlider.getIntValue();
		config.graphLowFpsThreshold = graphLowFpsSlider.getIntValue();
		config.graphHighFpsThreshold = graphHighFpsSlider.getIntValue();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Draw dark overlay background
		context.fill(0, 0, this.width, this.height, 0xC0101010);
		
		// Title
		context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
		
		// Calculate positions for section headers
		int centerX = this.width / 2;
		int y = 32;
		
		// === GENERAL SECTION HEADER ===
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("§e§l[ General ]"), centerX, y + 5, 0xFFFF55);
		y += ROW_HEIGHT;
		
		// Skip all the general widgets
		y += ROW_HEIGHT * 10 + 8; // 10 options
		
		// === ADAPTIVE COLORS SECTION HEADER ===
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("§e§l[ Adaptive Colors ]"), centerX, y + 5, 0xFFFF55);
		y += ROW_HEIGHT;
		
		// Skip adaptive widgets
		y += ROW_HEIGHT * 3 + 8; // 3 options
		
		// === FPS GRAPH SECTION HEADER ===
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("§e§l[ FPS Graph ]"), centerX, y + 5, 0xFFFF55);
		
		// Note about Cloth Config
		context.drawCenteredTextWithShadow(textRenderer, 
			Text.literal("§7Install Cloth Config for enhanced features!"), 
			centerX, this.height - 42, 0x888888);
		
		// Render widgets
		super.render(context, mouseX, mouseY, delta);
		
		// Check for tooltip hover
		currentTooltip = null;
		for (TooltipEntry entry : tooltips) {
			if (mouseX >= entry.x && mouseX < entry.x + entry.width &&
				mouseY >= entry.y && mouseY < entry.y + entry.height) {
				currentTooltip = entry.tooltip;
				break;
			}
		}
		
		// Draw tooltip last (on top of everything)
		if (currentTooltip != null && !currentTooltip.isEmpty()) {
			// Use Minecraft's built-in tooltip rendering
			context.drawTooltip(textRenderer, Text.literal(currentTooltip), mouseX, mouseY);
		}
	}
	
	@Override
	public void close() {
		if (client != null) {
			client.setScreen(parent);
		}
	}
	
	@Override
	public boolean shouldPause() {
		return false;
	}
	
	// Tooltip entry record
	private record TooltipEntry(int x, int y, int width, int height, String tooltip) {}
	
	// Custom slider widget for integer values with reset support
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
}
