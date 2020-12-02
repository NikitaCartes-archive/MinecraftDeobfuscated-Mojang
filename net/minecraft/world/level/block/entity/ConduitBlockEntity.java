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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ConduitBlockEntity
extends BlockEntity {
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

    public ConduitBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CONDUIT, blockPos, blockState);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
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

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, ConduitBlockEntity conduitBlockEntity) {
        ++conduitBlockEntity.tickCount;
        long l = level.getGameTime();
        List<BlockPos> list = conduitBlockEntity.effectBlocks;
        if (l % 40L == 0L) {
            conduitBlockEntity.isActive = ConduitBlockEntity.updateShape(level, blockPos, list);
            ConduitBlockEntity.updateHunting(conduitBlockEntity, list);
        }
        ConduitBlockEntity.updateClientTarget(level, blockPos, conduitBlockEntity);
        ConduitBlockEntity.animationTick(level, blockPos, list, conduitBlockEntity.destroyTarget, conduitBlockEntity.tickCount);
        if (conduitBlockEntity.isActive()) {
            conduitBlockEntity.activeRotation += 1.0f;
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, ConduitBlockEntity conduitBlockEntity) {
        ++conduitBlockEntity.tickCount;
        long l = level.getGameTime();
        List<BlockPos> list = conduitBlockEntity.effectBlocks;
        if (l % 40L == 0L) {
            boolean bl = ConduitBlockEntity.updateShape(level, blockPos, list);
            if (bl != conduitBlockEntity.isActive) {
                SoundEvent soundEvent = bl ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE;
                level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            conduitBlockEntity.isActive = bl;
            ConduitBlockEntity.updateHunting(conduitBlockEntity, list);
            if (bl) {
                ConduitBlockEntity.applyEffects(level, blockPos, list);
                ConduitBlockEntity.updateDestroyTarget(level, blockPos, blockState, list, conduitBlockEntity);
            }
        }
        if (conduitBlockEntity.isActive()) {
            if (l % 80L == 0L) {
                level.playSound(null, blockPos, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            if (l > conduitBlockEntity.nextAmbientSoundActivation) {
                conduitBlockEntity.nextAmbientSoundActivation = l + 60L + (long)level.getRandom().nextInt(40);
                level.playSound(null, blockPos, SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    private static void updateHunting(ConduitBlockEntity conduitBlockEntity, List<BlockPos> list) {
        conduitBlockEntity.setHunting(list.size() >= 42);
    }

    private static boolean updateShape(Level level, BlockPos blockPos, List<BlockPos> list) {
        int k;
        int j;
        int i;
        list.clear();
        for (i = -1; i <= 1; ++i) {
            for (j = -1; j <= 1; ++j) {
                for (k = -1; k <= 1; ++k) {
                    BlockPos blockPos2 = blockPos.offset(i, j, k);
                    if (level.isWaterAt(blockPos2)) continue;
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
                    BlockPos blockPos3 = blockPos.offset(i, j, k);
                    BlockState blockState = level.getBlockState(blockPos3);
                    for (Block block : VALID_BLOCKS) {
                        if (!blockState.is(block)) continue;
                        list.add(blockPos3);
                    }
                }
            }
        }
        return list.size() >= 16;
    }

    private static void applyEffects(Level level, BlockPos blockPos, List<BlockPos> list) {
        int m;
        int l;
        int i = list.size();
        int j = i / 7 * 16;
        int k = blockPos.getX();
        AABB aABB = new AABB(k, l = blockPos.getY(), m = blockPos.getZ(), k + 1, l + 1, m + 1).inflate(j).expandTowards(0.0, level.getHeight(), 0.0);
        List<Player> list2 = level.getEntitiesOfClass(Player.class, aABB);
        if (list2.isEmpty()) {
            return;
        }
        for (Player player : list2) {
            if (!blockPos.closerThan(player.blockPosition(), (double)j) || !player.isInWaterOrRain()) continue;
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
        }
    }

    private static void updateDestroyTarget(Level level, BlockPos blockPos, BlockState blockState, List<BlockPos> list, ConduitBlockEntity conduitBlockEntity) {
        LivingEntity livingEntity2 = conduitBlockEntity.destroyTarget;
        int i = list.size();
        if (i < 42) {
            conduitBlockEntity.destroyTarget = null;
        } else if (conduitBlockEntity.destroyTarget == null && conduitBlockEntity.destroyTargetUUID != null) {
            conduitBlockEntity.destroyTarget = ConduitBlockEntity.findDestroyTarget(level, blockPos, conduitBlockEntity.destroyTargetUUID);
            conduitBlockEntity.destroyTargetUUID = null;
        } else if (conduitBlockEntity.destroyTarget == null) {
            List<LivingEntity> list2 = level.getEntitiesOfClass(LivingEntity.class, ConduitBlockEntity.getDestroyRangeAABB(blockPos), livingEntity -> livingEntity instanceof Enemy && livingEntity.isInWaterOrRain());
            if (!list2.isEmpty()) {
                conduitBlockEntity.destroyTarget = list2.get(level.random.nextInt(list2.size()));
            }
        } else if (!conduitBlockEntity.destroyTarget.isAlive() || !blockPos.closerThan(conduitBlockEntity.destroyTarget.blockPosition(), 8.0)) {
            conduitBlockEntity.destroyTarget = null;
        }
        if (conduitBlockEntity.destroyTarget != null) {
            level.playSound(null, conduitBlockEntity.destroyTarget.getX(), conduitBlockEntity.destroyTarget.getY(), conduitBlockEntity.destroyTarget.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0f, 1.0f);
            conduitBlockEntity.destroyTarget.hurt(DamageSource.MAGIC, 4.0f);
        }
        if (livingEntity2 != conduitBlockEntity.destroyTarget) {
            level.sendBlockUpdated(blockPos, blockState, blockState, 2);
        }
    }

    private static void updateClientTarget(Level level, BlockPos blockPos, ConduitBlockEntity conduitBlockEntity) {
        if (conduitBlockEntity.destroyTargetUUID == null) {
            conduitBlockEntity.destroyTarget = null;
        } else if (conduitBlockEntity.destroyTarget == null || !conduitBlockEntity.destroyTarget.getUUID().equals(conduitBlockEntity.destroyTargetUUID)) {
            conduitBlockEntity.destroyTarget = ConduitBlockEntity.findDestroyTarget(level, blockPos, conduitBlockEntity.destroyTargetUUID);
            if (conduitBlockEntity.destroyTarget == null) {
                conduitBlockEntity.destroyTargetUUID = null;
            }
        }
    }

    private static AABB getDestroyRangeAABB(BlockPos blockPos) {
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        return new AABB(i, j, k, i + 1, j + 1, k + 1).inflate(8.0);
    }

    @Nullable
    private static LivingEntity findDestroyTarget(Level level, BlockPos blockPos, UUID uUID) {
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, ConduitBlockEntity.getDestroyRangeAABB(blockPos), livingEntity -> livingEntity.getUUID().equals(uUID));
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    private static void animationTick(Level level, BlockPos blockPos, List<BlockPos> list, @Nullable Entity entity, int i) {
        float f;
        Random random = level.random;
        double d = Mth.sin((float)(i + 35) * 0.1f) / 2.0f + 0.5f;
        d = (d * d + d) * (double)0.3f;
        Vec3 vec3 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.5 + d, (double)blockPos.getZ() + 0.5);
        for (BlockPos blockPos2 : list) {
            if (random.nextInt(50) != 0) continue;
            BlockPos blockPos3 = blockPos2.subtract(blockPos);
            f = -0.5f + random.nextFloat() + (float)blockPos3.getX();
            float g = -2.0f + random.nextFloat() + (float)blockPos3.getY();
            float h = -0.5f + random.nextFloat() + (float)blockPos3.getZ();
            level.addParticle(ParticleTypes.NAUTILUS, vec3.x, vec3.y, vec3.z, f, g, h);
        }
        if (entity != null) {
            Vec3 vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            float j = (-0.5f + random.nextFloat()) * (3.0f + entity.getBbWidth());
            float k = -1.0f + random.nextFloat() * entity.getBbHeight();
            f = (-0.5f + random.nextFloat()) * (3.0f + entity.getBbWidth());
            Vec3 vec33 = new Vec3(j, k, f);
            level.addParticle(ParticleTypes.NAUTILUS, vec32.x, vec32.y, vec32.z, vec33.x, vec33.y, vec33.z);
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isHunting() {
        return this.isHunting;
    }

    private void setHunting(boolean bl) {
        this.isHunting = bl;
    }

    @Environment(value=EnvType.CLIENT)
    public float getActiveRotation(float f) {
        return (this.activeRotation + f) * -0.0375f;
    }
}

