package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ShearsItem extends Item {
	public ShearsItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (!level.isClientSide && !blockState.is(BlockTags.FIRE)) {
			itemStack.hurtAndBreak(1, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}

		return !blockState.is(BlockTags.LEAVES)
				&& !blockState.is(Blocks.COBWEB)
				&& !blockState.is(Blocks.GRASS)
				&& !blockState.is(Blocks.FERN)
				&& !blockState.is(Blocks.DEAD_BUSH)
				&& !blockState.is(Blocks.HANGING_ROOTS)
				&& !blockState.is(Blocks.VINE)
				&& !blockState.is(Blocks.TRIPWIRE)
				&& !blockState.is(BlockTags.WOOL)
			? super.mineBlock(itemStack, level, blockState, blockPos, livingEntity)
			: true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockState) {
		return blockState.is(Blocks.COBWEB) || blockState.is(Blocks.REDSTONE_WIRE) || blockState.is(Blocks.TRIPWIRE);
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		if (blockState.is(Blocks.COBWEB) || blockState.is(BlockTags.LEAVES)) {
			return 15.0F;
		} else if (blockState.is(BlockTags.WOOL)) {
			return 5.0F;
		} else {
			return !blockState.is(Blocks.VINE) && !blockState.is(Blocks.GLOW_LICHEN) ? super.getDestroySpeed(itemStack, blockState) : 2.0F;
		}
	}
}
