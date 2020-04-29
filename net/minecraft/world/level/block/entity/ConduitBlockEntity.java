/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ConduitBlockEntity
extends BlockEntity
implements TickableBlockEntity {
    private static final Block[] VALID_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
    public int tickCount;
    private float activeRotation;
    private boolean isActive;
    private boolean isHunting;
    private final List<BlockPos> effectBlocks = Lists.newArrayList();
    @Nullable
    private LivingEntity destroyTarget;
    @Nullable
    private UUID destroyTargetUUID;
    private long nextAmbientSoundActivation;

    public ConduitBlockEntity() {
        this(BlockEntityType.CONDUIT);
    }

    public ConduitBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Override
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.destroyTargetUUID = compoundTag.hasUUID("Target") ? compoundTag.getUUID("Target") : null;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (this.destroyTarget != null) {
            compoundTag.putUUID("Target", this.destroyTarget.getUUID());
        }
        return compoundTag;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 5, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void tick() {
        ++this.tickCount;
        long l = this.level.getGameTime();
        if (l % 40L == 0L) {
            this.setActive(this.updateShape());
            if (!this.level.isClientSide && this.isActive()) {
                this.applyEffects();
                this.updateDestroyTarget();
            }
        }
        if (l % 80L == 0L && this.isActive()) {
            this.playSound(SoundEvents.CONDUIT_AMBIENT);
        }
        if (l > this.nextAmbientSoundActivation && this.isActive()) {
            this.nextAmbientSoundActivation = l + 60L + (long)this.level.getRandom().nextInt(40);
            this.playSound(SoundEvents.CONDUIT_AMBIENT_SHORT);
        }
        if (this.level.isClientSide) {
            this.updateClientTarget();
            this.animationTick();
            if (this.isActive()) {
                this.activeRotation += 1.0f;
            }
        }
    }

    private boolean updateShape() {
        int k;
        int j;
        int i;
        this.effectBlocks.clear();
        for (i = -1; i <= 1; ++i) {
            for (j = -1; j <= 1; ++j) {
                for (k = -1; k <= 1; ++k) {
                    BlockPos blockPos = this.worldPosition.offset(i, j, k);
                    if (this.level.isWaterAt(blockPos)) continue;
                    return false;
                }
            }
        }
        for (i = -2; i <= 2; ++i) {
            for (j = -2; j <= 2; ++j) {
                for (k = -2; k <= 2; ++k) {
                    int l = Math.abs(i);
                    int m = Math.abs(j);
                    int n = Math.abs(k);
                    if (l <= 1 && m <= 1 && n <= 1 || (i != 0 || m != 2 && n != 2) && (j != 0 || l != 2 && n != 2) && (k != 0 || l != 2 && m != 2)) continue;
                    BlockPos blockPos2 = this.worldPosition.offset(i, j, k);
                    BlockState blockState = this.level.getBlockState(blockPos2);
                    for (Block block : VALID_BLOCKS) {
                        if (!blockState.is(block)) continue;
                        this.effectBlocks.add(blockPos2);
                    }
                }
            }
        }
        this.setHunting(this.effectBlocks.size() >= 42);
        return this.effectBlocks.size() >= 16;
    }

    private void applyEffects() {
        int m;
        int l;
        int i = this.effectBlocks.size();
        int j = i / 7 * 16;
        int k = this.worldPosition.getX();
        AABB aABB = new AABB(k, l = this.worldPosition.getY(), m = this.worldPosition.getZ(), k + 1, l + 1, m + 1).inflate(j).expandTowards(0.0, this.level.getMaxBuildHeight(), 0.0);
        List<Player> list = this.level.getEntitiesOfClass(Player.class, aABB);
        if (list.isEmpty()) {
            return;
        }
        for (Player player : list) {
            if (!this.worldPosition.closerThan(player.blockPosition(), (double)j) || !player.isInWaterOrRain()) continue;
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
        }
    }

    private void updateDestroyTarget() {
        LivingEntity livingEntity2 = this.destroyTarget;
        int i = this.effectBlocks.size();
        if (i < 42) {
            this.destroyTarget = null;
        } else if (this.destroyTarget == null && this.destroyTargetUUID != null) {
            this.destroyTarget = this.findDestroyTarget();
            this.destroyTargetUUID = null;
        } else if (this.destroyTarget == null) {
            List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getDestroyRangeAABB(), livingEntity -> livingEntity instanceof Enemy && livingEntity.isInWaterOrRain());
            if (!list.isEmpty()) {
                this.destroyTarget = list.get(this.level.random.nextInt(list.size()));
            }
        } else if (!this.destroyTarget.isAlive() || !this.worldPosition.closerThan(this.destroyTarget.blockPosition(), 8.0)) {
            this.destroyTarget = null;
        }
        if (this.destroyTarget != null) {
            this.level.playSound(null, this.destroyTarget.getX(), this.destroyTarget.getY(), this.destroyTarget.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.destroyTarget.hurt(DamageSource.MAGIC, 4.0f);
        }
        if (livingEntity2 != this.destroyTarget) {
            BlockState blockState = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, 2);
        }
    }

    private void updateClientTarget() {
        if (this.destroyTargetUUID == null) {
            this.destroyTarget = null;
        } else if (this.destroyTarget == null || !this.destroyTarget.getUUID().equals(this.destroyTargetUUID)) {
            this.destroyTarget = this.findDestroyTarget();
            if (this.destroyTarget == null) {
                this.destroyTargetUUID = null;
            }
        }
    }

    private AABB getDestroyRangeAABB() {
        int i = this.worldPosition.getX();
        int j = this.worldPosition.getY();
        int k = this.worldPosition.getZ();
        return new AABB(i, j, k, i + 1, j + 1, k + 1).inflate(8.0);
    }

    @Nullable
    private LivingEntity findDestroyTarget() {
        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getDestroyRangeAABB(), livingEntity -> livingEntity.getUUID().equals(this.destroyTargetUUID));
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    private void animationTick() {
        float g;
        float f;
        Random random = this.level.random;
        double d = Mth.sin((float)(this.tickCount + 35) * 0.1f) / 2.0f + 0.5f;
        d = (d * d + d) * (double)0.3f;
        Vec3 vec3 = new Vec3((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 1.5 + d, (double)this.worldPosition.getZ() + 0.5);
        for (BlockPos blockPos : this.effectBlocks) {
            if (random.nextInt(50) != 0) continue;
            f = -0.5f + random.nextFloat();
            g = -2.0f + random.nextFloat();
            float h = -0.5f + random.nextFloat();
            BlockPos blockPos2 = blockPos.subtract(this.worldPosition);
            Vec3 vec32 = new Vec3(f, g, h).add(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
            this.level.addParticle(ParticleTypes.NAUTILUS, vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
        }
        if (this.destroyTarget != null) {
            Vec3 vec33 = new Vec3(this.destroyTarget.getX(), this.destroyTarget.getEyeY(), this.destroyTarget.getZ());
            float i = (-0.5f + random.nextFloat()) * (3.0f + this.destroyTarget.getBbWidth());
            f = -1.0f + random.nextFloat() * this.destroyTarget.getBbHeight();
            g = (-0.5f + random.nextFloat()) * (3.0f + this.destroyTarget.getBbWidth());
            Vec3 vec34 = new Vec3(i, f, g);
            this.level.addParticle(ParticleTypes.NAUTILUS, vec33.x, vec33.y, vec33.z, vec34.x, vec34.y, vec34.z);
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isHunting() {
        return this.isHunting;
    }

    private void setActive(boolean bl) {
        if (bl != this.isActive) {
            this.playSound(bl ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE);
        }
        this.isActive = bl;
    }

    private void setHunting(boolean bl) {
        this.isHunting = bl;
    }

    @Environment(value=EnvType.CLIENT)
    public float getActiveRotation(float f) {
        return (this.activeRotation + f) * -0.0375f;
    }

    public void playSound(SoundEvent soundEvent) {
        this.level.playSound(null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}

