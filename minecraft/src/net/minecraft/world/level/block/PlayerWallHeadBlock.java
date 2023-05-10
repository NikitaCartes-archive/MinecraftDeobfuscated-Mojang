package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public class PlayerWallHeadBlock extends WallSkullBlock {
	protected PlayerWallHeadBlock(BlockBehaviour.Properties properties) {
		super(SkullBlock.Types.PLAYER, properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		Blocks.PLAYER_HEAD.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
	}

	@Override
	public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
		return Blocks.PLAYER_HEAD.getDrops(blockState, builder);
	}
}
