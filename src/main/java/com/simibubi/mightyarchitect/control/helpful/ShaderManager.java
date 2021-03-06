package com.simibubi.mightyarchitect.control.helpful;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

@EventBusSubscriber
public class ShaderManager {

	private static Shaders activeShader = Shaders.None;
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void preRender(RenderTickEvent event) {
		activeShader.setActive(true);
	}

	public static Shaders getActiveShader() {
		return activeShader;
	}

	public static void setActiveShader(Shaders activeShader) {
		ShaderManager.activeShader = activeShader;
	}
	
	public static void stopUsingShaders() {
		activeShader = Shaders.None;
	}
	
}
