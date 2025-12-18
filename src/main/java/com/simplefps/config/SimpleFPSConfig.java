package com.simplefps.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleFPSConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("SimpleFPS-Config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "simplefps.json");
	private static SimpleFPSConfig INSTANCE;

	// ==================== FPS Counter Options ====================
	public boolean enabled = true;
	public boolean showLabel = true;
	public boolean showDirection = false; // Shows N/E/S/W after FPS
	
	// FPS Counter Position
	public int positionX = 5;
	public int positionY = 5;
	
	// FPS Counter Appearance
	public String textColor = "#FFFFFF";
	public float textSize = 1.0f;
	public int textOpacity = 100; // 0-100 percentage
	
	// FPS Counter Background
	public boolean showBackground = true;
	public String backgroundColor = "#000000";
	public int backgroundOpacity = 50; // 0-100 percentage
	
	// FPS Adaptive color options
	public boolean adaptiveColorEnabled = false;
	public int lowFpsThreshold = 25;  // Red at or below this
	public int highFpsThreshold = 60; // Green at or above this
	
	// ==================== FPS Graph Options ====================
	public boolean graphEnabled = false;
	public int graphX = 5;
	public int graphY = 100;
	public int graphScale = 100; // 50-200%
	public boolean graphShowBackground = true;
	
	// Graph-specific thresholds
	public int graphLowFpsThreshold = 30;
	public int graphHighFpsThreshold = 60;
	
	// ==================== Coordinates Options ====================
	public boolean coordinatesEnabled = false;
	public int coordinatesX = 5;
	public int coordinatesY = 50;
	public String coordinatesTextColor = "#FFFFFF";
	public float coordinatesTextSize = 1.0f;
	public int coordinatesTextOpacity = 100;
	public boolean coordinatesShowBackground = true;
	public String coordinatesBackgroundColor = "#000000";
	public int coordinatesBackgroundOpacity = 50;
	
	// ==================== Biome Options ====================
	public boolean biomeEnabled = false;
	public int biomeX = 5;
	public int biomeY = 70;
	public String biomeTextColor = "#FFFFFF";
	public float biomeTextSize = 1.0f;
	public int biomeTextOpacity = 100;
	public boolean biomeShowBackground = true;
	public String biomeBackgroundColor = "#000000";
	public int biomeBackgroundOpacity = 50;
	
	// ==================== Time Clock Options ====================
	public boolean timeClockEnabled = false;
	public int timeClockX = 5;
	public int timeClockY = 90;
	public String timeClockTextColor = "#FFFFFF";
	public float timeClockTextSize = 1.0f;
	public int timeClockTextOpacity = 100;
	public boolean timeClockShowBackground = true;
	public String timeClockBackgroundColor = "#000000";
	public int timeClockBackgroundOpacity = 50;
	public boolean timeClock24Hour = false;      // false = 12-hour, true = 24-hour
	public boolean timeClockMinimalist = false;  // false = "Day: X Time: Y", true = "X Y"

	// ==================== Reference Resolution (for scaling) ====================
	// These store the screen size when positions were last set
	// Positions will scale proportionally when window is resized
	public int referenceWidth = 0;  // 0 means not set yet
	public int referenceHeight = 0;

	public static SimpleFPSConfig load() {
		if (INSTANCE == null) {
			if (CONFIG_FILE.exists()) {
				try (FileReader reader = new FileReader(CONFIG_FILE)) {
					INSTANCE = GSON.fromJson(reader, SimpleFPSConfig.class);
					LOGGER.info("Loaded config from file");
				} catch (IOException e) {
					LOGGER.error("Failed to load config, using defaults", e);
					INSTANCE = new SimpleFPSConfig();
				}
			} else {
				INSTANCE = new SimpleFPSConfig();
				INSTANCE.save();
				LOGGER.info("Created new config file with defaults");
			}
		}
		return INSTANCE;
	}

	public void save() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			GSON.toJson(this, writer);
			LOGGER.info("Config successfully saved to file");
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}

	public static void reload() {
		if (CONFIG_FILE.exists()) {
			try (FileReader reader = new FileReader(CONFIG_FILE)) {
				INSTANCE = GSON.fromJson(reader, SimpleFPSConfig.class);
				LOGGER.info("Reloaded config from file");
			} catch (IOException e) {
				LOGGER.error("Failed to reload config", e);
			}
		}
	}

	public static SimpleFPSConfig getInstance() {
		if (INSTANCE == null) {
			return load();
		}
		return INSTANCE;
	}
	
	/**
	 * Parse hex color string to integer color value.
	 * Supports formats: #RRGGBB, RRGGBB, #RGB, RGB
	 */
	public static int parseHexColor(String hex) {
		try {
			String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
			
			// Handle short format (RGB -> RRGGBB)
			if (cleaned.length() == 3) {
				char r = cleaned.charAt(0);
				char g = cleaned.charAt(1);
				char b = cleaned.charAt(2);
				cleaned = "" + r + r + g + g + b + b;
			}
			
			if (cleaned.length() != 6) {
				return 0xFFFFFF; // Default to white
			}
			
			return Integer.parseInt(cleaned, 16);
		} catch (NumberFormatException e) {
			return 0xFFFFFF; // Default to white on parse error
		}
	}
	
	/**
	 * Get the text color as an integer with alpha applied.
	 */
	public int getTextColorWithAlpha() {
		int rgb = parseHexColor(textColor);
		int alpha = (int) (255 * (textOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	/**
	 * Get the background color as an integer with alpha applied.
	 */
	public int getBackgroundColorWithAlpha() {
		int rgb = parseHexColor(backgroundColor);
		int alpha = (int) (255 * (backgroundOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	// ==================== Position Scaling Methods ====================
	
	/**
	 * Get scaled X position using anchor-based scaling.
	 * Elements positioned in the left third scale from left, center third stays centered,
	 * right third scales from right edge.
	 */
	private int getScaledX(int originalX, int currentWidth) {
		if (referenceWidth <= 0 || referenceWidth == currentWidth) {
			return originalX;
		}
		
		// Determine which third of the screen the element was in
		int leftThird = referenceWidth / 3;
		int rightThird = referenceWidth * 2 / 3;
		
		if (originalX < leftThird) {
			// Left third: scale from left edge (keep distance from left)
			return (int) ((float) originalX / referenceWidth * currentWidth);
		} else if (originalX > rightThird) {
			// Right third: scale from right edge (keep distance from right)
			int distFromRight = referenceWidth - originalX;
			int scaledDistFromRight = (int) ((float) distFromRight / referenceWidth * currentWidth);
			return currentWidth - scaledDistFromRight;
		} else {
			// Center third: keep centered (maintain relative center position)
			float relativeCenter = (float) originalX / referenceWidth;
			return (int) (relativeCenter * currentWidth);
		}
	}
	
	/**
	 * Get scaled X position for FPS counter based on current screen size.
	 */
	public int getScaledPositionX(int currentWidth) {
		return getScaledX(positionX, currentWidth);
	}
	
	/**
	 * Get scaled Y position for FPS counter based on current screen size.
	 */
	public int getScaledPositionY(int currentHeight) {
		if (referenceHeight <= 0 || referenceHeight == currentHeight) {
			return positionY;
		}
		return (int) ((float) positionY / referenceHeight * currentHeight);
	}
	
	/**
	 * Get scaled X position for Graph based on current screen size.
	 */
	public int getScaledGraphX(int currentWidth) {
		return getScaledX(graphX, currentWidth);
	}
	
	/**
	 * Get scaled Y position for Graph based on current screen size.
	 */
	public int getScaledGraphY(int currentHeight) {
		if (referenceHeight <= 0 || referenceHeight == currentHeight) {
			return graphY;
		}
		return (int) ((float) graphY / referenceHeight * currentHeight);
	}
	
	/**
	 * Get scaled X position for Coordinates based on current screen size.
	 */
	public int getScaledCoordinatesX(int currentWidth) {
		return getScaledX(coordinatesX, currentWidth);
	}
	
	/**
	 * Get scaled Y position for Coordinates based on current screen size.
	 */
	public int getScaledCoordinatesY(int currentHeight) {
		if (referenceHeight <= 0 || referenceHeight == currentHeight) {
			return coordinatesY;
		}
		return (int) ((float) coordinatesY / referenceHeight * currentHeight);
	}
	
	/**
	 * Get scaled X position for Biome based on current screen size.
	 */
	public int getScaledBiomeX(int currentWidth) {
		return getScaledX(biomeX, currentWidth);
	}
	
	/**
	 * Get scaled Y position for Biome based on current screen size.
	 */
	public int getScaledBiomeY(int currentHeight) {
		if (referenceHeight <= 0 || referenceHeight == currentHeight) {
			return biomeY;
		}
		return (int) ((float) biomeY / referenceHeight * currentHeight);
	}
	
	/**
	 * Get scaled X position for Time Clock based on current screen size.
	 */
	public int getScaledTimeClockX(int currentWidth) {
		return getScaledX(timeClockX, currentWidth);
	}
	
	/**
	 * Get scaled Y position for Time Clock based on current screen size.
	 */
	public int getScaledTimeClockY(int currentHeight) {
		if (referenceHeight <= 0 || referenceHeight == currentHeight) {
			return timeClockY;
		}
		return (int) ((float) timeClockY / referenceHeight * currentHeight);
	}
	
	/**
	 * Update reference resolution to current screen size.
	 * Call this when user drags an element to a new position.
	 */
	public void updateReferenceResolution(int width, int height) {
		this.referenceWidth = width;
		this.referenceHeight = height;
	}

	/**
	 * Creates a copy of this config for live preview purposes.
	 */
	public SimpleFPSConfig copy() {
		SimpleFPSConfig copy = new SimpleFPSConfig();
		// FPS Counter
		copy.enabled = this.enabled;
		copy.showLabel = this.showLabel;
		copy.showDirection = this.showDirection;
		copy.positionX = this.positionX;
		copy.positionY = this.positionY;
		copy.textColor = this.textColor;
		copy.textSize = this.textSize;
		copy.textOpacity = this.textOpacity;
		copy.showBackground = this.showBackground;
		copy.backgroundColor = this.backgroundColor;
		copy.backgroundOpacity = this.backgroundOpacity;
		copy.adaptiveColorEnabled = this.adaptiveColorEnabled;
		copy.lowFpsThreshold = this.lowFpsThreshold;
		copy.highFpsThreshold = this.highFpsThreshold;
		// Graph
		copy.graphEnabled = this.graphEnabled;
		copy.graphX = this.graphX;
		copy.graphY = this.graphY;
		copy.graphScale = this.graphScale;
		copy.graphShowBackground = this.graphShowBackground;
		copy.graphLowFpsThreshold = this.graphLowFpsThreshold;
		copy.graphHighFpsThreshold = this.graphHighFpsThreshold;
		// Coordinates
		copy.coordinatesEnabled = this.coordinatesEnabled;
		copy.coordinatesX = this.coordinatesX;
		copy.coordinatesY = this.coordinatesY;
		copy.coordinatesTextColor = this.coordinatesTextColor;
		copy.coordinatesTextSize = this.coordinatesTextSize;
		copy.coordinatesTextOpacity = this.coordinatesTextOpacity;
		copy.coordinatesShowBackground = this.coordinatesShowBackground;
		copy.coordinatesBackgroundColor = this.coordinatesBackgroundColor;
		copy.coordinatesBackgroundOpacity = this.coordinatesBackgroundOpacity;
		// Biome
		copy.biomeEnabled = this.biomeEnabled;
		copy.biomeX = this.biomeX;
		copy.biomeY = this.biomeY;
		copy.biomeTextColor = this.biomeTextColor;
		copy.biomeTextSize = this.biomeTextSize;
		copy.biomeTextOpacity = this.biomeTextOpacity;
		copy.biomeShowBackground = this.biomeShowBackground;
		copy.biomeBackgroundColor = this.biomeBackgroundColor;
		copy.biomeBackgroundOpacity = this.biomeBackgroundOpacity;
		// Time Clock
		copy.timeClockEnabled = this.timeClockEnabled;
		copy.timeClockX = this.timeClockX;
		copy.timeClockY = this.timeClockY;
		copy.timeClockTextColor = this.timeClockTextColor;
		copy.timeClockTextSize = this.timeClockTextSize;
		copy.timeClockTextOpacity = this.timeClockTextOpacity;
		copy.timeClockShowBackground = this.timeClockShowBackground;
		copy.timeClockBackgroundColor = this.timeClockBackgroundColor;
		copy.timeClockBackgroundOpacity = this.timeClockBackgroundOpacity;
		copy.timeClock24Hour = this.timeClock24Hour;
		copy.timeClockMinimalist = this.timeClockMinimalist;
		// Reference resolution
		copy.referenceWidth = this.referenceWidth;
		copy.referenceHeight = this.referenceHeight;
		return copy;
	}
	
	/**
	 * Copies values from another config into this one.
	 */
	public void copyFrom(SimpleFPSConfig other) {
		// FPS Counter
		this.enabled = other.enabled;
		this.showLabel = other.showLabel;
		this.showDirection = other.showDirection;
		this.positionX = other.positionX;
		this.positionY = other.positionY;
		this.textColor = other.textColor;
		this.textSize = other.textSize;
		this.textOpacity = other.textOpacity;
		this.showBackground = other.showBackground;
		this.backgroundColor = other.backgroundColor;
		this.backgroundOpacity = other.backgroundOpacity;
		this.adaptiveColorEnabled = other.adaptiveColorEnabled;
		this.lowFpsThreshold = other.lowFpsThreshold;
		this.highFpsThreshold = other.highFpsThreshold;
		// Graph
		this.graphEnabled = other.graphEnabled;
		this.graphX = other.graphX;
		this.graphY = other.graphY;
		this.graphScale = other.graphScale;
		this.graphShowBackground = other.graphShowBackground;
		this.graphLowFpsThreshold = other.graphLowFpsThreshold;
		this.graphHighFpsThreshold = other.graphHighFpsThreshold;
		// Coordinates
		this.coordinatesEnabled = other.coordinatesEnabled;
		this.coordinatesX = other.coordinatesX;
		this.coordinatesY = other.coordinatesY;
		this.coordinatesTextColor = other.coordinatesTextColor;
		this.coordinatesTextSize = other.coordinatesTextSize;
		this.coordinatesTextOpacity = other.coordinatesTextOpacity;
		this.coordinatesShowBackground = other.coordinatesShowBackground;
		this.coordinatesBackgroundColor = other.coordinatesBackgroundColor;
		this.coordinatesBackgroundOpacity = other.coordinatesBackgroundOpacity;
		// Biome
		this.biomeEnabled = other.biomeEnabled;
		this.biomeX = other.biomeX;
		this.biomeY = other.biomeY;
		this.biomeTextColor = other.biomeTextColor;
		this.biomeTextSize = other.biomeTextSize;
		this.biomeTextOpacity = other.biomeTextOpacity;
		this.biomeShowBackground = other.biomeShowBackground;
		this.biomeBackgroundColor = other.biomeBackgroundColor;
		this.biomeBackgroundOpacity = other.biomeBackgroundOpacity;
		// Time Clock
		this.timeClockEnabled = other.timeClockEnabled;
		this.timeClockX = other.timeClockX;
		this.timeClockY = other.timeClockY;
		this.timeClockTextColor = other.timeClockTextColor;
		this.timeClockTextSize = other.timeClockTextSize;
		this.timeClockTextOpacity = other.timeClockTextOpacity;
		this.timeClockShowBackground = other.timeClockShowBackground;
		this.timeClockBackgroundColor = other.timeClockBackgroundColor;
		this.timeClockBackgroundOpacity = other.timeClockBackgroundOpacity;
		this.timeClock24Hour = other.timeClock24Hour;
		this.timeClockMinimalist = other.timeClockMinimalist;
		// Reference resolution
		this.referenceWidth = other.referenceWidth;
		this.referenceHeight = other.referenceHeight;
	}
	
	// ==================== Color Helper Methods ====================
	
	/**
	 * Get coordinates text color with alpha applied.
	 */
	public int getCoordinatesTextColorWithAlpha() {
		int rgb = parseHexColor(coordinatesTextColor);
		int alpha = (int) (255 * (coordinatesTextOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	/**
	 * Get coordinates background color with alpha applied.
	 */
	public int getCoordinatesBackgroundColorWithAlpha() {
		int rgb = parseHexColor(coordinatesBackgroundColor);
		int alpha = (int) (255 * (coordinatesBackgroundOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	/**
	 * Get biome text color with alpha applied.
	 */
	public int getBiomeTextColorWithAlpha() {
		int rgb = parseHexColor(biomeTextColor);
		int alpha = (int) (255 * (biomeTextOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	/**
	 * Get biome background color with alpha applied.
	 */
	public int getBiomeBackgroundColorWithAlpha() {
		int rgb = parseHexColor(biomeBackgroundColor);
		int alpha = (int) (255 * (biomeBackgroundOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	/**
	 * Get time clock text color with alpha applied.
	 */
	public int getTimeClockTextColorWithAlpha() {
		int rgb = parseHexColor(timeClockTextColor);
		int alpha = (int) (255 * (timeClockTextOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	/**
	 * Get time clock background color with alpha applied.
	 */
	public int getTimeClockBackgroundColorWithAlpha() {
		int rgb = parseHexColor(timeClockBackgroundColor);
		int alpha = (int) (255 * (timeClockBackgroundOpacity / 100.0f));
		return (alpha << 24) | rgb;
	}
	
	/**
	 * Get the FPS-based adaptive color.
	 * Red for low FPS, Yellow for medium, Green for high.
	 */
	public int getAdaptiveColor(int fps) {
		if (fps <= lowFpsThreshold) {
			return 0xFF5555; // Red
		} else if (fps >= highFpsThreshold) {
			return 0x55FF55; // Green
		} else {
			return 0xFFFF55; // Yellow
		}
	}
}
