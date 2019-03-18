package com.simibubi.mightyarchitect.control.design;

import java.util.List;

import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.design.StyleGroupManager.StyleGroupDesignProvider;
import com.simibubi.mightyarchitect.control.design.partials.Design.DesignInstance;
import com.simibubi.mightyarchitect.control.helpful.DesignHelper;

import net.minecraft.util.math.BlockPos;

public class StandardDesignPicker implements IPickDesigns {

	private DesignTheme theme;
	
	public Sketch assembleSketch(GroundPlan groundPlan) {
		Sketch sketch = pickDesigns(groundPlan);
		return sketch;
	}

	private Sketch pickDesigns(GroundPlan groundPlan) {
		Sketch sketch = new Sketch();
		StyleGroupManager styleGroupManager = new StyleGroupManager();
		
		groundPlan.forEachStack(stack -> {
			stack.forEach(room -> {
				
				BlockPos origin = room.getOrigin();
				List<DesignInstance> designList = room.secondaryPalette ? sketch.secondary : sketch.primary;
				StyleGroupDesignProvider styleGroup = styleGroupManager.getStyleGroup(room.styleGroup);

				BlockPos size = room.getSize();
				DesignHelper.addCuboid(styleGroup, designList, theme, room.designLayer, origin, size);
				
				if (room != stack.highest())
					return;
				
				switch (room.roofType) {
				case ROOF:
					if (room.width == room.length) {
						DesignHelper.addNormalCrossRoof(styleGroup, designList, theme, DesignLayer.Independent, origin.up(room.height), size);
					} else {
						DesignHelper.addNormalRoof(styleGroup, designList, theme, DesignLayer.Independent, origin.up(room.height), size);
					}
					break;
					
				case FLAT_ROOF:
					DesignHelper.addFlatRoof(styleGroup, designList, theme, DesignLayer.Independent, origin.up(room.height), size);
					break;
					
				default:
					break;
				}
				
			});
		});
		
		sketch.interior = groundPlan.getInterior();
		return sketch;
	}

	@Override
	public void setTheme(DesignTheme theme) {
		this.theme = theme;
	}

}