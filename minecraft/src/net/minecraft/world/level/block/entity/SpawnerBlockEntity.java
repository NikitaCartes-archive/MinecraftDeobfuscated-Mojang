package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlockEntity extends BlockEntity implements Spawner {
	private final BaseSpawner spawner = new BaseSpawner() {
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
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.spawner.load(this.level, this.worldPosition, compoundTag);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
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
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag compoundTag = this.saveCustomOnly(provider);
		compoundTag.remove("SpawnPotentials");
		return compoundTag;
	}

	@Override
	public boolean triggerEvent(int i, int j) {
		return this.spawner.onEventTriggered(this.level, i) ? true : super.triggerEvent(i, j);
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	@Override
	public void setEntityId(EntityType<?> entityType, RandomSource randomSource) {
		this.spawner.setEntityId(entityType, this.level, randomSource, this.worldPosition);
		this.setChanged();
	}

	public BaseSpawner getSpawner() {
		return this.spawner;
	}
}
