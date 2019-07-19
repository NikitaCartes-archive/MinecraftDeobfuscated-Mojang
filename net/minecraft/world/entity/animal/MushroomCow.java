/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal;

import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

public class MushroomCow
extends Cow {
    private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.STRING);
    private MobEffect effect;
    private int effectDuration;
    private UUID lastLightningBoltUUID;

    public MushroomCow(EntityType<? extends MushroomCow> entityType, Level level) {
        super((EntityType<? extends Cow>)entityType, level);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos.below()).getBlock() == Blocks.MYCELIUM) {
            return 10.0f;
        }
        return levelReader.getBrightness(blockPos) - 0.5f;
    }

    public static boolean checkMushroomSpawnRules(EntityType<MushroomCow> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return levelAccessor.getBlockState(blockPos.below()).getBlock() == Blocks.MYCELIUM && levelAccessor.getRawBrightness(blockPos, 0) > 8;
    }

    @Override
    public void thunderHit(LightningBolt lightningBolt) {
        UUID uUID = lightningBolt.getUUID();
        if (!uUID.equals(this.lastLightningBoltUUID)) {
            this.setMushroomType(this.getMushroomType() == MushroomType.RED ? MushroomType.BROWN : MushroomType.RED);
            this.lastLightningBoltUUID = uUID;
            this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0f, 1.0f);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE, MushroomType.RED.type);
    }

    @Override
    public boolean mobInteract(Player player2, InteractionHand interactionHand) {
        ItemStack itemStack = player2.getItemInHand(interactionHand);
        if (itemStack.getItem() == Items.BOWL && this.getAge() >= 0 && !player2.abilities.instabuild) {
            ItemStack itemStack2;
            itemStack.shrink(1);
            boolean bl = false;
            if (this.effect != null) {
                bl = true;
                itemStack2 = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffect(itemStack2, this.effect, this.effectDuration);
                this.effect = null;
                this.effectDuration = 0;
            } else {
                itemStack2 = new ItemStack(Items.MUSHROOM_STEW);
            }
            if (itemStack.isEmpty()) {
                player2.setItemInHand(interactionHand, itemStack2);
            } else if (!player2.inventory.add(itemStack2)) {
                player2.drop(itemStack2, false);
            }
            SoundEvent soundEvent = bl ? SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY : SoundEvents.MOOSHROOM_MILK;
            this.playSound(soundEvent, 1.0f, 1.0f);
            return true;
        }
        if (itemStack.getItem() == Items.SHEARS && this.getAge() >= 0) {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y + (double)(this.getBbHeight() / 2.0f), this.z, 0.0, 0.0, 0.0);
            if (!this.level.isClientSide) {
                this.remove();
                Cow cow = EntityType.COW.create(this.level);
                cow.moveTo(this.x, this.y, this.z, this.yRot, this.xRot);
                cow.setHealth(this.getHealth());
                cow.yBodyRot = this.yBodyRot;
                if (this.hasCustomName()) {
                    cow.setCustomName(this.getCustomName());
                }
                this.level.addFreshEntity(cow);
                for (int i = 0; i < 5; ++i) {
                    this.level.addFreshEntity(new ItemEntity(this.level, this.x, this.y + (double)this.getBbHeight(), this.z, new ItemStack(this.getMushroomType().blockState.getBlock())));
                }
                itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(interactionHand));
                this.playSound(SoundEvents.MOOSHROOM_SHEAR, 1.0f, 1.0f);
            }
            return true;
        }
        if (this.getMushroomType() == MushroomType.BROWN && itemStack.getItem().is(ItemTags.SMALL_FLOWERS)) {
            if (this.effect != null) {
                for (int j = 0; j < 2; ++j) {
                    this.level.addParticle(ParticleTypes.SMOKE, this.x + (double)(this.random.nextFloat() / 2.0f), this.y + (double)(this.getBbHeight() / 2.0f), this.z + (double)(this.random.nextFloat() / 2.0f), 0.0, this.random.nextFloat() / 5.0f, 0.0);
                }
            } else {
                Pair<MobEffect, Integer> pair = this.getEffectFromItemStack(itemStack);
                if (!player2.abilities.instabuild) {
                    itemStack.shrink(1);
                }
                for (int i = 0; i < 4; ++i) {
                    this.level.addParticle(ParticleTypes.EFFECT, this.x + (double)(this.random.nextFloat() / 2.0f), this.y + (double)(this.getBbHeight() / 2.0f), this.z + (double)(this.random.nextFloat() / 2.0f), 0.0, this.random.nextFloat() / 5.0f, 0.0);
                }
                this.effect = pair.getLeft();
                this.effectDuration = pair.getRight();
                this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0f, 1.0f);
            }
        }
        return super.mobInteract(player2, interactionHand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putString("Type", this.getMushroomType().type);
        if (this.effect != null) {
            compoundTag.putByte("EffectId", (byte)MobEffect.getId(this.effect));
            compoundTag.putInt("EffectDuration", this.effectDuration);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setMushroomType(MushroomType.byType(compoundTag.getString("Type")));
        if (compoundTag.contains("EffectId", 1)) {
            this.effect = MobEffect.byId(compoundTag.getByte("EffectId"));
        }
        if (compoundTag.contains("EffectDuration", 3)) {
            this.effectDuration = compoundTag.getInt("EffectDuration");
        }
    }

    private Pair<MobEffect, Integer> getEffectFromItemStack(ItemStack itemStack) {
        FlowerBlock flowerBlock = (FlowerBlock)((BlockItem)itemStack.getItem()).getBlock();
        return Pair.of(flowerBlock.getSuspiciousStewEffect(), flowerBlock.getEffectDuration());
    }

    private void setMushroomType(MushroomType mushroomType) {
        this.entityData.set(DATA_TYPE, mushroomType.type);
    }

    public MushroomType getMushroomType() {
        return MushroomType.byType(this.entityData.get(MushroomCow.DATA_TYPE));
    }

    @Override
    public MushroomCow getBreedOffspring(AgableMob agableMob) {
        MushroomCow mushroomCow = EntityType.MOOSHROOM.create(this.level);
        mushroomCow.setMushroomType(this.getOffspringType((MushroomCow)agableMob));
        return mushroomCow;
    }

    private MushroomType getOffspringType(MushroomCow mushroomCow) {
        MushroomType mushroomType2;
        MushroomType mushroomType = this.getMushroomType();
        MushroomType mushroomType3 = mushroomType == (mushroomType2 = mushroomCow.getMushroomType()) && this.random.nextInt(1024) == 0 ? (mushroomType == MushroomType.BROWN ? MushroomType.RED : MushroomType.BROWN) : (this.random.nextBoolean() ? mushroomType : mushroomType2);
        return mushroomType3;
    }

    @Override
    public /* synthetic */ Cow getBreedOffspring(AgableMob agableMob) {
        return this.getBreedOffspring(agableMob);
    }

    @Override
    public /* synthetic */ AgableMob getBreedOffspring(AgableMob agableMob) {
        return this.getBreedOffspring(agableMob);
    }

    public static enum MushroomType {
        RED("red", Blocks.RED_MUSHROOM.defaultBlockState()),
        BROWN("brown", Blocks.BROWN_MUSHROOM.defaultBlockState());

        private final String type;
        private final BlockState blockState;

        private MushroomType(String string2, BlockState blockState) {
            this.type = string2;
            this.blockState = blockState;
        }

        @Environment(value=EnvType.CLIENT)
        public BlockState getBlockState() {
            return this.blockState;
        }

        private static MushroomType byType(String string) {
            for (MushroomType mushroomType : MushroomType.values()) {
                if (!mushroomType.type.equals(string)) continue;
                return mushroomType;
            }
            return RED;
        }
    }
}

