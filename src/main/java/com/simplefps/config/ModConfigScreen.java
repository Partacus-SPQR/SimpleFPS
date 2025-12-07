package com.simplefps.config;

import com.simplefps.SimpleFPSClient;
import com.simplefps.hud.FPSGraphRenderer;
import com.simplefps.hud.FPSHudRenderer;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModConfigScreen {

	public static Screen createConfigScreen(Screen parent) {
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();

		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Text.translatable("simplefps.config.title"))
			.setTransparentBackground(true) // Allow seeing the game behind
			.setAfterInitConsumer(screen -> {
				// Register to render the FPS preview on top of this screen
				ScreenEvents.afterRender(screen).register((scr, context, mouseX, mouseY, tickDelta) -> {
					// Only render previews if they are enabled
					if (config.enabled) {
						FPSHudRenderer.renderFPS(context, false);
					}
					if (config.graphEnabled) {
						FPSGraphRenderer.renderGraph(context, false);
					}
				});
				
				// Add a "Save" button to the screen
				if (screen instanceof me.shedaniel.clothconfig2.gui.AbstractConfigScreen configScreen) {
					int buttonWidth = Math.min(200, (screen.width - 50 - 12) / 3);
					int saveButtonX = screen.width / 2 - buttonWidth - 3 - buttonWidth - 6; // Left of Cancel
					
					ButtonWidget saveButton = ButtonWidget.builder(
						Text.literal("Save"),
						button -> {
							// Call saveAll which triggers all the save consumers, then save to file
							configScreen.saveAll(false);
							config.save();
						}
					).dimensions(saveButtonX, screen.height - 26, buttonWidth, 20).build();
					
					Screens.getButtons(screen).add(saveButton);
					
					// Add color picker buttons at the bottom left (visible area)
					int pickerWidth = 85;
					int pickerY = screen.height - 26; // Same row as other buttons
					int pickerX = 5; // Left side of screen
					
					ButtonWidget textColorPicker = ButtonWidget.builder(
						Text.literal("Pick Text Color"),
						button -> {
							// First save current config
							configScreen.saveAll(false);
							config.save();
							// Open color picker for text color
							MinecraftClient.getInstance().setScreen(new ColorPickerScreen(screen, config.textColor, newColor -> {
								config.textColor = newColor;
								config.save();
								// Re-open config screen after selection
								MinecraftClient.getInstance().setScreen(createConfigScreen(parent));
							}));
						}
					).dimensions(pickerX, pickerY, pickerWidth, 20).build();
					
					ButtonWidget bgColorPicker = ButtonWidget.builder(
						Text.literal("Pick BG Color"),
						button -> {
							// First save current config
							configScreen.saveAll(false);
							config.save();
							// Open color picker for background color
							MinecraftClient.getInstance().setScreen(new ColorPickerScreen(screen, config.backgroundColor, newColor -> {
								config.backgroundColor = newColor;
								config.save();
								// Re-open config screen after selection
								MinecraftClient.getInstance().setScreen(createConfigScreen(parent));
							}));
						}
					).dimensions(pickerX + pickerWidth + 5, pickerY, pickerWidth, 20).build();
					
					Screens.getButtons(screen).add(textColorPicker);
					Screens.getButtons(screen).add(bgColorPicker);
				}
			});

		// Save changes when closing
		builder.setSavingRunnable(() -> {
			config.save();
		});

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		// General Category - includes FPS counter settings, appearance, position, and background
		ConfigCategory generalCategory = builder.getOrCreateCategory(
			Text.translatable("simplefps.config.category.general"));

		// Add tip about Save button
		generalCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Tip: ").formatted(Formatting.GOLD)
				.append(Text.literal("Click \"Save\" at the bottom to preview changes while in the config, or \"Save & Quit\" to save and exit.").formatted(Formatting.WHITE)))
			.build());

		// Enable toggle
		generalCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.enabled"),
			config.enabled)
			.setDefaultValue(true)
			.setTooltip(Text.translatable("simplefps.config.enabled.tooltip"))
			.setSaveConsumer(newValue -> config.enabled = newValue)
			.build());

		generalCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.showLabel"),
			config.showLabel)
			.setDefaultValue(true)
			.setTooltip(Text.translatable("simplefps.config.showLabel.tooltip"))
			.setSaveConsumer(newValue -> config.showLabel = newValue)
			.build());

		// Appearance options - add note about color picker buttons
		generalCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Color Pickers: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use the 'Pick Text Color' and 'Pick BG Color' buttons at the bottom-left of this screen.").formatted(Formatting.WHITE)))
			.build());

		generalCategory.addEntry(entryBuilder.startStrField(
			Text.translatable("simplefps.config.textColor"),
			config.textColor)
			.setDefaultValue("#FFFFFF")
			.setTooltip(
				Text.translatable("simplefps.config.textColor.tooltip"),
				Text.literal("Or use the 'Pick Text Color' button at the bottom!").formatted(Formatting.GOLD)
			)
			.setSaveConsumer(newValue -> config.textColor = newValue)
			.build());

		generalCategory.addEntry(entryBuilder.startIntSlider(
			Text.translatable("simplefps.config.textOpacity"),
			config.textOpacity,
			0, 100)
			.setDefaultValue(100)
			.setTooltip(Text.translatable("simplefps.config.textOpacity.tooltip"))
			.setTextGetter(value -> Text.literal(value + "%"))
			.setSaveConsumer(newValue -> config.textOpacity = newValue)
			.build());

		// Convert textSize (0.5-4.0) to percentage (50-400) for slider
		int textSizePercent = (int) (config.textSize * 100);
		generalCategory.addEntry(entryBuilder.startIntSlider(
			Text.translatable("simplefps.config.textSize"),
			textSizePercent,
			50, 400)
			.setDefaultValue(100)
			.setTooltip(
				Text.translatable("simplefps.config.textSize.tooltip"),
				Text.literal("50% = half size, 100% = normal, 200% = double").formatted(Formatting.WHITE)
			)
			.setTextGetter(value -> Text.literal(value + "%"))
			.setSaveConsumer(newValue -> config.textSize = newValue / 100.0f)
			.build());

		// Background options
		generalCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.showBackground"),
			config.showBackground)
			.setDefaultValue(true)
			.setTooltip(Text.translatable("simplefps.config.showBackground.tooltip"))
			.setSaveConsumer(newValue -> config.showBackground = newValue)
			.build());

		generalCategory.addEntry(entryBuilder.startStrField(
			Text.translatable("simplefps.config.backgroundColor"),
			config.backgroundColor)
			.setDefaultValue("#000000")
			.setTooltip(
				Text.translatable("simplefps.config.backgroundColor.tooltip"),
				Text.literal("Use the 'BG Color' button at the bottom for a color picker!").formatted(Formatting.GOLD)
			)
			.setSaveConsumer(newValue -> config.backgroundColor = newValue)
			.build());

		generalCategory.addEntry(entryBuilder.startIntSlider(
			Text.translatable("simplefps.config.backgroundOpacity"),
			config.backgroundOpacity,
			0, 100)
			.setDefaultValue(50)
			.setTooltip(Text.translatable("simplefps.config.backgroundOpacity.tooltip"))
			.setTextGetter(value -> Text.literal(value + "%"))
			.setSaveConsumer(newValue -> config.backgroundOpacity = newValue)
			.build());

		// Position options
		generalCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Position: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use the keybind 'Drag FPS Counter' (in Controls) to visually drag, or set coordinates below.").formatted(Formatting.WHITE)))
			.build());

		generalCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.positionX"),
			config.positionX)
			.setDefaultValue(5)
			.setMin(0)
			.setMax(3840)
			.setTooltip(Text.translatable("simplefps.config.positionX.tooltip"))
			.setSaveConsumer(newValue -> config.positionX = newValue)
			.build());

		generalCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.positionY"),
			config.positionY)
			.setDefaultValue(5)
			.setMin(0)
			.setMax(2160)
			.setTooltip(Text.translatable("simplefps.config.positionY.tooltip"))
			.setSaveConsumer(newValue -> config.positionY = newValue)
			.build());

		// Adaptive Color Category
		ConfigCategory adaptiveCategory = builder.getOrCreateCategory(
			Text.translatable("simplefps.config.category.adaptive"));

		adaptiveCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Note: ").formatted(Formatting.GOLD)
				.append(Text.literal("Enabling adaptive colors will override the custom text color. Colors change based on FPS:").formatted(Formatting.WHITE))
				.append(Text.literal("\n  Red").formatted(Formatting.RED))
				.append(Text.literal(" = Low FPS (at or below threshold)").formatted(Formatting.WHITE))
				.append(Text.literal("\n  Yellow").formatted(Formatting.YELLOW))
				.append(Text.literal(" = Medium FPS (between thresholds)").formatted(Formatting.WHITE))
				.append(Text.literal("\n  Green").formatted(Formatting.GREEN))
				.append(Text.literal(" = High FPS (at or above threshold)").formatted(Formatting.WHITE)))
			.build());

		adaptiveCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.adaptiveColor"),
			config.adaptiveColorEnabled)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.adaptiveColor.tooltip"))
			.setSaveConsumer(newValue -> config.adaptiveColorEnabled = newValue)
			.build());

		adaptiveCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.lowFpsThreshold"),
			config.lowFpsThreshold)
			.setDefaultValue(25)
			.setMin(1)
			.setMax(999)
			.setTooltip(Text.translatable("simplefps.config.lowFpsThreshold.tooltip"))
			.setSaveConsumer(newValue -> config.lowFpsThreshold = newValue)
			.build());

		adaptiveCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.highFpsThreshold"),
			config.highFpsThreshold)
			.setDefaultValue(60)
			.setMin(1)
			.setMax(999)
			.setTooltip(Text.translatable("simplefps.config.highFpsThreshold.tooltip"))
			.setSaveConsumer(newValue -> config.highFpsThreshold = newValue)
			.build());

		// FPS Graph Category
		ConfigCategory graphCategory = builder.getOrCreateCategory(
			Text.translatable("simplefps.config.category.graph"));

		graphCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Note: ").formatted(Formatting.GOLD)
				.append(Text.literal("The FPS Graph shows FPS history with Min/Max/Avg stats. When enabled, it can be dragged to reposition. Stats reset every 30 seconds.").formatted(Formatting.WHITE)))
			.build());

		graphCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Tip: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use the keybind 'Drag FPS Graph' (set in Controls) to visually drag the graph, or set coordinates below.").formatted(Formatting.WHITE)))
			.build());

		graphCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.graphEnabled"),
			config.graphEnabled)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.graphEnabled.tooltip"))
			.setSaveConsumer(newValue -> config.graphEnabled = newValue)
			.build());

		graphCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.literal("Show Graph Background").formatted(Formatting.WHITE),
			config.graphShowBackground)
			.setDefaultValue(true)
			.setTooltip(Text.literal("Show the black background and white border around the graph"))
			.setSaveConsumer(newValue -> config.graphShowBackground = newValue)
			.build());

		graphCategory.addEntry(entryBuilder.startIntSlider(
			Text.translatable("simplefps.config.graphScale"),
			config.graphScale,
			50, 200)
			.setDefaultValue(100)
			.setTooltip(Text.translatable("simplefps.config.graphScale.tooltip"))
			.setTextGetter(value -> Text.literal(value + "%"))
			.setSaveConsumer(newValue -> config.graphScale = newValue)
			.build());

		graphCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.graphX"),
			config.graphX)
			.setDefaultValue(5)
			.setMin(0)
			.setMax(3840)
			.setTooltip(Text.translatable("simplefps.config.graphX.tooltip"))
			.setSaveConsumer(newValue -> config.graphX = newValue)
			.build());

		graphCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.graphY"),
			config.graphY)
			.setDefaultValue(100)
			.setMin(0)
			.setMax(2160)
			.setTooltip(Text.translatable("simplefps.config.graphY.tooltip"))
			.setSaveConsumer(newValue -> config.graphY = newValue)
			.build());

		// Graph Thresholds section
		graphCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Graph Thresholds:").formatted(Formatting.GOLD))
			.build());

		graphCategory.addEntry(entryBuilder.startIntField(
			Text.literal("Low FPS Threshold (Red)").formatted(Formatting.WHITE),
			config.graphLowFpsThreshold)
			.setDefaultValue(30)
			.setMin(1)
			.setMax(999)
			.setTooltip(Text.literal("FPS at or below this value will be shown in red on the graph"))
			.setSaveConsumer(newValue -> config.graphLowFpsThreshold = newValue)
			.build());

		graphCategory.addEntry(entryBuilder.startIntField(
			Text.literal("High FPS Threshold (Green)").formatted(Formatting.WHITE),
			config.graphHighFpsThreshold)
			.setDefaultValue(60)
			.setMin(1)
			.setMax(999)
			.setTooltip(Text.literal("FPS at or above this value will be shown in green on the graph"))
			.setSaveConsumer(newValue -> config.graphHighFpsThreshold = newValue)
			.build());

		// Keybindings Category
		ConfigCategory keybindsCategory = builder.getOrCreateCategory(
			Text.translatable("simplefps.config.category.keybinds"));

		// Add instruction text for keybinds
		keybindsCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Note: ").formatted(Formatting.GOLD)
				.append(Text.literal("Keybinds set here are also accessible in Options > Controls > Key Binds.").formatted(Formatting.WHITE)))
			.build());

		keybindsCategory.addEntry(entryBuilder.fillKeybindingField(
			Text.translatable("simplefps.key.toggle"),
			SimpleFPSClient.toggleKeyBinding)
			.build());

		keybindsCategory.addEntry(entryBuilder.fillKeybindingField(
			Text.translatable("simplefps.key.config"),
			SimpleFPSClient.configKeyBinding)
			.build());

		keybindsCategory.addEntry(entryBuilder.fillKeybindingField(
			Text.translatable("simplefps.key.drag"),
			SimpleFPSClient.dragKeyBinding)
			.build());

		keybindsCategory.addEntry(entryBuilder.fillKeybindingField(
			Text.translatable("simplefps.key.dragGraph"),
			SimpleFPSClient.dragGraphKeyBinding)
			.build());

		keybindsCategory.addEntry(entryBuilder.fillKeybindingField(
			Text.translatable("simplefps.key.reload"),
			SimpleFPSClient.reloadKeyBinding)
			.build());

		return builder.build();
	}
}
