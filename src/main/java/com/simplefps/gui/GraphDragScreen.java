package com.simplefps.gui;

import com.simplefps.config.SimpleFPSConfig;
import com.simplefps.hud.FPSGraphRenderer;
import com.simplefps.hud.FPSHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * A screen that allows the user to drag the FPS Graph to reposition it.
 * Press ESC to exit and save the position.
 * 
 * Uses GLFW for mouse input detection since 1.21.10 API changed.
 */
public class GraphDragScreen extends Screen {
	private final Screen parent;
	private final SimpleFPSConfig config;
	
	private boolean draggingGraph = false;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private boolean wasMouseDown = false;
	
	public GraphDragScreen(Screen parent) {
		super(Text.literal("Drag FPS Graph"));
		this.parent = parent;
		this.config = SimpleFPSConfig.getInstance();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		// Handle mouse input using GLFW
		long windowHandle = client.getWindow().getHandle();
		boolean isMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		
		// Detect mouse click start
		if (isMouseDown && !wasMouseDown) {
			handleMouseClick(mouseX, mouseY);
		}
		
		// Handle dragging
		if (isMouseDown && draggingGraph) {
			handleMouseDrag(mouseX, mouseY, client);
		}
		
		// Detect mouse release
		if (!isMouseDown && wasMouseDown) {
			handleMouseRelease();
		}
		
		wasMouseDown = isMouseDown;
		
		// Render semi-transparent background
		context.fill(0, 0, this.width, this.height, 0x80000000);
		
		// Draw instructions at top (like FPS drag screen)
		String instruction = "Drag the FPS graph to reposition. Press ESC to save.";
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			instruction,
			this.width / 2,
			10,
			0xFFFFFF
		);
		
		// Draw the FPS counter (normal position)
		FPSHudRenderer.renderFPS(context, false);
		
		// Draw the FPS graph (draggable)
		FPSGraphRenderer.renderGraph(context, true);
		
		// Draw "Drag me!" label on graph if not dragging
		if (!draggingGraph) {
			float scale = config.graphScale / 100f;
			int graphWidth = (int) (150 * scale);
			int graphHeight = (int) (50 * scale) + (int) (12 * scale);
			int labelX = config.graphX + graphWidth / 2;
			int labelY = config.graphY + graphHeight / 2;
			context.drawCenteredTextWithShadow(this.textRenderer, "Drag me!", labelX, labelY, 0xFFFF00);
		}
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	private void handleMouseClick(int mouseX, int mouseY) {
		// Check if clicking on graph
		float scale = config.graphScale / 100f;
		int graphWidth = (int) (150 * scale);
		int graphHeight = (int) (50 * scale) + (int) (12 * scale);
		
		if (mouseX >= config.graphX && mouseX <= config.graphX + graphWidth &&
			mouseY >= config.graphY && mouseY <= config.graphY + graphHeight) {
			draggingGraph = true;
			dragOffsetX = mouseX - config.graphX;
			dragOffsetY = mouseY - config.graphY;
		}
	}
	
	private void handleMouseDrag(int mouseX, int mouseY, MinecraftClient client) {
		float scale = config.graphScale / 100f;
		int graphWidth = (int) (150 * scale);
		int graphHeight = (int) (50 * scale) + (int) (12 * scale);
		
		config.graphX = (int) Math.max(0, Math.min(client.getWindow().getScaledWidth() - graphWidth, mouseX - dragOffsetX));
		config.graphY = (int) Math.max(0, Math.min(client.getWindow().getScaledHeight() - graphHeight, mouseY - dragOffsetY));
	}
	
	private void handleMouseRelease() {
		if (draggingGraph) {
			draggingGraph = false;
			config.save();
		}
	}
	
	@Override
	public void close() {
		config.save();
		this.client.setScreen(parent);
	}
	
	@Override
	public boolean shouldPause() {
		return false;
	}
}
