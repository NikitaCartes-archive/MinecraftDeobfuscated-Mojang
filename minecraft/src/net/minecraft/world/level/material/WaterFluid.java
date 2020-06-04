package net.minecraft.world.level.material;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public abstract class WaterFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return Fluids.FLOWING_WATER;
	}

	@Override
	public Fluid getSource() {
		return Fluids.WATER;
	}

	@Override
	public Item getBucket() {
		return Items.WATER_BUCKET;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
		if (!fluidState.isSource() && !(Boolean)fluidState.getValue(FALLING)) {
			if (random.nextInt(64) == 0) {
				level.playLocalSound(
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					SoundEvents.WATER_AMBIENT,
					SoundSource.BLOCKS,
					random.nextFloat() * 0.25F + 0.75F,
					random.nextFloat() + 0.5F,
					false
				);
			}
		} else if (random.nextInt(10) == 0) {
			level.addParticle(
				ParticleTypes.UNDERWATER,
				(double)blockPos.getX() + random.nextDouble(),
				(double)blockPos.getY() + random.nextDouble(),
				(double)blockPos.getZ() + random.nextDouble(),
				0.0,
				0.0,
				0.0
			);
		}
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	@Override
	public ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_WATER;
	}

	@Override
	protected boolean canConvertToSource() {
		return true;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		BlockEntity blockEntity = blockState.getBlock().isEntityBlock() ? levelAccessor.getBlockEntity(blockPos) : null;
		Block.dropResources(blockState, levelAccessor.getLevel(), blockPos, blockEntity);
	}

	@Override
	public int getSlopeFindDistance(LevelReader levelReader) {
		return 4;
	}

	@Override
	public BlockState createLegacyBlock(FluidState fluidState) {
		return Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(fluidState)));
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
	}

	@Override
	public int getDropOff(LevelReader levelReader) {
		return 1;
	}

	@Override
	public int getTickDelay(LevelReader levelReader) {
		return 5;
	}

	@Override
	public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
		return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	public static class Flowing extends WaterFluid {
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState fluidState) {
			return (Integer)fluidState.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState fluidState) {
			return false;
		}
	}

	public static class Source extends WaterFluid {
		@Override
		public int getAmount(FluidState fluidState) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState fluidState) {
			return true;
		}
	}
}
