/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class ThrownPotion
extends ThrowableItemProjectile
implements ItemSupplier {
    public static final double SPLASH_RANGE = 4.0;
    private static final double SPLASH_RANGE_SQ = 16.0;
    public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = livingEntity -> livingEntity.isSensitiveToWater() || livingEntity.isOnFire();

    public ThrownPotion(EntityType<? extends ThrownPotion> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownPotion(Level level, LivingEntity livingEntity) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.POTION, livingEntity, level);
    }

    public ThrownPotion(Level level, double d, double e, double f) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.POTION, d, e, f, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected float getGravity() {
        return 0.05f;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level.isClientSide) {
            return;
        }
        ItemStack itemStack = this.getItem();
        Potion potion = PotionUtils.getPotion(itemStack);
        List<MobEffectInstance> list = PotionUtils.getMobEffects(itemStack);
        boolean bl = potion == Potions.WATER && list.isEmpty();
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockPos blockPos2 = blockPos.relative(direction);
        if (bl) {
            this.dowseFire(blockPos2);
            this.dowseFire(blockPos2.relative(direction.getOpposite()));
            for (Direction direction2 : Direction.Plane.HORIZONTAL) {
                this.dowseFire(blockPos2.relative(direction2));
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        boolean bl;
        super.onHit(hitResult);
        if (this.level.isClientSide) {
            return;
        }
        ItemStack itemStack = this.getItem();
        Potion potion = PotionUtils.getPotion(itemStack);
        List<MobEffectInstance> list = PotionUtils.getMobEffects(itemStack);
        boolean bl2 = bl = potion == Potions.WATER && list.isEmpty();
        if (bl) {
            this.applyWater();
        } else if (!list.isEmpty()) {
            if (this.isLingering()) {
                this.makeAreaOfEffectCloud(itemStack, potion);
            } else {
                this.applySplash(list, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)hitResult).getEntity() : null);
            }
        }
        int i = potion.hasInstantEffects() ? 2007 : 2002;
        this.level.levelEvent(i, this.blockPosition(), PotionUtils.getColor(itemStack));
        this.discard();
    }

    private void applyWater() {
        AABB aABB = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, aABB, WATER_SENSITIVE_OR_ON_FIRE);
        for (LivingEntity livingEntity : list) {
            double d = this.distanceToSqr(livingEntity);
            if (!(d < 16.0)) continue;
            if (livingEntity.isSensitiveToWater()) {
                livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 1.0f);
            }
            if (!livingEntity.isOnFire() || !livingEntity.isAlive()) continue;
            livingEntity.extinguishFire();
        }
        List<Axolotl> list2 = this.level.getEntitiesOfClass(Axolotl.class, aABB);
        for (Axolotl axolotl : list2) {
            axolotl.rehydrate();
        }
    }

    private void applySplash(List<MobEffectInstance> list, @Nullable Entity entity) {
        AABB aABB = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list2 = this.level.getEntitiesOfClass(LivingEntity.class, aABB);
        if (!list2.isEmpty()) {
            Entity entity2 = this.getEffectSource();
            for (LivingEntity livingEntity : list2) {
                double d;
                if (!livingEntity.isAffectedByPotions() || !((d = this.distanceToSqr(livingEntity)) < 16.0)) continue;
                double e = livingEntity == entity ? 1.0 : 1.0 - Math.sqrt(d) / 4.0;
                for (MobEffectInstance mobEffectInstance : list) {
                    MobEffect mobEffect = mobEffectInstance.getEffect();
                    if (mobEffect.isInstantenous()) {
                        mobEffect.applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance.getAmplifier(), e);
                        continue;
                    }
                    int i2 = mobEffectInstance.mapDuration(i -> (int)(e * (double)i + 0.5));
                    MobEffectInstance mobEffectInstance2 = new MobEffectInstance(mobEffect, i2, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible());
                    if (mobEffectInstance2.endsWithin(20)) continue;
                    livingEntity.addEffect(mobEffectInstance2, entity2);
                }
            }
        }
    }

    private void makeAreaOfEffectCloud(ItemStack itemStack, Potion potion) {
        AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            areaEffectCloud.setOwner((LivingEntity)entity);
        }
        areaEffectCloud.setRadius(3.0f);
        areaEffectCloud.setRadiusOnUse(-0.5f);
        areaEffectCloud.setWaitTime(10);
        areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float)areaEffectCloud.getDuration());
        areaEffectCloud.setPotion(potion);
        for (MobEffectInstance mobEffectInstance : PotionUtils.getCustomEffects(itemStack)) {
            areaEffectCloud.addEffect(new MobEffectInstance(mobEffectInstance));
        }
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains("CustomPotionColor", 99)) {
            areaEffectCloud.setFixedColor(compoundTag.getInt("CustomPotionColor"));
        }
        this.level.addFreshEntity(areaEffectCloud);
    }

    private boolean isLingering() {
        return this.getItem().is(Items.LINGERING_POTION);
    }

    private void dowseFire(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.is(BlockTags.FIRE)) {
            this.level.removeBlock(blockPos, false);
        } else if (AbstractCandleBlock.isLit(blockState)) {
            AbstractCandleBlock.extinguish(null, blockState, this.level, blockPos);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            this.level.levelEvent(null, 1009, blockPos, 0);
            CampfireBlock.dowse(this.getOwner(), this.level, blockPos, blockState);
            this.level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(CampfireBlock.LIT, false));
        }
    }
}

