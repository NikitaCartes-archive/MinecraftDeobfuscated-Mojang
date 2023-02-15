/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
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
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.Nullable;

public class Horse
extends AbstractHorse
implements VariantHolder<Variant> {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);

    public Horse(EntityType<? extends Horse> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomSource) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Horse.generateMaxHealth(randomSource::nextInt));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Horse.generateSpeed(randomSource::nextDouble));
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(Horse.generateJumpStrength(randomSource::nextDouble));
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
        this.setDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        ItemStack itemStack;
        super.readAdditionalSaveData(compoundTag);
        this.setTypeVariant(compoundTag.getInt("Variant"));
        if (compoundTag.contains("ArmorItem", 10) && !(itemStack = ItemStack.of(compoundTag.getCompound("ArmorItem"))).isEmpty() && this.isArmor(itemStack)) {
            this.inventory.setItem(1, itemStack);
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

    @Override
    public Variant getVariant() {
        return Variant.byId(this.getTypeVariant() & 0xFF);
    }

    @Override
    public void setVariant(Variant variant) {
        this.setTypeVariant(variant.getId() & 0xFF | this.getTypeVariant() & 0xFFFFFF00);
    }

    public Markings getMarkings() {
        return Markings.byId((this.getTypeVariant() & 0xFF00) >> 8);
    }

    @Override
    protected void updateContainerEquipment() {
        if (this.level.isClientSide) {
            return;
        }
        super.updateContainerEquipment();
        this.setArmorEquipment(this.inventory.getItem(1));
        this.setDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    private void setArmorEquipment(ItemStack itemStack) {
        this.setArmor(itemStack);
        if (!this.level.isClientSide) {
            int i;
            this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
            if (this.isArmor(itemStack) && (i = ((HorseArmorItem)itemStack.getItem()).getProtection()) != 0) {
                this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)i, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    @Override
    public void containerChanged(Container container) {
        ItemStack itemStack = this.getArmor();
        super.containerChanged(container);
        ItemStack itemStack2 = this.getArmor();
        if (this.tickCount > 20 && this.isArmor(itemStack2) && itemStack != itemStack2) {
            this.playSound(SoundEvents.HORSE_ARMOR, 0.5f, 1.0f);
        }
    }

    @Override
    protected void playGallopSound(SoundType soundType) {
        super.playGallopSound(soundType);
        if (this.random.nextInt(10) == 0) {
            this.playSound(SoundEvents.HORSE_BREATHE, soundType.getVolume() * 0.6f, soundType.getPitch());
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

    @Override
    @Nullable
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
        boolean bl;
        boolean bl2 = bl = !this.isBaby() && this.isTamed() && player.isSecondaryUseActive();
        if (this.isVehicle() || bl) {
            return super.mobInteract(player, interactionHand);
        }
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
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        if (animal instanceof Donkey || animal instanceof Horse) {
            return this.canParent() && ((AbstractHorse)animal).canParent();
        }
        return false;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        if (ageableMob instanceof Donkey) {
            Mule mule = EntityType.MULE.create(serverLevel);
            if (mule != null) {
                this.setOffspringAttributes(ageableMob, mule);
            }
            return mule;
        }
        Horse horse = (Horse)ageableMob;
        Horse horse2 = EntityType.HORSE.create(serverLevel);
        if (horse2 != null) {
            int i = this.random.nextInt(9);
            Variant variant = i < 4 ? this.getVariant() : (i < 8 ? horse.getVariant() : Util.getRandom(Variant.values(), this.random));
            int j = this.random.nextInt(5);
            Markings markings = j < 2 ? this.getMarkings() : (j < 4 ? horse.getMarkings() : Util.getRandom(Markings.values(), this.random));
            horse2.setVariantAndMarkings(variant, markings);
            this.setOffspringAttributes(ageableMob, horse2);
        }
        return horse2;
    }

    @Override
    public boolean canWearArmor() {
        return true;
    }

    @Override
    public boolean isArmor(ItemStack itemStack) {
        return itemStack.getItem() instanceof HorseArmorItem;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        Variant variant;
        RandomSource randomSource = serverLevelAccessor.getRandom();
        if (spawnGroupData instanceof HorseGroupData) {
            variant = ((HorseGroupData)spawnGroupData).variant;
        } else {
            variant = Util.getRandom(Variant.values(), randomSource);
            spawnGroupData = new HorseGroupData(variant);
        }
        this.setVariantAndMarkings(variant, Util.getRandom(Markings.values(), randomSource));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static class HorseGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public HorseGroupData(Variant variant) {
            super(true);
            this.variant = variant;
        }
    }
}

