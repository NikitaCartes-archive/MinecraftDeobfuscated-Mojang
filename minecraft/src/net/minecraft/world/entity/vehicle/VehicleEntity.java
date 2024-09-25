package net.minecraft.world.entity.vehicle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
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
	public boolean hurtClient(DamageSource damageSource) {
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		if (this.isRemoved()) {
			return true;
		} else if (this.isInvulnerableToBase(damageSource)) {
			return false;
		} else {
			boolean var10000;
			label32: {
				this.setHurtDir(-this.getHurtDir());
				this.setHurtTime(10);
				this.markHurt();
				this.setDamage(this.getDamage() + f * 10.0F);
				this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
				if (damageSource.getEntity() instanceof Player player && player.getAbilities().instabuild) {
					var10000 = true;
					break label32;
				}

				var10000 = false;
			}

			boolean bl = var10000;
			if ((bl || !(this.getDamage() > 40.0F)) && !this.shouldSourceDestroy(damageSource)) {
				if (bl) {
					this.discard();
				}
			} else {
				this.destroy(serverLevel, damageSource);
			}

			return true;
		}
	}

	boolean shouldSourceDestroy(DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return explosion.getIndirectSourceEntity() instanceof Mob && !explosion.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
	}

	public void destroy(ServerLevel serverLevel, Item item) {
		this.kill(serverLevel);
		if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			ItemStack itemStack = new ItemStack(item);
			itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
			this.spawnAtLocation(serverLevel, itemStack);
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ID_HURT, 0);
		builder.define(DATA_ID_HURTDIR, 1);
		builder.define(DATA_ID_DAMAGE, 0.0F);
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

	protected void destroy(ServerLevel serverLevel, DamageSource damageSource) {
		this.destroy(serverLevel, this.getDropItem());
	}

	@Override
	public int getDimensionChangingDelay() {
		return 10;
	}

	protected abstract Item getDropItem();
}
