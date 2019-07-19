package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ShearsItem extends Item {
	public ShearsItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (!level.isClientSide) {
			itemStack.hurtAndBreak(1, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}

		Block block = blockState.getBlock();
		return !blockState.is(BlockTags.LEAVES)
				&& block != Blocks.COBWEB
				&& block != Blocks.GRASS
				&& block != Blocks.FERN
				&& block != Blocks.DEAD_BUSH
				&& block != Blocks.VINE
				&& block != Blocks.TRIPWIRE
				&& !block.is(BlockTags.WOOL)
			? super.mineBlock(itemStack, level, blockState, blockPos, livingEntity)
			: true;
	}

	@Override
	public boolean canDestroySpecial(BlockState blockState) {
		Block block = blockState.getBlock();
		return block == Blocks.COBWEB || block == Blocks.REDSTONE_WIRE || block == Blocks.TRIPWIRE;
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		Block block = blockState.getBlock();
		if (block == Blocks.COBWEB || blockState.is(BlockTags.LEAVES)) {
			return 15.0F;
		} else {
			return block.is(BlockTags.WOOL) ? 5.0F : super.getDestroySpeed(itemStack, blockState);
		}
	}
}
