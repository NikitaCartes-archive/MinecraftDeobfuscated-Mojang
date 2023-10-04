package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MagmaBlock extends Block {
	public static final MapCodec<MagmaBlock> CODEC = simpleCodec(MagmaBlock::new);
	private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

	@Override
	public MapCodec<MagmaBlock> codec() {
		return CODEC;
	}

	public MagmaBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (!entity.isSteppingCarefully() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
			entity.hurt(level.damageSources().hotFloor(), 1.0F);
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		BubbleColumnBlock.updateColumn(serverLevel, blockPos.above(), blockState);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.UP && blockState2.is(Blocks.WATER)) {
			levelAccessor.scheduleTick(blockPos, this, 20);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.scheduleTick(blockPos, this, 20);
	}
}
