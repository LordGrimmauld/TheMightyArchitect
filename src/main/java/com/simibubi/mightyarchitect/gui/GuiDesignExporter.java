package com.simibubi.mightyarchitect.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class GuiDesignExporter extends GuiScreen {

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;

	private List<ScrollArea> scrollAreas;

	private ScrollArea scrollAreaLayer;
	private ScrollArea scrollAreaType;
	private ScrollArea scrollAreaAdditionalData;

	private DynamicLabel labelTheme;
	private DynamicLabel labelLayer;
	private DynamicLabel labelType;
	private DynamicLabel labelAdditionalData;

	private String additionalDataKey;
	private int additionalDataValue;
	private float animationProgress;

	@Override
	public void initGui() {
		super.initGui();
		animationProgress = 0;
		xSize = GuiResources.EXPORTER.width + 100;
		ySize = GuiResources.EXPORTER.height + 50;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		DesignTheme theme = DesignExporter.theme;
		DesignLayer layer = DesignExporter.layer;
		DesignType type = DesignExporter.type;

		additionalDataValue = DesignExporter.designParameter;

		labelTheme = new DynamicLabel(xTopLeft + 96, yTopLeft + 28);
		labelLayer = new DynamicLabel(xTopLeft + 96, yTopLeft + 48);
		labelType = new DynamicLabel(xTopLeft + 96, yTopLeft + 68);
		labelAdditionalData = new DynamicLabel(xTopLeft + 96, yTopLeft + 88);

		scrollAreas = new LinkedList<>();
		additionalDataKey = "";
		initScrollAreas(theme, layer, type);
	}

	private void initScrollAreas(DesignTheme theme, DesignLayer layer, DesignType type) {
		scrollAreas.clear();

		List<DesignLayer> layers = theme.getLayers();

		if (!layers.contains(layer))
			layer = DesignLayer.Regular;

		List<String> layerOptions = new ArrayList<>();
		layers.forEach(l -> layerOptions.add(l.getDisplayName()));

		scrollAreaLayer = new ScrollArea(layerOptions, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelLayer.text = layerOptions.get(position);
				initTypeScrollArea(theme, layers.get(position), DesignExporter.type);
			}
		});
		scrollAreaLayer.setBounds(xTopLeft + 93, yTopLeft + 45, 90, 14);
		scrollAreaLayer.setTitle("Style Layer");
		scrollAreaLayer.setState(layers.indexOf(layer));
		labelLayer.text = layer.getDisplayName();
		scrollAreas.add(scrollAreaLayer);

		initTypeScrollArea(theme, layer, type);

		labelTheme.text = theme.getDisplayName();
	}

	protected void initTypeScrollArea(DesignTheme theme, DesignLayer layer, DesignType type) {
		List<DesignType> types = new ArrayList<>(theme.getTypes());
		
		// Roofs only in Roofing layer and vice versa
		if (layer == DesignLayer.Roofing) {
			types.retainAll(DesignType.roofTypes());
		} else {
			types.removeAll(DesignType.roofTypes());			
		}
		
		// Fallback if previous type is not selectable anymore
		if (!types.contains(type)) {
			type = DesignType.WALL;
			if (layer == DesignLayer.Roofing) {
				for (DesignType dt : DesignType.roofTypes()) {
					if (types.contains(dt)) {
						type = dt;
						break;
					}
				}
			}
		}

		// Prepare options
		List<String> typeOptions = new ArrayList<>();
		types.forEach(t -> typeOptions.add(t.getDisplayName()));

		if (scrollAreas.contains(scrollAreaType))
			scrollAreas.remove(scrollAreaType);
		
		scrollAreaType = new ScrollArea(typeOptions, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelType.text = typeOptions.get(position);
				DesignExporter.type = types.get(position);
				initAdditionalDataScrollArea(types.get(position));
			}
		});
		scrollAreaType.setBounds(xTopLeft + 93, yTopLeft + 65, 90, 14);
		scrollAreaType.setTitle("Design Type");
		scrollAreaType.setState(types.indexOf(type));
		labelType.text = type.getDisplayName();
		scrollAreas.add(scrollAreaType);
		
		initAdditionalDataScrollArea(type);
	}

	private void initAdditionalDataScrollArea(DesignType type) {
		if (type.hasAdditionalData()) {

			additionalDataKey = type.getAdditionalDataName();

			if (type.hasSizeData()) {
				
				if (type == DesignType.ROOF) {
					if (additionalDataValue % 2 == 0)
						additionalDataValue++;
				}
				if (additionalDataValue < type.getMinSize())
					additionalDataValue = type.getMinSize();
				if (additionalDataValue > type.getMaxSize())
					additionalDataValue = type.getMaxSize();
				labelAdditionalData.text = additionalDataValue + "m";
				
				if (type == DesignType.ROOF) {
					int min = (type.getMinSize() - 1) / 2;
					int max = (type.getMaxSize() - 1) / 2;
					
					scrollAreaAdditionalData = new ScrollArea(min, max +1, new IScrollAction() {
						@Override
						public void onScroll(int position) {
							additionalDataValue = position * 2 + 1;
							labelAdditionalData.text = additionalDataValue + "m";
						}
					});
					scrollAreaAdditionalData.setState((additionalDataValue -1) / 2);
					
				} else {
					int min = type.getMinSize();
					int max = type.getMaxSize();
					
					scrollAreaAdditionalData = new ScrollArea(min, max +1, new IScrollAction() {
						@Override
						public void onScroll(int position) {
							additionalDataValue = position;
							labelAdditionalData.text = position + "m";
						}
					});
					scrollAreaAdditionalData.setState(additionalDataValue);
				}
				
				scrollAreaAdditionalData.setNumeric(true);

			} else if (type.hasSubtypes()) {
				if (additionalDataValue == -1)
					additionalDataValue = 0;

				List<String> subtypeOptions = type.getSubtypeOptions();
				if (additionalDataValue >= subtypeOptions.size())
					additionalDataValue = 0;

				labelAdditionalData.text = subtypeOptions.get(additionalDataValue);
				scrollAreaAdditionalData = new ScrollArea(subtypeOptions, new IScrollAction() {
					@Override
					public void onScroll(int position) {
						additionalDataValue = position;
						labelAdditionalData.text = subtypeOptions.get(position);
					}
				});
				scrollAreaAdditionalData.setNumeric(false);
				scrollAreaAdditionalData.setState(additionalDataValue);
			}

			scrollAreaAdditionalData.setTitle(additionalDataKey);
			scrollAreaAdditionalData.setBounds(xTopLeft + 93, yTopLeft + 85, 90, 14);

		} else {

			additionalDataValue = -1;
			additionalDataKey = "";
			labelAdditionalData.text = "";
			scrollAreaAdditionalData = null;

		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GuiResources.EXPORTER.draw(this, xTopLeft, yTopLeft);
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		RenderHelper.enableStandardItemLighting();
        GlStateManager.pushMatrix();
		GlStateManager.translate((this.width - this.xSize) / 2 + 250, 280, 100);
		GlStateManager.rotate(-30, .4f, 0, -.2f);
		GlStateManager.rotate(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scale(300, -300, 300);
		itemRender.renderItem(mc.player.getHeldItemMainhand(), TransformType.GROUND);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		animationProgress++;
		
		int color = GuiResources.FONT_COLOR;
		fontRenderer.drawString("Export custom Designs", xTopLeft + 10, yTopLeft + 10, color, false);

		fontRenderer.drawString("Theme", xTopLeft + 10, yTopLeft + 28, color, false);
		fontRenderer.drawString("Building Layer", xTopLeft + 10, yTopLeft + 48, color, false);
		fontRenderer.drawString("Design Type", xTopLeft + 10, yTopLeft + 68, color, false);
		fontRenderer.drawString(additionalDataKey, xTopLeft + 10, yTopLeft + 88, color, false);

		labelTheme.draw(this);
		labelLayer.draw(this);
		labelType.draw(this);
		labelAdditionalData.draw(this);

		scrollAreas.forEach(area -> area.draw(this, mouseX, mouseY));
		if (scrollAreaAdditionalData != null)
			scrollAreaAdditionalData.draw(this, mouseX, mouseY);

	}

	@Override
	public void onGuiClosed() {
		DesignTheme theme = DesignExporter.theme;
		DesignExporter.layer = theme.getLayers().get(scrollAreaLayer.getState());
		
		List<DesignType> types = new ArrayList<>(theme.getTypes());
		
		// Roofs only in Roofing layer and vice versa
		if (DesignExporter.layer == DesignLayer.Roofing) {
			types.retainAll(DesignType.roofTypes());
		} else {
			types.removeAll(DesignType.roofTypes());			
		}
		
		DesignExporter.type = types.get(scrollAreaType.getState());
		DesignExporter.designParameter = additionalDataValue;
		PhaseEditTheme.setVisualization(PhaseEditTheme.selectedDesign);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int scrollAmount = ((mouseButton == 0) ? -1 : 1) * ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) ? 5 : 1);
		scrollAreaLayer.tryScroll(mouseX, mouseY, scrollAmount);
		scrollAreaType.tryScroll(mouseX, mouseY, scrollAmount);
		if (scrollAreaAdditionalData != null)
			scrollAreaAdditionalData.tryScroll(mouseX, mouseY, scrollAmount);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			int amount = (int) (scroll / -120f);
			scrollAreaLayer.tryScroll(i, j, amount);
			scrollAreaType.tryScroll(i, j, amount);
			if (scrollAreaAdditionalData != null)
				scrollAreaAdditionalData.tryScroll(i, j, amount);
		}
	}

}
