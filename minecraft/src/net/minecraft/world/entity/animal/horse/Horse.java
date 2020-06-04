package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;

public class Horse extends AbstractHorse {
	private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
	private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);

	public Horse(EntityType<? extends Horse> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void randomizeAttributes() {
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.generateRandomMaxHealth());
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.generateRandomSpeed());
		this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Variant", this.getTypeVariant());
		if (!this.inventory.getItem(1).isEmpty()) {
			compoundTag.put("ArmorItem", this.inventory.getItem(1).save(new CompoundTag()));
		}
	}

	public ItemStack getArmor() {
		return this.getItemBySlot(EquipmentSlot.CHEST);
	}

	private void setArmor(ItemStack itemStack) {
		this.setItemSlot(EquipmentSlot.CHEST, itemStack);
		this.setDropChance(EquipmentSlot.CHEST, 0.0F);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setTypeVariant(compoundTag.getInt("Variant"));
		if (compoundTag.contains("ArmorItem", 10)) {
			ItemStack itemStack = ItemStack.of(compoundTag.getCompound("ArmorItem"));
			if (!itemStack.isEmpty() && this.isArmor(itemStack)) {
				this.inventory.setItem(1, itemStack);
			}
		}

		this.updateContainerEquipment();
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

	public Markings getMarkings() {
		return Markings.byId((this.getTypeVariant() & 0xFF00) >> 8);
	}

	@Override
	protected void updateContainerEquipment() {
		if (!this.level.isClientSide) {
			super.updateContainerEquipment();
			this.setArmorEquipment(this.inventory.getItem(1));
			this.setDropChance(EquipmentSlot.CHEST, 0.0F);
		}
	}

	private void setArmorEquipment(ItemStack itemStack) {
		this.setArmor(itemStack);
		if (!this.level.isClientSide) {
			this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
			if (this.isArmor(itemStack)) {
				int i = ((HorseArmorItem)itemStack.getItem()).getProtection();
				if (i != 0) {
					this.getAttribute(Attributes.ARMOR)
						.addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)i, AttributeModifier.Operation.ADDITION));
				}
			}
		}
	}

	@Override
	public void containerChanged(Container container) {
		ItemStack itemStack = this.getArmor();
		super.containerChanged(container);
		ItemStack itemStack2 = this.getArmor();
		if (this.tickCount > 20 && this.isArmor(itemStack2) && itemStack != itemStack2) {
			this.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
		}
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
		super.getAmbientSound();
		return SoundEvents.HORSE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		super.getDeathSound();
		return SoundEvents.HORSE_DEATH;
	}

	@Nullable
	@Override
	protected SoundEvent getEatingSound() {
		return SoundEvents.HORSE_EAT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		super.getHurtSound(damageSource);
		return SoundEvents.HORSE_HURT;
	}

	@Override
	protected SoundEvent getAngrySound() {
		super.getAngrySound();
		return SoundEvents.HORSE_ANGRY;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!this.isBaby()) {
			if (this.isTamed() && player.isSecondaryUseActive()) {
				this.openInventory(player);
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}

			if (this.isVehicle()) {
				return super.mobInteract(player, interactionHand);
			}
		}

		if (!itemStack.isEmpty()) {
			if (this.isFood(itemStack)) {
				boolean bl = this.handleEating(player, itemStack);
				if (!player.abilities.instabuild) {
					itemStack.shrink(1);
				}

				return bl ? InteractionResult.sidedSuccess(this.level.isClientSide) : InteractionResult.CONSUME;
			}

			InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
			if (interactionResult.consumesAction()) {
				return interactionResult;
			}

			if (!this.isTamed()) {
				this.makeMad();
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}

			boolean bl2 = !this.isBaby() && !this.isSaddled() && itemStack.getItem() == Items.SADDLE;
			if (this.isArmor(itemStack) || bl2) {
				this.openInventory(player);
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
		}

		if (this.isBaby()) {
			return super.mobInteract(player, interactionHand);
		} else {
			this.doPlayerRide(player);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
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

	@Override
	public AgableMob getBreedOffspring(AgableMob agableMob) {
		AbstractHorse abstractHorse;
		if (agableMob instanceof Donkey) {
			abstractHorse = EntityType.MULE.create(this.level);
		} else {
			Horse horse = (Horse)agableMob;
			abstractHorse = EntityType.HORSE.create(this.level);
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

			((Horse)abstractHorse).setVariantAndMarkings(variant, markings);
		}

		this.setOffspringAttributes(agableMob, abstractHorse);
		return abstractHorse;
	}

	@Override
	public boolean canWearArmor() {
		return true;
	}

	@Override
	public boolean isArmor(ItemStack itemStack) {
		return itemStack.getItem() instanceof HorseArmorItem;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		Variant variant;
		if (spawnGroupData instanceof Horse.HorseGroupData) {
			variant = ((Horse.HorseGroupData)spawnGroupData).variant;
		} else {
			variant = Util.getRandom(Variant.values(), this.random);
			spawnGroupData = new Horse.HorseGroupData(variant);
		}

		this.setVariantAndMarkings(variant, Util.getRandom(Markings.values(), this.random));
		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public static class HorseGroupData extends AgableMob.AgableMobGroupData {
		public final Variant variant;

		public HorseGroupData(Variant variant) {
			this.variant = variant;
		}
	}
}
