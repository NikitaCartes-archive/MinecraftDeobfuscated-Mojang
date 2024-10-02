package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;

public class Horse extends AbstractHorse implements VariantHolder<Variant> {
	private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);
	private static final EntityDimensions BABY_DIMENSIONS = EntityType.HORSE
		.getDimensions()
		.withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, EntityType.HORSE.getHeight() + 0.125F, 0.0F))
		.scale(0.5F);

	public Horse(EntityType<? extends Horse> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void randomizeAttributes(RandomSource randomSource) {
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)generateMaxHealth(randomSource::nextInt));
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(generateSpeed(randomSource::nextDouble));
		this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateJumpStrength(randomSource::nextDouble));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ID_TYPE_VARIANT, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Variant", this.getTypeVariant());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setTypeVariant(compoundTag.getInt("Variant"));
	}

	private void setTypeVariant(int i) {
		this.entityData.set(DATA_ID_TYPE_VARIANT, i);
	}

	private int getTypeVariant() {
		return this.entityData.get(DATA_ID_TYPE_VARIANT);
	}

	private void setVariantAndMarkings(Variant variant, Markings markings) {
		this.setTypeVariant(variant.getId() & 0xFF | markings.getId() << 8 & 0xFF00);
	}

	public Variant getVariant() {
		return Variant.byId(this.getTypeVariant() & 0xFF);
	}

	public void setVariant(Variant variant) {
		this.setTypeVariant(variant.getId() & 0xFF | this.getTypeVariant() & -256);
	}

	public Markings getMarkings() {
		return Markings.byId((this.getTypeVariant() & 0xFF00) >> 8);
	}

	@Override
	protected void playGallopSound(SoundType soundType) {
		super.playGallopSound(soundType);
		if (this.random.nextInt(10) == 0) {
			this.playSound(SoundEvents.HORSE_BREATHE, soundType.getVolume() * 0.6F, soundType.getPitch());
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.HORSE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.HORSE_DEATH;
	}

	@Nullable
	@Override
	protected SoundEvent getEatingSound() {
		return SoundEvents.HORSE_EAT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.HORSE_HURT;
	}

	@Override
	protected SoundEvent getAngrySound() {
		return SoundEvents.HORSE_ANGRY;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		boolean bl = !this.isBaby() && this.isTamed() && player.isSecondaryUseActive();
		if (!this.isVehicle() && !bl) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			if (!itemStack.isEmpty()) {
				if (this.isFood(itemStack)) {
					return this.fedFood(player, itemStack);
				}

				if (!this.isTamed()) {
					this.makeMad();
					return InteractionResult.SUCCESS;
				}
			}

			return super.mobInteract(player, interactionHand);
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	public boolean canMate(Animal animal) {
		if (animal == this) {
			return false;
		} else {
			return !(animal instanceof Donkey) && !(animal instanceof Horse) ? false : this.canParent() && ((AbstractHorse)animal).canParent();
		}
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		if (ageableMob instanceof Donkey) {
			Mule mule = EntityType.MULE.create(serverLevel, EntitySpawnReason.BREEDING);
			if (mule != null) {
				this.setOffspringAttributes(ageableMob, mule);
			}

			return mule;
		} else {
			Horse horse = (Horse)ageableMob;
			Horse horse2 = EntityType.HORSE.create(serverLevel, EntitySpawnReason.BREEDING);
			if (horse2 != null) {
				int i = this.random.nextInt(9);
				Variant variant;
				if (i < 4) {
					variant = this.getVariant();
				} else if (i < 8) {
					variant = horse.getVariant();
				} else {
					variant = Util.getRandom(Variant.values(), this.random);
				}

				int j = this.random.nextInt(5);
				Markings markings;
				if (j < 2) {
					markings = this.getMarkings();
				} else if (j < 4) {
					markings = horse.getMarkings();
				} else {
					markings = Util.getRandom(Markings.values(), this.random);
				}

				horse2.setVariantAndMarkings(variant, markings);
				this.setOffspringAttributes(ageableMob, horse2);
			}

			return horse2;
		}
	}

	@Override
	public boolean canUseSlot(EquipmentSlot equipmentSlot) {
		return true;
	}

	@Override
	protected void hurtArmor(DamageSource damageSource, float f) {
		this.doHurtEquipment(damageSource, f, new EquipmentSlot[]{EquipmentSlot.BODY});
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		RandomSource randomSource = serverLevelAccessor.getRandom();
		Variant variant;
		if (spawnGroupData instanceof Horse.HorseGroupData) {
			variant = ((Horse.HorseGroupData)spawnGroupData).variant;
		} else {
			variant = Util.getRandom(Variant.values(), randomSource);
			spawnGroupData = new Horse.HorseGroupData(variant);
		}

		this.setVariantAndMarkings(variant, Util.getRandom(Markings.values(), randomSource));
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
	}

	public static class HorseGroupData extends AgeableMob.AgeableMobGroupData {
		public final Variant variant;

		public HorseGroupData(Variant variant) {
			super(true);
			this.variant = variant;
		}
	}
}
