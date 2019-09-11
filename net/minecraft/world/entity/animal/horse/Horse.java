/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.Nullable;

public class Horse
extends AbstractHorse {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);
    private static final String[] VARIANT_TEXTURES = new String[]{"textures/entity/horse/horse_white.png", "textures/entity/horse/horse_creamy.png", "textures/entity/horse/horse_chestnut.png", "textures/entity/horse/horse_brown.png", "textures/entity/horse/horse_black.png", "textures/entity/horse/horse_gray.png", "textures/entity/horse/horse_darkbrown.png"};
    private static final String[] VARIANT_HASHES = new String[]{"hwh", "hcr", "hch", "hbr", "hbl", "hgr", "hdb"};
    private static final String[] MARKING_TEXTURES = new String[]{null, "textures/entity/horse/horse_markings_white.png", "textures/entity/horse/horse_markings_whitefield.png", "textures/entity/horse/horse_markings_whitedots.png", "textures/entity/horse/horse_markings_blackdots.png"};
    private static final String[] MARKING_HASHES = new String[]{"", "wo_", "wmo", "wdo", "bdo"};
    private String layerTextureHashName;
    private final String[] layerTextureLayers = new String[2];

    public Horse(EntityType<? extends Horse> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", this.getVariant());
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
        this.setVariant(compoundTag.getInt("Variant"));
        if (compoundTag.contains("ArmorItem", 10) && !(itemStack = ItemStack.of(compoundTag.getCompound("ArmorItem"))).isEmpty() && this.isArmor(itemStack)) {
            this.inventory.setItem(1, itemStack);
        }
        this.updateEquipment();
    }

    public void setVariant(int i) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, i);
        this.clearLayeredTextureInfo();
    }

    public int getVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    private void clearLayeredTextureInfo() {
        this.layerTextureHashName = null;
    }

    @Environment(value=EnvType.CLIENT)
    private void rebuildLayeredTextureInfo() {
        int i = this.getVariant();
        int j = (i & 0xFF) % 7;
        int k = ((i & 0xFF00) >> 8) % 5;
        this.layerTextureLayers[0] = VARIANT_TEXTURES[j];
        this.layerTextureLayers[1] = MARKING_TEXTURES[k];
        this.layerTextureHashName = "horse/" + VARIANT_HASHES[j] + MARKING_HASHES[k];
    }

    @Environment(value=EnvType.CLIENT)
    public String getLayeredTextureHashName() {
        if (this.layerTextureHashName == null) {
            this.rebuildLayeredTextureInfo();
        }
        return this.layerTextureHashName;
    }

    @Environment(value=EnvType.CLIENT)
    public String[] getLayeredTextureLayers() {
        if (this.layerTextureHashName == null) {
            this.rebuildLayeredTextureInfo();
        }
        return this.layerTextureLayers;
    }

    @Override
    protected void updateEquipment() {
        super.updateEquipment();
        this.setArmorEquipment(this.inventory.getItem(1));
        this.setDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    private void setArmorEquipment(ItemStack itemStack) {
        this.setArmor(itemStack);
        if (!this.level.isClientSide) {
            int i;
            this.getAttribute(SharedMonsterAttributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
            if (this.isArmor(itemStack) && (i = ((HorseArmorItem)itemStack.getItem()).getProtection()) != 0) {
                this.getAttribute(SharedMonsterAttributes.ARMOR).addModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)i, AttributeModifier.Operation.ADDITION).setSerialize(false));
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
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.generateRandomMaxHealth());
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.generateRandomSpeed());
        this.getAttribute(JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide && this.entityData.isDirty()) {
            this.entityData.clearDirty();
            this.clearLayeredTextureInfo();
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
    public boolean mobInteract(Player player, InteractionHand interactionHand) {
        boolean bl;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean bl2 = bl = !itemStack.isEmpty();
        if (bl && itemStack.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(player, interactionHand);
        }
        if (!this.isBaby()) {
            if (this.isTamed() && player.isSecondaryUseActive()) {
                this.openInventory(player);
                return true;
            }
            if (this.isVehicle()) {
                return super.mobInteract(player, interactionHand);
            }
        }
        if (bl) {
            boolean bl22;
            if (this.handleEating(player, itemStack)) {
                if (!player.abilities.instabuild) {
                    itemStack.shrink(1);
                }
                return true;
            }
            if (itemStack.interactEnemy(player, this, interactionHand)) {
                return true;
            }
            if (!this.isTamed()) {
                this.makeMad();
                return true;
            }
            boolean bl3 = bl22 = !this.isBaby() && !this.isSaddled() && itemStack.getItem() == Items.SADDLE;
            if (this.isArmor(itemStack) || bl22) {
                this.openInventory(player);
                return true;
            }
        }
        if (this.isBaby()) {
            return super.mobInteract(player, interactionHand);
        }
        this.doPlayerRide(player);
        return true;
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
    public AgableMob getBreedOffspring(AgableMob agableMob) {
        AbstractHorse abstractHorse;
        if (agableMob instanceof Donkey) {
            abstractHorse = EntityType.MULE.create(this.level);
        } else {
            Horse horse = (Horse)agableMob;
            abstractHorse = EntityType.HORSE.create(this.level);
            int i = this.random.nextInt(9);
            int j = i < 4 ? this.getVariant() & 0xFF : (i < 8 ? horse.getVariant() & 0xFF : this.random.nextInt(7));
            int k = this.random.nextInt(5);
            j = k < 2 ? (j |= this.getVariant() & 0xFF00) : (k < 4 ? (j |= horse.getVariant() & 0xFF00) : (j |= this.random.nextInt(5) << 8 & 0xFF00));
            ((Horse)abstractHorse).setVariant(j);
        }
        this.setOffspringAttributes(agableMob, abstractHorse);
        return abstractHorse;
    }

    @Override
    public boolean wearsArmor() {
        return true;
    }

    @Override
    public boolean isArmor(ItemStack itemStack) {
        return itemStack.getItem() instanceof HorseArmorItem;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(LevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        int i;
        if (spawnGroupData instanceof HorseGroupData) {
            i = ((HorseGroupData)spawnGroupData).variant;
        } else {
            i = this.random.nextInt(7);
            spawnGroupData = new HorseGroupData(i);
        }
        this.setVariant(i | this.random.nextInt(5) << 8);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public static class HorseGroupData
    extends AgableMob.AgableMobGroupData {
        public final int variant;

        public HorseGroupData(int i) {
            this.variant = i;
        }
    }
}

