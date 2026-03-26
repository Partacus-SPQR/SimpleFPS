package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

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
	//? if >=26.1 {
	public static void render(GuiGraphicsExtractor context, boolean isPreview) {
	//?} else {
	/*public static void render(GuiGraphics context, boolean isPreview) {
	*///?}
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		if (!config.timeClockEnabled && !isPreview) {
			return;
		}
		
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return;
		}
		
		Font font = client.font;
		
		// Get current time and day - pass world directly to ensure fresh reference
		String timeText = formatTimeDisplay(client.level, config);
		
		// Calculate text dimensions with scaling
		float scale = config.timeClockTextSize;
		int textWidth = (int) (font.width(timeText) * scale);
		int textHeight = (int) (font.lineHeight * scale);
		
		// Get position (scaled and clamped to screen bounds)
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		
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
			context.pose().pushMatrix();
			context.pose().translate(x, y);
			context.pose().scale(scale, scale);
			//? if >=26.1 {
			context.text(font, timeText, 0, 0, textColor);
			//?} else {
			/*context.drawString(font, timeText, 0, 0, textColor);
			*///?}
			context.pose().popMatrix();
		} else {
			//? if >=26.1 {
			context.text(font, timeText, x, y, textColor);
			//?} else {
			/*context.drawString(font, timeText, x, y, textColor);
			*///?}
		}
	}
	
	/**
	 * Formats the time display based on config settings.
	 */
	private static String formatTimeDisplay(net.minecraft.client.multiplayer.ClientLevel world, SimpleFPSConfig config) {
		// Get the current time of day directly from the world
		//? if >=26.1 {
		long timeOfDay = world.getDefaultClockTime();
		long totalTime = world.getDefaultClockTime();
		//?} else {
		/*// getDayTime() returns the time within the current day (0-23999)
		long timeOfDay = world.getDayTime();
		
		// Get total time for day calculation
		long totalTime = world.getGameTime();
		*///?}
		
		// Calculate day number (days start at 1)
		long dayNumber = (totalTime / 24000L) + 1;
		
		// Convert ticks to hours and minutes
		// Minecraft day cycle: 0=6AM, 6000=12PM, 12000=6PM, 18000=12AM, 24000=6AM (next day)
		// Formula: (ticks / 1000) gives hours since 6AM, (ticks % 1000) gives fraction of hour
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
