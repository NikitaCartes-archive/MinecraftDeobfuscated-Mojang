/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpawnerBlockEntity
extends BlockEntity {
    private final BaseSpawner spawner = new BaseSpawner(){

        @Override
        public void broadcastEvent(Level level, BlockPos blockPos, int i) {
            level.blockEvent(blockPos, Blocks.SPAWNER, i, 0);
        }

        @Override
        public void setNextSpawnData(@Nullable Level level, BlockPos blockPos, SpawnData spawnData) {
            super.setNextSpawnData(level, blockPos, spawnData);
            if (level != null) {
                BlockState blockState = level.getBlockState(blockPos);
                level.sendBlockUpdated(blockPos, blockState, blockState, 4);
            }
        }
    };

    public SpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.MOB_SPAWNER, blockPos, blockState);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.spawner.load(this.level, this.worldPosition, compoundTag);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        this.spawner.save(compoundTag);
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, SpawnerBlockEntity spawnerBlockEntity) {
        spawnerBlockEntity.spawner.clientTick(level, blockPos);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SpawnerBlockEntity spawnerBlockEntity) {
        spawnerBlockEntity.spawner.serverTick((ServerLevel)level, blockPos);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = this.saveWithoutMetadata();
        compoundTag.remove("SpawnPotentials");
        return compoundTag;
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (this.spawner.onEventTriggered(this.level, i)) {
            return true;
        }
        return super.triggerEvent(i, j);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public void setEntityId(EntityType<?> entityType, RandomSource randomSource) {
        this.spawner.setEntityId(entityType, this.level, randomSource, this.worldPosition);
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}

