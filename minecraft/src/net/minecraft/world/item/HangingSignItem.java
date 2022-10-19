package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem extends StandingAndWallBlockItem {
	public HangingSignItem(Block block, Block block2, Item.Properties properties) {
		super(block, block2, properties, Direction.UP);
	}

	@Override
	protected boolean canPlace(LevelReader levelReader, BlockState blockState, BlockPos blockPos) {
		if (blockState.getBlock() instanceof WallHangingSignBlock wallHangingSignBlock && !wallHangingSignBlock.canPlace(blockState, levelReader, blockPos)) {
			return false;
		}

		return super.canPlace(levelReader, blockState, blockPos);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, @Nullable Player player, ItemStack itemStack, BlockState blockState) {
		boolean bl = super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
		if (!level.isClientSide && !bl && player != null && level.getBlockEntity(blockPos) instanceof SignBlockEntity signBlockEntity) {
			player.openTextEdit(signBlockEntity);
		}

		return bl;
	}
}
