/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class ThrownEnderpearl
extends ThrowableItemProjectile {
    private LivingEntity originalOwner;

    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownEnderpearl(Level level, LivingEntity livingEntity) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.ENDER_PEARL, livingEntity, level);
        this.originalOwner = livingEntity;
    }

    @Environment(value=EnvType.CLIENT)
    public ThrownEnderpearl(Level level, double d, double e, double f) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.ENDER_PEARL, d, e, f, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        BlockPos blockPos;
        BlockEntity blockEntity;
        LivingEntity livingEntity = this.getOwner();
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult)hitResult).getEntity();
            if (entity == this.originalOwner) {
                return;
            }
            entity.hurt(DamageSource.thrown(this, livingEntity), 0.0f);
        }
        if (hitResult.getType() == HitResult.Type.BLOCK && (blockEntity = this.level.getBlockEntity(blockPos = ((BlockHitResult)hitResult).getBlockPos())) instanceof TheEndGatewayBlockEntity) {
            TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity;
            if (livingEntity != null) {
                if (livingEntity instanceof ServerPlayer) {
                    CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)livingEntity, this.level.getBlockState(blockPos));
                }
                theEndGatewayBlockEntity.teleportEntity(livingEntity);
                this.remove();
                return;
            }
            theEndGatewayBlockEntity.teleportEntity(this);
            return;
        }
        for (int i = 0; i < 32; ++i) {
            this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
        }
        if (!this.level.isClientSide) {
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                if (serverPlayer.connection.getConnection().isConnected() && serverPlayer.level == this.level && !serverPlayer.isSleeping()) {
                    if (this.random.nextFloat() < 0.05f && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        Endermite endermite = EntityType.ENDERMITE.create(this.level);
                        endermite.setPlayerSpawned(true);
                        endermite.moveTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), livingEntity.yRot, livingEntity.xRot);
                        this.level.addFreshEntity(endermite);
                    }
                    if (livingEntity.isPassenger()) {
                        livingEntity.stopRiding();
                    }
                    livingEntity.teleportTo(this.getX(), this.getY(), this.getZ());
                    livingEntity.fallDistance = 0.0f;
                    livingEntity.hurt(DamageSource.FALL, 5.0f);
                }
            } else if (livingEntity != null) {
                livingEntity.teleportTo(this.getX(), this.getY(), this.getZ());
                livingEntity.fallDistance = 0.0f;
            }
            this.remove();
        }
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.getOwner();
        if (livingEntity != null && livingEntity instanceof Player && !livingEntity.isAlive()) {
            this.remove();
        } else {
            super.tick();
        }
    }

    @Override
    @Nullable
    public Entity changeDimension(DimensionType dimensionType) {
        if (this.owner.dimension != dimensionType) {
            this.owner = null;
        }
        return super.changeDimension(dimensionType);
    }
}

