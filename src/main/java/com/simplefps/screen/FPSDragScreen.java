package com.simplefps.screen;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * A screen that allows the user to drag the FPS counter to reposition it.
 * Press ESC to exit and save the position.
 * 
 * Uses GLFW for mouse input detection since 1.21.10 API changed.
 */
public class FPSDragScreen extends Screen {
	
	private final Screen parent;
	private boolean isDragging = false;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private boolean wasMouseDown = false;
	
	public FPSDragScreen(Screen parent) {
		super(Text.literal("Drag FPS Counter"));
		this.parent = parent;
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		// Check mouse button state using GLFW
		long windowHandle = client.getWindow().getHandle();
		boolean isMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		
		// Detect mouse click start
		if (isMouseDown && !wasMouseDown) {
			handleMouseClick(mouseX, mouseY);
		}
		
		// Detect mouse release
		if (!isMouseDown && wasMouseDown) {
			handleMouseRelease();
		}
		
		wasMouseDown = isMouseDown;
		
		// Draw semi-transparent background
		context.fill(0, 0, this.width, this.height, 0x80000000);
		
		// Draw instructions at top
		TextRenderer textRenderer = this.textRenderer;
		String instructions = "Drag the FPS counter to reposition. Press ESC to save.";
		int instructionWidth = textRenderer.getWidth(instructions);
		context.drawTextWithShadow(
			textRenderer,
			instructions,
			(this.width - instructionWidth) / 2,
			10,
			0xFFFFFF
		);
		
		// Draw the FPS counter preview
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		int fps = client.getCurrentFps();
		String fpsText = config.showLabel ? fps + " FPS" : String.valueOf(fps);
		
		float scale = config.textSize;
		int textWidth = (int) (textRenderer.getWidth(fpsText) * scale);
		int textHeight = (int) (textRenderer.fontHeight * scale);
		
		int x = config.positionX;
		int y = config.positionY;
		
		// Handle dragging - update position based on mouse
		if (isDragging) {
			int newX = (int) (mouseX - dragOffsetX);
			int newY = (int) (mouseY - dragOffsetY);
			
			// Clamp to screen bounds
			newX = Math.max(0, Math.min(newX, this.width - textWidth - 4));
			newY = Math.max(0, Math.min(newY, this.height - textHeight - 4));
			
			config.positionX = newX;
			config.positionY = newY;
			x = newX;
			y = newY;
		}
		
		// Draw highlight border around FPS counter
		int padding = 4;
		context.fill(
			x - padding - 1,
			y - padding - 1,
			x + textWidth + padding + 1,
			y + textHeight + padding + 1,
			isDragging ? 0xFFFFFF00 : 0xFF00FF00 // Yellow when dragging, green otherwise
		);
		
		// Draw background
		if (config.showBackground && config.backgroundOpacity > 0) {
			int bgColor = config.getBackgroundColorWithAlpha();
			context.fill(
				x - 2,
				y - 2,
				x + textWidth + 2,
				y + textHeight + 2,
				bgColor
			);
		}
		
		// Get text color with alpha
		int textColor = config.getTextColorWithAlpha();
		
		// Draw text with scaling
		if (scale != 1.0f) {
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(x, y);
			context.getMatrices().scale(scale, scale);
			context.drawTextWithShadow(textRenderer, fpsText, 0, 0, textColor);
			context.getMatrices().popMatrix();
		} else {
			context.drawTextWithShadow(textRenderer, fpsText, x, y, textColor);
		}
		
		// Draw "Drag me!" label
		String previewLabel = "← Drag me!";
		int labelX = x + textWidth + 10;
		int labelY = y;
		context.drawTextWithShadow(textRenderer, previewLabel, labelX, labelY, 0xFF55FF55);
		
		// Draw center instruction box
		String escInstruction = "Press \"ESC\" key to save changes!";
		int escWidth = textRenderer.getWidth(escInstruction);
		int boxPadding = 10;
		int boxWidth = escWidth + boxPadding * 2;
		int boxHeight = textRenderer.fontHeight + boxPadding * 2;
		int boxX = (this.width - boxWidth) / 2;
		int boxY = (this.height - boxHeight) / 2 + 50; // Slightly below center
		
		// Draw subtle dark background (no red border)
		context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xC0000000); // Dark background
		
		// Draw instruction text centered in box
		context.drawTextWithShadow(
			textRenderer,
			escInstruction,
			boxX + boxPadding,
			boxY + boxPadding,
			0xFF55FF55 // Green text
		);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	private void handleMouseClick(int mouseX, int mouseY) {
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		float scale = config.textSize;
		int textWidth = (int) (textRenderer.getWidth(config.showLabel ? "999 FPS" : "999") * scale);
		int textHeight = (int) (textRenderer.fontHeight * scale);
		
		int x = config.positionX;
		int y = config.positionY;
		int padding = 4;
		
		// Check if click is within FPS counter bounds
		if (mouseX >= x - padding && mouseX <= x + textWidth + padding &&
			mouseY >= y - padding && mouseY <= y + textHeight + padding) {
			isDragging = true;
			dragOffsetX = mouseX - x;
			dragOffsetY = mouseY - y;
		}
	}
	
	private void handleMouseRelease() {
		if (isDragging) {
			isDragging = false;
			// Save the new position
			SimpleFPSConfig config = SimpleFPSConfig.getInstance();
			config.save();
		}
	}
	
	@Override
	public void close() {
		// Save position when closing
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		config.save();
		
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}
	
	@Override
	public boolean shouldPause() {
		return false;
	}
}
