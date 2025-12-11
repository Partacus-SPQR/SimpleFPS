package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public class BiomeRenderer {
	
	/**
	 * Renders the biome display.
	 * @param context The draw context
	 * @param isPreview If true, shows "Drag me!" indicator
	 */
	public static void render(DrawContext context, boolean isPreview) {
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		if (!config.biomeEnabled && !isPreview) {
			return;
		}
		
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) {
			return;
		}
		
		TextRenderer textRenderer = client.textRenderer;
		
		// Get current biome
		String biomeText = getCurrentBiome(client);
		
		// Calculate text dimensions with scaling
		float scale = config.biomeTextSize;
		int textWidth = (int) (textRenderer.getWidth(biomeText) * scale);
		int textHeight = (int) (textRenderer.fontHeight * scale);
		
		// Get position (scaled and clamped to screen bounds)
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		
		int x = Math.max(0, Math.min(config.getScaledBiomeX(screenWidth), screenWidth - textWidth - 4));
		int y = Math.max(0, Math.min(config.getScaledBiomeY(screenHeight), screenHeight - textHeight - 4));
		
		// Draw background if enabled
		if (config.biomeShowBackground && config.biomeBackgroundOpacity > 0) {
			int bgColor = config.getBiomeBackgroundColorWithAlpha();
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
		int textColor = config.getBiomeTextColorWithAlpha();
		
		// Draw text with scaling
		if (scale != 1.0f) {
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(x, y);
			context.getMatrices().scale(scale, scale);
			context.drawTextWithShadow(textRenderer, biomeText, 0, 0, textColor);
			context.getMatrices().popMatrix();
		} else {
			context.drawTextWithShadow(textRenderer, biomeText, x, y, textColor);
		}
		
	}
	// Note: Preview labels are handled by HudDragScreen
	
	/**
	 * Gets the current biome name formatted nicely.
	 */
	private static String getCurrentBiome(MinecraftClient client) {
		if (client.player == null || client.world == null) return "Unknown";
		
		BlockPos pos = client.player.getBlockPos();
		Optional<RegistryKey<Biome>> biomeKey = client.world.getBiome(pos).getKey();
		
		if (biomeKey.isPresent()) {
			// Get the biome path and format it nicely
			String biomePath = biomeKey.get().getValue().getPath();
			// Convert underscore_case to Title Case
			String[] words = biomePath.split("_");
			StringBuilder formatted = new StringBuilder();
			for (String word : words) {
				if (!formatted.isEmpty()) formatted.append(" ");
				if (!word.isEmpty()) {
					formatted.append(word.substring(0, 1).toUpperCase())
					         .append(word.substring(1).toLowerCase());
				}
			}
			return formatted.toString();
		}
		return "Unknown";
	}
}
