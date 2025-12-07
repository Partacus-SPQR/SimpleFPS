package com.simplefps;

import com.simplefps.config.SimpleFPSConfig;
import com.simplefps.gui.GraphDragScreen;
import com.simplefps.hud.FPSGraphRenderer;
import com.simplefps.hud.FPSHudRenderer;
import com.simplefps.screen.FPSDragScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleFPSClient implements ClientModInitializer {
	public static final String MOD_ID = "simplefps";
	public static final Logger LOGGER = LoggerFactory.getLogger("SimpleFPS");
	
	// Define a custom category for our keybindings
	// The translation key will be "key.category.simplefps.category"
	private static final KeyBinding.Category SIMPLEFPS_CATEGORY = new KeyBinding.Category(Identifier.of(MOD_ID, "category"));
	
	public static KeyBinding toggleKeyBinding;
	public static KeyBinding configKeyBinding;
	public static KeyBinding dragKeyBinding;
	public static KeyBinding dragGraphKeyBinding;
	public static KeyBinding reloadKeyBinding;
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing SimpleFPS Client");
		
		// Load config
		SimpleFPSConfig.load();
		
		// Register the FPS HUD renderer using HudRenderCallback
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			FPSHudRenderer.render(context, tickCounter);
			// Also render the graph if enabled
			FPSGraphRenderer.renderGraph(context, false);
		});
		
		// Register keybindings with the new Category-based constructor
		// All keybindings are unbound by default - users can set their own in Controls
		toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.simplefps.toggle",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			SIMPLEFPS_CATEGORY
		));
		
		configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.simplefps.config",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			SIMPLEFPS_CATEGORY
		));
		
		dragKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.simplefps.drag",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			SIMPLEFPS_CATEGORY
		));
		
		dragGraphKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.simplefps.dragGraph",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			SIMPLEFPS_CATEGORY
		));
		
		reloadKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.simplefps.reload",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			SIMPLEFPS_CATEGORY
		));
		
		// Register tick event for keybinding handling
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKeyBinding.wasPressed()) {
				SimpleFPSConfig config = SimpleFPSConfig.getInstance();
				config.enabled = !config.enabled;
				config.save();
				LOGGER.info("FPS Counter toggled: {}", config.enabled);
			}
			
			while (configKeyBinding.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(com.simplefps.config.ModConfigScreen.createConfigScreen(null));
				}
			}
			
			while (dragKeyBinding.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new FPSDragScreen(null));
				}
			}
			
			while (dragGraphKeyBinding.wasPressed()) {
				if (client.currentScreen == null) {
					SimpleFPSConfig config = SimpleFPSConfig.getInstance();
					if (config.graphEnabled) {
						client.setScreen(new GraphDragScreen(null));
					}
				}
			}
			
			while (reloadKeyBinding.wasPressed()) {
				SimpleFPSConfig.reload();
				LOGGER.info("SimpleFPS config reloaded from file");
			}
		});
		
		LOGGER.info("SimpleFPS Client initialized successfully!");
	}
}
