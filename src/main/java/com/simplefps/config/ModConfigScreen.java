package com.simplefps.config;

import com.simplefps.SimpleFPSClient;
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

	// Flag to suppress saving when reopening from color picker
	private static boolean skipNextSave = false;

	public static Screen createConfigScreen(Screen parent) {
		return createConfigScreen(parent, 0);
	}
	
	public static Screen createConfigScreen(Screen parent, int initialTab) {
		SimpleFPSConfig config = SimpleFPSConfig.getInstance();
		
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Text.translatable("simplefps.config.title"))
			.setTransparentBackground(true) // Allow seeing the game behind
			.setAfterInitConsumer(screen -> {
				// Set the initial tab if specified
				if (screen instanceof me.shedaniel.clothconfig2.gui.AbstractConfigScreen configScreen) {
					if (initialTab > 0) {
						configScreen.selectedCategoryIndex = initialTab;
					}
					
					int buttonWidth = Math.min(200, (screen.width - 50 - 12) / 3);
					int saveButtonX = screen.width / 2 - buttonWidth - 3 - buttonWidth - 6;
					
					ButtonWidget saveButton = ButtonWidget.builder(
						Text.literal("Save"),
						button -> {
							configScreen.saveAll(false);
							config.save();
						}
					).dimensions(saveButtonX, screen.height - 26, buttonWidth, 20).build();
					
					Screens.getButtons(screen).add(saveButton);
					
					// Add color picker buttons at the bottom left
					int pickerWidth = 85;
					int pickerY = screen.height - 26;
					int pickerX = 5;
					
					ButtonWidget textColorPicker = ButtonWidget.builder(
						Text.literal("Text Color"),
						button -> {
							// Get CURRENT tab at click time
							int clickedTab = configScreen.selectedCategoryIndex;
							// Only work on tabs 0, 1, 2, 3 (FPS, Coordinates, Biome, Time Clock)
							if (clickedTab > 3) return;
							
							String categoryName = switch (clickedTab) {
								case 1 -> "Coordinates";
								case 2 -> "Biome";
								case 3 -> "Time Clock";
								default -> "FPS Counter";
							};
							
							SimpleFPSConfig cfg = SimpleFPSConfig.getInstance();
							String currentColor = getTextColorForCategory(cfg, categoryName);
							final int tabToRestore = clickedTab;
							final Screen originalParent = parent; // Capture parent explicitly
							
							MinecraftClient.getInstance().setScreen(new ColorPickerScreen(screen, currentColor, newColor -> {
								SimpleFPSConfig freshConfig = SimpleFPSConfig.getInstance();
								setTextColorForCategory(freshConfig, categoryName, newColor);
								freshConfig.save();
								
								// Skip the save that happens when closing old screen
								skipNextSave = true;
								
								// Create new screen with updated config values
								Screen newScreen = createConfigScreen(originalParent, tabToRestore);
								MinecraftClient.getInstance().setScreen(newScreen);
							}));
						}
					).dimensions(pickerX, pickerY, pickerWidth, 20).build();
					
					ButtonWidget bgColorPicker = ButtonWidget.builder(
						Text.literal("BG Color"),
						button -> {
							// Get CURRENT tab at click time
							int clickedTab = configScreen.selectedCategoryIndex;
							// Only work on tabs 0, 1, 2, 3 (FPS, Coordinates, Biome, Time Clock)
							if (clickedTab > 3) return;
							
							String categoryName = switch (clickedTab) {
								case 1 -> "Coordinates";
								case 2 -> "Biome";
								case 3 -> "Time Clock";
								default -> "FPS Counter";
							};
							
							SimpleFPSConfig cfg = SimpleFPSConfig.getInstance();
							String currentColor = getBgColorForCategory(cfg, categoryName);
							final int tabToRestore = clickedTab;
							final Screen originalParent = parent;
							
							MinecraftClient.getInstance().setScreen(new ColorPickerScreen(screen, currentColor, newColor -> {
								SimpleFPSConfig freshConfig = SimpleFPSConfig.getInstance();
								setBgColorForCategory(freshConfig, categoryName, newColor);
								freshConfig.save();
								
								// Skip the save that happens when closing old screen
								skipNextSave = true;

								Screen newScreen = createConfigScreen(originalParent, tabToRestore);
								MinecraftClient.getInstance().setScreen(newScreen);
							}));
						}
					).dimensions(pickerX + pickerWidth + 5, pickerY, pickerWidth, 20).build();
					
					Screens.getButtons(screen).add(textColorPicker);
					Screens.getButtons(screen).add(bgColorPicker);
					
					// Register to render the FPS preview AND dynamically show/hide color picker buttons
					ScreenEvents.afterRender(screen).register((scr, context, mouseX, mouseY, tickDelta) -> {
						// Only render FPS preview if enabled
						if (config.enabled) {
							FPSHudRenderer.renderFPS(context, false);
						}
						
						// Show/hide color picker buttons based on current tab
						// Tabs 0, 1, 2, 3 = FPS, Coordinates, Biome, Time Clock (have colors)
						// Tabs 4, 5, 6 = Graph, Adaptive, Keybinds (no color pickers)
						int currentTab = configScreen.selectedCategoryIndex;
						boolean showColorButtons = currentTab <= 3;
						textColorPicker.visible = showColorButtons;
						bgColorPicker.visible = showColorButtons;
					});
				}
			});

		// Save changes when closing
		builder.setSavingRunnable(() -> {
			// Skip saving if we're just recreating the screen from color picker
			if (skipNextSave) {
				skipNextSave = false;
				return;
			}
			
			// Update reference resolution so positions scale correctly when window is resized
			MinecraftClient client = MinecraftClient.getInstance();
			if (client != null && client.getWindow() != null) {
				config.updateReferenceResolution(
					client.getWindow().getScaledWidth(),
					client.getWindow().getScaledHeight()
				);
			}
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

		generalCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.showDirection"),
			config.showDirection)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.showDirection.tooltip"))
			.setSaveConsumer(newValue -> config.showDirection = newValue)
			.build());

		// Appearance options
		generalCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Colors: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use the 'Text Color' and 'BG Color' buttons at the bottom-left to pick colors.").formatted(Formatting.WHITE)))
			.build());

		generalCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.textOpacity"),
			config.textOpacity)
			.setDefaultValue(100)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.textOpacity.tooltip"))
			.setSaveConsumer(newValue -> config.textOpacity = newValue)
			.build());

		// Text size as percentage (50-400 = 0.5x to 4.0x)
		int textSizePercent = (int) (config.textSize * 100);
		generalCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.textSize"),
			textSizePercent)
			.setDefaultValue(100)
			.setMin(50)
			.setMax(400)
			.setTooltip(
				Text.translatable("simplefps.config.textSize.tooltip"),
				Text.literal("50 = half size, 100 = normal, 200 = double").formatted(Formatting.WHITE)
			)
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

		generalCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.backgroundOpacity"),
			config.backgroundOpacity)
			.setDefaultValue(50)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.backgroundOpacity.tooltip"))
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

		// ==================== Coordinates Category (Tab 1) ====================
		ConfigCategory coordinatesCategory = builder.getOrCreateCategory(
			Text.translatable("simplefps.config.category.coordinates"));

		coordinatesCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Note: ").formatted(Formatting.GOLD)
				.append(Text.literal("Displays your current X/Y/Z coordinates on screen.").formatted(Formatting.WHITE)))
			.build());

		coordinatesCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Colors: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use the 'Text Color' and 'BG Color' buttons at the bottom-left to pick colors.").formatted(Formatting.WHITE)))
			.build());

		coordinatesCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.coordinatesEnabled"),
			config.coordinatesEnabled)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.coordinatesEnabled.tooltip"))
			.setSaveConsumer(newValue -> config.coordinatesEnabled = newValue)
			.build());

		int coordTextSizePercent = (int) (config.coordinatesTextSize * 100);
		coordinatesCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.coordinatesTextSize"),
			coordTextSizePercent)
			.setDefaultValue(100)
			.setMin(50)
			.setMax(200)
			.setTooltip(Text.translatable("simplefps.config.coordinatesTextSize.tooltip"))
			.setSaveConsumer(newValue -> config.coordinatesTextSize = newValue / 100.0f)
			.build());

		coordinatesCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.coordinatesTextOpacity"),
			config.coordinatesTextOpacity)
			.setDefaultValue(100)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.coordinatesTextOpacity.tooltip"))
			.setSaveConsumer(newValue -> config.coordinatesTextOpacity = newValue)
			.build());

		coordinatesCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.coordinatesShowBackground"),
			config.coordinatesShowBackground)
			.setDefaultValue(true)
			.setTooltip(Text.translatable("simplefps.config.coordinatesShowBackground.tooltip"))
			.setSaveConsumer(newValue -> config.coordinatesShowBackground = newValue)
			.build());

		coordinatesCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.coordinatesBackgroundOpacity"),
			config.coordinatesBackgroundOpacity)
			.setDefaultValue(50)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.coordinatesBackgroundOpacity.tooltip"))
			.setSaveConsumer(newValue -> config.coordinatesBackgroundOpacity = newValue)
			.build());

		coordinatesCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Position: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use 'Drag HUD Elements' keybind to visually reposition.").formatted(Formatting.WHITE)))
			.build());

		coordinatesCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.coordinatesX"),
			config.coordinatesX)
			.setDefaultValue(5)
			.setMin(0)
			.setMax(3840)
			.setTooltip(Text.translatable("simplefps.config.coordinatesX.tooltip"))
			.setSaveConsumer(newValue -> config.coordinatesX = newValue)
			.build());

		coordinatesCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.coordinatesY"),
			config.coordinatesY)
			.setDefaultValue(50)
			.setMin(0)
			.setMax(2160)
			.setTooltip(Text.translatable("simplefps.config.coordinatesY.tooltip"))
			.setSaveConsumer(newValue -> config.coordinatesY = newValue)
			.build());

		// ==================== Biome Category (Tab 2) ====================
		ConfigCategory biomeCategory = builder.getOrCreateCategory(
			Text.translatable("simplefps.config.category.biome"));

		biomeCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Note: ").formatted(Formatting.GOLD)
				.append(Text.literal("Displays the current biome name on screen.").formatted(Formatting.WHITE)))
			.build());

		biomeCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Colors: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use the 'Text Color' and 'BG Color' buttons at the bottom-left to pick colors.").formatted(Formatting.WHITE)))
			.build());

		biomeCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.biomeEnabled"),
			config.biomeEnabled)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.biomeEnabled.tooltip"))
			.setSaveConsumer(newValue -> config.biomeEnabled = newValue)
			.build());

		int biomeTextSizePercent = (int) (config.biomeTextSize * 100);
		biomeCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.biomeTextSize"),
			biomeTextSizePercent)
			.setDefaultValue(100)
			.setMin(50)
			.setMax(200)
			.setTooltip(Text.translatable("simplefps.config.biomeTextSize.tooltip"))
			.setSaveConsumer(newValue -> config.biomeTextSize = newValue / 100.0f)
			.build());

		biomeCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.biomeTextOpacity"),
			config.biomeTextOpacity)
			.setDefaultValue(100)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.biomeTextOpacity.tooltip"))
			.setSaveConsumer(newValue -> config.biomeTextOpacity = newValue)
			.build());

		biomeCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.biomeShowBackground"),
			config.biomeShowBackground)
			.setDefaultValue(true)
			.setTooltip(Text.translatable("simplefps.config.biomeShowBackground.tooltip"))
			.setSaveConsumer(newValue -> config.biomeShowBackground = newValue)
			.build());

		biomeCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.biomeBackgroundOpacity"),
			config.biomeBackgroundOpacity)
			.setDefaultValue(50)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.biomeBackgroundOpacity.tooltip"))
			.setSaveConsumer(newValue -> config.biomeBackgroundOpacity = newValue)
			.build());

		biomeCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Position: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use 'Drag HUD Elements' keybind to visually reposition.").formatted(Formatting.WHITE)))
			.build());

		biomeCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.biomeX"),
			config.biomeX)
			.setDefaultValue(5)
			.setMin(0)
			.setMax(3840)
			.setTooltip(Text.translatable("simplefps.config.biomeX.tooltip"))
			.setSaveConsumer(newValue -> config.biomeX = newValue)
			.build());

		biomeCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.biomeY"),
			config.biomeY)
			.setDefaultValue(70)
			.setMin(0)
			.setMax(2160)
			.setTooltip(Text.translatable("simplefps.config.biomeY.tooltip"))
			.setSaveConsumer(newValue -> config.biomeY = newValue)
			.build());

		// ==================== Time Clock Category (Tab 3) ====================
		ConfigCategory timeClockCategory = builder.getOrCreateCategory(
			Text.translatable("simplefps.config.category.timeclock"));

		timeClockCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Note: ").formatted(Formatting.GOLD)
				.append(Text.literal("Displays the current Minecraft day number and time on screen. Time is synced with the day/night cycle.").formatted(Formatting.WHITE)))
			.build());

		timeClockCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Colors: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use the 'Text Color' and 'BG Color' buttons at the bottom-left to pick colors.").formatted(Formatting.WHITE)))
			.build());

		timeClockCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.timeClockEnabled"),
			config.timeClockEnabled)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.timeClockEnabled.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockEnabled = newValue)
			.build());

		timeClockCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.timeClock24Hour"),
			config.timeClock24Hour)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.timeClock24Hour.tooltip"))
			.setSaveConsumer(newValue -> config.timeClock24Hour = newValue)
			.build());

		timeClockCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.timeClockMinimalist"),
			config.timeClockMinimalist)
			.setDefaultValue(false)
			.setTooltip(Text.translatable("simplefps.config.timeClockMinimalist.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockMinimalist = newValue)
			.build());

		int timeClockTextSizePercent = (int) (config.timeClockTextSize * 100);
		timeClockCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.timeClockTextSize"),
			timeClockTextSizePercent)
			.setDefaultValue(100)
			.setMin(50)
			.setMax(200)
			.setTooltip(Text.translatable("simplefps.config.timeClockTextSize.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockTextSize = newValue / 100.0f)
			.build());

		timeClockCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.timeClockTextOpacity"),
			config.timeClockTextOpacity)
			.setDefaultValue(100)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.timeClockTextOpacity.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockTextOpacity = newValue)
			.build());

		timeClockCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.translatable("simplefps.config.timeClockShowBackground"),
			config.timeClockShowBackground)
			.setDefaultValue(true)
			.setTooltip(Text.translatable("simplefps.config.timeClockShowBackground.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockShowBackground = newValue)
			.build());

		timeClockCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.timeClockBackgroundOpacity"),
			config.timeClockBackgroundOpacity)
			.setDefaultValue(50)
			.setMin(0)
			.setMax(100)
			.setTooltip(Text.translatable("simplefps.config.timeClockBackgroundOpacity.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockBackgroundOpacity = newValue)
			.build());

		timeClockCategory.addEntry(entryBuilder.startTextDescription(
			Text.literal("Position: ").formatted(Formatting.GOLD)
				.append(Text.literal("Use 'Drag HUD Elements' keybind to visually reposition.").formatted(Formatting.WHITE)))
			.build());

		timeClockCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.timeClockX"),
			config.timeClockX)
			.setDefaultValue(5)
			.setMin(0)
			.setMax(3840)
			.setTooltip(Text.translatable("simplefps.config.timeClockX.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockX = newValue)
			.build());

		timeClockCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.timeClockY"),
			config.timeClockY)
			.setDefaultValue(90)
			.setMin(0)
			.setMax(2160)
			.setTooltip(Text.translatable("simplefps.config.timeClockY.tooltip"))
			.setSaveConsumer(newValue -> config.timeClockY = newValue)
			.build());

		// ==================== FPS Graph Category (Tab 4) ====================
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

		graphCategory.addEntry(entryBuilder.startIntField(
			Text.translatable("simplefps.config.graphScale"),
			config.graphScale)
			.setDefaultValue(100)
			.setMin(50)
			.setMax(200)
			.setTooltip(Text.translatable("simplefps.config.graphScale.tooltip"))
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

		// ==================== Adaptive Color Category (Tab 5) ====================
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

		// ==================== Keybindings Category (Tab 6) ====================
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
			Text.translatable("simplefps.key.reload"),
			SimpleFPSClient.reloadKeyBinding)
			.build());

		return builder.build();
	}
	
	// Get the text color for the specified category
	private static String getTextColorForCategory(SimpleFPSConfig config, String category) {
		return switch (category) {
			case "Coordinates" -> config.coordinatesTextColor;
			case "Biome" -> config.biomeTextColor;
			case "Time Clock" -> config.timeClockTextColor;
			default -> config.textColor; // FPS Counter and others
		};
	}
	
	// Set the text color for the specified category
	private static void setTextColorForCategory(SimpleFPSConfig config, String category, String color) {
		switch (category) {
			case "Coordinates" -> config.coordinatesTextColor = color;
			case "Biome" -> config.biomeTextColor = color;
			case "Time Clock" -> config.timeClockTextColor = color;
			default -> config.textColor = color; // FPS Counter
		}
	}
	
	// Get the background color for the specified category
	private static String getBgColorForCategory(SimpleFPSConfig config, String category) {
		return switch (category) {
			case "Coordinates" -> config.coordinatesBackgroundColor;
			case "Biome" -> config.biomeBackgroundColor;
			case "Time Clock" -> config.timeClockBackgroundColor;
			default -> config.backgroundColor; // FPS Counter
		};
	}
	
	// Set the background color for the specified category
	private static void setBgColorForCategory(SimpleFPSConfig config, String category, String color) {
		switch (category) {
			case "Coordinates" -> config.coordinatesBackgroundColor = color;
			case "Biome" -> config.biomeBackgroundColor = color;
			case "Time Clock" -> config.timeClockBackgroundColor = color;
			default -> config.backgroundColor = color; // FPS Counter
		}
	}
}
