package com.simplefps;

import com.simplefps.config.SimpleFPSConfig;
import com.simplefps.gui.HudDragScreen;
import com.simplefps.hud.FPSGraphRenderer;
import com.simplefps.hud.FPSHudRenderer;
import com.simplefps.hud.CoordinatesRenderer;
import com.simplefps.hud.BiomeRenderer;
import com.simplefps.hud.TimeClockRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}
import net.minecraft.client.KeyMapping;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleFPSClient implements ClientModInitializer {
	public static final String MOD_ID = "simplefps";
	public static final Logger LOGGER = LoggerFactory.getLogger("SimpleFPS");
	
	// Define a custom category for our keybindings
	//? if >=1.21.11 {
	private static final KeyMapping.Category SIMPLEFPS_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, "category"));
	//?} else {
	/*private static final KeyMapping.Category SIMPLEFPS_CATEGORY = new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, "category"));*/
	//?}
	
	public static KeyMapping toggleKeyBinding;
	public static KeyMapping configKeyBinding;
	public static KeyMapping dragKeyBinding;
	public static KeyMapping reloadKeyBinding;
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing SimpleFPS Client");
		
		// Load config
		SimpleFPSConfig.load();
		
		// Register HUD renderers
		//? if >=26.1 {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hud"), (context, tickCounter) -> {
			FPSHudRenderer.render(context, tickCounter);
			FPSGraphRenderer.renderGraph(context, false);
			CoordinatesRenderer.render(context, false);
			BiomeRenderer.render(context, false);
			TimeClockRenderer.render(context, false);
		});
		//?} else {
		/*HudRenderCallback.EVENT.register((context, tickCounter) -> {
			FPSHudRenderer.render(context, tickCounter);
			FPSGraphRenderer.renderGraph(context, false);
			CoordinatesRenderer.render(context, false);
			BiomeRenderer.render(context, false);
			TimeClockRenderer.render(context, false);
		});
		*///?}
		
		// Register keybindings (all unbound by default)
		//? if >=26.1 {
		toggleKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.simplefps.toggle",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		
		configKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.simplefps.config",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		
		dragKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.simplefps.drag",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		
		reloadKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.simplefps.reload",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		//?} else {
		/*toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.simplefps.toggle",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		
		configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.simplefps.config",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		
		dragKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.simplefps.drag",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		
		reloadKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.simplefps.reload",
			GLFW.GLFW_KEY_UNKNOWN,
			SIMPLEFPS_CATEGORY
		));
		*///?}
		
		// Register tick event for keybinding handling
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKeyBinding.consumeClick()) {
				SimpleFPSConfig config = SimpleFPSConfig.getInstance();
				config.enabled = !config.enabled;
				config.save();
				LOGGER.info("FPS Counter toggled: {}", config.enabled);
			}
			
			while (configKeyBinding.consumeClick()) {
				if (client.screen == null) {
					try {
						Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
						client.setScreen(com.simplefps.config.ModConfigScreen.createConfigScreen(null));
					} catch (Throwable e) {
						client.setScreen(new com.simplefps.config.SimpleFPSConfigScreen(null));
					}
				}
			}
			
			while (dragKeyBinding.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new HudDragScreen(null));
				}
			}
			
			while (reloadKeyBinding.consumeClick()) {
				SimpleFPSConfig.reload();
				LOGGER.info("SimpleFPS config reloaded from file");
			}
		});
		
		LOGGER.info("SimpleFPS Client initialized successfully!");
	}
}
