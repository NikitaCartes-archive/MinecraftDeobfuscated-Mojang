package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class IceBlock extends HalfTransparentBlock {
	public IceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static BlockState meltsInto() {
		return Blocks.WATER.defaultBlockState();
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
			if (level.dimensionType().ultraWarm()) {
				level.removeBlock(blockPos, false);
				return;
			}

			BlockState blockState2 = level.getBlockState(blockPos.below());
			if (blockState2.blocksMotion() || blockState2.liquid()) {
				level.setBlockAndUpdate(blockPos, meltsInto());
			}
		}
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.getBrightness(LightLayer.BLOCK, blockPos) > 11 - blockState.getLightBlock(serverLevel, blockPos)) {
			this.melt(blockState, serverLevel, blockPos);
		}
	}

	protected void melt(BlockState blockState, Level level, BlockPos blockPos) {
		if (level.dimensionType().ultraWarm()) {
			level.removeBlock(blockPos, false);
		} else {
			level.setBlockAndUpdate(blockPos, meltsInto());
			level.neighborChanged(blockPos, meltsInto().getBlock(), blockPos);
		}
	}
}
