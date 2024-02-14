package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class AbstractWindCharge extends AbstractHurtingProjectile implements ItemSupplier {
	public AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
		super(entityType, level);
	}

	public AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level, Entity entity, double d, double e, double f) {
		super(entityType, d, e, f, level);
		this.setOwner(entity);
	}

	AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, double d, double e, double f, double g, double h, double i, Level level) {
		super(entityType, d, e, f, g, h, i, level);
	}

	@Override
	protected AABB makeBoundingBox() {
		float f = this.getType().getDimensions().width() / 2.0F;
		float g = this.getType().getDimensions().height();
		float h = 0.15F;
		return new AABB(
			this.position().x - (double)f,
			this.position().y - 0.15F,
			this.position().z - (double)f,
			this.position().x + (double)f,
			this.position().y - 0.15F + (double)g,
			this.position().z + (double)f
		);
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		return entity instanceof AbstractWindCharge ? false : super.canCollideWith(entity);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return entity instanceof AbstractWindCharge ? false : super.canHitEntity(entity);
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (!this.level().isClientSide) {
			entityHitResult.getEntity().hurt(this.damageSources().windCharge(this, this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null), 1.0F);
			this.explode();
		}
	}

	protected abstract void explode();

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		if (!this.level().isClientSide) {
			this.explode();
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		Level level = this.level();
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
			BlockState blockState = level.getBlockState(blockPos);
			if (!blockState.isCollisionShapeFullBlock(level, blockPos)) {
				return;
			}
		}

		super.onHit(hitResult);
		if (!this.level().isClientSide && !this.isDeflected) {
			this.discard();
		}

		this.isDeflected = false;
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Override
	public ItemStack getItem() {
		return ItemStack.EMPTY;
	}

	@Override
	protected float getInertia() {
		return 1.0F;
	}

	@Override
	protected float getLiquidInertia() {
		return this.getInertia();
	}

	@Nullable
	@Override
	protected ParticleOptions getTrailParticle() {
		return null;
	}

	@Override
	protected ClipContext.Block getClipType() {
		return ClipContext.Block.OUTLINE;
	}

	public static class WindChargeDamageCalculator extends ExplosionDamageCalculator {
		@Override
		public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
			return false;
		}

		@Override
		public Optional<Float> getBlockExplosionResistance(
			Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState
		) {
			return blockState.is(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS) ? Optional.of(3600000.0F) : Optional.empty();
		}
	}
}
