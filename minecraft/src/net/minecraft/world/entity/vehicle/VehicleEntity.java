package net.minecraft.world.entity.vehicle;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class VehicleEntity extends Entity {
	protected static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
	protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
	protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

	public VehicleEntity(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.level().isClientSide || this.isRemoved()) {
			return true;
		} else if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			this.setHurtDir(-this.getHurtDir());
			this.setHurtTime(10);
			this.markHurt();
			this.setDamage(this.getDamage() + f * 10.0F);
			this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
			boolean bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).getAbilities().instabuild;
			if ((bl || !(this.getDamage() > 40.0F)) && (!bl || !this.shouldVehicleAlwaysReactToDamageSource())) {
				if (bl) {
					this.discard();
				}
			} else {
				this.destroy(damageSource);
			}

			return true;
		}
	}

	boolean shouldVehicleAlwaysReactToDamageSource() {
		return false;
	}

	public void destroy(Item item) {
		this.kill();
		if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			ItemStack itemStack = new ItemStack(item);
			if (this.hasCustomName()) {
				itemStack.setHoverName(this.getCustomName());
			}

			this.spawnAtLocation(itemStack);
		}
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_ID_HURT, 0);
		this.entityData.define(DATA_ID_HURTDIR, 1);
		this.entityData.define(DATA_ID_DAMAGE, 0.0F);
	}

	public void setHurtTime(int i) {
		this.entityData.set(DATA_ID_HURT, i);
	}

	public void setHurtDir(int i) {
		this.entityData.set(DATA_ID_HURTDIR, i);
	}

	public void setDamage(float f) {
		this.entityData.set(DATA_ID_DAMAGE, f);
	}

	public float getDamage() {
		return this.entityData.get(DATA_ID_DAMAGE);
	}

	public int getHurtTime() {
		return this.entityData.get(DATA_ID_HURT);
	}

	public int getHurtDir() {
		return this.entityData.get(DATA_ID_HURTDIR);
	}

	protected void destroy(DamageSource damageSource) {
		this.destroy(this.getDropItem());
	}

	abstract Item getDropItem();
}
