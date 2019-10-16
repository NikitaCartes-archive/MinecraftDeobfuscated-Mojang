package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoneyBlock extends HalfTransparentBlock {
	protected static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);

	public HoneyBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
		this.doLandingParticleEffect(level, blockPos, entity);
		if (entity.causeFallDamage(f, 0.2F)) {
			entity.playSound(this.soundType.getFallSound(), this.soundType.getVolume() * 0.5F, this.soundType.getPitch() * 0.75F);
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (this.isSlidingDown(blockPos, entity)) {
			Vec3 vec3 = entity.getDeltaMovement();
			if (vec3.y < -0.05) {
				entity.setDeltaMovement(new Vec3(vec3.x, -0.05, vec3.z));
			}

			entity.fallDistance = 0.0F;
			this.doSlideDownParticleEffects(level, blockPos, entity);
			if (level.getGameTime() % 10L == 0L) {
				entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
			}
		}

		super.entityInside(blockState, level, blockPos, entity);
	}

	private boolean isSlidingDown(BlockPos blockPos, Entity entity) {
		if (entity.onGround) {
			return false;
		} else if (entity.getY() > (double)blockPos.getY() + 0.9375 - 1.0E-7) {
			return false;
		} else if (entity.getDeltaMovement().y >= -0.04) {
			return false;
		} else {
			double d = Math.abs((double)blockPos.getX() + 0.5 - entity.getX());
			double e = Math.abs((double)blockPos.getZ() + 0.5 - entity.getZ());
			double f = 0.4375 + (double)(entity.getBbWidth() / 2.0F);
			return d + 1.0E-7 > f || e + 1.0E-7 > f;
		}
	}

	private void doSlideDownParticleEffects(Level level, BlockPos blockPos, Entity entity) {
		float f = entity.getDimensions(Pose.STANDING).width;
		this.doParticleEffects(
			entity,
			level,
			blockPos,
			1,
			((double)level.random.nextFloat() - 0.5) * (double)f,
			(double)(level.random.nextFloat() / 2.0F),
			((double)level.random.nextFloat() - 0.5) * (double)f,
			(double)level.random.nextFloat() - 0.5,
			(double)(level.random.nextFloat() - 1.0F),
			(double)level.random.nextFloat() - 0.5
		);
	}

	private void doLandingParticleEffect(Level level, BlockPos blockPos, Entity entity) {
		float f = entity.getDimensions(Pose.STANDING).width;
		this.doParticleEffects(
			entity,
			level,
			blockPos,
			10,
			((double)level.random.nextFloat() - 0.5) * (double)f,
			0.0,
			((double)level.random.nextFloat() - 0.5) * (double)f,
			(double)level.random.nextFloat() - 0.5,
			0.5,
			(double)level.random.nextFloat() - 0.5
		);
	}

	private void doParticleEffects(Entity entity, Level level, BlockPos blockPos, int i, double d, double e, double f, double g, double h, double j) {
		BlockState blockState = level.getBlockState(new BlockPos(blockPos));

		for (int k = 0; k < i; k++) {
			entity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), entity.getX() + d, entity.getY() + e, entity.getZ() + f, g, h, j);
		}
	}
}
