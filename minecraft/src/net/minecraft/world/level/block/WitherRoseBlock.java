package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WitherRoseBlock extends FlowerBlock {
	public static final MapCodec<WitherRoseBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(instance, WitherRoseBlock::new)
	);

	@Override
	public MapCodec<WitherRoseBlock> codec() {
		return CODEC;
	}

	public WitherRoseBlock(Holder<MobEffect> holder, float f, BlockBehaviour.Properties properties) {
		this(makeEffectList(holder, f), properties);
	}

	public WitherRoseBlock(SuspiciousStewEffects suspiciousStewEffects, BlockBehaviour.Properties properties) {
		super(suspiciousStewEffects, properties);
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return super.mayPlaceOn(blockState, blockGetter, blockPos)
			|| blockState.is(Blocks.NETHERRACK)
			|| blockState.is(Blocks.SOUL_SAND)
			|| blockState.is(Blocks.SOUL_SOIL);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		VoxelShape voxelShape = this.getShape(blockState, level, blockPos, CollisionContext.empty());
		Vec3 vec3 = voxelShape.bounds().getCenter();
		double d = (double)blockPos.getX() + vec3.x;
		double e = (double)blockPos.getZ() + vec3.z;

		for (int i = 0; i < 3; i++) {
			if (randomSource.nextBoolean()) {
				level.addParticle(
					ParticleTypes.SMOKE,
					d + randomSource.nextDouble() / 5.0,
					(double)blockPos.getY() + (0.5 - randomSource.nextDouble()),
					e + randomSource.nextDouble() / 5.0,
					0.0,
					0.0,
					0.0
				);
			}
		}
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide && level.getDifficulty() != Difficulty.PEACEFUL) {
			if (entity instanceof LivingEntity livingEntity && !livingEntity.isInvulnerableTo(level.damageSources().wither())) {
				livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40));
			}
		}
	}
}
