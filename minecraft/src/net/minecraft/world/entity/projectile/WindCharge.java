package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WindCharge extends AbstractHurtingProjectile implements ItemSupplier {
	public WindCharge(EntityType<? extends WindCharge> entityType, Level level) {
		super(entityType, level);
	}

	public WindCharge(EntityType<? extends WindCharge> entityType, Breeze breeze, Level level) {
		super(entityType, breeze.getX(), breeze.getSnoutYPosition(), breeze.getZ(), level);
		this.setOwner(breeze);
	}

	@Override
	protected AABB makeBoundingBox() {
		float f = this.getType().getDimensions().width / 2.0F;
		float g = this.getType().getDimensions().height;
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
	protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 0.0F;
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		return entity instanceof WindCharge ? false : super.canCollideWith(entity);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return entity instanceof WindCharge ? false : super.canHitEntity(entity);
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (!this.level().isClientSide) {
			entityHitResult.getEntity().hurt(this.damageSources().mobProjectile(this, this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null), 1.0F);
			this.explode();
		}
	}

	private void explode() {
		this.level()
			.explode(
				this,
				null,
				null,
				this.getX(),
				this.getY(),
				this.getZ(),
				(float)(3.0 + this.random.nextDouble()),
				false,
				Level.ExplosionInteraction.BLOW,
				ParticleTypes.GUST,
				ParticleTypes.GUST_EMITTER,
				SoundEvents.WIND_BURST
			);
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		this.explode();
		this.discard();
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide) {
			this.discard();
		}
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

	@Nullable
	@Override
	protected ParticleOptions getTrailParticle() {
		return null;
	}

	@Override
	protected ClipContext.Block getClipType() {
		return ClipContext.Block.OUTLINE;
	}
}
