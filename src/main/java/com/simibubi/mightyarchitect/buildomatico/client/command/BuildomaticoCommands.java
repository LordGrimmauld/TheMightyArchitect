package com.simibubi.mightyarchitect.buildomatico.client.command;

import net.minecraft.command.CommandBase;
import net.minecraftforge.client.ClientCommandHandler;

public class BuildomaticoCommands {

	public static void init() {
		CommandBase[] commands = new CommandBase[] {
				new CommandStartDrawing(),
				new CommandDesign(),
				new CommandPalette(),
				new CommandInstantPrint(),
				new CommandSaveSchematic(),
				new CommandUnload()
		};
		
		for (CommandBase commandBase : commands) {
			ClientCommandHandler.instance.registerCommand(commandBase);
		}
	}

}