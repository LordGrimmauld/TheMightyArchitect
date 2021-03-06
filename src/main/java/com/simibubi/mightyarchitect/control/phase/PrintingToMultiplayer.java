package com.simibubi.mightyarchitect.control.phase;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class PrintingToMultiplayer extends PhaseBase {

	static List<BlockPos> remaining;
	static int cooldown;
	static boolean approved;
	
	@Override
	public void whenEntered() {
		remaining = new LinkedList<>(((TemplateBlockAccess) getModel().getMaterializedSketch()).getAllPositions());
		Minecraft.getMinecraft().player.sendChatMessage("/setblock checking permission for 'The Mighty Architect'.");
		cooldown = 500;
		approved = false;
	}

	@Override
	public void update() {
		if (cooldown > 0 && !approved) {
			cooldown--;
			return;
		}
		if (cooldown == 0) {
			ArchitectManager.enterPhase(ArchitectPhases.Previewing);
			return;
		}
		
		for (int i = 0; i < 10; i++) {
			if (!remaining.isEmpty()) {
				BlockPos pos = remaining.get(0);
				remaining.remove(0);
				pos = pos.add(getModel().getAnchor());
				IBlockState state = getModel().getMaterializedSketch().getBlockState(pos);
				
				if (!minecraft.world.mayPlace(state.getBlock(), pos, true, EnumFacing.DOWN, minecraft.player))
					continue;
				
				Minecraft.getMinecraft().player.sendChatMessage("/setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
				+ " " + state.getBlock().getRegistryName() + " " + state.getBlock().getMetaFromState(state));
			} else {
				ArchitectManager.unload();
				break;
			}			
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public static void onCommandFeedback(ClientChatReceivedEvent event) {
		if (event.getMessage() == null)
			return;
		
		if (cooldown > 0) {
			List<ITextComponent> checking = new LinkedList<>();
			checking.add(event.getMessage());
			
			while (!checking.isEmpty()) {
				ITextComponent iTextComponent = checking.get(0);
				if (iTextComponent instanceof TextComponentTranslation) {
					String test = ((TextComponentTranslation) iTextComponent).getKey();
					if (test.equals("commands.generic.permission")) {
						cooldown = 0;
						return;
					}
					if (test.equals("commands.generic.num.invalid")) {
						approved = true;
						Minecraft.getMinecraft().player.sendChatMessage("/me is printing a structure created by the Mighty Architect.");
						Minecraft.getMinecraft().player.sendChatMessage("/gamerule sendCommandFeedback false");
						Minecraft.getMinecraft().player.sendChatMessage("/gamerule logAdminCommands false");
						return;
					}
				} else {
					checking.addAll(iTextComponent.getSiblings());
				}
				checking.remove(iTextComponent);
			}
		}
	}
	
	@Override
	public void render() {
	}

	@Override
	public void whenExited() {
		if (approved) {
			Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString("Finished Printing, enjoy!"), false);
			Minecraft.getMinecraft().player.sendChatMessage("/gamerule logAdminCommands true");
			Minecraft.getMinecraft().player.sendChatMessage("/gamerule sendCommandFeedback true");			
		}
		cooldown = 0;
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Please be patient while your building is being transferred.");
	}

}
