# Making Fabric Mods with Optional Dependencies (Cloth Config & ModMenu)

This guide documents how to make a Fabric mod that works **standalone** with only Fabric API required, while optionally supporting Cloth Config and ModMenu for enhanced features when installed.

## Overview

**Goal:** Create a mod where:
- ✅ **Fabric API** is the ONLY required dependency
- ✅ **Cloth Config** is optional but recommended (provides better config UI)
- ✅ **ModMenu** is optional but recommended (provides mod list integration)
- ✅ Mod works perfectly fine without either optional dependency
- ✅ **CONFIG KEYBIND IS MANDATORY** - Opens config screen (uses Cloth Config if available, fallback otherwise)
- ✅ **ALL KEYBINDS MUST BE UNBOUND BY DEFAULT** (use `GLFW.GLFW_KEY_UNKNOWN`)

---

## Part 1: Upgrading Fabric Mod from 1.21.10 → 1.21.11

### Step 1: Create Update Branch (Recommended)
```bash
git checkout -b update/1.21.11
```

### Step 2: Update `gradle.properties`
```properties
minecraft_version=1.21.11
yarn_mappings=1.21.11+build.1
loader_version=0.18.2
fabric_version=0.139.4+1.21.11
```

### Step 3: Update `build.gradle` (Loom Plugin)
```gradle
plugins {
    id 'fabric-loom' version '1.14-SNAPSHOT'
    id 'maven-publish'
}
```

### Step 4: Update `gradle/wrapper/gradle-wrapper.properties`
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
```

### Step 5: Update `src/main/resources/fabric.mod.json`
```json
"depends": {
    "fabricloader": ">=0.18.0",
    "minecraft": "~1.21.11",
    "java": ">=21",
    "fabric-api": "*"
}
```

### Step 6: Clean and Build
```powershell
.\gradlew clean build --refresh-dependencies
```

### Step 7: (If Build Fails) Run Mappings Migration
Only if you get "cannot find symbol" errors:
```powershell
.\gradlew migrateMappings --mappings "1.21.11+build.1"
```
Then copy files from `remappedSrc` to `src/main/java`.

### Version Summary Table
| Component | Old (1.21.10) | New (1.21.11) |
|-----------|---------------|---------------|
| Minecraft | 1.21.10 | 1.21.11 |
| Yarn Mappings | 1.21.10+build.1 | 1.21.11+build.1 |
| Fabric Loader | 0.16.9 | 0.18.2 |
| Fabric API | 0.138.3+1.21.10 | 0.139.4+1.21.11 |
| Fabric Loom | 1.11.7 | 1.14-SNAPSHOT |
| Gradle | 8.14 | 9.2.1 |

⚠️ **Important:** Loom 1.14 requires Gradle 9.2+. This is a significant upgrade!

---

## Part 2: Making Cloth Config & ModMenu Optional

### Step 1: Update `build.gradle` Dependencies

Change from `modImplementation` to `modCompileOnly` for optional dependencies:

```gradle
dependencies {
    // Required
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
    
    // OPTIONAL - Cloth Config (compile only, not bundled)
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:20.0.149") {
        exclude(group: "net.fabricmc.fabric-api")
    }
    
    // OPTIONAL - ModMenu (runtime only for dev testing, compile only for release)
    modRuntimeOnly "com.terraformersmc:modmenu:17.0.0-alpha.1"
    modCompileOnly "com.terraformersmc:modmenu:17.0.0-alpha.1"
}
```

**Key differences:**
- `modImplementation` = Required, bundled with mod
- `modCompileOnly` = Available at compile time, NOT bundled (user must install)
- `modRuntimeOnly` = Available during `runClient` for testing

### Step 2: Update `fabric.mod.json`

Change `depends` to `recommends` for optional dependencies:

```json
{
    "depends": {
        "fabricloader": ">=0.18.0",
        "minecraft": "~1.21.11",
        "java": ">=21",
        "fabric-api": "*"
    },
    "recommends": {
        "modmenu": ">=17.0.0",
        "cloth-config": ">=20.0.0"
    }
}
```

### Step 3: Create ModMenu Integration Class

Create `src/main/java/com/yourmod/config/ModMenuIntegration.java`:

```java
package com.yourmod.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModMenuIntegration implements ModMenuApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("YourMod");
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> getConfigScreen(parent);
    }
    
    /**
     * Gets the appropriate config screen based on available dependencies.
     * Tries Cloth Config first, falls back to vanilla screen if unavailable.
     */
    public static Screen getConfigScreen(Screen parent) {
        // Try Cloth Config first
        if (isClothConfigAvailable()) {
            try {
                return ModConfigScreen.createConfigScreen(parent);
            } catch (Throwable e) {
                LOGGER.warn("Cloth Config available but failed to create screen, using fallback", e);
            }
        }
        
        // Fallback to vanilla config screen
        LOGGER.info("Using fallback config screen (Cloth Config unavailable or incompatible)");
        return new SimpleFallbackConfigScreen(parent);
    }
    
    /**
     * Checks if Cloth Config is available at runtime.
     */
    private static boolean isClothConfigAvailable() {
        try {
            Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
```

### Step 4: Register ModMenu Entrypoint in `fabric.mod.json`

```json
{
    "entrypoints": {
        "client": ["com.yourmod.YourModClient"],
        "modmenu": ["com.yourmod.config.ModMenuIntegration"]
    }
}
```

---

## Part 3: Creating a Fallback Config Screen (Vanilla Widgets)

When Cloth Config isn't installed, you need a fallback screen using vanilla Minecraft widgets.

### ⚠️ MANDATORY REQUIREMENTS for ALL Fallback Config Screens:

When creating a fallback config screen for ANY mod, you **MUST** include:

1. ✅ **SLIDERS** - Use `SliderWidget` for all numeric values (NO text fields for numbers)
2. ✅ **TOOLTIPS** - Every config option MUST have a tooltip explaining what it does
3. ✅ **RESET BUTTONS (↺)** - Every config option MUST have a reset button to restore default value
4. ✅ **SCROLLABLE CONTENT** - Config screen MUST scroll when content doesn't fit window (see below)

**Why these are required:**
- **Sliders:** Provide better UX than text fields, prevent invalid input, match Cloth Config behavior
- **Tooltips:** Users need to understand what each option does without external documentation
- **Reset Buttons:** Allow users to quickly restore defaults without remembering original values
- **Scrollable:** Screen must remain usable at any window size, preventing buttons from overlapping

### Layout Standards:

```
┌─────────────────────────────────────────────────┐
│              Your Mod Configuration             │
├─────────────────────────────────────────────────┤
│                                                 │
│   [  Slider Widget (180px)  ] [↺] (40px)      │  ← Tooltip on hover
│                                                 │
│   [  Toggle Button (180px)  ] [↺] (40px)      │  ← Tooltip on hover
│                                                 │
│   [  Another Slider (180px) ] [↺] (40px)      │  ← Tooltip on hover
│                                                 │
│                [ Save & Close ]                 │
└─────────────────────────────────────────────────┘
```

**Layout Constants (Use these for consistency):**
```java
private static final int WIDGET_WIDTH = 180;
private static final int RESET_BTN_WIDTH = 40;
private static final int SPACING = 4;  // Between widget and reset button
private static final int ROW_HEIGHT = 22;  // Space between rows
```

### Example Fallback Config Screen (COMPLETE):

```java
package com.yourmod.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class SimpleFallbackConfigScreen extends Screen {
    private final Screen parent;
    private final YourConfig config;
    
    // Layout constants - USE THESE FOR ALL MODS
    private static final int WIDGET_WIDTH = 180;
    private static final int RESET_BTN_WIDTH = 40;
    private static final int SPACING = 4;
    private static final int ROW_HEIGHT = 22;
    
    // Tooltip tracking - REQUIRED for all config screens
    private record TooltipEntry(int x, int y, int width, int height, String tooltip) {}
    private final List<TooltipEntry> tooltips = new ArrayList<>();
    private String currentTooltip = null;
    
    public SimpleFallbackConfigScreen(Screen parent) {
        super(Text.literal("Your Mod Configuration"));
        this.parent = parent;
        this.config = YourConfig.getInstance();
    }
    
    @Override
    protected void init() {
        super.init();
        tooltips.clear();
        
        int centerX = this.width / 2;
        int totalWidth = WIDGET_WIDTH + SPACING + RESET_BTN_WIDTH;
        int widgetX = centerX - totalWidth / 2;
        int resetX = widgetX + WIDGET_WIDTH + SPACING;
        int y = 50;
        
        // ========================================
        // EXAMPLE 1: Toggle Button with Tooltip & Reset
        // ========================================
        addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Enable or disable this feature. Default: ON");
        
        ButtonWidget toggleBtn = addDrawableChild(ButtonWidget.builder(
            Text.literal("Feature: " + (config.enabled ? "ON" : "OFF")),
            button -> {
                config.enabled = !config.enabled;
                button.setMessage(Text.literal("Feature: " + (config.enabled ? "ON" : "OFF")));
            }
        ).dimensions(widgetX, y, WIDGET_WIDTH, 20).build());
        
        // Reset button - REQUIRED
        addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
            config.enabled = true; // Default value
            toggleBtn.setMessage(Text.literal("Feature: ON"));
        }).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());
        
        y += ROW_HEIGHT;
        
        // ========================================
        // EXAMPLE 2: Integer Slider with Tooltip & Reset
        // ========================================
        addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Adjust render distance. Higher values = more visible chunks. Default: 50");
        
        IntSlider distanceSlider = new IntSlider(
            widgetX, y, WIDGET_WIDTH, 20,
            "Distance", 10, 100, config.distance, ""
        );
        addDrawableChild(distanceSlider);
        
        // Reset button - REQUIRED
        addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
            distanceSlider.setValue(50, 10, 100); // Default: 50
        }).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());
        
        y += ROW_HEIGHT;
        
        // ========================================
        // EXAMPLE 3: Percentage Slider with Tooltip & Reset
        // ========================================
        addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Opacity of the overlay (0% = invisible, 100% = fully visible). Default: 75%");
        
        IntSlider opacitySlider = new IntSlider(
            widgetX, y, WIDGET_WIDTH, 20,
            "Opacity", 0, 100, config.opacity, "%"
        );
        addDrawableChild(opacitySlider);
        
        // Reset button - REQUIRED
        addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
            opacitySlider.setValue(75, 0, 100); // Default: 75%
        }).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());
        
        y += ROW_HEIGHT;
        
        // ========================================
        // EXAMPLE 4: Float Slider (Multiplier) with Tooltip & Reset
        // ========================================
        addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Speed multiplier for animations (0.5x = slower, 2.0x = faster). Default: 1.0x");
        
        FloatSlider speedSlider = new FloatSlider(
            widgetX, y, WIDGET_WIDTH, 20,
            "Speed", 0.5f, 2.0f, config.speedMultiplier, "x"
        );
        addDrawableChild(speedSlider);
        
        // Reset button - REQUIRED
        addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
            speedSlider.setValue(1.0f, 0.5f, 2.0f); // Default: 1.0x
        }).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());
        
        y += ROW_HEIGHT;
        
        // ========================================
        // Save & Close Button (Bottom Center)
        // ========================================
        int bottomY = this.height - 28;
        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), button -> {
            saveConfig();
            close();
        }).dimensions(centerX - 50, bottomY, 100, 20).build());
    }
    
    /**
     * REQUIRED: Add tooltip for a widget area.
     * Call this for EVERY config option.
     */
    private void addTooltip(int x, int y, int width, int height, String tooltip) {
        tooltips.add(new TooltipEntry(x, y, width, height, tooltip));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark background
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        
        // Title
        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        
        // Render widgets
        super.render(context, mouseX, mouseY, delta);
        
        // Check tooltip hover - REQUIRED tooltip rendering
        currentTooltip = null;
        for (TooltipEntry entry : tooltips) {
            if (mouseX >= entry.x && mouseX < entry.x + entry.width &&
                mouseY >= entry.y && mouseY < entry.y + entry.height) {
                currentTooltip = entry.tooltip;
                break;
            }
        }
        
        // Draw tooltip using Minecraft's native method - REQUIRED
        if (currentTooltip != null && !currentTooltip.isEmpty()) {
            context.drawTooltip(textRenderer, Text.literal(currentTooltip), mouseX, mouseY);
        }
    }
    
    private void saveConfig() {
        // Save config values to file
        config.save();
    }
    
    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false; // Don't pause game when config is open
    }
    
    // ========================================
    // REQUIRED: IntSlider Widget
    // Use this for all integer values
    // ========================================
    private static class IntSlider extends SliderWidget {
        private final int min, max;
        private final String suffix, label;
        
        public IntSlider(int x, int y, int width, int height, String label, int min, int max, int value, String suffix) {
            super(x, y, width, height, Text.literal(label + ": " + value + suffix), (double)(value - min) / (max - min));
            this.min = min;
            this.max = max;
            this.suffix = suffix;
            this.label = label;
        }
        
        public int getIntValue() {
            return (int) Math.round(this.value * (max - min) + min);
        }
        
        public void setValue(int newValue, int min, int max) {
            this.value = (double)(newValue - min) / (max - min);
            updateMessage();
        }
        
        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + getIntValue() + suffix));
        }
        
        @Override
        protected void applyValue() {
            // Value is applied when Save & Close is clicked
        }
    }
    
    // ========================================
    // REQUIRED: FloatSlider Widget
    // Use this for all float/double values
    // ========================================
    private static class FloatSlider extends SliderWidget {
        private final float min, max;
        private final String suffix, label;
        
        public FloatSlider(int x, int y, int width, int height, String label, float min, float max, float value, String suffix) {
            super(x, y, width, height, Text.literal(label + ": " + String.format("%.1f", value) + suffix), (value - min) / (max - min));
            this.min = min;
            this.max = max;
            this.suffix = suffix;
            this.label = label;
        }
        
        public float getFloatValue() {
            return (float) (this.value * (max - min) + min);
        }
        
        public void setValue(float newValue, float min, float max) {
            this.value = (newValue - min) / (max - min);
            updateMessage();
        }
        
        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + String.format("%.1f", getFloatValue()) + suffix));
        }
        
        @Override
        protected void applyValue() {
            // Value is applied when Save & Close is clicked
        }
    }
}
```

### Tooltip Implementation Details

**Why use `context.drawTooltip()` instead of custom rendering?**

✅ **CORRECT (Minecraft's native method):**
```java
context.drawTooltip(textRenderer, Text.literal(tooltip), mouseX, mouseY);
```

❌ **INCORRECT (Custom rendering - more code, more bugs):**
```java
// Don't do this - too much work and error-prone
context.fill(mouseX, mouseY, mouseX + width, mouseY + height, 0xFF000000);
context.drawTextWithShadow(textRenderer, tooltip, mouseX + 5, mouseY + 5, 0xFFFFFF);
```

**Benefits of native `drawTooltip()`:**
- Automatic background rendering
- Automatic border rendering
- Automatic text wrapping for long tooltips
- Automatic positioning (won't render off-screen)
- Consistent with vanilla Minecraft UI

### Reset Button Pattern

**ALWAYS use this pattern for reset buttons:**

```java
// For toggle buttons:
addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
    config.value = DEFAULT_VALUE;
    targetWidget.setMessage(Text.literal("Label: " + DEFAULT_VALUE));
}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());

// For sliders:
addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
    slider.setValue(DEFAULT_VALUE, MIN_VALUE, MAX_VALUE);
}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());
```

### Common Mistakes to Avoid

❌ **DON'T use TextFieldWidget for numeric values:**
```java
// BAD - allows invalid input, no visual feedback
TextFieldWidget field = new TextFieldWidget(...);
```

✅ **DO use SliderWidget for numeric values:**
```java
// GOOD - constrained input, visual feedback, better UX
IntSlider slider = new IntSlider(...);
```

❌ **DON'T forget tooltips:**
```java
// BAD - users won't know what the option does
ButtonWidget btn = addDrawableChild(...);
```

✅ **DO add tooltips for every option:**
```java
// GOOD - users understand the option
addTooltip(x, y, width, height, "Description of what this does. Default: value");
ButtonWidget btn = addDrawableChild(...);
```

❌ **DON'T forget reset buttons:**
```java
// BAD - users have to remember default values
IntSlider slider = addDrawableChild(...);
```

✅ **DO add reset buttons next to every option:**
```java
// GOOD - users can easily restore defaults
IntSlider slider = addDrawableChild(...);
addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
    slider.setValue(DEFAULT_VALUE, MIN, MAX);
}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());
```

### Scrollable Fallback Config (MANDATORY for MC 1.21.11+)

**⚠️ CRITICAL: Your fallback config MUST be scrollable!**

When the Minecraft window is resized smaller, config options can overlap with bottom buttons. You MUST implement scrolling to handle this gracefully.

**Why NOT use `EntryListWidget`?**

In Minecraft 1.21.11, the `EntryListWidget.Entry.render()` method signature changed from 10 parameters to 5 parameters:
- **Old (pre-1.21.11):** `render(DrawContext, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta)`
- **New (1.21.11+):** `render(DrawContext, int mouseX, int mouseY, boolean hovered, float delta)`

This makes it very difficult to use the widget-based approach. Instead, use **manual scroll with scissoring**.

### Manual Scroll Implementation Pattern:

**Layout with scrolling:**
```
┌─────────────────────────────────────────────────┐
│              Your Mod Configuration             │  ← Fixed Header (35px)
├─────────────────────────────────────────────────┤
│ ▲ (scroll indicator when not at top)            │
│                                                 │
│   [  Slider Widget (180px)  ] [↺] (40px)      │
│   [  Toggle Button (180px)  ] [↺] (40px)      │  ← Scrollable Content
│   [  Another Slider (180px) ] [↺] (40px)      │    (uses scissoring)
│   [  More Options...        ] [↺] (40px)      │
│                                                 │
│ ▼ (scroll indicator when not at bottom)         │
├─────────────────────────────────────────────────┤
│    [ Save & Close ] [ Key Binds ] [ Cancel ]   │  ← Fixed Footer (35px)
└─────────────────────────────────────────────────┘
```

**Key Constants:**
```java
// Layout constants for scrollable config
private static final int HEADER_HEIGHT = 35;   // Fixed header area
private static final int FOOTER_HEIGHT = 35;   // Fixed footer area
private static final int ROW_HEIGHT = 24;      // Height per option row
private static final int WIDGET_WIDTH = 180;
private static final int RESET_BTN_WIDTH = 40;
private static final int SCROLL_SPEED = 10;    // Pixels per scroll wheel notch

// Scroll state
private int scrollOffset = 0;
private int maxScrollOffset = 0;
private int contentHeight = 0;
```

**Calculate scroll bounds:**
```java
@Override
protected void init() {
    super.init();
    
    // Calculate available content area
    int contentAreaHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
    
    // Total content height = number of options * row height
    contentHeight = numberOfOptions * ROW_HEIGHT;
    
    // Maximum scroll = content that doesn't fit
    maxScrollOffset = Math.max(0, contentHeight - contentAreaHeight);
    
    // Clamp current scroll offset
    scrollOffset = Math.min(scrollOffset, maxScrollOffset);
    
    // ... create widgets
}
```

**Handle mouse wheel scrolling:**
```java
@Override
public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    // Only scroll if mouse is in the scrollable content area
    if (mouseY > HEADER_HEIGHT && mouseY < this.height - FOOTER_HEIGHT) {
        scrollOffset -= (int)(verticalAmount * SCROLL_SPEED);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
}
```

**Render with scissoring (clipping):**
```java
@Override
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    // Draw background
    context.fill(0, 0, this.width, this.height, 0xC0101010);
    
    // Draw title in header area (fixed, not scrolled)
    context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
    
    // Enable scissor to clip content to the scrollable area
    int scissorTop = HEADER_HEIGHT;
    int scissorBottom = this.height - FOOTER_HEIGHT;
    context.enableScissor(0, scissorTop, this.width, scissorBottom);
    
    // Render scrollable content (widgets will be repositioned based on scrollOffset)
    super.render(context, mouseX, mouseY, delta);
    
    // Disable scissor
    context.disableScissor();
    
    // Draw scroll indicators if needed
    if (scrollOffset > 0) {
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("▲"), this.width / 2, scissorTop + 2, 0xAAAAAA);
    }
    if (scrollOffset < maxScrollOffset) {
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("▼"), this.width / 2, scissorBottom - 10, 0xAAAAAA);
    }
    
    // Draw bottom buttons AFTER scissor is disabled (so they're always visible)
    // ... render fixed footer buttons
    
    // Draw tooltips LAST (so they appear above everything)
    // ... tooltip rendering
}
```

**Reposition widgets based on scroll:**
```java
// When creating widgets in init(), store original Y positions
// Then in render() or a helper method, reposition based on scroll:

private void updateWidgetPositions() {
    for (WidgetEntry entry : scrollableWidgets) {
        int adjustedY = entry.originalY - scrollOffset;
        entry.widget.setY(adjustedY);
        
        // Hide widgets that are outside visible area
        boolean visible = adjustedY >= HEADER_HEIGHT - 20 && adjustedY < this.height - FOOTER_HEIGHT;
        entry.widget.visible = visible;
    }
}
```

**Check if mouse is in scroll area (for tooltips):**
```java
private boolean isMouseInScrollArea(double mouseX, double mouseY) {
    return mouseY >= HEADER_HEIGHT && mouseY < this.height - FOOTER_HEIGHT;
}

// Only show tooltips for visible widgets
if (isMouseInScrollArea(mouseX, mouseY)) {
    // Check tooltip hover
}
```

### Common Scroll Mistakes to Avoid

❌ **DON'T use EntryListWidget on MC 1.21.11+:**
```java
// BAD - API signature changed, will cause compile errors
public class ConfigEntry extends ElementListWidget.Entry<ConfigEntry> {
    @Override
    public void render(...) { // Wrong signature!
```

✅ **DO use manual scroll with scissoring:**
```java
// GOOD - Works on all MC versions
context.enableScissor(0, HEADER_HEIGHT, this.width, this.height - FOOTER_HEIGHT);
// ... render content
context.disableScissor();
```

❌ **DON'T forget to clamp scroll bounds:**
```java
// BAD - allows scrolling past content
scrollOffset -= (int)(verticalAmount * SCROLL_SPEED);
```

✅ **DO always clamp scroll offset:**
```java
// GOOD - prevents over-scrolling
scrollOffset -= (int)(verticalAmount * SCROLL_SPEED);
scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
```

❌ **DON'T render tooltips inside scissor region:**
```java
// BAD - tooltips get cut off
context.enableScissor(...);
super.render(...);
context.drawTooltip(...); // Gets clipped!
context.disableScissor();
```

✅ **DO render tooltips after disabling scissor:**
```java
// GOOD - tooltips render on top of everything
context.enableScissor(...);
super.render(...);
context.disableScissor();
// Now render tooltips
if (tooltip != null) {
    context.drawTooltip(textRenderer, Text.literal(tooltip), mouseX, mouseY);
}
```

### Interactive Scrollbar (Click & Drag)

**⚠️ RECOMMENDED: Make your scrollbar interactive!**

Users expect to click/drag the scrollbar, not just use the mouse wheel. Here's how to implement it:

**Add scrollbar tracking fields:**
```java
// Scrollbar interaction state
private static final int SCROLLBAR_WIDTH = 6;
private boolean isDraggingScrollbar = false;
private int scrollbarDragOffset = 0;
```

**Implement mouse click for scrollbar (MC 1.21.11+ uses Click class):**
```java
@Override
public boolean mouseClicked(Click click, boolean doubleClick) {
    double mouseX = click.x();
    double mouseY = click.y();
    int button = click.button();
    
    // Check if clicking on the scrollbar track area
    if (button == 0 && maxScrollOffset > 0) {
        int scrollbarX = this.width - SCROLLBAR_WIDTH - 2;
        int scrollbarTrackTop = HEADER_HEIGHT;
        int scrollbarTrackBottom = this.height - FOOTER_HEIGHT;
        
        if (mouseX >= scrollbarX && mouseX <= this.width - 2 &&
            mouseY >= scrollbarTrackTop && mouseY <= scrollbarTrackBottom) {
            
            // Calculate scrollbar thumb position and size
            int trackHeight = scrollbarTrackBottom - scrollbarTrackTop;
            int thumbHeight = Math.max(20, trackHeight * trackHeight / (maxScrollOffset + trackHeight));
            int thumbY = scrollbarTrackTop + (int)((trackHeight - thumbHeight) * ((float)scrollOffset / maxScrollOffset));
            
            if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                // Clicked on thumb - start dragging
                isDraggingScrollbar = true;
                scrollbarDragOffset = (int)(mouseY - thumbY);
            } else {
                // Clicked on track - jump to position
                int clickOffset = (int)mouseY - scrollbarTrackTop - thumbHeight / 2;
                float scrollPercent = (float)clickOffset / (trackHeight - thumbHeight);
                scrollOffset = (int)(scrollPercent * maxScrollOffset);
                scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
                isDraggingScrollbar = true;
                scrollbarDragOffset = thumbHeight / 2;
            }
            return true;
        }
    }
    return super.mouseClicked(click, doubleClick);
}
```

**Implement mouse drag for scrollbar:**
```java
@Override
public boolean mouseDragged(Click click, double deltaX, double deltaY) {
    int button = click.button();
    double mouseY = click.y();
    
    if (isDraggingScrollbar && button == 0 && maxScrollOffset > 0) {
        int scrollbarTrackTop = HEADER_HEIGHT;
        int scrollbarTrackBottom = this.height - FOOTER_HEIGHT;
        int trackHeight = scrollbarTrackBottom - scrollbarTrackTop;
        int thumbHeight = Math.max(20, trackHeight * trackHeight / (maxScrollOffset + trackHeight));
        
        // Calculate new scroll position based on mouse Y
        int thumbY = (int)mouseY - scrollbarDragOffset - scrollbarTrackTop;
        float scrollPercent = (float)thumbY / (trackHeight - thumbHeight);
        scrollOffset = (int)(scrollPercent * maxScrollOffset);
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
        return true;
    }
    return super.mouseDragged(click, deltaX, deltaY);
}
```

**Release scrollbar on mouse release:**
```java
@Override
public boolean mouseReleased(Click click) {
    if (click.button() == 0) {
        isDraggingScrollbar = false;
    }
    return super.mouseReleased(click);
}
```

**Note on MC 1.21.11 API:** The `Click` class provides `x()`, `y()`, and `button()` methods. Import it with:
```java
import net.minecraft.client.gui.Click;
```

---

## Part 4: Config Keybind (MANDATORY)

### ⚠️ CRITICAL REQUIREMENTS:

**Every mod with a config screen MUST have a keybind to open it!**

This is **NON-NEGOTIABLE** because:
1. Users without ModMenu need a way to access config
2. Users may prefer keyboard shortcuts over menu navigation
3. The fallback config screen is useless if users can't open it

### Keybind Rules:

| Rule | Description | Example |
|------|-------------|--------|
| **UNBOUND by default** | ALL keybinds MUST use `GLFW.GLFW_KEY_UNKNOWN` | `GLFW.GLFW_KEY_UNKNOWN` |
| **Config keybind required** | Every mod with config MUST have a keybind | `key.yourmod.config` |
| **Detect Cloth Config** | Keybind opens Cloth Config if available, fallback otherwise | See code below |
| **Only override saved** | If user saved a keybind previously, Minecraft restores it | Automatic |

### Why UNBOUND by default?

❌ **DON'T assign a default key:**
```java
// BAD - may conflict with other mods or vanilla keybinds
GLFW.GLFW_KEY_K  // K key
GLFW.GLFW_KEY_O  // O key
```

✅ **DO use GLFW_KEY_UNKNOWN:**
```java
// GOOD - user assigns their own key, no conflicts
GLFW.GLFW_KEY_UNKNOWN  // Unbound by default
```

**Exception:** If upgrading a mod that previously had a bound key, Minecraft will restore the user's saved keybind automatically from `options.txt`. You don't need to handle this - Fabric's KeyBindingHelper does it for you.

### Register Keybind in Client Entrypoint:

```java
package com.yourmod;

import com.yourmod.config.ModMenuIntegration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class YourModClient implements ClientModInitializer {
    
    // ============================================
    // MANDATORY: Config keybind - MUST be UNBOUND by default
    // ============================================
    private static KeyBinding configKeyBinding;
    
    @Override
    public void onInitializeClient() {
        // ============================================
        // STEP 1: Register config keybind (MANDATORY)
        // - MUST use GLFW.GLFW_KEY_UNKNOWN (unbound)
        // - MUST have a translation key for the keybind name
        // - MUST have a category for the mod
        // ============================================
        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.yourmod.config",           // Translation key for keybind name
            InputUtil.Type.KEYSYM,           // Keyboard key type
            GLFW.GLFW_KEY_UNKNOWN,           // ⚠️ UNBOUND BY DEFAULT - REQUIRED!
            "category.yourmod"               // Translation key for category
        ));
        
        // ============================================
        // STEP 2: Handle keybind press (MANDATORY)
        // - Opens Cloth Config screen if available
        // - Falls back to vanilla config screen otherwise
        // - Only opens when no other screen is active
        // ============================================
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    // IMPORTANT: Use ModMenuIntegration.getConfigScreen()
                    // This automatically detects Cloth Config and uses it if available
                    // Otherwise it opens the fallback vanilla config screen
                    client.setScreen(ModMenuIntegration.getConfigScreen(null));
                }
            }
        });
    }
}
```

### Alternative: Direct Cloth Config Detection (Without ModMenuIntegration)

If you don't have a ModMenuIntegration class, you can detect Cloth Config directly:

```java
// In your keybind handler
ClientTickEvents.END_CLIENT_TICK.register(client -> {
    while (configKeyBinding.wasPressed()) {
        if (client.currentScreen == null) {
            // Try Cloth Config first, fall back to vanilla screen
            try {
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                // Cloth Config is available - use it
                client.setScreen(ModConfigScreen.createConfigScreen(null));
            } catch (ClassNotFoundException e) {
                // Cloth Config not available - use fallback
                client.setScreen(new SimpleFallbackConfigScreen(null));
            }
        }
    }
});
```

### Add Translation Keys in `assets/yourmod/lang/en_us.json`:

```json
{
    "key.yourmod.config": "Open Config Screen",
    "category.yourmod": "Your Mod"
}
```

---

## Part 5: Testing

### Build and Run
```powershell
cd C:\YourMod
.\gradlew build
.\gradlew runClient
```

### Test Scenarios

1. **With Cloth Config + ModMenu installed:**
   - ModMenu should show your mod with a config button
   - Clicking config or pressing keybind opens Cloth Config screen

2. **Without Cloth Config (only ModMenu):**
   - ModMenu shows your mod with config button
   - Opens fallback vanilla config screen
   - All sliders, tooltips, and reset buttons work

3. **Without either dependency:**
   - Mod works normally
   - Keybind opens fallback config screen
   - No crashes or missing class errors

### Testing Checklist for Fallback Screen

- [ ] All numeric options use sliders (NO text fields)
- [ ] Every option has a tooltip explaining what it does
- [ ] Every option has a reset button (↺)
- [ ] Tooltips appear on hover and render correctly
- [ ] Reset buttons restore default values
- [ ] Sliders show current value in label
- [ ] Save & Close button saves and exits
- [ ] Screen has dark background and centered title
- [ ] Layout is centered and looks professional

### Testing Checklist for Keybinds

- [ ] Config keybind is registered and appears in Controls menu
- [ ] Config keybind is UNBOUND by default (shows nothing, not a key)
- [ ] Keybind can be assigned by user in Controls menu
- [ ] Pressing keybind opens Cloth Config (if installed)
- [ ] Pressing keybind opens fallback config (if Cloth Config not installed)
- [ ] Keybind only works when no other screen is open
- [ ] ALL other mod keybinds are also UNBOUND by default

### Development Dependencies Location

For testing with dependencies during development:
- **ModMenu 17.0.0-alpha.1**: Downloaded automatically via `modRuntimeOnly`
- **Cloth Config fork (for 1.21.11 testing)**: `C:\clot-configForkTest\fabric\build\libs\cloth-config-20.0.9999-fabric.jar`

To use the fork in dev testing, add to `build.gradle`:
```gradle
dependencies {
    // Use local fork for dev testing
    modRuntimeOnly files("C:/clot-configForkTest/fabric/build/libs/cloth-config-20.0.9999-fabric.jar")
}
```

---

## Summary Checklist

### Dependencies & Build
- [ ] Update all version files for 1.21.11
- [ ] Change Cloth Config from `modImplementation` to `modCompileOnly`
- [ ] Change ModMenu to `modCompileOnly` (and optionally `modRuntimeOnly` for dev)
- [ ] Update `fabric.mod.json`: use `recommends` instead of `depends`

### Config Integration
- [ ] Create `ModMenuIntegration.java` with Cloth Config detection
- [ ] Create fallback config screen using vanilla widgets

### Fallback Screen (ALL REQUIRED)
- [ ] **SLIDERS** for ALL numeric values (NO text fields)
- [ ] **TOOLTIPS** for ALL options
- [ ] **RESET BUTTONS** for ALL options

### Keybinds (ALL REQUIRED)
- [ ] **CONFIG KEYBIND MUST EXIST** - Opens config screen
- [ ] **ALL KEYBINDS MUST BE UNBOUND** by default (`GLFW.GLFW_KEY_UNKNOWN`)
- [ ] Keybind opens Cloth Config if available, fallback otherwise
- [ ] Add translation keys for keybind name and category

### Testing
- [ ] Test with and without optional dependencies
- [ ] Verify keybind appears in Controls menu (unbound)
- [ ] Verify keybind opens correct screen based on Cloth Config availability

---

## File Structure Example

```
src/main/java/com/yourmod/
├── YourModClient.java          # Client entrypoint, keybind registration
├── config/
│   ├── YourConfig.java         # Config data class with save/load
│   ├── ModConfigScreen.java    # Cloth Config screen (only used if CC available)
│   ├── ModMenuIntegration.java # ModMenu API + fallback detection
│   └── SimpleFallbackConfigScreen.java  # Vanilla fallback screen
│                                         # MUST have: sliders, tooltips, reset buttons
└── ...

src/main/resources/
├── fabric.mod.json             # Mod metadata with recommends
└── assets/yourmod/lang/
    └── en_us.json              # Translation keys
```

---

## Quick Reference: Fallback Config Screen Requirements

### MANDATORY Components (Copy this for every mod):

```java
// 1. LAYOUT CONSTANTS
private static final int WIDGET_WIDTH = 180;
private static final int RESET_BTN_WIDTH = 40;
private static final int SPACING = 4;
private static final int ROW_HEIGHT = 22;

// 2. TOOLTIP SYSTEM
private record TooltipEntry(int x, int y, int width, int height, String tooltip) {}
private final List<TooltipEntry> tooltips = new ArrayList<>();
private String currentTooltip = null;

private void addTooltip(int x, int y, int width, int height, String tooltip) {
    tooltips.add(new TooltipEntry(x, y, width, height, tooltip));
}

// 3. TOOLTIP RENDERING (in render() method)
currentTooltip = null;
for (TooltipEntry entry : tooltips) {
    if (mouseX >= entry.x && mouseX < entry.x + entry.width &&
        mouseY >= entry.y && mouseY < entry.y + entry.height) {
        currentTooltip = entry.tooltip;
        break;
    }
}
if (currentTooltip != null && !currentTooltip.isEmpty()) {
    context.drawTooltip(textRenderer, Text.literal(currentTooltip), mouseX, mouseY);
}

// 4. SLIDER WIDGETS (IntSlider and FloatSlider classes)
// Copy from the example above - REQUIRED for all numeric values

// 5. RESET BUTTON PATTERN
addDrawableChild(ButtonWidget.builder(Text.literal("↺"), button -> {
    slider.setValue(DEFAULT_VALUE, MIN_VALUE, MAX_VALUE);
}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build());
```

### For Every Config Option, Add:

1. **Tooltip:** `addTooltip(widgetX, y, WIDGET_WIDTH, 20, "Description. Default: value");`
2. **Widget:** Slider for numbers, Button for toggles
3. **Reset Button:** `addDrawableChild(ButtonWidget.builder(Text.literal("↺"), ...)`

### NO EXCEPTIONS:
- ❌ NO text fields for numeric input
- ❌ NO options without tooltips
- ❌ NO options without reset buttons
- ❌ NO mods without a config keybind
- ❌ NO keybinds with default key assignments (must be UNBOUND)

---

## Quick Reference: Keybind Requirements

### MANDATORY Config Keybind:

```java
// 1. Register keybind (MUST be UNBOUND by default)
configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
    "key.yourmod.config",       // Translation key
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_UNKNOWN,       // ⚠️ UNBOUND - REQUIRED!
    "category.yourmod"
));

// 2. Handle keybind (MUST detect Cloth Config)
ClientTickEvents.END_CLIENT_TICK.register(client -> {
    while (configKeyBinding.wasPressed()) {
        if (client.currentScreen == null) {
            // Opens Cloth Config if available, fallback otherwise
            client.setScreen(ModMenuIntegration.getConfigScreen(null));
        }
    }
});
```

### Translation Keys (Required):

```json
// assets/yourmod/lang/en_us.json
{
    "key.yourmod.config": "Open Config Screen",
    "category.yourmod": "Your Mod Name"
}
```

### Keybind Behavior:

| Cloth Config | ModMenu | Keybind Opens |
|--------------|---------|---------------|
| ✅ Installed | ✅ Installed | Cloth Config screen |
| ✅ Installed | ❌ Not installed | Cloth Config screen |
| ❌ Not installed | ✅ Installed | Fallback vanilla screen |
| ❌ Not installed | ❌ Not installed | Fallback vanilla screen |

---

*This guide was created during the SimpleFPS 1.21.11 upgrade. The same pattern MUST be applied to ALL Fabric mods with optional dependencies.*
