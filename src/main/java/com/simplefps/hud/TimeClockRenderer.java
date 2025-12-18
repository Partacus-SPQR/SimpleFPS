package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Renders the time clock display showing Minecraft day and time.
 * 
 * Minecraft time:
 * - 0 ticks = 6:00 AM (dawn)
 * - 6000 ticks = 12:00 PM (noon)
 * - 12000 ticks = 6:00 PM (dusk)
 * - 18000 ticks = 12:00 AM (midnight)
 * - Full day = 24000 ticks
 */
public class TimeClockRenderer {
	
	/**
	 * Renders the time clock display.
	 * @param context The draw context
	 * @param isPreview If true, shows preview mode
	 */
	public static void render(DrawContext context, boolean isPreview) {
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		if (!config.timeClockEnabled && !isPreview) {
			return;
		}
		
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) {
			return;
		}
		
		TextRenderer textRenderer = client.textRenderer;
		
		// Get current time and day
		String timeText = formatTimeDisplay(client, config);
		
		// Calculate text dimensions with scaling
		float scale = config.timeClockTextSize;
		int textWidth = (int) (textRenderer.getWidth(timeText) * scale);
		int textHeight = (int) (textRenderer.fontHeight * scale);
		
		// Get position (scaled and clamped to screen bounds)
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		
		int x = Math.max(0, Math.min(config.getScaledTimeClockX(screenWidth), screenWidth - textWidth - 4));
		int y = Math.max(0, Math.min(config.getScaledTimeClockY(screenHeight), screenHeight - textHeight - 4));
		
		// Draw background if enabled
		if (config.timeClockShowBackground && config.timeClockBackgroundOpacity > 0) {
			int bgColor = config.getTimeClockBackgroundColorWithAlpha();
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
		int textColor = config.getTimeClockTextColorWithAlpha();
		
		// Draw text with scaling
		if (scale != 1.0f) {
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(x, y);
			context.getMatrices().scale(scale, scale);
			context.drawTextWithShadow(textRenderer, timeText, 0, 0, textColor);
			context.getMatrices().popMatrix();
		} else {
			context.drawTextWithShadow(textRenderer, timeText, x, y, textColor);
		}
	}
	
	/**
	 * Formats the time display based on config settings.
	 */
	private static String formatTimeDisplay(MinecraftClient client, SimpleFPSConfig config) {
		// getTime() returns total world time since creation
		// getTimeOfDay() returns time % 24000 (just time of day, not useful for day count)
		long totalTime = client.world.getTime();
		
		// Calculate day number (days start at 1)
		long dayNumber = (totalTime / 24000L) + 1;
		
		// Get time of day (0-23999)
		long timeOfDay = totalTime % 24000L;
		
		// Convert to hours and minutes
		// Minecraft time: 0 ticks = 6:00 AM, so we add 6 hours offset
		// Reference: 0=6AM, 6000=12PM, 12000=6PM, 18000=12AM, 23000=5AM
		int totalMinutes = (int) ((timeOfDay * 24 * 60) / 24000);
		int hours24 = (totalMinutes / 60 + 6) % 24;
		int minutes = totalMinutes % 60;
		
		String timeString;
		if (config.timeClock24Hour) {
			// 24-hour format: 07:30
			timeString = String.format("%02d:%02d", hours24, minutes);
		} else {
			// 12-hour format: 7:30 AM
			int hours12 = hours24 % 12;
			if (hours12 == 0) hours12 = 12;
			String ampm = hours24 < 12 ? "AM" : "PM";
			timeString = String.format("%d:%02d %s", hours12, minutes, ampm);
		}
		
		// Format based on minimalist setting
		if (config.timeClockMinimalist) {
			// Minimalist: "1342 7:30 AM"
			return dayNumber + " " + timeString;
		} else {
			// Verbose: "Day: 1342 Time: 7:30 AM"
			return "Day: " + dayNumber + " Time: " + timeString;
		}
	}
	
	/**
	 * Gets sample text for width calculation in drag screen.
	 */
	public static String getSampleText(SimpleFPSConfig config) {
		if (config.timeClockMinimalist) {
			return config.timeClock24Hour ? "9999 23:59" : "9999 12:59 AM";
		} else {
			return config.timeClock24Hour ? "Day: 9999 Time: 23:59" : "Day: 9999 Time: 12:59 AM";
		}
	}
}
