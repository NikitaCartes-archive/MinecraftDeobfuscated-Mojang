package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractWindCharge extends AbstractHurtingProjectile implements ItemSupplier {
	public static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
		true, false, Optional.empty(), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
	);

	public AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
		super(entityType, level);
		this.accelerationPower = 0.0;
	}

	public AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level, Entity entity, double d, double e, double f) {
		super(entityType, d, e, f, level);
		this.setOwner(entity);
		this.accelerationPower = 0.0;
	}

	AbstractWindCharge(EntityType<? extends AbstractWindCharge> entityType, double d, double e, double f, Vec3 vec3, Level level) {
		super(entityType, d, e, f, vec3, level);
		this.accelerationPower = 0.0;
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
		if (entity instanceof AbstractWindCharge) {
			return false;
		} else {
			return entity.getType() == EntityType.END_CRYSTAL ? false : super.canHitEntity(entity);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (!this.level().isClientSide) {
			LivingEntity livingEntity2 = this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
			Entity entity = (Entity)entityHitResult.getEntity().getPassengerClosestTo(entityHitResult.getLocation()).orElse(entityHitResult.getEntity());
			if (livingEntity2 != null) {
				livingEntity2.setLastHurtMob(entity);
			}

			entity.hurt(this.damageSources().windCharge(this, livingEntity2), 1.0F);
			this.explode();
		}
	}

	@Override
	public void push(double d, double e, double f) {
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
	public void tick() {
		if (!this.level().isClientSide && this.getBlockY() > this.level().getMaxBuildHeight() + 30) {
			this.explode();
			this.discard();
		} else {
			super.tick();
		}
	}
}
