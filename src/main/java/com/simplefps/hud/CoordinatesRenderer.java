package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.core.BlockPos;

public class CoordinatesRenderer {
	
	/**
	 * Renders the coordinates display.
	 * @param context The draw context
	 * @param isPreview If true, shows "Drag me!" indicator
	 */
	//? if >=26.1 {
	public static void render(GuiGraphicsExtractor context, boolean isPreview) {
	//?} else {
	/*public static void render(GuiGraphics context, boolean isPreview) {
	*///?}
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		if (!config.coordinatesEnabled && !isPreview) {
			return;
		}
		
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		
		Font font = client.font;
		
		// Get player coordinates
		BlockPos pos = client.player.blockPosition();
		String coordText = "X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ();
		
		// Calculate text dimensions with scaling
		float scale = config.coordinatesTextSize;
		int textWidth = (int) (font.width(coordText) * scale);
		int textHeight = (int) (font.lineHeight * scale);
		
		// Get position (scaled and clamped to screen bounds)
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		
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
			context.pose().pushMatrix();
			context.pose().translate(x, y);
			context.pose().scale(scale, scale);
			//? if >=26.1 {
			context.text(font, coordText, 0, 0, textColor);
			//?} else {
			/*context.drawString(font, coordText, 0, 0, textColor);
			*///?}
			context.pose().popMatrix();
		} else {
			//? if >=26.1 {
			context.text(font, coordText, x, y, textColor);
			//?} else {
			/*context.drawString(font, coordText, x, y, textColor);
			*///?}
		}
		
	}
	// Note: Preview labels are handled by HudDragScreen
}
