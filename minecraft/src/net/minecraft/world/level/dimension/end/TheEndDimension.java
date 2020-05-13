package net.minecraft.world.level.dimension.end;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;

public class TheEndDimension extends Dimension {
	public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
	private final EndDragonFight dragonFight;

	public TheEndDimension(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
		if (level instanceof ServerLevel) {
			ServerLevel serverLevel = (ServerLevel)level;
			LevelData levelData = serverLevel.getLevelData();
			if (levelData instanceof ServerLevelData) {
				CompoundTag compoundTag = ((ServerLevelData)levelData).getDimensionData();
				this.dragonFight = new EndDragonFight(serverLevel, compoundTag.getCompound("DragonFight"));
			} else {
				this.dragonFight = null;
			}
		} else {
			this.dragonFight = null;
		}
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.0F;
	}

	@Override
	public boolean mayRespawn() {
		return false;
	}

	@Override
	public boolean isNaturalDimension() {
		return false;
	}

	@Nullable
	@Override
	public BlockPos getSpawnPosInChunk(long l, ChunkPos chunkPos, boolean bl) {
		Random random = new Random(l);
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX() + random.nextInt(15), 0, chunkPos.getMaxBlockZ() + random.nextInt(15));
		return this.level.getTopBlockState(blockPos).getMaterial().blocksMotion() ? blockPos : null;
	}

	@Override
	public BlockPos getDimensionSpecificSpawn() {
		return END_SPAWN_POINT;
	}

	@Nullable
	@Override
	public BlockPos getValidSpawnPosition(long l, int i, int j, boolean bl) {
		return this.getSpawnPosInChunk(l, new ChunkPos(i >> 4, j >> 4), bl);
	}

	@Override
	public DimensionType getType() {
		return DimensionType.THE_END;
	}

	@Override
	public void saveData(ServerLevelData serverLevelData) {
		CompoundTag compoundTag = new CompoundTag();
		if (this.dragonFight != null) {
			compoundTag.put("DragonFight", this.dragonFight.saveData());
		}

		serverLevelData.setDimensionData(compoundTag);
	}

	@Override
	public void tick() {
		if (this.dragonFight != null) {
			this.dragonFight.tick();
		}
	}

	@Nullable
	public EndDragonFight getDragonFight() {
		return this.dragonFight;
	}
}
