package com.simplefps.gui;

import com.simplefps.config.SimpleFPSConfig;
import com.simplefps.hud.FPSHudRenderer;
import com.simplefps.hud.FPSGraphRenderer;
import com.simplefps.hud.CoordinatesRenderer;
import com.simplefps.hud.BiomeRenderer;
import com.simplefps.hud.TimeClockRenderer;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
	private enum DragTarget { NONE, FPS, GRAPH, COORDINATES, BIOME, TIMECLOCK }
	private DragTarget currentDrag = DragTarget.NONE;
	
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private boolean wasMouseDown = false;
	
	public HudDragScreen(Screen parent) {
		super(Component.literal("Drag HUD Elements"));
		this.parent = parent;
		this.config = SimpleFPSConfig.getInstance();
	}
	
	@Override
	//? if >=26.1 {
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
	//?} else {
	/*public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
	*///?}
		Minecraft client = Minecraft.getInstance();
		
		// Handle mouse input using GLFW
		long windowHandle = client.getWindow().handle();
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
		//? if >=26.1 {
		context.centeredText(this.font, instruction, this.width / 2, 10, 0xFFFFFF);
		//?} else {
		/*context.drawCenteredString(this.font, instruction, this.width / 2, 10, 0xFFFFFF);
		*///?}
		
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
		
		// Time Clock
		if (config.timeClockEnabled) {
			TimeClockRenderer.render(context, true);
			drawElementBorder(context, config.timeClockX, config.timeClockY,
				getTimeClockWidth(), getTimeClockHeight(), currentDrag == DragTarget.TIMECLOCK, "Clock");
		}
		
		// Draw hint at bottom
		String hint = "\u00a77Enabled elements: ";
		if (config.enabled) hint += "FPS ";
		if (config.graphEnabled) hint += "Graph ";
		if (config.coordinatesEnabled) hint += "Coords ";
		if (config.biomeEnabled) hint += "Biome ";
		if (config.timeClockEnabled) hint += "Clock ";
		//? if >=26.1 {
		context.centeredText(this.font, Component.literal(hint), this.width / 2, this.height - 30, 0xAAAAAA);
		//?} else {
		/*context.drawCenteredString(this.font, Component.literal(hint), this.width / 2, this.height - 30, 0xAAAAAA);
		*///?}
		
		//? if >=26.1 {
		super.extractRenderState(context, mouseX, mouseY, delta);
		//?} else {
		/*super.render(context, mouseX, mouseY, delta);
		*///?}
	}
	
	//? if >=26.1 {
	private void drawElementBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean isDragging, String label) {
	//?} else {
	/*private void drawElementBorder(GuiGraphics context, int x, int y, int width, int height, boolean isDragging, String label) {
	*///?}
		int color = isDragging ? 0xFFFFFF00 : 0xFF00FF00; // Yellow when dragging, green otherwise
		int padding = 2;
		
		// Draw border
		context.fill(x - padding, y - padding, x + width + padding, y - padding + 1, color);
		context.fill(x - padding, y + height + padding - 1, x + width + padding, y + height + padding, color);
		context.fill(x - padding, y - padding, x - padding + 1, y + height + padding, color);
		context.fill(x + width + padding - 1, y - padding, x + width + padding, y + height + padding, color);
		
		// Draw small label to the right - only show element type, not "Drag me"
		int labelColor = isDragging ? 0xFFFFFF00 : 0xFF55FF55;
		//? if >=26.1 {
		context.text(this.font, "\u2190 " + label, x + width + 6, y + (height / 2) - 4, labelColor);
		//?} else {
		/*context.drawString(this.font, "\u2190 " + label, x + width + 6, y + (height / 2) - 4, labelColor);
		*///?}
	}
	
	private void handleMouseClick(int mouseX, int mouseY, Minecraft client) {
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
		
		// Check Time Clock
		if (config.timeClockEnabled) {
			int w = getTimeClockWidth();
			int h = getTimeClockHeight();
			if (isInBounds(mouseX, mouseY, config.timeClockX, config.timeClockY, w, h)) {
				currentDrag = DragTarget.TIMECLOCK;
				dragOffsetX = mouseX - config.timeClockX;
				dragOffsetY = mouseY - config.timeClockY;
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
	
	private void handleMouseDrag(int mouseX, int mouseY, Minecraft client) {
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();
		
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
			case TIMECLOCK -> {
				int w = getTimeClockWidth();
				int h = getTimeClockHeight();
				config.timeClockX = clamp(mouseX - dragOffsetX, 0, screenWidth - w);
				config.timeClockY = clamp(mouseY - dragOffsetY, 0, screenHeight - h);
			}
			case NONE -> {}
		}
	}
	
	private void handleMouseRelease() {
		if (currentDrag != DragTarget.NONE) {
			// Save reference resolution so positions scale correctly when window is resized
			Minecraft client = Minecraft.getInstance();
			config.updateReferenceResolution(
				client.getWindow().getGuiScaledWidth(),
				client.getWindow().getGuiScaledHeight()
			);
			currentDrag = DragTarget.NONE;
			config.save();
		}
	}
	
	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
	
	private int getFpsWidth() {
		Minecraft client = Minecraft.getInstance();
		int fps = client.getFps();
		String text = config.showLabel ? fps + " FPS" : String.valueOf(fps);
		if (config.showDirection && client.player != null) {
			text += " N"; // Approximate width
		}
		return (int) (client.font.width(text) * config.textSize);
	}
	
	private int getFpsHeight() {
		Minecraft client = Minecraft.getInstance();
		return (int) (client.font.lineHeight * config.textSize);
	}
	
	private int getCoordinatesWidth() {
		Minecraft client = Minecraft.getInstance();
		String text = "X: 0000 Y: 000 Z: 0000";
		return (int) (client.font.width(text) * config.coordinatesTextSize);
	}
	
	private int getCoordinatesHeight() {
		Minecraft client = Minecraft.getInstance();
		return (int) (client.font.lineHeight * config.coordinatesTextSize);
	}
	
	private int getBiomeWidth() {
		Minecraft client = Minecraft.getInstance();
		String text = "Sample Biome Name";
		return (int) (client.font.width(text) * config.biomeTextSize);
	}
	
	private int getBiomeHeight() {
		Minecraft client = Minecraft.getInstance();
		return (int) (client.font.lineHeight * config.biomeTextSize);
	}
	
	private int getTimeClockWidth() {
		Minecraft client = Minecraft.getInstance();
		String text = TimeClockRenderer.getSampleText(config);
		return (int) (client.font.width(text) * config.timeClockTextSize);
	}
	
	private int getTimeClockHeight() {
		Minecraft client = Minecraft.getInstance();
		return (int) (client.font.lineHeight * config.timeClockTextSize);
	}
	
	@Override
	public void onClose() {
		config.save();
		this.minecraft.setScreen(parent);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
