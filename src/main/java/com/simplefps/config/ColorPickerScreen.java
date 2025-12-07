package com.simplefps.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * A color picker screen with an HSV color wheel, RGB sliders, and preset colors.
 */
public class ColorPickerScreen extends Screen {
	private final Screen parent;
	private final String initialColor;
	private final Consumer<String> onColorSelected;
	
	// Current color in RGB (0-255)
	private int red = 255;
	private int green = 255;
	private int blue = 255;
	
	// Color wheel
	private static final int WHEEL_SIZE = 150;
	private int wheelX, wheelY;
	private boolean draggingWheel = false;
	
	// Brightness slider
	private int brightnessSliderX, brightnessSliderY;
	private static final int BRIGHTNESS_WIDTH = 20;
	private static final int BRIGHTNESS_HEIGHT = 150;
	private float brightness = 1.0f;
	private boolean draggingBrightness = false;
	
	// RGB sliders
	private int sliderStartX, sliderStartY;
	private static final int SLIDER_WIDTH = 150;
	private static final int SLIDER_HEIGHT = 16;
	private int draggingSlider = -1; // 0=R, 1=G, 2=B
	
	// Hex input
	private TextFieldWidget hexField;
	
	// Preset colors
	private static final String[] PRESET_COLORS = {
		"#FFFFFF", "#C0C0C0", "#808080", "#404040", "#000000",
		"#FF0000", "#FF8000", "#FFFF00", "#80FF00", "#00FF00",
		"#00FF80", "#00FFFF", "#0080FF", "#0000FF", "#8000FF",
		"#FF00FF", "#FF0080", "#FF5555", "#55FF55", "#5555FF"
	};
	private int presetsX, presetsY;
	
	// Preview
	private int previewX, previewY;
	private static final int PREVIEW_SIZE = 40;
	
	// Mouse tracking
	private boolean wasMouseDown = false;
	
	public ColorPickerScreen(Screen parent, String initialColor, Consumer<String> onColorSelected) {
		super(Text.literal("Color Picker"));
		this.parent = parent;
		this.initialColor = initialColor;
		this.onColorSelected = onColorSelected;
		
		// Parse initial color
		parseHexColor(initialColor);
	}
	
	private void parseHexColor(String hex) {
		try {
			String clean = hex.startsWith("#") ? hex.substring(1) : hex;
			if (clean.length() == 6) {
				red = Integer.parseInt(clean.substring(0, 2), 16);
				green = Integer.parseInt(clean.substring(2, 4), 16);
				blue = Integer.parseInt(clean.substring(4, 6), 16);
			}
		} catch (Exception e) {
			red = green = blue = 255;
		}
	}
	
	private String getHexColor() {
		return String.format("#%02X%02X%02X", red, green, blue);
	}
	
	@Override
	protected void init() {
		super.init();
		
		int centerX = width / 2;
		int centerY = height / 2;
		
		// Position elements
		wheelX = centerX - WHEEL_SIZE - 40;
		wheelY = centerY - WHEEL_SIZE / 2 - 20;
		
		brightnessSliderX = wheelX + WHEEL_SIZE + 10;
		brightnessSliderY = wheelY;
		
		sliderStartX = centerX + 20;
		sliderStartY = centerY - 60;
		
		presetsX = centerX - 100;
		presetsY = centerY + WHEEL_SIZE / 2 + 10;
		
		previewX = sliderStartX;
		previewY = sliderStartY + 80;
		
		// Hex input field
		hexField = new TextFieldWidget(textRenderer, sliderStartX, sliderStartY + 60, 80, 16, Text.literal("Hex"));
		hexField.setMaxLength(7);
		hexField.setText(getHexColor());
		hexField.setChangedListener(text -> {
			if (text.matches("#[0-9A-Fa-f]{6}")) {
				parseHexColor(text);
			}
		});
		addDrawableChild(hexField);
		
		// Apply button - calls the callback which handles screen transition
		addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button -> {
			onColorSelected.accept(getHexColor());
			// Don't call close() - the callback handles screen transition
		}).dimensions(centerX - 55, height - 30, 50, 20).build());
		
		// Cancel button
		addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
			close();
		}).dimensions(centerX + 5, height - 30, 50, 20).build());
	}
	
	@Override
	public void close() {
		if (client != null) {
			client.setScreen(parent);
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Draw semi-transparent background
		context.fill(0, 0, width, height, 0xCC000000);
		
		// Title
		context.drawCenteredTextWithShadow(textRenderer, "Color Picker", width / 2, 10, 0xFFFFFF);
		
		// Note about auto-save above buttons (draw after everything else so it's on top)
		String noteText = "Note: Clicking Apply automatically saves the value.";
		context.drawCenteredTextWithShadow(textRenderer, noteText, width / 2, height - 55, 0xFFFFAA00);
		
		// Draw color wheel (simplified as color grid)
		renderColorWheel(context, mouseX, mouseY);
		
		// Draw brightness slider
		renderBrightnessSlider(context, mouseX, mouseY);
		
		// Draw RGB sliders
		renderRGBSliders(context, mouseX, mouseY);
		
		// Draw preview
		context.drawTextWithShadow(textRenderer, "Preview:", previewX, previewY - 12, 0xAAAAAA);
		int previewColor = 0xFF000000 | (red << 16) | (green << 8) | blue;
		context.fill(previewX, previewY, previewX + PREVIEW_SIZE, previewY + PREVIEW_SIZE, previewColor);
		// Border
		context.fill(previewX - 1, previewY - 1, previewX + PREVIEW_SIZE + 1, previewY, 0xFFFFFFFF);
		context.fill(previewX - 1, previewY + PREVIEW_SIZE, previewX + PREVIEW_SIZE + 1, previewY + PREVIEW_SIZE + 1, 0xFFFFFFFF);
		context.fill(previewX - 1, previewY, previewX, previewY + PREVIEW_SIZE, 0xFFFFFFFF);
		context.fill(previewX + PREVIEW_SIZE, previewY, previewX + PREVIEW_SIZE + 1, previewY + PREVIEW_SIZE, 0xFFFFFFFF);
		
		// Draw preset colors
		renderPresets(context, mouseX, mouseY);
		
		// Labels
		context.drawTextWithShadow(textRenderer, "Hex:", sliderStartX - 30, sliderStartY + 64, 0xAAAAAA);
		
		// Handle mouse input
		handleMouseInput(mouseX, mouseY);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	private void renderColorWheel(DrawContext context, int mouseX, int mouseY) {
		context.drawTextWithShadow(textRenderer, "Color Wheel:", wheelX, wheelY - 12, 0xAAAAAA);
		
		// Draw a simplified HSV color grid
		int gridSize = 10;
		int cols = WHEEL_SIZE / gridSize;
		int rows = WHEEL_SIZE / gridSize;
		
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				float hue = (float) col / cols;
				float saturation = 1.0f - (float) row / rows;
				
				int[] rgb = hsvToRgb(hue, saturation, brightness);
				int color = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
				
				int x = wheelX + col * gridSize;
				int y = wheelY + row * gridSize;
				context.fill(x, y, x + gridSize, y + gridSize, color);
			}
		}
		
		// Border
		context.fill(wheelX - 1, wheelY - 1, wheelX + WHEEL_SIZE + 1, wheelY, 0xFFFFFFFF);
		context.fill(wheelX - 1, wheelY + WHEEL_SIZE, wheelX + WHEEL_SIZE + 1, wheelY + WHEEL_SIZE + 1, 0xFFFFFFFF);
		context.fill(wheelX - 1, wheelY, wheelX, wheelY + WHEEL_SIZE, 0xFFFFFFFF);
		context.fill(wheelX + WHEEL_SIZE, wheelY, wheelX + WHEEL_SIZE + 1, wheelY + WHEEL_SIZE, 0xFFFFFFFF);
	}
	
	private void renderBrightnessSlider(DrawContext context, int mouseX, int mouseY) {
		context.drawTextWithShadow(textRenderer, "Brightness", brightnessSliderX - 5, wheelY - 12, 0xAAAAAA);
		
		// Draw gradient from white to black
		for (int i = 0; i < BRIGHTNESS_HEIGHT; i++) {
			float b = 1.0f - (float) i / BRIGHTNESS_HEIGHT;
			int gray = (int) (b * 255);
			int color = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
			context.fill(brightnessSliderX, brightnessSliderY + i, brightnessSliderX + BRIGHTNESS_WIDTH, brightnessSliderY + i + 1, color);
		}
		
		// Border
		context.fill(brightnessSliderX - 1, brightnessSliderY - 1, brightnessSliderX + BRIGHTNESS_WIDTH + 1, brightnessSliderY, 0xFFFFFFFF);
		context.fill(brightnessSliderX - 1, brightnessSliderY + BRIGHTNESS_HEIGHT, brightnessSliderX + BRIGHTNESS_WIDTH + 1, brightnessSliderY + BRIGHTNESS_HEIGHT + 1, 0xFFFFFFFF);
		context.fill(brightnessSliderX - 1, brightnessSliderY, brightnessSliderX, brightnessSliderY + BRIGHTNESS_HEIGHT, 0xFFFFFFFF);
		context.fill(brightnessSliderX + BRIGHTNESS_WIDTH, brightnessSliderY, brightnessSliderX + BRIGHTNESS_WIDTH + 1, brightnessSliderY + BRIGHTNESS_HEIGHT, 0xFFFFFFFF);
		
		// Indicator
		int indicatorY = brightnessSliderY + (int) ((1.0f - brightness) * BRIGHTNESS_HEIGHT);
		context.fill(brightnessSliderX - 3, indicatorY - 2, brightnessSliderX + BRIGHTNESS_WIDTH + 3, indicatorY + 2, 0xFFFFFFFF);
	}
	
	private void renderRGBSliders(DrawContext context, int mouseX, int mouseY) {
		context.drawTextWithShadow(textRenderer, "RGB Sliders:", sliderStartX, sliderStartY - 25, 0xAAAAAA);
		
		// Red slider
		context.drawTextWithShadow(textRenderer, "R:", sliderStartX - 15, sliderStartY + 4, 0xFF5555);
		renderSlider(context, sliderStartX, sliderStartY, red, 0xFFFF0000);
		context.drawTextWithShadow(textRenderer, String.valueOf(red), sliderStartX + SLIDER_WIDTH + 5, sliderStartY + 4, 0xFFFFFF);
		
		// Green slider
		context.drawTextWithShadow(textRenderer, "G:", sliderStartX - 15, sliderStartY + 20 + 4, 0x55FF55);
		renderSlider(context, sliderStartX, sliderStartY + 20, green, 0xFF00FF00);
		context.drawTextWithShadow(textRenderer, String.valueOf(green), sliderStartX + SLIDER_WIDTH + 5, sliderStartY + 20 + 4, 0xFFFFFF);
		
		// Blue slider
		context.drawTextWithShadow(textRenderer, "B:", sliderStartX - 15, sliderStartY + 40 + 4, 0x5555FF);
		renderSlider(context, sliderStartX, sliderStartY + 40, blue, 0xFF0000FF);
		context.drawTextWithShadow(textRenderer, String.valueOf(blue), sliderStartX + SLIDER_WIDTH + 5, sliderStartY + 40 + 4, 0xFFFFFF);
	}
	
	private void renderSlider(DrawContext context, int x, int y, int value, int color) {
		// Background
		context.fill(x, y, x + SLIDER_WIDTH, y + SLIDER_HEIGHT, 0xFF333333);
		
		// Filled portion
		int fillWidth = (int) ((value / 255.0f) * SLIDER_WIDTH);
		context.fill(x, y, x + fillWidth, y + SLIDER_HEIGHT, color);
		
		// Border
		context.fill(x - 1, y - 1, x + SLIDER_WIDTH + 1, y, 0xFF666666);
		context.fill(x - 1, y + SLIDER_HEIGHT, x + SLIDER_WIDTH + 1, y + SLIDER_HEIGHT + 1, 0xFF666666);
		context.fill(x - 1, y, x, y + SLIDER_HEIGHT, 0xFF666666);
		context.fill(x + SLIDER_WIDTH, y, x + SLIDER_WIDTH + 1, y + SLIDER_HEIGHT, 0xFF666666);
		
		// Handle
		int handleX = x + fillWidth - 2;
		context.fill(handleX, y - 2, handleX + 4, y + SLIDER_HEIGHT + 2, 0xFFFFFFFF);
	}
	
	private void renderPresets(DrawContext context, int mouseX, int mouseY) {
		context.drawTextWithShadow(textRenderer, "Presets:", presetsX, presetsY - 12, 0xAAAAAA);
		
		int cols = 10;
		int size = 18;
		int padding = 2;
		
		for (int i = 0; i < PRESET_COLORS.length; i++) {
			int col = i % cols;
			int row = i / cols;
			int x = presetsX + col * (size + padding);
			int y = presetsY + row * (size + padding);
			
			// Parse preset color
			String hex = PRESET_COLORS[i];
			int r = Integer.parseInt(hex.substring(1, 3), 16);
			int g = Integer.parseInt(hex.substring(3, 5), 16);
			int b = Integer.parseInt(hex.substring(5, 7), 16);
			int color = 0xFF000000 | (r << 16) | (g << 8) | b;
			
			context.fill(x, y, x + size, y + size, color);
			
			// Border - highlight if hovered
			int borderColor = (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) ? 0xFFFFFFFF : 0xFF666666;
			context.fill(x - 1, y - 1, x + size + 1, y, borderColor);
			context.fill(x - 1, y + size, x + size + 1, y + size + 1, borderColor);
			context.fill(x - 1, y, x, y + size, borderColor);
			context.fill(x + size, y, x + size + 1, y + size, borderColor);
		}
	}
	
	private void handleMouseInput(int mouseX, int mouseY) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) return;
		
		long windowHandle = client.getWindow().getHandle();
		boolean mouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean justClicked = mouseDown && !wasMouseDown;
		wasMouseDown = mouseDown;
		
		// Handle color wheel
		if (mouseX >= wheelX && mouseX < wheelX + WHEEL_SIZE && 
			mouseY >= wheelY && mouseY < wheelY + WHEEL_SIZE) {
			if (justClicked) {
				draggingWheel = true;
			}
		}
		
		if (draggingWheel && mouseDown) {
			int gridSize = 10;
			int cols = WHEEL_SIZE / gridSize;
			int rows = WHEEL_SIZE / gridSize;
			
			int col = Math.max(0, Math.min(cols - 1, (mouseX - wheelX) / gridSize));
			int row = Math.max(0, Math.min(rows - 1, (mouseY - wheelY) / gridSize));
			
			float hue = (float) col / cols;
			float saturation = 1.0f - (float) row / rows;
			
			int[] rgb = hsvToRgb(hue, saturation, brightness);
			red = rgb[0];
			green = rgb[1];
			blue = rgb[2];
			hexField.setText(getHexColor());
		}
		
		if (!mouseDown) {
			draggingWheel = false;
		}
		
		// Handle brightness slider
		if (mouseX >= brightnessSliderX && mouseX < brightnessSliderX + BRIGHTNESS_WIDTH &&
			mouseY >= brightnessSliderY && mouseY < brightnessSliderY + BRIGHTNESS_HEIGHT) {
			if (justClicked) {
				draggingBrightness = true;
			}
		}
		
		if (draggingBrightness && mouseDown) {
			brightness = 1.0f - (float) Math.max(0, Math.min(BRIGHTNESS_HEIGHT, mouseY - brightnessSliderY)) / BRIGHTNESS_HEIGHT;
			hexField.setText(getHexColor());
		}
		
		if (!mouseDown) {
			draggingBrightness = false;
		}
		
		// Handle RGB sliders
		for (int i = 0; i < 3; i++) {
			int sliderY = sliderStartY + i * 20;
			if (mouseX >= sliderStartX && mouseX < sliderStartX + SLIDER_WIDTH &&
				mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT) {
				if (justClicked) {
					draggingSlider = i;
				}
			}
		}
		
		if (draggingSlider >= 0 && mouseDown) {
			int value = (int) ((float) Math.max(0, Math.min(SLIDER_WIDTH, mouseX - sliderStartX)) / SLIDER_WIDTH * 255);
			switch (draggingSlider) {
				case 0 -> red = value;
				case 1 -> green = value;
				case 2 -> blue = value;
			}
			hexField.setText(getHexColor());
		}
		
		if (!mouseDown) {
			draggingSlider = -1;
		}
		
		// Handle preset clicks
		if (justClicked) {
			int cols = 10;
			int size = 18;
			int padding = 2;
			
			for (int i = 0; i < PRESET_COLORS.length; i++) {
				int col = i % cols;
				int row = i / cols;
				int x = presetsX + col * (size + padding);
				int y = presetsY + row * (size + padding);
				
				if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
					parseHexColor(PRESET_COLORS[i]);
					hexField.setText(getHexColor());
					break;
				}
			}
		}
	}
	
	private int[] hsvToRgb(float h, float s, float v) {
		float c = v * s;
		float x = c * (1 - Math.abs((h * 6) % 2 - 1));
		float m = v - c;
		
		float r, g, b;
		if (h < 1.0f/6) { r = c; g = x; b = 0; }
		else if (h < 2.0f/6) { r = x; g = c; b = 0; }
		else if (h < 3.0f/6) { r = 0; g = c; b = x; }
		else if (h < 4.0f/6) { r = 0; g = x; b = c; }
		else if (h < 5.0f/6) { r = x; g = 0; b = c; }
		else { r = c; g = 0; b = x; }
		
		return new int[] {
			(int) ((r + m) * 255),
			(int) ((g + m) * 255),
			(int) ((b + m) * 255)
		};
	}
}
