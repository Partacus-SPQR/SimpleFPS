package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class CoordinatesRenderer {
	
	/**
	 * Renders the coordinates display.
	 * @param context The draw context
	 * @param isPreview If true, shows "Drag me!" indicator
	 */
	public static void render(DrawContext context, boolean isPreview) {
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		if (!config.coordinatesEnabled && !isPreview) {
			return;
		}
		
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}
		
		TextRenderer textRenderer = client.textRenderer;
		
		// Get player coordinates
		BlockPos pos = client.player.getBlockPos();
		String coordText = "X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ();
		
		// Calculate text dimensions with scaling
		float scale = config.coordinatesTextSize;
		int textWidth = (int) (textRenderer.getWidth(coordText) * scale);
		int textHeight = (int) (textRenderer.fontHeight * scale);
		
		// Get position (scaled and clamped to screen bounds)
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		
		int x = Math.max(0, Math.min(config.getScaledCoordinatesX(screenWidth), screenWidth - textWidth - 4));
		int y = Math.max(0, Math.min(config.getScaledCoordinatesY(screenHeight), screenHeight - textHeight - 4));
		
		// Draw background if enabled
		if (config.coordinatesShowBackground && config.coordinatesBackgroundOpacity > 0) {
			int bgColor = config.getCoordinatesBackgroundColorWithAlpha();
			int padding = 2;
			context.fill(
				x - padding,
				y - padding,
				x + textWidth + padding,
				y + textHeight + padding,
				bgColor
			);
		}
		
		// Get text color with alpha
		int textColor = config.getCoordinatesTextColorWithAlpha();
		
		// Draw text with scaling
		if (scale != 1.0f) {
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(x, y);
			context.getMatrices().scale(scale, scale);
			context.drawTextWithShadow(textRenderer, coordText, 0, 0, textColor);
			context.getMatrices().popMatrix();
		} else {
			context.drawTextWithShadow(textRenderer, coordText, x, y, textColor);
		}
		
	}
	// Note: Preview labels are handled by HudDragScreen
}
