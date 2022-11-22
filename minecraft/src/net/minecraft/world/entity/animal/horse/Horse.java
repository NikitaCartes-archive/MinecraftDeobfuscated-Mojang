package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
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
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;

public class Horse extends AbstractHorse implements VariantHolder<Variant> {
	private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
	private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);

	public Horse(EntityType<? extends Horse> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void randomizeAttributes(RandomSource randomSource) {
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.generateRandomMaxHealth(randomSource));
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.generateRandomSpeed(randomSource));
		this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength(randomSource));
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

	public void setVariant(Variant variant) {
		this.setTypeVariant(variant.getId() & 0xFF | this.getTypeVariant() & -256);
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
					return InteractionResult.sidedSuccess(this.level.isClientSide);
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
			Mule mule = EntityType.MULE.create(serverLevel);
			if (mule != null) {
				this.setOffspringAttributes(ageableMob, mule);
			}

			return mule;
		} else {
			Horse horse = (Horse)ageableMob;
			Horse horse2 = EntityType.HORSE.create(serverLevel);
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
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
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
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public static class HorseGroupData extends AgeableMob.AgeableMobGroupData {
		public final Variant variant;

		public HorseGroupData(Variant variant) {
			super(true);
			this.variant = variant;
		}
	}
}
