/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class ThrownEnderpearl
extends ThrowableItemProjectile {
    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownEnderpearl(Level level, LivingEntity livingEntity) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.ENDER_PEARL, livingEntity, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        entityHitResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        for (int i = 0; i < 32; ++i) {
            this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
        }
        if (!this.level.isClientSide && !this.isRemoved()) {
            Entity entity = this.getOwner();
            if (entity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)entity;
                if (serverPlayer.connection.isAcceptingMessages() && serverPlayer.level == this.level && !serverPlayer.isSleeping()) {
                    Endermite endermite;
                    if (this.random.nextFloat() < 0.05f && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && (endermite = EntityType.ENDERMITE.create(this.level)) != null) {
                        endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                        this.level.addFreshEntity(endermite);
                    }
                    if (entity.isPassenger()) {
                        serverPlayer.dismountTo(this.getX(), this.getY(), this.getZ());
                    } else {
                        entity.teleportTo(this.getX(), this.getY(), this.getZ());
                    }
                    entity.resetFallDistance();
                    entity.hurt(this.damageSources().fall(), 5.0f);
                }
            } else if (entity != null) {
                entity.teleportTo(this.getX(), this.getY(), this.getZ());
                entity.resetFallDistance();
            }
            this.discard();
        }
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();
        if (entity instanceof Player && !entity.isAlive()) {
            this.discard();
        } else {
            super.tick();
        }
    }

    @Override
    @Nullable
    public Entity changeDimension(ServerLevel serverLevel) {
        Entity entity = this.getOwner();
        if (entity != null && entity.level.dimension() != serverLevel.dimension()) {
            this.setOwner(null);
        }
        return super.changeDimension(serverLevel);
    }
}

