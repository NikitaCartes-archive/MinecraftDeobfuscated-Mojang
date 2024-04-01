package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PotatoPortalBlock extends Block {
	public static final MapCodec<PotatoPortalBlock> CODEC = simpleCodec(PotatoPortalBlock::new);
	protected static final VoxelShape FULL_AXIS_AABB = Block.box(3.0, 0.0, 3.0, 13.0, 24.0, 13.0);

	@Override
	public MapCodec<PotatoPortalBlock> codec() {
		return CODEC;
	}

	public PotatoPortalBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return FULL_AXIS_AABB;
	}

	@Override
	protected boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return false;
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity.canChangeDimensions()) {
			entity.handleInsidePortal(blockPos);
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(100) == 0) {
			level.playLocalSound(
				(double)blockPos.getX() + 0.5,
				(double)blockPos.getY() + 0.5,
				(double)blockPos.getZ() + 0.5,
				SoundEvents.PLAGUEWHALE_AMBIENT,
				SoundSource.BLOCKS,
				0.5F,
				randomSource.nextFloat() * 0.4F + 0.8F,
				false
			);
		}

		for (int i = 0; i < 4; i++) {
			double d = (double)blockPos.getX() + randomSource.nextDouble();
			double e = (double)blockPos.getY() + randomSource.nextDouble();
			double f = (double)blockPos.getZ() + randomSource.nextDouble();
			double g = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			double h = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			double j = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			int k = randomSource.nextInt(2) * 2 - 1;
			if (!level.getBlockState(blockPos.west()).is(this) && !level.getBlockState(blockPos.east()).is(this)) {
				d = (double)blockPos.getX() + 0.5 + 0.25 * (double)k;
				g = (double)(randomSource.nextFloat() * 2.0F * (float)k);
			} else {
				f = (double)blockPos.getZ() + 0.5 + 0.25 * (double)k;
				j = (double)(randomSource.nextFloat() * 2.0F * (float)k);
			}

			level.addParticle(ParticleTypes.POTATO_LIGHTNING, d, e, f, g, h, j);
			level.addParticle(ParticleTypes.REVERSE_POTATO_LIGHTNING, d, e, f, g, h, j);
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}
}
