package com.simplefps.config;

import com.simplefps.SimpleFPSClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

/**
 * ModMenu integration for SimpleFPS.
 * Provides access to the configuration screen from ModMenu.
 * 
 * This implementation tries to use Cloth Config first for a richer experience,
 * but falls back to a custom lightweight config screen if Cloth Config is
 * unavailable or incompatible with the current Minecraft version.
 */
public class ModMenuIntegration implements ModMenuApi {
	
	// Cache the result of Cloth Config compatibility check
	private static Boolean clothConfigCompatible = null;
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return this::createConfigScreen;
	}
	
	/**
	 * Creates the configuration screen, trying Cloth Config first with fallback.
	 */
	private Screen createConfigScreen(Screen parent) {
		// Check if Cloth Config is compatible (only check once)
		if (clothConfigCompatible == null) {
			clothConfigCompatible = checkClothConfigCompatibility();
		}
		
		if (clothConfigCompatible) {
			try {
				SimpleFPSClient.LOGGER.debug("Using Cloth Config for config screen");
				return ModConfigScreen.createConfigScreen(parent);
			} catch (Throwable e) {
				// Cloth Config failed at runtime - mark as incompatible for future
				clothConfigCompatible = false;
				SimpleFPSClient.LOGGER.warn("Cloth Config failed at runtime, switching to fallback: {}", e.getMessage());
			}
		}
		
		// Use fallback config screen
		SimpleFPSClient.LOGGER.info("Using fallback config screen (Cloth Config unavailable or incompatible)");
		return new SimpleFPSConfigScreen(parent);
	}
	
	/**
	 * Checks if Cloth Config is compatible with the current Minecraft version.
	 * This proactively tests if the library will work before trying to use it.
	 */
	private boolean checkClothConfigCompatibility() {
		try {
			// Try to load a Cloth Config class that would fail if incompatible
			Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
			
			// Cloth Config is present - assume compatible
			// Runtime errors will be caught in createConfigScreen if it fails
			SimpleFPSClient.LOGGER.debug("Cloth Config found, assuming compatible");
			return true;
		} catch (ClassNotFoundException e) {
			SimpleFPSClient.LOGGER.debug("Cloth Config not found: {}", e.getMessage());
			return false;
		} catch (Throwable e) {
			SimpleFPSClient.LOGGER.warn("Error checking Cloth Config compatibility: {}", e.getMessage());
			return false;
		}
	}
}
