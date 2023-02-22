/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity
extends BlockEntity
implements GameEventListener {
    private final BlockPositionSource blockPosSource;
    private final SculkSpreader sculkSpreader;

    public SculkCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SCULK_CATALYST, blockPos, blockState);
        this.blockPosSource = new BlockPositionSource(this.worldPosition);
        this.sculkSpreader = SculkSpreader.createLevelSpreader();
    }

    @Override
    public PositionSource getListenerSource() {
        return this.blockPosSource;
    }

    @Override
    public int getListenerRadius() {
        return 8;
    }

    @Override
    public GameEventListener.DeliveryMode getDeliveryMode() {
        return GameEventListener.DeliveryMode.BY_DISTANCE;
    }

    @Override
    public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
        Entity entity;
        if (gameEvent == GameEvent.ENTITY_DIE && (entity = context.sourceEntity()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (!livingEntity.wasExperienceConsumed()) {
                int i = livingEntity.getExperienceReward();
                if (livingEntity.shouldDropExperience() && i > 0) {
                    this.sculkSpreader.addCursors(BlockPos.containing(vec3.relative(Direction.UP, 0.5)), i);
                    this.tryAwardItSpreadsAdvancement(livingEntity);
                }
                livingEntity.skipDropExperience();
                SculkCatalystBlock.bloom(serverLevel, this.worldPosition, this.getBlockState(), serverLevel.getRandom());
            }
            return true;
        }
        return false;
    }

    private void tryAwardItSpreadsAdvancement(LivingEntity livingEntity) {
        LivingEntity livingEntity2 = livingEntity.getLastHurtByMob();
        if (livingEntity2 instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity2;
            DamageSource damageSource = livingEntity.getLastDamageSource() == null ? this.level.damageSources().playerAttack(serverPlayer) : livingEntity.getLastDamageSource();
            CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverPlayer, livingEntity, damageSource);
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SculkCatalystBlockEntity sculkCatalystBlockEntity) {
        sculkCatalystBlockEntity.sculkSpreader.updateCursors(level, blockPos, level.getRandom(), true);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.sculkSpreader.load(compoundTag);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        this.sculkSpreader.save(compoundTag);
        super.saveAdditional(compoundTag);
    }

    @VisibleForTesting
    public SculkSpreader getSculkSpreader() {
        return this.sculkSpreader;
    }

    private static /* synthetic */ Integer method_41518(SculkSpreader.ChargeCursor chargeCursor) {
        return 1;
    }
}

