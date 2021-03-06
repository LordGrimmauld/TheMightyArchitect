package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.NBTTagCompound;

public class Facade extends Wall {

	@Override
	public Design fromNBT(NBTTagCompound compound) {
		Facade facade = new Facade();
		facade.expandBehaviour = ExpandBehaviour.None;
		facade.applyNBT(compound);
		return facade;
	}
	
}
