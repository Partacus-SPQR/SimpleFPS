package com.simplefps.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * A color picker screen with HSV color wheel, RGB sliders, preset colors, and hex input.
 * Uses GLFW for mouse input since 1.21.10 API changed.
 */
public class ColorPickerScreen extends Screen {
	private final Screen parent;
	private final Consumer<String> onColorSelected;
	private final String title;
	
	// Color wheel properties
	private int wheelCenterX, wheelCenterY;
	private int wheelRadius = 60;
	
	// Current color in HSV
	private float hue = 0f;
	private float saturation = 1f;
	private float value = 1f;
	
	// RGB values
	private int red = 255, green = 255, blue = 255;
	
	// Hex input
	private TextFieldWidget hexField;
	
	// Preset colors
	private static final int[] PRESET_COLORS = {
		0xFF0000, 0xFF8000, 0xFFFF00, 0x80FF00,
		0x00FF00, 0x00FF80, 0x00FFFF, 0x0080FF,
		0x0000FF, 0x8000FF, 0xFF00FF, 0xFF0080,
		0xFFFFFF, 0xC0C0C0, 0x808080, 0x404040,
		0x000000, 0x800000, 0x008000, 0x000080
	};
	
	// Dragging state
	private boolean draggingWheel = false;
	private boolean draggingValue = false;
	private boolean draggingRed = false;
	private boolean draggingGreen = false;
	private boolean draggingBlue = false;
	private boolean wasMouseDown = false;
	
	public ColorPickerScreen(Screen parent, String title, String initialColor, Consumer<String> onColorSelected) {
		super(Text.literal(title));
		this.parent = parent;
		this.title = title;
		this.onColorSelected = onColorSelected;
		
		// Parse initial color
		try {
			String hex = initialColor.startsWith("#") ? initialColor.substring(1) : initialColor;
			int color = Integer.parseInt(hex, 16);
			red = (color >> 16) & 0xFF;
			green = (color >> 8) & 0xFF;
			blue = color & 0xFF;
			rgbToHsv();
		} catch (Exception e) {
			// Use default white
		}
	}
	
	@Override
	protected void init() {
		wheelCenterX = this.width / 2 - 80;
		wheelCenterY = this.height / 2 - 20;
		
		// Hex input field
		hexField = new TextFieldWidget(
			this.textRenderer,
			this.width / 2 + 30, this.height / 2 + 60,
			80, 20,
			Text.literal("Hex")
		);
		hexField.setMaxLength(7);
		hexField.setText(getHexColor());
		hexField.setChangedListener(this::onHexChanged);
		this.addDrawableChild(hexField);
		
		// Apply button
		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Apply"),
			button -> {
				onColorSelected.accept(getHexColor());
				this.client.setScreen(parent);
			}
		).dimensions(this.width / 2 - 102, this.height - 30, 100, 20).build());
		
		// Cancel button
		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Cancel"),
			button -> this.client.setScreen(parent)
		).dimensions(this.width / 2 + 2, this.height - 30, 100, 20).build());
	}
	
	private void onHexChanged(String hex) {
		try {
			String cleanHex = hex.startsWith("#") ? hex.substring(1) : hex;
			if (cleanHex.length() == 6) {
				int color = Integer.parseInt(cleanHex, 16);
				red = (color >> 16) & 0xFF;
				green = (color >> 8) & 0xFF;
				blue = color & 0xFF;
				rgbToHsv();
			}
		} catch (Exception e) {
			// Invalid hex, ignore
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		
		// Handle mouse input using GLFW
		MinecraftClient client = MinecraftClient.getInstance();
		long windowHandle = client.getWindow().getHandle();
		boolean isMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		
		// Detect mouse click start
		if (isMouseDown && !wasMouseDown) {
			handleMouseClick(mouseX, mouseY);
		}
		
		// Handle dragging
		if (isMouseDown) {
			handleMouseDrag(mouseX, mouseY);
		}
		
		// Detect mouse release
		if (!isMouseDown && wasMouseDown) {
			handleMouseRelease();
		}
		
		wasMouseDown = isMouseDown;
		
		// Title
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
		
		// Draw color wheel
		drawColorWheel(context);
		
		// Draw value/brightness bar
		drawValueBar(context);
		
		// Draw preset colors
		drawPresetColors(context, mouseX, mouseY);
		
		// Draw RGB sliders
		drawRgbSliders(context, mouseX, mouseY);
		
		// Draw color preview
		int previewX = this.width / 2 + 30;
		int previewY = this.height / 2 + 10;
		context.fill(previewX - 1, previewY - 1, previewX + 81, previewY + 41, 0xFFFFFFFF);
		context.fill(previewX, previewY, previewX + 80, previewY + 40, 0xFF000000 | (red << 16) | (green << 8) | blue);
		
		// Hex label
		context.drawTextWithShadow(this.textRenderer, "Hex:", previewX, previewY + 50, 0xFFFFFF);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	private void drawColorWheel(DrawContext context) {
		// Draw the color wheel using small rectangles (approximation)
		for (int y = -wheelRadius; y <= wheelRadius; y++) {
			for (int x = -wheelRadius; x <= wheelRadius; x++) {
				double distance = Math.sqrt(x * x + y * y);
				if (distance <= wheelRadius) {
					double angle = Math.atan2(y, x);
					float h = (float) ((angle + Math.PI) / (2 * Math.PI));
					float s = (float) (distance / wheelRadius);
					int color = hsvToRgb(h, s, value);
					context.fill(wheelCenterX + x, wheelCenterY + y, wheelCenterX + x + 1, wheelCenterY + y + 1, 0xFF000000 | color);
				}
			}
		}
		
		// Draw selection indicator on wheel
		double selAngle = hue * 2 * Math.PI - Math.PI;
		int selX = wheelCenterX + (int) (Math.cos(selAngle) * saturation * wheelRadius);
		int selY = wheelCenterY + (int) (Math.sin(selAngle) * saturation * wheelRadius);
		context.fill(selX - 3, selY - 3, selX + 3, selY + 3, 0xFFFFFFFF);
		context.fill(selX - 2, selY - 2, selX + 2, selY + 2, 0xFF000000);
	}
	
	private void drawValueBar(DrawContext context) {
		int barX = wheelCenterX + wheelRadius + 15;
		int barY = wheelCenterY - wheelRadius;
		int barWidth = 15;
		int barHeight = wheelRadius * 2;
		
		// Draw gradient from white to black
		for (int y = 0; y < barHeight; y++) {
			float v = 1f - (float) y / barHeight;
			int color = hsvToRgb(hue, saturation, v);
			context.fill(barX, barY + y, barX + barWidth, barY + y + 1, 0xFF000000 | color);
		}
		
		// Draw selection indicator
		int selY = barY + (int) ((1f - value) * barHeight);
		context.fill(barX - 2, selY - 2, barX + barWidth + 2, selY + 2, 0xFFFFFFFF);
	}
	
	private void drawPresetColors(DrawContext context, int mouseX, int mouseY) {
		int startX = this.width / 2 - 100;
		int startY = this.height - 70;
		int size = 12;
		int cols = 10;
		
		context.drawTextWithShadow(this.textRenderer, "Presets:", startX, startY - 12, 0xAAAAAA);
		
		for (int i = 0; i < PRESET_COLORS.length; i++) {
			int x = startX + (i % cols) * (size + 2);
			int y = startY + (i / cols) * (size + 2);
			
			// Border
			context.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFFFFFFFF);
			// Color
			context.fill(x, y, x + size, y + size, 0xFF000000 | PRESET_COLORS[i]);
		}
	}
	
	private void drawRgbSliders(DrawContext context, int mouseX, int mouseY) {
		int sliderX = this.width / 2 + 30;
		int sliderY = this.height / 2 - 80;
		int sliderWidth = 100;
		int sliderHeight = 12;
		int spacing = 20;
		
		// Red slider
		context.drawTextWithShadow(this.textRenderer, "R:", sliderX - 15, sliderY + 2, 0xFF5555);
		drawSlider(context, sliderX, sliderY, sliderWidth, sliderHeight, red / 255f, 0xFF0000);
		context.drawTextWithShadow(this.textRenderer, String.valueOf(red), sliderX + sliderWidth + 5, sliderY + 2, 0xFFFFFF);
		
		// Green slider
		context.drawTextWithShadow(this.textRenderer, "G:", sliderX - 15, sliderY + spacing + 2, 0x55FF55);
		drawSlider(context, sliderX, sliderY + spacing, sliderWidth, sliderHeight, green / 255f, 0x00FF00);
		context.drawTextWithShadow(this.textRenderer, String.valueOf(green), sliderX + sliderWidth + 5, sliderY + spacing + 2, 0xFFFFFF);
		
		// Blue slider
		context.drawTextWithShadow(this.textRenderer, "B:", sliderX - 15, sliderY + spacing * 2 + 2, 0x5555FF);
		drawSlider(context, sliderX, sliderY + spacing * 2, sliderWidth, sliderHeight, blue / 255f, 0x0000FF);
		context.drawTextWithShadow(this.textRenderer, String.valueOf(blue), sliderX + sliderWidth + 5, sliderY + spacing * 2 + 2, 0xFFFFFF);
	}
	
	private void drawSlider(DrawContext context, int x, int y, int width, int height, float sliderValue, int color) {
		// Background
		context.fill(x, y, x + width, y + height, 0xFF404040);
		// Filled portion
		context.fill(x, y, x + (int)(width * sliderValue), y + height, 0xFF000000 | color);
		// Border
		drawBorder(context, x, y, width, height, 0xFFFFFFFF);
		// Handle
		int handleX = x + (int)(width * sliderValue);
		context.fill(handleX - 2, y - 1, handleX + 2, y + height + 1, 0xFFFFFFFF);
	}
	
	private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color); // Top
		context.fill(x, y + height - 1, x + width, y + height, color); // Bottom
		context.fill(x, y, x + 1, y + height, color); // Left
		context.fill(x + width - 1, y, x + width, y + height, color); // Right
	}
	
	private void handleMouseClick(int mouseX, int mouseY) {
		// Check color wheel
		double dx = mouseX - wheelCenterX;
		double dy = mouseY - wheelCenterY;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance <= wheelRadius) {
			draggingWheel = true;
			updateFromWheel(mouseX, mouseY);
			return;
		}
		
		// Check value bar
		int barX = wheelCenterX + wheelRadius + 15;
		int barY = wheelCenterY - wheelRadius;
		if (mouseX >= barX && mouseX <= barX + 15 && mouseY >= barY && mouseY <= barY + wheelRadius * 2) {
			draggingValue = true;
			updateFromValueBar(mouseY);
			return;
		}
		
		// Check preset colors
		int startX = this.width / 2 - 100;
		int startY = this.height - 70;
		int size = 12;
		int cols = 10;
		for (int i = 0; i < PRESET_COLORS.length; i++) {
			int x = startX + (i % cols) * (size + 2);
			int y = startY + (i / cols) * (size + 2);
			if (mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size) {
				int color = PRESET_COLORS[i];
				red = (color >> 16) & 0xFF;
				green = (color >> 8) & 0xFF;
				blue = color & 0xFF;
				rgbToHsv();
				hexField.setText(getHexColor());
				return;
			}
		}
		
		// Check RGB sliders
		int sliderX = this.width / 2 + 30;
		int sliderY = this.height / 2 - 80;
		int sliderWidth = 100;
		int spacing = 20;
		
		if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth) {
			if (mouseY >= sliderY && mouseY <= sliderY + 12) {
				draggingRed = true;
				updateRedSlider(mouseX, sliderX, sliderWidth);
				return;
			}
			if (mouseY >= sliderY + spacing && mouseY <= sliderY + spacing + 12) {
				draggingGreen = true;
				updateGreenSlider(mouseX, sliderX, sliderWidth);
				return;
			}
			if (mouseY >= sliderY + spacing * 2 && mouseY <= sliderY + spacing * 2 + 12) {
				draggingBlue = true;
				updateBlueSlider(mouseX, sliderX, sliderWidth);
				return;
			}
		}
	}
	
	private void handleMouseDrag(int mouseX, int mouseY) {
		if (draggingWheel) {
			updateFromWheel(mouseX, mouseY);
		}
		if (draggingValue) {
			updateFromValueBar(mouseY);
		}
		if (draggingRed) {
			updateRedSlider(mouseX, this.width / 2 + 30, 100);
		}
		if (draggingGreen) {
			updateGreenSlider(mouseX, this.width / 2 + 30, 100);
		}
		if (draggingBlue) {
			updateBlueSlider(mouseX, this.width / 2 + 30, 100);
		}
	}
	
	private void handleMouseRelease() {
		draggingWheel = false;
		draggingValue = false;
		draggingRed = false;
		draggingGreen = false;
		draggingBlue = false;
	}
	
	private void updateFromWheel(double mouseX, double mouseY) {
		double dx = mouseX - wheelCenterX;
		double dy = mouseY - wheelCenterY;
		double distance = Math.min(Math.sqrt(dx * dx + dy * dy), wheelRadius);
		double angle = Math.atan2(dy, dx);
		
		hue = (float) ((angle + Math.PI) / (2 * Math.PI));
		saturation = (float) (distance / wheelRadius);
		
		hsvToRgb();
		hexField.setText(getHexColor());
	}
	
	private void updateFromValueBar(double mouseY) {
		int barY = wheelCenterY - wheelRadius;
		int barHeight = wheelRadius * 2;
		value = 1f - (float) Math.max(0, Math.min(barHeight, mouseY - barY)) / barHeight;
		
		hsvToRgb();
		hexField.setText(getHexColor());
	}
	
	private void updateRedSlider(double mouseX, int sliderX, int sliderWidth) {
		red = (int) (Math.max(0, Math.min(1, (mouseX - sliderX) / sliderWidth)) * 255);
		rgbToHsv();
		hexField.setText(getHexColor());
	}
	
	private void updateGreenSlider(double mouseX, int sliderX, int sliderWidth) {
		green = (int) (Math.max(0, Math.min(1, (mouseX - sliderX) / sliderWidth)) * 255);
		rgbToHsv();
		hexField.setText(getHexColor());
	}
	
	private void updateBlueSlider(double mouseX, int sliderX, int sliderWidth) {
		blue = (int) (Math.max(0, Math.min(1, (mouseX - sliderX) / sliderWidth)) * 255);
		rgbToHsv();
		hexField.setText(getHexColor());
	}
	
	private void rgbToHsv() {
		float r = red / 255f;
		float g = green / 255f;
		float b = blue / 255f;
		
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		
		value = max;
		saturation = max == 0 ? 0 : delta / max;
		
		if (delta == 0) {
			hue = 0;
		} else if (max == r) {
			hue = ((g - b) / delta) / 6f;
			if (hue < 0) hue += 1;
		} else if (max == g) {
			hue = ((b - r) / delta + 2) / 6f;
		} else {
			hue = ((r - g) / delta + 4) / 6f;
		}
	}
	
	private void hsvToRgb() {
		int rgb = hsvToRgb(hue, saturation, value);
		red = (rgb >> 16) & 0xFF;
		green = (rgb >> 8) & 0xFF;
		blue = rgb & 0xFF;
	}
	
	private int hsvToRgb(float h, float s, float v) {
		float r, g, b;
		
		int i = (int) (h * 6);
		float f = h * 6 - i;
		float p = v * (1 - s);
		float q = v * (1 - f * s);
		float t = v * (1 - (1 - f) * s);
		
		switch (i % 6) {
			case 0: r = v; g = t; b = p; break;
			case 1: r = q; g = v; b = p; break;
			case 2: r = p; g = v; b = t; break;
			case 3: r = p; g = q; b = v; break;
			case 4: r = t; g = p; b = v; break;
			default: r = v; g = p; b = q; break;
		}
		
		return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
	}
	
	private String getHexColor() {
		return String.format("#%02X%02X%02X", red, green, blue);
	}
	
	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
