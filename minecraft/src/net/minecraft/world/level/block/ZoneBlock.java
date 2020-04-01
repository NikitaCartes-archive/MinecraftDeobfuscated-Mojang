package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ZoneBlock extends Block {
	protected ZoneBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.block();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		double d = (double)blockPos.getX() + 0.5;
		double e = (double)blockPos.getZ() + 0.5;

		for (int i = 0; i < 3; i++) {
			if (random.nextBoolean()) {
				level.addParticle(
					ParticleTypes.COMPOSTER,
					d + (double)(random.nextFloat() / 5.0F),
					(double)blockPos.getY() + (0.5 - (double)random.nextFloat()),
					e + (double)(random.nextFloat() / 5.0F),
					0.0,
					0.0,
					0.0
				);
			}
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity instanceof LivingEntity) {
			((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.POISON, 60, 3, true, true));
			((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, true, true));
		}

		if (entity instanceof ItemEntity) {
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.0, 2.0, 0.0));
		}

		super.entityInside(blockState, level, blockPos, entity);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}
}
