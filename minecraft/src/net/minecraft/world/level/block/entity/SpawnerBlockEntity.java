package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlockEntity extends BlockEntity implements TickableBlockEntity {
	private final BaseSpawner spawner = new BaseSpawner() {
		@Override
		public void broadcastEvent(int i) {
			SpawnerBlockEntity.this.level.blockEvent(SpawnerBlockEntity.this.worldPosition, Blocks.SPAWNER, i, 0);
		}

		@Override
		public Level getLevel() {
			return SpawnerBlockEntity.this.level;
		}

		@Override
		public BlockPos getPos() {
			return SpawnerBlockEntity.this.worldPosition;
		}

		@Override
		public void setNextSpawnData(SpawnData spawnData) {
			super.setNextSpawnData(spawnData);
			if (this.getLevel() != null) {
				BlockState blockState = this.getLevel().getBlockState(this.getPos());
				this.getLevel().sendBlockUpdated(SpawnerBlockEntity.this.worldPosition, blockState, blockState, 4);
			}
		}
	};

	public SpawnerBlockEntity() {
		super(BlockEntityType.MOB_SPAWNER);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.spawner.load(compoundTag);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		this.spawner.save(compoundTag);
		return compoundTag;
	}

	@Override
	public void tick() {
		this.spawner.tick();
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
		return this.spawner.onEventTriggered(i) ? true : super.triggerEvent(i, j);
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	public BaseSpawner getSpawner() {
		return this.spawner;
	}
}
