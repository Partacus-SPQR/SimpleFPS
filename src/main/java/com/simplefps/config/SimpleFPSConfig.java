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

	// General options
	public boolean enabled = true;
	public boolean showLabel = true;
	
	// Position options (can be dragged by user)
	public int positionX = 5;
	public int positionY = 5;
	
	// Appearance options
	public String textColor = "#FFFFFF";
	public float textSize = 1.0f;
	public int textOpacity = 100; // 0-100 percentage
	
	// Background options
	public boolean showBackground = true;
	public String backgroundColor = "#000000";
	public int backgroundOpacity = 50; // 0-100 percentage
	
	// Adaptive color options
	public boolean adaptiveColorEnabled = false;
	public int lowFpsThreshold = 25;  // Red at or below this
	public int highFpsThreshold = 60; // Green at or above this
	
	// FPS Graph options
	public boolean graphEnabled = false;
	public int graphX = 5;
	public int graphY = 100;
	public int graphScale = 100; // 50-200%
	public boolean graphShowBackground = true;
	
	// Graph-specific thresholds (separate from FPS counter adaptive colors)
	public int graphLowFpsThreshold = 30;
	public int graphHighFpsThreshold = 60;

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
	
	/**
	 * Creates a copy of this config for live preview purposes.
	 */
	public SimpleFPSConfig copy() {
		SimpleFPSConfig copy = new SimpleFPSConfig();
		copy.enabled = this.enabled;
		copy.showLabel = this.showLabel;
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
		copy.graphEnabled = this.graphEnabled;
		copy.graphX = this.graphX;
		copy.graphY = this.graphY;
		copy.graphScale = this.graphScale;
		return copy;
	}
	
	/**
	 * Copies values from another config into this one.
	 */
	public void copyFrom(SimpleFPSConfig other) {
		this.enabled = other.enabled;
		this.showLabel = other.showLabel;
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
		this.graphEnabled = other.graphEnabled;
		this.graphX = other.graphX;
		this.graphY = other.graphY;
		this.graphScale = other.graphScale;
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
