package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

public abstract class AgableMob extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgableMob.class, EntityDataSerializers.BOOLEAN);
	protected int age;
	protected int forcedAge;
	protected int forcedAgeTimer;

	protected AgableMob(EntityType<? extends AgableMob> entityType, Level level) {
		super(entityType, level);
	}

	@Nullable
	public abstract AgableMob getBreedOffspring(AgableMob agableMob);

	protected void onOffspringSpawnedFromEgg(Player player, AgableMob agableMob) {
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		if (item instanceof SpawnEggItem && ((SpawnEggItem)item).spawnsEntity(itemStack.getTag(), this.getType())) {
			if (!this.level.isClientSide) {
				AgableMob agableMob = this.getBreedOffspring(this);
				if (agableMob != null) {
					agableMob.setAge(-24000);
					agableMob.moveTo(this.x, this.y, this.z, 0.0F, 0.0F);
					this.level.addFreshEntity(agableMob);
					if (itemStack.hasCustomHoverName()) {
						agableMob.setCustomName(itemStack.getHoverName());
					}

					this.onOffspringSpawnedFromEgg(player, agableMob);
					if (!player.abilities.instabuild) {
						itemStack.shrink(1);
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_BABY_ID, false);
	}

	public int getAge() {
		if (this.level.isClientSide) {
			return this.entityData.get(DATA_BABY_ID) ? -1 : 1;
		} else {
			return this.age;
		}
	}

	public void ageUp(int i, boolean bl) {
		int j = this.getAge();
		j += i * 20;
		if (j > 0) {
			j = 0;
		}

		int l = j - j;
		this.setAge(j);
		if (bl) {
			this.forcedAge += l;
			if (this.forcedAgeTimer == 0) {
				this.forcedAgeTimer = 40;
			}
		}

		if (this.getAge() == 0) {
			this.setAge(this.forcedAge);
		}
	}

	public void ageUp(int i) {
		this.ageUp(i, false);
	}

	public void setAge(int i) {
		int j = this.age;
		this.age = i;
		if (j < 0 && i >= 0 || j >= 0 && i < 0) {
			this.entityData.set(DATA_BABY_ID, i < 0);
			this.ageBoundaryReached();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Age", this.getAge());
		compoundTag.putInt("ForcedAge", this.forcedAge);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setAge(compoundTag.getInt("Age"));
		this.forcedAge = compoundTag.getInt("ForcedAge");
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_BABY_ID.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level.isClientSide) {
			if (this.forcedAgeTimer > 0) {
				if (this.forcedAgeTimer % 4 == 0) {
					this.level
						.addParticle(
							ParticleTypes.HAPPY_VILLAGER,
							this.x + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
							this.y + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
							this.z + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
							0.0,
							0.0,
							0.0
						);
				}

				this.forcedAgeTimer--;
			}
		} else if (this.isAlive()) {
			int i = this.getAge();
			if (i < 0) {
				this.setAge(++i);
			} else if (i > 0) {
				this.setAge(--i);
			}
		}
	}

	protected void ageBoundaryReached() {
	}

	@Override
	public boolean isBaby() {
		return this.getAge() < 0;
	}
}
