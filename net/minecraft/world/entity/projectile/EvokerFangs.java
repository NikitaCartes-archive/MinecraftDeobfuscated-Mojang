/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EvokerFangs
extends Entity {
    private int warmupDelayTicks;
    private boolean sentSpikeEvent;
    private int lifeTicks = 22;
    private boolean clientSideAttackStarted;
    private LivingEntity owner;
    private UUID ownerUUID;

    public EvokerFangs(EntityType<? extends EvokerFangs> entityType, Level level) {
        super(entityType, level);
    }

    public EvokerFangs(Level level, double d, double e, double f, float g, int i, LivingEntity livingEntity) {
        this((EntityType<? extends EvokerFangs>)EntityType.EVOKER_FANGS, level);
        this.warmupDelayTicks = i;
        this.setOwner(livingEntity);
        this.yRot = g * 57.295776f;
        this.setPos(d, e, f);
    }

    @Override
    protected void defineSynchedData() {
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = livingEntity;
        this.ownerUUID = livingEntity == null ? null : livingEntity.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        Entity entity;
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel && (entity = ((ServerLevel)this.level).getEntity(this.ownerUUID)) instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
        }
        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.warmupDelayTicks = compoundTag.getInt("Warmup");
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Warmup", this.warmupDelayTicks);
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.clientSideAttackStarted) {
                --this.lifeTicks;
                if (this.lifeTicks == 14) {
                    for (int i = 0; i < 12; ++i) {
                        double d = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double e = this.getY() + 0.05 + this.random.nextDouble();
                        double f = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double g = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        double h = 0.3 + this.random.nextDouble() * 0.3;
                        double j = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        this.level.addParticle(ParticleTypes.CRIT, d, e + 1.0, f, g, h, j);
                    }
                }
            }
        } else if (--this.warmupDelayTicks < 0) {
            if (this.warmupDelayTicks == -8) {
                List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2, 0.0, 0.2));
                for (LivingEntity livingEntity : list) {
                    this.dealDamageTo(livingEntity);
                }
            }
            if (!this.sentSpikeEvent) {
                this.level.broadcastEntityEvent(this, (byte)4);
                this.sentSpikeEvent = true;
            }
            if (--this.lifeTicks < 0) {
                this.discard();
            }
        }
    }

    private void dealDamageTo(LivingEntity livingEntity) {
        LivingEntity livingEntity2 = this.getOwner();
        if (!livingEntity.isAlive() || livingEntity.isInvulnerable() || livingEntity == livingEntity2) {
            return;
        }
        if (livingEntity2 == null) {
            livingEntity.hurt(DamageSource.MAGIC, 6.0f);
        } else {
            if (livingEntity2.isAlliedTo(livingEntity)) {
                return;
            }
            livingEntity.hurt(DamageSource.indirectMagic(this, livingEntity2), 6.0f);
        }
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void handleEntityEvent(byte b) {
        super.handleEntityEvent(b);
        if (b == 4) {
            this.clientSideAttackStarted = true;
            if (!this.isSilent()) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0f, this.random.nextFloat() * 0.2f + 0.85f, false);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public float getAnimationProgress(float f) {
        if (!this.clientSideAttackStarted) {
            return 0.0f;
        }
        int i = this.lifeTicks - 2;
        if (i <= 0) {
            return 1.0f;
        }
        return 1.0f - ((float)i - f) / 20.0f;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

