package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class FPSHudRenderer {
	
	public static void render(DrawContext context, RenderTickCounter tickCounter) {
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
	public static void renderFPS(DrawContext context, boolean isPreview) {
		renderFPSWithConfig(context, SimpleFPSConfig.getInstance(), isPreview);
	}
	
	/**
	 * Renders the FPS counter with a specific config (used for live preview).
	 * @param context The draw context
	 * @param config The config to use for rendering
	 * @param isPreview If true, shows "Drag me!" indicator
	 */
	public static void renderFPSWithConfig(DrawContext context, SimpleFPSConfig config, boolean isPreview) {
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer textRenderer = client.textRenderer;
		
		// Get current FPS from client
		int fps = client.getCurrentFps();
		
		// Build display text
		String fpsText = config.showLabel ? fps + " FPS" : String.valueOf(fps);
		
		// Calculate text dimensions with scaling
		float scale = config.textSize;
		int textWidth = (int) (textRenderer.getWidth(fpsText) * scale);
		int textHeight = (int) (textRenderer.fontHeight * scale);
		
		// Get position (clamped to screen bounds)
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		
		int x = Math.max(0, Math.min(config.positionX, screenWidth - textWidth - 4));
		int y = Math.max(0, Math.min(config.positionY, screenHeight - textHeight - 4));
		
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
		
		// Draw text with scaling using DrawContext's scale method
		if (scale != 1.0f) {
			// Scale around the text position
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(x, y);
			context.getMatrices().scale(scale, scale);
			
			// Draw text with shadow for better visibility
			context.drawTextWithShadow(
				textRenderer,
				fpsText,
				0,
				0,
				textColor
			);
			
			context.getMatrices().popMatrix();
		} else {
			// No scaling needed, draw directly
			context.drawTextWithShadow(
				textRenderer,
				fpsText,
				x,
				y,
				textColor
			);
		}
		
		// Show preview indicator if in preview mode
		if (isPreview) {
			String previewLabel = "← Drag me!";
			int labelX = x + textWidth + 10;
			int labelY = y + (textHeight > textRenderer.fontHeight ? y + (int)(textHeight / 2) - (textRenderer.fontHeight / 2) : y);
			context.drawTextWithShadow(textRenderer, previewLabel, labelX, labelY, 0xFF55FF55);
		}
	}
}
