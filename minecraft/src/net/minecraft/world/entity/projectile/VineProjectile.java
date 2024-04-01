package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VineProjectile extends AbstractHurtingProjectile {
	private static final ItemStack POTATO_STACKO = new ItemStack(Items.POISONOUS_POTATO);
	private static final EntityDataAccessor<Float> STRENGTH = SynchedEntityData.defineId(VineProjectile.class, EntityDataSerializers.FLOAT);
	private int lifetime = 60;

	public VineProjectile(EntityType<? extends VineProjectile> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STRENGTH, 5.0F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("lifetime", this.lifetime);
		compoundTag.putFloat("strength", this.strength());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.lifetime = compoundTag.getInt("lifetime");
		this.setStrength(compoundTag.getFloat("strength"));
	}

	public void setStrength(float f) {
		this.entityData.set(STRENGTH, f);
	}

	public float strength() {
		return this.entityData.get(STRENGTH);
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide) {
			this.lifetime--;
			if (this.lifetime <= 0) {
				this.discard();
				return;
			}
		}

		HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, Entity::isAlive, this.getClipType());
		this.onHit(hitResult);
		Vec3 vec3 = this.position();
		Vec3 vec32 = this.getDeltaMovement();
		Vec3 vec33 = vec3.add(vec32);
		Vec3 vec34 = vec3.add(vec32.scale(0.5));
		float f = this.strength();
		if (this.random.nextFloat() < f / 2.0F) {
			this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, vec33.x, vec33.y, vec33.z, 0.0, 0.0, 0.0);
		}

		if (this.random.nextFloat() < f / 2.0F) {
			this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, POTATO_STACKO), vec34.x, vec34.y, vec34.z, 0.0, 0.0, 0.0);
		}

		this.setPos(vec33.x, vec33.y, vec33.z);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		HitResult.Type type = hitResult.getType();
		if (type == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult)hitResult).getEntity();
			entity.hurt(this.level().damageSources().potatoMagic(), this.strength());
		} else if (type == HitResult.Type.BLOCK) {
			this.discard();
		}
	}
}
