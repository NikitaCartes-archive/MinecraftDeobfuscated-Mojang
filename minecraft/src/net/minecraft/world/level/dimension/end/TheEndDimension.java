package net.minecraft.world.level.dimension.end;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.phys.Vec3;

public class TheEndDimension extends Dimension {
	public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
	private final EndDragonFight dragonFight;

	public TheEndDimension(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
		CompoundTag compoundTag = level.getLevelData().getDimensionData(DimensionType.THE_END);
		this.dragonFight = level instanceof ServerLevel ? new EndDragonFight((ServerLevel)level, compoundTag.getCompound("DragonFight")) : null;
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		TheEndGeneratorSettings theEndGeneratorSettings = ChunkGeneratorType.FLOATING_ISLANDS.createSettings();
		theEndGeneratorSettings.setDefaultBlock(Blocks.END_STONE.defaultBlockState());
		theEndGeneratorSettings.setDefaultFluid(Blocks.AIR.defaultBlockState());
		theEndGeneratorSettings.setSpawnPosition(this.getDimensionSpecificSpawn());
		return ChunkGeneratorType.FLOATING_ISLANDS
			.create(this.level, BiomeSourceType.THE_END.create(BiomeSourceType.THE_END.createSettings(this.level.getSeed())), theEndGeneratorSettings);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.0F;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	@Override
	public float[] getSunriseColor(float f, float g) {
		return null;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return vec3.scale(0.15F);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean hasGround() {
		return false;
	}

	@Override
	public boolean mayRespawn() {
		return false;
	}

	@Override
	public boolean isNaturalDimension() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public float getCloudHeight() {
		return 8.0F;
	}

	@Nullable
	@Override
	public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
		Random random = new Random(this.level.getSeed());
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX() + random.nextInt(15), 0, chunkPos.getMaxBlockZ() + random.nextInt(15));
		return this.level.getTopBlockState(blockPos).getMaterial().blocksMotion() ? blockPos : null;
	}

	@Override
	public BlockPos getDimensionSpecificSpawn() {
		return END_SPAWN_POINT;
	}

	@Nullable
	@Override
	public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
		return this.getSpawnPosInChunk(new ChunkPos(i >> 4, j >> 4), bl);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return false;
	}

	@Override
	public DimensionType getType() {
		return DimensionType.THE_END;
	}

	@Override
	public void saveData() {
		CompoundTag compoundTag = new CompoundTag();
		if (this.dragonFight != null) {
			compoundTag.put("DragonFight", this.dragonFight.saveData());
		}

		this.level.getLevelData().setDimensionData(DimensionType.THE_END, compoundTag);
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
