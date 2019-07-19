package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WitherWallSkullBlock extends WallSkullBlock {
	protected WitherWallSkullBlock(Block.Properties properties) {
		super(SkullBlock.Types.WITHER_SKELETON, properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		Blocks.WITHER_SKELETON_SKULL.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
	}
}
