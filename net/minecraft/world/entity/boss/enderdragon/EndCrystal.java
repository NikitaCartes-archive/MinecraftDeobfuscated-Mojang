/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.dimension.end.TheEndDimension;
import org.jetbrains.annotations.Nullable;

public class EndCrystal
extends Entity {
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
    public int time;

    public EndCrystal(EntityType<? extends EndCrystal> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
    }

    public EndCrystal(Level level, double d, double e, double f) {
        this((EntityType<? extends EndCrystal>)EntityType.END_CRYSTAL, level);
        this.setPos(d, e, f);
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_BEAM_TARGET, Optional.empty());
        this.getEntityData().define(DATA_SHOW_BOTTOM, true);
    }

    @Override
    public void tick() {
        ++this.time;
        if (!this.level.isClientSide) {
            BlockPos blockPos = new BlockPos(this);
            if (this.level.dimension instanceof TheEndDimension && this.level.getBlockState(blockPos).isAir()) {
                this.level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.getBeamTarget() != null) {
            compoundTag.put("BeamTarget", NbtUtils.writeBlockPos(this.getBeamTarget()));
        }
        compoundTag.putBoolean("ShowBottom", this.showsBottom());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("BeamTarget", 10)) {
            this.setBeamTarget(NbtUtils.readBlockPos(compoundTag.getCompound("BeamTarget")));
        }
        if (compoundTag.contains("ShowBottom", 1)) {
            this.setShowBottom(compoundTag.getBoolean("ShowBottom"));
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (damageSource.getEntity() instanceof EnderDragon) {
            return false;
        }
        if (!this.removed && !this.level.isClientSide) {
            this.remove();
            if (!damageSource.isExplosion()) {
                this.level.explode(null, this.getX(), this.getY(), this.getZ(), 6.0f, Explosion.BlockInteraction.DESTROY);
            }
            this.onDestroyedBy(damageSource);
        }
        return true;
    }

    @Override
    public void kill() {
        this.onDestroyedBy(DamageSource.GENERIC);
        super.kill();
    }

    private void onDestroyedBy(DamageSource damageSource) {
        TheEndDimension theEndDimension;
        EndDragonFight endDragonFight;
        if (this.level.dimension instanceof TheEndDimension && (endDragonFight = (theEndDimension = (TheEndDimension)this.level.dimension).getDragonFight()) != null) {
            endDragonFight.onCrystalDestroyed(this, damageSource);
        }
    }

    public void setBeamTarget(@Nullable BlockPos blockPos) {
        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(blockPos));
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return this.getEntityData().get(DATA_BEAM_TARGET).orElse(null);
    }

    public void setShowBottom(boolean bl) {
        this.getEntityData().set(DATA_SHOW_BOTTOM, bl);
    }

    public boolean showsBottom() {
        return this.getEntityData().get(DATA_SHOW_BOTTOM);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean shouldRenderAtSqrDistance(double d) {
        return super.shouldRenderAtSqrDistance(d) || this.getBeamTarget() != null;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

