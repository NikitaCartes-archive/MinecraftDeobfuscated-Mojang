/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.OptionalInt;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity
extends BlockEntity
implements VibrationListener.VibrationListenerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LISTENER_RADIUS = 8;
    private static final int WARNING_SOUND_RADIUS = 10;
    private static final int WARDEN_SPAWN_ATTEMPTS = 20;
    private static final int WARDEN_SPAWN_RANGE_XZ = 5;
    private static final int WARDEN_SPAWN_RANGE_Y = 6;
    private static final int DARKNESS_RADIUS = 40;
    private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
        int2ObjectOpenHashMap.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
        int2ObjectOpenHashMap.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
        int2ObjectOpenHashMap.put(4, SoundEvents.WARDEN_LISTENING_ANGRY);
    });
    private static final int SHRIEKING_TICKS = 90;
    private int warningLevel;
    private VibrationListener listener;

    public SculkShriekerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SCULK_SHRIEKER, blockPos, blockState);
        this.listener = new VibrationListener(new BlockPositionSource(this.worldPosition), 8, this);
    }

    public VibrationListener getListener() {
        return this.listener;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains("warning_level", 99)) {
            this.warningLevel = compoundTag.getInt("warning_level");
        }
        if (compoundTag.contains("listener", 10)) {
            VibrationListener.codec(this).parse(new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent(vibrationListener -> {
                this.listener = vibrationListener;
            });
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putInt("warning_level", this.warningLevel);
        VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.listener).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("listener", (Tag)tag));
    }

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.SHRIEKER_CAN_LISTEN;
    }

    @Override
    public boolean shouldListen(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
        return this.getBlockState().getValue(SculkShriekerBlock.SHRIEKING) == false && SculkShriekerBlockEntity.tryGetPlayer(context.sourceEntity()) != null;
    }

    @Nullable
    public static ServerPlayer tryGetPlayer(@Nullable Entity entity) {
        ItemEntity itemEntity;
        ServerPlayer serverPlayer2;
        Projectile projectile;
        Entity entity2;
        LivingEntity livingEntity;
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            return serverPlayer;
        }
        if (entity != null && (livingEntity = entity.getControllingPassenger()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            return serverPlayer;
        }
        if (entity instanceof Projectile && (entity2 = (projectile = (Projectile)entity).getOwner()) instanceof ServerPlayer) {
            serverPlayer2 = (ServerPlayer)entity2;
            return serverPlayer2;
        }
        if (entity instanceof ItemEntity && (entity2 = (itemEntity = (ItemEntity)entity).getOwner()) instanceof ServerPlayer) {
            serverPlayer2 = (ServerPlayer)entity2;
            return serverPlayer2;
        }
        return null;
    }

    @Override
    public void onSignalReceive(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity2, float f) {
        this.tryShriek(serverLevel, SculkShriekerBlockEntity.tryGetPlayer(entity2 != null ? entity2 : entity));
    }

    public void tryShriek(ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer) {
        if (serverPlayer == null) {
            return;
        }
        BlockState blockState = this.getBlockState();
        if (blockState.getValue(SculkShriekerBlock.SHRIEKING).booleanValue()) {
            return;
        }
        this.warningLevel = 0;
        if (this.canRespond(serverLevel) && !this.tryToWarn(serverLevel, serverPlayer)) {
            return;
        }
        this.shriek(serverLevel, serverPlayer);
    }

    private boolean tryToWarn(ServerLevel serverLevel, ServerPlayer serverPlayer) {
        OptionalInt optionalInt = WardenSpawnTracker.tryWarn(serverLevel, this.getBlockPos(), serverPlayer);
        optionalInt.ifPresent(i -> {
            this.warningLevel = i;
        });
        return optionalInt.isPresent();
    }

    private void shriek(ServerLevel serverLevel, @Nullable Entity entity) {
        BlockPos blockPos = this.getBlockPos();
        BlockState blockState = this.getBlockState();
        serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(SculkShriekerBlock.SHRIEKING, true), 2);
        serverLevel.scheduleTick(blockPos, blockState.getBlock(), 90);
        serverLevel.levelEvent(3007, blockPos, 0);
        serverLevel.gameEvent(GameEvent.SHRIEK, blockPos, GameEvent.Context.of(entity));
    }

    private boolean canRespond(ServerLevel serverLevel) {
        return this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON) != false && serverLevel.getDifficulty() != Difficulty.PEACEFUL && serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING);
    }

    public void tryRespond(ServerLevel serverLevel) {
        if (this.canRespond(serverLevel) && this.warningLevel > 0) {
            if (!this.trySummonWarden(serverLevel)) {
                this.playWardenReplySound();
            }
            Warden.applyDarknessAround(serverLevel, Vec3.atCenterOf(this.getBlockPos()), null, 40);
        }
    }

    private void playWardenReplySound() {
        SoundEvent soundEvent = (SoundEvent)SOUND_BY_LEVEL.get(this.warningLevel);
        if (soundEvent != null) {
            BlockPos blockPos = this.getBlockPos();
            int i = blockPos.getX() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
            int j = blockPos.getY() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
            int k = blockPos.getZ() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
            this.level.playSound(null, i, j, k, soundEvent, SoundSource.HOSTILE, 5.0f, 1.0f);
        }
    }

    private boolean trySummonWarden(ServerLevel serverLevel) {
        if (this.warningLevel < 4) {
            return false;
        }
        return SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, serverLevel, this.getBlockPos(), 20, 5, 6, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER).isPresent();
    }

    @Override
    public void onSignalSchedule() {
        this.setChanged();
    }
}

