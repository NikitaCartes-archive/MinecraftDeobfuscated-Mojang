package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlockEntity extends BlockEntity {
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
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.spawner.load(this.level, this.worldPosition, compoundTag);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		this.spawner.save(this.level, this.worldPosition, compoundTag);
		return compoundTag;
	}

	public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, SpawnerBlockEntity spawnerBlockEntity) {
		spawnerBlockEntity.spawner.clientTick(level, blockPos);
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SpawnerBlockEntity spawnerBlockEntity) {
		spawnerBlockEntity.spawner.serverTick((ServerLevel)level, blockPos);
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 1, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag compoundTag = this.save(new CompoundTag());
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

	public BaseSpawner getSpawner() {
		return this.spawner;
	}
}
