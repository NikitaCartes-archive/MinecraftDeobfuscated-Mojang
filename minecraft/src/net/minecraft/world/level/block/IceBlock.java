package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;

public class IceBlock extends HalfTransparentBlock {
	public IceBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public BlockLayer getRenderLayer() {
		return BlockLayer.TRANSLUCENT;
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
			if (level.dimension.isUltraWarm()) {
				level.removeBlock(blockPos, false);
				return;
			}

			Material material = level.getBlockState(blockPos.below()).getMaterial();
			if (material.blocksMotion() || material.isLiquid()) {
				level.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
			}
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (serverLevel.getBrightness(LightLayer.BLOCK, blockPos) > 11 - blockState.getLightBlock(serverLevel, blockPos)) {
			this.melt(blockState, serverLevel, blockPos);
		}
	}

	protected void melt(BlockState blockState, Level level, BlockPos blockPos) {
		if (level.dimension.isUltraWarm()) {
			level.removeBlock(blockPos, false);
		} else {
			level.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
			level.neighborChanged(blockPos, Blocks.WATER, blockPos);
		}
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.NORMAL;
	}

	@Override
	public boolean isValidSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return entityType == EntityType.POLAR_BEAR;
	}
}
