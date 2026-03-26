package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class BiomeRenderer {
	
	/**
	 * Renders the biome display.
	 * @param context The draw context
	 * @param isPreview If true, shows "Drag me!" indicator
	 */
	//? if >=26.1 {
	public static void render(GuiGraphicsExtractor context, boolean isPreview) {
	//?} else {
	/*public static void render(GuiGraphics context, boolean isPreview) {
	*///?}
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		if (!config.biomeEnabled && !isPreview) {
			return;
		}
		
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return;
		}
		
		Font font = client.font;
		
		// Get current biome
		String biomeText = getCurrentBiome(client);
		
		// Calculate text dimensions with scaling
		float scale = config.biomeTextSize;
		int textWidth = (int) (font.width(biomeText) * scale);
		int textHeight = (int) (font.lineHeight * scale);
		
		// Get position (scaled and clamped to screen bounds)
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		
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
			context.pose().pushMatrix();
			context.pose().translate(x, y);
			context.pose().scale(scale, scale);
			//? if >=26.1 {
			context.text(font, biomeText, 0, 0, textColor);
			//?} else {
			/*context.drawString(font, biomeText, 0, 0, textColor);
			*///?}
			context.pose().popMatrix();
		} else {
			//? if >=26.1 {
			context.text(font, biomeText, x, y, textColor);
			//?} else {
			/*context.drawString(font, biomeText, x, y, textColor);
			*///?}
		}
		
	}
	// Note: Preview labels are handled by HudDragScreen
	
	/**
	 * Gets the current biome name formatted nicely.
	 */
	private static String getCurrentBiome(Minecraft client) {
		if (client.player == null || client.level == null) return "Unknown";
		
		BlockPos pos = client.player.blockPosition();
		Optional<ResourceKey<Biome>> biomeKey = client.level.getBiome(pos).unwrapKey();
		
		if (biomeKey.isPresent()) {
			// Get the biome path and format it nicely
			//? if >=1.21.11 {
			String biomePath = biomeKey.get().identifier().getPath();
			//?} else {
			/*String biomePath = biomeKey.get().location().getPath();*/
			//?}
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
