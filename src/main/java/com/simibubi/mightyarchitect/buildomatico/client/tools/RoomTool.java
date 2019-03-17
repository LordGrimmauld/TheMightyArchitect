package com.simibubi.mightyarchitect.buildomatico.client.tools;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.buildomatico.client.GroundPlannerClient;
import com.simibubi.mightyarchitect.buildomatico.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Room;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Stack;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignType;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class RoomTool extends GroundPlanningToolBase {

	protected BlockPos firstPosition;
	protected Stack lastAddedStack;

	@Override
	public void init(GroundPlannerClient planner) {
		super.init(planner);
		firstPosition = null;
	}
	
	@Override
	public String handleRightClick() {
		super.handleRightClick();

		if (selectedPosition == null)
			return null;

		GroundPlan groundPlan = planner.getGroundPlan();

		if (firstPosition == null) {
			firstPosition = selectedPosition;
			return "First position marked";

		} else {
			Room room = new Room(firstPosition, selectedPosition.subtract(firstPosition));
			room.width++;
			room.length++;
			room.height = 2;
			room.designLayer = DesignLayer.Foundation;
			int facadeWidth = Math.min(room.width, room.length);

			if (facadeWidth % 2 == 0) {
				return "�cFacade cannot have even width: " + facadeWidth;
			}
			if (facadeWidth < 5) {
				return "�cFacade is too narrow (<5): " + facadeWidth;
			}
			if (facadeWidth > 25) {
				return "�cFacade is too wide (>25): " + facadeWidth;
			}

			room.roofType = facadeWidth > 15 ? DesignType.FLAT_ROOF : DesignType.ROOF;
			lastAddedStack = groundPlan.startStack(room);
			firstPosition = null;
			return "�aNew Room has been added";
		}
	}
	
	@Override
	public void handleKey(int key) {
		if (lastAddedStack == null)
			return;
		
		switch(key) {
		case Keyboard.KEY_UP:
			lastAddedStack.increase();
			break;
		case Keyboard.KEY_DOWN:
			lastAddedStack.decrease();
			if (lastAddedStack.floors() == 0) {
				planner.getGroundPlan().remove(lastAddedStack);
				lastAddedStack = null;
			}
			break;
		}
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (firstPosition == null)
			return;

		if (selectedPosition == null)
			return;

		BlockPos size = selectedPosition.subtract(firstPosition);
		if (size.getX() % 2 != 0) {
			selectedPosition = selectedPosition.east(size.getX() > 0 ? 1 : -1);
		}
		if (size.getZ() % 2 != 0) {
			selectedPosition = selectedPosition.south(size.getZ() > 0 ? 1 : -1);
		}
	}

	@Override
	public void render() {
		if (selectedPosition == null) {
			return;
		}
		
		BlockPos anchor = planner.getAnchor();
		BlockPos selectedPos = (anchor != null)? selectedPosition.add(anchor) : selectedPosition;
		BlockPos firstPos = (firstPosition != null)? firstPosition.add(anchor) : null;

		TesselatorTextures.Selection.bind();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		TessellatorHelper.walls(bufferBuilder, selectedPos, new BlockPos(1, 1, 1), 0.125, false, true);

		if (firstPos != null) {
			BlockPos size = selectedPos.subtract(firstPos);
			Cuboid selection = new Cuboid(firstPos, size.getX(), 1, size.getZ());
			selection.width += 1;
			selection.length += 1;
			TessellatorHelper.walls(bufferBuilder, selection.getOrigin(), selection.getSize(), -0.125, false, true);
			Tessellator.getInstance().draw();

			TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z - 1, true, false);

			TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z + selection.length + 1, true, false);

			TessellatorHelper.drawString("" + selection.length, selection.x + selection.width + 1, selection.y + .5f,
					selection.z + selection.length / 2f, true, false);

			TessellatorHelper.drawString("" + selection.length, selection.x - 1, selection.y + .5f,
					selection.z + selection.length / 2f, true, false);

		} else {
			Tessellator.getInstance().draw();
		}

	}

}
