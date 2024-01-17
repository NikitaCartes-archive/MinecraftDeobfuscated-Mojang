package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerWallHeadBlock extends WallSkullBlock {
	public static final MapCodec<PlayerWallHeadBlock> CODEC = simpleCodec(PlayerWallHeadBlock::new);

	@Override
	public MapCodec<PlayerWallHeadBlock> codec() {
		return CODEC;
	}

	protected PlayerWallHeadBlock(BlockBehaviour.Properties properties) {
		super(SkullBlock.Types.PLAYER, properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		PlayerHeadBlock.initializeSkullOwner(level, blockPos, itemStack);
	}
}
