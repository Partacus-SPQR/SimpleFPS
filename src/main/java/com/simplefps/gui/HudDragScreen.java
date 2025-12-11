package com.simplefps.gui;

import com.simplefps.config.SimpleFPSConfig;
import com.simplefps.hud.FPSHudRenderer;
import com.simplefps.hud.FPSGraphRenderer;
import com.simplefps.hud.CoordinatesRenderer;
import com.simplefps.hud.BiomeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * A unified drag screen that allows repositioning ALL enabled HUD elements.
 * Each element can be dragged independently.
 * Press ESC to exit and save positions.
 */
public class HudDragScreen extends Screen {
	private final Screen parent;
	private final SimpleFPSConfig config;
	
	// Which element is being dragged
	private enum DragTarget { NONE, FPS, GRAPH, COORDINATES, BIOME }
	private DragTarget currentDrag = DragTarget.NONE;
	
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private boolean wasMouseDown = false;
	
	public HudDragScreen(Screen parent) {
		super(Text.literal("Drag HUD Elements"));
		this.parent = parent;
		this.config = SimpleFPSConfig.getInstance();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		// Handle mouse input using GLFW
		long windowHandle = client.getWindow().getHandle();
		boolean isMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		
		// Detect mouse click start
		if (isMouseDown && !wasMouseDown) {
			handleMouseClick(mouseX, mouseY, client);
		}
		
		// Handle dragging
		if (isMouseDown && currentDrag != DragTarget.NONE) {
			handleMouseDrag(mouseX, mouseY, client);
		}
		
		// Detect mouse release
		if (!isMouseDown && wasMouseDown) {
			handleMouseRelease();
		}
		
		wasMouseDown = isMouseDown;
		
		// Render semi-transparent background
		context.fill(0, 0, this.width, this.height, 0x80000000);
		
		// Draw instructions at top
		String instruction = "Click and drag any element to reposition. Press ESC to save.";
		context.drawCenteredTextWithShadow(this.textRenderer, instruction, this.width / 2, 10, 0xFFFFFF);
		
		// Render all HUD elements with their drag indicators
		
		// FPS Counter (always available)
		if (config.enabled) {
			FPSHudRenderer.renderFPS(context, true);
			drawElementBorder(context, config.positionX, config.positionY, 
				getFpsWidth(), getFpsHeight(), currentDrag == DragTarget.FPS, "FPS");
		}
		
		// FPS Graph
		if (config.graphEnabled) {
			FPSGraphRenderer.renderGraph(context, true);
			float scale = config.graphScale / 100f;
			int graphWidth = (int) (150 * scale);
			int graphHeight = (int) (50 * scale) + 24;
			drawElementBorder(context, config.graphX, config.graphY, 
				graphWidth, graphHeight, currentDrag == DragTarget.GRAPH, "Graph");
		}
		
		// Coordinates
		if (config.coordinatesEnabled) {
			CoordinatesRenderer.render(context, true);
			drawElementBorder(context, config.coordinatesX, config.coordinatesY,
				getCoordinatesWidth(), getCoordinatesHeight(), currentDrag == DragTarget.COORDINATES, "Coords");
		}
		
		// Biome
		if (config.biomeEnabled) {
			BiomeRenderer.render(context, true);
			drawElementBorder(context, config.biomeX, config.biomeY,
				getBiomeWidth(), getBiomeHeight(), currentDrag == DragTarget.BIOME, "Biome");
		}
		
		// Draw hint at bottom
		String hint = "§7Enabled elements: ";
		if (config.enabled) hint += "FPS ";
		if (config.graphEnabled) hint += "Graph ";
		if (config.coordinatesEnabled) hint += "Coords ";
		if (config.biomeEnabled) hint += "Biome ";
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(hint), this.width / 2, this.height - 30, 0xAAAAAA);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	private void drawElementBorder(DrawContext context, int x, int y, int width, int height, boolean isDragging, String label) {
		int color = isDragging ? 0xFFFFFF00 : 0xFF00FF00; // Yellow when dragging, green otherwise
		int padding = 2;
		
		// Draw border
		context.fill(x - padding, y - padding, x + width + padding, y - padding + 1, color);
		context.fill(x - padding, y + height + padding - 1, x + width + padding, y + height + padding, color);
		context.fill(x - padding, y - padding, x - padding + 1, y + height + padding, color);
		context.fill(x + width + padding - 1, y - padding, x + width + padding, y + height + padding, color);
		
		// Draw small label to the right - only show element type, not "Drag me"
		int labelColor = isDragging ? 0xFFFFFF00 : 0xFF55FF55;
		context.drawTextWithShadow(this.textRenderer, "← " + label, x + width + 6, y + (height / 2) - 4, labelColor);
	}
	
	private void handleMouseClick(int mouseX, int mouseY, MinecraftClient client) {
		// Check each element in reverse order (top elements first)
		
		// Check Biome
		if (config.biomeEnabled) {
			int w = getBiomeWidth();
			int h = getBiomeHeight();
			if (isInBounds(mouseX, mouseY, config.biomeX, config.biomeY, w, h)) {
				currentDrag = DragTarget.BIOME;
				dragOffsetX = mouseX - config.biomeX;
				dragOffsetY = mouseY - config.biomeY;
				return;
			}
		}
		
		// Check Coordinates
		if (config.coordinatesEnabled) {
			int w = getCoordinatesWidth();
			int h = getCoordinatesHeight();
			if (isInBounds(mouseX, mouseY, config.coordinatesX, config.coordinatesY, w, h)) {
				currentDrag = DragTarget.COORDINATES;
				dragOffsetX = mouseX - config.coordinatesX;
				dragOffsetY = mouseY - config.coordinatesY;
				return;
			}
		}
		
		// Check Graph
		if (config.graphEnabled) {
			float scale = config.graphScale / 100f;
			int w = (int) (150 * scale);
			int h = (int) (50 * scale) + 24;
			if (isInBounds(mouseX, mouseY, config.graphX, config.graphY, w, h)) {
				currentDrag = DragTarget.GRAPH;
				dragOffsetX = mouseX - config.graphX;
				dragOffsetY = mouseY - config.graphY;
				return;
			}
		}
		
		// Check FPS
		if (config.enabled) {
			int w = getFpsWidth();
			int h = getFpsHeight();
			if (isInBounds(mouseX, mouseY, config.positionX, config.positionY, w, h)) {
				currentDrag = DragTarget.FPS;
				dragOffsetX = mouseX - config.positionX;
				dragOffsetY = mouseY - config.positionY;
				return;
			}
		}
	}
	
	private boolean isInBounds(int mouseX, int mouseY, int x, int y, int width, int height) {
		int padding = 4;
		return mouseX >= x - padding && mouseX <= x + width + padding &&
			   mouseY >= y - padding && mouseY <= y + height + padding;
	}
	
	private void handleMouseDrag(int mouseX, int mouseY, MinecraftClient client) {
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		
		switch (currentDrag) {
			case FPS -> {
				int w = getFpsWidth();
				int h = getFpsHeight();
				config.positionX = clamp(mouseX - dragOffsetX, 0, screenWidth - w);
				config.positionY = clamp(mouseY - dragOffsetY, 0, screenHeight - h);
			}
			case GRAPH -> {
				float scale = config.graphScale / 100f;
				int w = (int) (150 * scale);
				int h = (int) (50 * scale) + 24;
				config.graphX = clamp(mouseX - dragOffsetX, 0, screenWidth - w);
				config.graphY = clamp(mouseY - dragOffsetY, 0, screenHeight - h);
			}
			case COORDINATES -> {
				int w = getCoordinatesWidth();
				int h = getCoordinatesHeight();
				config.coordinatesX = clamp(mouseX - dragOffsetX, 0, screenWidth - w);
				config.coordinatesY = clamp(mouseY - dragOffsetY, 0, screenHeight - h);
			}
			case BIOME -> {
				int w = getBiomeWidth();
				int h = getBiomeHeight();
				config.biomeX = clamp(mouseX - dragOffsetX, 0, screenWidth - w);
				config.biomeY = clamp(mouseY - dragOffsetY, 0, screenHeight - h);
			}
			case NONE -> {}
		}
	}
	
	private void handleMouseRelease() {
		if (currentDrag != DragTarget.NONE) {
			// Save reference resolution so positions scale correctly when window is resized
			MinecraftClient client = MinecraftClient.getInstance();
			config.updateReferenceResolution(
				client.getWindow().getScaledWidth(),
				client.getWindow().getScaledHeight()
			);
			currentDrag = DragTarget.NONE;
			config.save();
		}
	}
	
	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
	
	private int getFpsWidth() {
		MinecraftClient client = MinecraftClient.getInstance();
		int fps = client.getCurrentFps();
		String text = config.showLabel ? fps + " FPS" : String.valueOf(fps);
		if (config.showDirection && client.player != null) {
			text += " N"; // Approximate width
		}
		return (int) (client.textRenderer.getWidth(text) * config.textSize);
	}
	
	private int getFpsHeight() {
		MinecraftClient client = MinecraftClient.getInstance();
		return (int) (client.textRenderer.fontHeight * config.textSize);
	}
	
	private int getCoordinatesWidth() {
		MinecraftClient client = MinecraftClient.getInstance();
		String text = "X: 0000 Y: 000 Z: 0000";
		return (int) (client.textRenderer.getWidth(text) * config.coordinatesTextSize);
	}
	
	private int getCoordinatesHeight() {
		MinecraftClient client = MinecraftClient.getInstance();
		return (int) (client.textRenderer.fontHeight * config.coordinatesTextSize);
	}
	
	private int getBiomeWidth() {
		MinecraftClient client = MinecraftClient.getInstance();
		String text = "Sample Biome Name";
		return (int) (client.textRenderer.getWidth(text) * config.biomeTextSize);
	}
	
	private int getBiomeHeight() {
		MinecraftClient client = MinecraftClient.getInstance();
		return (int) (client.textRenderer.fontHeight * config.biomeTextSize);
	}
	
	@Override
	public void close() {
		config.save();
		this.client.setScreen(parent);
	}
	
	@Override
	public boolean shouldPause() {
		return false;
	}
}
