package com.simplefps.hud;

import com.simplefps.config.SimpleFPSConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.LinkedList;

public class FPSGraphRenderer {
	private static final LinkedList<Integer> fpsHistory = new LinkedList<>();
	private static final int MAX_SAMPLES = 60; // 60 samples for graph
	private static long lastSampleTime = 0;
	private static final long SAMPLE_INTERVAL = 500; // Sample every 500ms
	
	private static int minFps = Integer.MAX_VALUE;
	private static int maxFps = 0;
	private static int avgFps = 0;
	private static long lastStatsReset = 0;
	private static final long STATS_RESET_INTERVAL = 30000; // Reset stats every 30 seconds
	
	public static void update() {
		long currentTime = System.currentTimeMillis();
		
		// Reset stats every 30 seconds
		if (currentTime - lastStatsReset >= STATS_RESET_INTERVAL) {
			minFps = Integer.MAX_VALUE;
			maxFps = 0;
			lastStatsReset = currentTime;
		}
		
		// Sample FPS at intervals
		if (currentTime - lastSampleTime >= SAMPLE_INTERVAL) {
			int currentFps = MinecraftClient.getInstance().getCurrentFps();
			
			fpsHistory.addLast(currentFps);
			if (fpsHistory.size() > MAX_SAMPLES) {
				fpsHistory.removeFirst();
			}
			
			// Update min/max
			if (currentFps < minFps) minFps = currentFps;
			if (currentFps > maxFps) maxFps = currentFps;
			
			// Calculate average
			if (!fpsHistory.isEmpty()) {
				int sum = 0;
				for (int fps : fpsHistory) {
					sum += fps;
				}
				avgFps = sum / fpsHistory.size();
			}
			
			lastSampleTime = currentTime;
		}
	}
	
	public static void renderGraph(DrawContext context, boolean isPreview) {
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		if (!config.graphEnabled && !isPreview) {
			return;
		}
		
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null && !isPreview) {
			return;
		}
		
		// Update samples
		update();
		
		// Calculate dimensions based on scale
		float scale = config.graphScale / 100f;
		int baseWidth = 150;
		int baseHeight = 50;
		int width = (int) (baseWidth * scale);
		int height = (int) (baseHeight * scale);
		int textHeight = 24; // Fixed height for 2 rows of text (labels + values)
		int totalHeight = height + textHeight;
		
		// Get scaled positions
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		
		int x = config.getScaledGraphX(screenWidth);
		int y = config.getScaledGraphY(screenHeight);
		
		// Ensure within screen bounds
		if (x + width > screenWidth) x = screenWidth - width;
		if (y + totalHeight > screenHeight) y = screenHeight - totalHeight;
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		
		// Draw background and border only if enabled
		if (config.graphShowBackground) {
			int bgColor = 0x80000000; // Semi-transparent black
			context.fill(x, y, x + width, y + totalHeight, bgColor);
			
			// Draw border
			drawBorder(context, x, y, width, totalHeight, 0xFFFFFFFF);
		}
		
		// Draw graph title
		String title = "FPS Graph";
		context.drawTextWithShadow(client.textRenderer, title, x + 3, y + 2, 0xFFFFFF);
		
		// Draw horizontal line separator (only if background enabled)
		if (config.graphShowBackground) {
			context.fill(x, y + 12, x + width, y + 13, 0x80FFFFFF);
		}
		
		// Graph area - add padding to keep bars inside border
		int graphPadding = config.graphShowBackground ? 2 : 0;
		int graphY = y + 14;
		int graphHeight = height - 14;
		int graphStartX = x + graphPadding;
		int graphEndX = x + width - graphPadding;
		int graphWidth = graphEndX - graphStartX;
		
		// Draw graph grid lines
		context.fill(x, graphY + graphHeight / 2, x + width, graphY + graphHeight / 2 + 1, 0x40FFFFFF);
		
		// Draw FPS history as bars (within padded bounds)
		if (!fpsHistory.isEmpty()) {
			int barWidth = Math.max(1, graphWidth / MAX_SAMPLES);
			int maxDisplayFps = Math.max(120, maxFps + 10); // Scale to max FPS or 120
			
			int i = 0;
			for (int fps : fpsHistory) {
				int barHeight = (int) ((float) fps / maxDisplayFps * graphHeight);
				barHeight = Math.min(barHeight, graphHeight);
				
				int barX = graphStartX + (i * graphWidth / MAX_SAMPLES);
				int barY = graphY + graphHeight - barHeight;
				
				// Ensure bar stays within bounds
				int barEndX = Math.min(barX + barWidth, graphEndX);
				
				// Color based on GRAPH-specific FPS thresholds
				int barColor;
				if (fps <= config.graphLowFpsThreshold) {
					barColor = 0xFFFF5555; // Red
				} else if (fps < config.graphHighFpsThreshold) {
					barColor = 0xFFFFFF55; // Yellow
				} else {
					barColor = 0xFF55FF55; // Green
				}
				
				context.fill(barX, barY, barEndX, graphY + graphHeight, barColor);
				i++;
			}
		}
		
		// Draw min/max/avg below graph in 2 rows (labels on top, values below)
		int statsY = y + height + 1;
		int labelY = statsY;
		int valueY = statsY + 11;
		int currentFps = client.getCurrentFps();
		
		int displayMin = minFps == Integer.MAX_VALUE ? currentFps : minFps;
		int displayMax = maxFps == 0 ? currentFps : maxFps;
		int displayAvg = avgFps == 0 ? currentFps : avgFps;
		
		// Calculate colors based on GRAPH-specific thresholds
		int minColor = displayMin <= config.graphLowFpsThreshold ? 0xFFFF5555 : 
					   displayMin < config.graphHighFpsThreshold ? 0xFFFFFF55 : 0xFF55FF55;
		int maxColor = displayMax <= config.graphLowFpsThreshold ? 0xFFFF5555 : 
					   displayMax < config.graphHighFpsThreshold ? 0xFFFFFF55 : 0xFF55FF55;
		int avgColor = displayAvg <= config.graphLowFpsThreshold ? 0xFFFF5555 : 
					   displayAvg < config.graphHighFpsThreshold ? 0xFFFFFF55 : 0xFF55FF55;
		
		// Divide width into 3 columns with padding to keep values inside border
		int padding = 4;
		int usableWidth = width - (padding * 2);
		int colWidth = usableWidth / 3;
		int col1X = x + padding;
		int col2X = x + padding + colWidth;
		int col3X = x + padding + colWidth * 2;
		
		// Row 1: Labels (Min: / Max: / Avg:) in gray
		context.drawTextWithShadow(client.textRenderer, "Min:", col1X, labelY, 0xFFAAAAAA);
		context.drawTextWithShadow(client.textRenderer, "Max:", col2X, labelY, 0xFFAAAAAA);
		context.drawTextWithShadow(client.textRenderer, "Avg:", col3X, labelY, 0xFFAAAAAA);
		
		// Row 2: Values (colored based on thresholds)
		context.drawTextWithShadow(client.textRenderer, String.valueOf(displayMin), col1X, valueY, minColor);
		context.drawTextWithShadow(client.textRenderer, String.valueOf(displayMax), col2X, valueY, maxColor);
		context.drawTextWithShadow(client.textRenderer, String.valueOf(displayAvg), col3X, valueY, avgColor);
	}
	
	private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color); // Top
		context.fill(x, y + height - 1, x + width, y + height, color); // Bottom
		context.fill(x, y, x + 1, y + height, color); // Left
		context.fill(x + width - 1, y, x + width, y + height, color); // Right
	}
	
	public static int getMinFps() {
		return minFps == Integer.MAX_VALUE ? MinecraftClient.getInstance().getCurrentFps() : minFps;
	}
	
	public static int getMaxFps() {
		return maxFps == 0 ? MinecraftClient.getInstance().getCurrentFps() : maxFps;
	}
	
	public static int getAvgFps() {
		return avgFps == 0 ? MinecraftClient.getInstance().getCurrentFps() : avgFps;
	}
	
	public static void resetStats() {
		minFps = Integer.MAX_VALUE;
		maxFps = 0;
		avgFps = 0;
		fpsHistory.clear();
		lastStatsReset = System.currentTimeMillis();
	}
}
