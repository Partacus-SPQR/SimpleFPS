package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
*///?}
import net.minecraft.core.Direction;

public class FPSHudRenderer {
	
	//? if >=26.1 {
	public static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
	//?} else {
	/*public static void render(GuiGraphics context, DeltaTracker tickCounter) {
	*///?}
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		// Don't render if disabled
		if (!config.enabled) {
			return;
		}
		
		renderFPS(context, false);
	}
	
	/**
	 * Renders the FPS counter. Can be called from HUD or from a screen for preview.
	 * @param context The draw context
	 * @param isPreview If true, shows "Drag me!" indicator
	 */
	//? if >=26.1 {
	public static void renderFPS(GuiGraphicsExtractor context, boolean isPreview) {
	//?} else {
	/*public static void renderFPS(GuiGraphics context, boolean isPreview) {
	*///?}
		renderFPSWithConfig(context, SimpleFPSConfig.getInstance(), isPreview);
	}
	
	/**
	 * Renders the FPS counter with a specific config (used for live preview).
	 * @param context The draw context
	 * @param config The config to use for rendering
	 * @param isPreview If true, shows "Drag me!" indicator
	 */
	//? if >=26.1 {
	public static void renderFPSWithConfig(GuiGraphicsExtractor context, SimpleFPSConfig config, boolean isPreview) {
	//?} else {
	/*public static void renderFPSWithConfig(GuiGraphics context, SimpleFPSConfig config, boolean isPreview) {
	*///?}
		Minecraft client = Minecraft.getInstance();
		Font font = client.font;
		
		// Get current FPS from client
		int fps = client.getFps();
		
		// Build display text
		StringBuilder textBuilder = new StringBuilder();
		if (config.showLabel) {
			textBuilder.append(fps).append(" FPS");
		} else {
			textBuilder.append(fps);
		}
		
		// Add direction if enabled
		if (config.showDirection && client.player != null) {
			String direction = getPlayerDirection(client);
			textBuilder.append(" ").append(direction);
		}
		
		String fpsText = textBuilder.toString();
		
		// Calculate text dimensions with scaling
		float scale = config.textSize;
		int textWidth = (int) (font.width(fpsText) * scale);
		int textHeight = (int) (font.lineHeight * scale);
		
		// Get position (scaled and clamped to screen bounds)
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		
		int x = Math.max(0, Math.min(config.getScaledPositionX(screenWidth), screenWidth - textWidth - 4));
		int y = Math.max(0, Math.min(config.getScaledPositionY(screenHeight), screenHeight - textHeight - 4));
		
		// Draw background if enabled
		if (config.showBackground && config.backgroundOpacity > 0) {
			int bgColor = config.getBackgroundColorWithAlpha();
			int padding = 2;
			context.fill(
				x - padding,
				y - padding,
				x + textWidth + padding,
				y + textHeight + padding,
				bgColor
			);
		}
		
		// Get text color with alpha - use adaptive color if enabled
		int textColor;
		if (config.adaptiveColorEnabled) {
			int adaptiveRgb = config.getAdaptiveColor(fps);
			int alpha = (int) (255 * (config.textOpacity / 100.0f));
			textColor = (alpha << 24) | adaptiveRgb;
		} else {
			textColor = config.getTextColorWithAlpha();
		}
		
		// Draw text with scaling using GuiGraphics's scale method
		if (scale != 1.0f) {
			// Scale around the text position
			context.pose().pushMatrix();
			context.pose().translate(x, y);
			context.pose().scale(scale, scale);
			
			// Draw text with shadow for better visibility
			//? if >=26.1 {
			context.text(font, fpsText, 0, 0, textColor);
			//?} else {
			/*context.drawString(font, fpsText, 0, 0, textColor);
			*///?}
			
			context.pose().popMatrix();
		} else {
			// No scaling needed, draw directly
			//? if >=26.1 {
			context.text(font, fpsText, x, y, textColor);
			//?} else {
			/*context.drawString(font, fpsText, x, y, textColor);
			*///?}
		}
		
	}
	// Note: Preview labels are handled by HudDragScreen
	
	/**
	 * Gets the player's current facing direction as a single letter.
	 */
	private static String getPlayerDirection(Minecraft client) {
		if (client.player == null) return "";
		
		Direction direction = client.player.getDirection();
		return switch (direction) {
			case NORTH -> "N";
			case SOUTH -> "S";
			case EAST -> "E";
			case WEST -> "W";
			default -> "";
		};
	}
}
