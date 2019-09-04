package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class MagmaBlock extends Block {
	public MagmaBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, Entity entity) {
		if (!entity.fireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
			entity.hurt(DamageSource.HOT_FLOOR, 1.0F);
		}

		super.stepOn(level, blockPos, entity);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean emissiveRendering(BlockState blockState) {
		return true;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		BubbleColumnBlock.growColumn(serverLevel, blockPos.above(), true);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.UP && blockState2.getBlock() == Blocks.WATER) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, this.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		BlockPos blockPos2 = blockPos.above();
		if (serverLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
			serverLevel.playSound(
				null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.8F
			);
			serverLevel.sendParticles(
				ParticleTypes.LARGE_SMOKE, (double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.25, (double)blockPos2.getZ() + 0.5, 8, 0.5, 0.25, 0.5, 0.0
			);
		}
	}

	@Override
	public int getTickDelay(LevelReader levelReader) {
		return 20;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.getBlockTicks().scheduleTick(blockPos, this, this.getTickDelay(level));
	}

	@Override
	public boolean isValidSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return entityType.fireImmune();
	}

	@Override
	public boolean hasPostProcess(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}
}
