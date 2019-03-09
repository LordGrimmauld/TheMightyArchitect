package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.buildomatico.DesignStorage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemWandArchitect extends ItemForMightyArchitects {

	public ItemWandArchitect(String name) {
		super(name);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos anchor, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			IBlockState blockState = worldIn.getBlockState(anchor);

			if (blockState.getBlock() == Blocks.WOOL) {
				String name = DesignStorage.exportDesign(worldIn, player, anchor, blockState);
				if (!name.isEmpty()) {
					player.sendMessage(new TextComponentString("Exported new Design: " + name));
				}
			} else if (blockState.getBlock() == Blocks.DIAMOND_BLOCK) {
				DesignStorage.designMatrix = null;
				player.sendMessage(new TextComponentString("Reloading desings..."));
			}

			if (player.isSneaking()) {
				player.getHeldItem(hand).setStackDisplayName("Building Placer");
			}

		}
		player.getCooldownTracker().setCooldown(this, 10);
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

}