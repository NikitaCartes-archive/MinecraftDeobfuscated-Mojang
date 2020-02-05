package net.minecraft.world.level.dimension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardBiomeSourceSettings;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSourceSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.level.levelgen.NetherLevelSource;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.levelgen.TheEndLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.phys.Vec3;

public class NormalDimension extends Dimension {
	public NormalDimension(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public DimensionType getType() {
		return DimensionType.OVERWORLD;
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		LevelType levelType = this.level.getLevelData().getGeneratorType();
		ChunkGeneratorType<FlatLevelGeneratorSettings, FlatLevelSource> chunkGeneratorType = ChunkGeneratorType.FLAT;
		ChunkGeneratorType<DebugGeneratorSettings, DebugLevelSource> chunkGeneratorType2 = ChunkGeneratorType.DEBUG;
		ChunkGeneratorType<NetherGeneratorSettings, NetherLevelSource> chunkGeneratorType3 = ChunkGeneratorType.CAVES;
		ChunkGeneratorType<TheEndGeneratorSettings, TheEndLevelSource> chunkGeneratorType4 = ChunkGeneratorType.FLOATING_ISLANDS;
		ChunkGeneratorType<OverworldGeneratorSettings, OverworldLevelSource> chunkGeneratorType5 = ChunkGeneratorType.SURFACE;
		BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> biomeSourceType = BiomeSourceType.FIXED;
		BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> biomeSourceType2 = BiomeSourceType.VANILLA_LAYERED;
		BiomeSourceType<CheckerboardBiomeSourceSettings, CheckerboardColumnBiomeSource> biomeSourceType3 = BiomeSourceType.CHECKERBOARD;
		if (levelType == LevelType.FLAT) {
			FlatLevelGeneratorSettings flatLevelGeneratorSettings = FlatLevelGeneratorSettings.fromObject(
				new Dynamic<>(NbtOps.INSTANCE, this.level.getLevelData().getGeneratorOptions())
			);
			FixedBiomeSourceSettings fixedBiomeSourceSettings = biomeSourceType.createSettings(this.level.getLevelData())
				.setBiome(flatLevelGeneratorSettings.getBiome());
			return chunkGeneratorType.create(this.level, biomeSourceType.create(fixedBiomeSourceSettings), flatLevelGeneratorSettings);
		} else if (levelType == LevelType.DEBUG_ALL_BLOCK_STATES) {
			FixedBiomeSourceSettings fixedBiomeSourceSettings2 = biomeSourceType.createSettings(this.level.getLevelData()).setBiome(Biomes.PLAINS);
			return chunkGeneratorType2.create(this.level, biomeSourceType.create(fixedBiomeSourceSettings2), chunkGeneratorType2.createSettings());
		} else if (levelType != LevelType.BUFFET) {
			OverworldGeneratorSettings overworldGeneratorSettings2 = chunkGeneratorType5.createSettings();
			OverworldBiomeSourceSettings overworldBiomeSourceSettings2 = biomeSourceType2.createSettings(this.level.getLevelData())
				.setGeneratorSettings(overworldGeneratorSettings2);
			return chunkGeneratorType5.create(this.level, biomeSourceType2.create(overworldBiomeSourceSettings2), overworldGeneratorSettings2);
		} else {
			BiomeSource biomeSource = null;
			JsonElement jsonElement = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, this.level.getLevelData().getGeneratorOptions());
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			JsonObject jsonObject2 = jsonObject.getAsJsonObject("biome_source");
			if (jsonObject2 != null && jsonObject2.has("type") && jsonObject2.has("options")) {
				BiomeSourceType<?, ?> biomeSourceType4 = Registry.BIOME_SOURCE_TYPE.get(new ResourceLocation(jsonObject2.getAsJsonPrimitive("type").getAsString()));
				JsonObject jsonObject3 = jsonObject2.getAsJsonObject("options");
				Biome[] biomes = new Biome[]{Biomes.OCEAN};
				if (jsonObject3.has("biomes")) {
					JsonArray jsonArray = jsonObject3.getAsJsonArray("biomes");
					biomes = jsonArray.size() > 0 ? new Biome[jsonArray.size()] : new Biome[]{Biomes.OCEAN};

					for (int i = 0; i < jsonArray.size(); i++) {
						biomes[i] = (Biome)Registry.BIOME.getOptional(new ResourceLocation(jsonArray.get(i).getAsString())).orElse(Biomes.OCEAN);
					}
				}

				if (BiomeSourceType.FIXED == biomeSourceType4) {
					FixedBiomeSourceSettings fixedBiomeSourceSettings3 = biomeSourceType.createSettings(this.level.getLevelData()).setBiome(biomes[0]);
					biomeSource = biomeSourceType.create(fixedBiomeSourceSettings3);
				}

				if (BiomeSourceType.CHECKERBOARD == biomeSourceType4) {
					int j = jsonObject3.has("size") ? jsonObject3.getAsJsonPrimitive("size").getAsInt() : 2;
					CheckerboardBiomeSourceSettings checkerboardBiomeSourceSettings = biomeSourceType3.createSettings(this.level.getLevelData())
						.setAllowedBiomes(biomes)
						.setSize(j);
					biomeSource = biomeSourceType3.create(checkerboardBiomeSourceSettings);
				}

				if (BiomeSourceType.VANILLA_LAYERED == biomeSourceType4) {
					OverworldBiomeSourceSettings overworldBiomeSourceSettings = biomeSourceType2.createSettings(this.level.getLevelData());
					biomeSource = biomeSourceType2.create(overworldBiomeSourceSettings);
				}
			}

			if (biomeSource == null) {
				biomeSource = biomeSourceType.create(biomeSourceType.createSettings(this.level.getLevelData()).setBiome(Biomes.OCEAN));
			}

			BlockState blockState = Blocks.STONE.defaultBlockState();
			BlockState blockState2 = Blocks.WATER.defaultBlockState();
			JsonObject jsonObject4 = jsonObject.getAsJsonObject("chunk_generator");
			if (jsonObject4 != null && jsonObject4.has("options")) {
				JsonObject jsonObject5 = jsonObject4.getAsJsonObject("options");
				if (jsonObject5.has("default_block")) {
					String string = jsonObject5.getAsJsonPrimitive("default_block").getAsString();
					blockState = Registry.BLOCK.get(new ResourceLocation(string)).defaultBlockState();
				}

				if (jsonObject5.has("default_fluid")) {
					String string = jsonObject5.getAsJsonPrimitive("default_fluid").getAsString();
					blockState2 = Registry.BLOCK.get(new ResourceLocation(string)).defaultBlockState();
				}
			}

			if (jsonObject4 != null && jsonObject4.has("type")) {
				ChunkGeneratorType<?, ?> chunkGeneratorType6 = Registry.CHUNK_GENERATOR_TYPE
					.get(new ResourceLocation(jsonObject4.getAsJsonPrimitive("type").getAsString()));
				if (ChunkGeneratorType.CAVES == chunkGeneratorType6) {
					NetherGeneratorSettings netherGeneratorSettings = chunkGeneratorType3.createSettings();
					netherGeneratorSettings.setDefaultBlock(blockState);
					netherGeneratorSettings.setDefaultFluid(blockState2);
					return chunkGeneratorType3.create(this.level, biomeSource, netherGeneratorSettings);
				}

				if (ChunkGeneratorType.FLOATING_ISLANDS == chunkGeneratorType6) {
					TheEndGeneratorSettings theEndGeneratorSettings = chunkGeneratorType4.createSettings();
					theEndGeneratorSettings.setSpawnPosition(new BlockPos(0, 64, 0));
					theEndGeneratorSettings.setDefaultBlock(blockState);
					theEndGeneratorSettings.setDefaultFluid(blockState2);
					return chunkGeneratorType4.create(this.level, biomeSource, theEndGeneratorSettings);
				}
			}

			OverworldGeneratorSettings overworldGeneratorSettings = chunkGeneratorType5.createSettings();
			overworldGeneratorSettings.setDefaultBlock(blockState);
			overworldGeneratorSettings.setDefaultFluid(blockState2);
			return chunkGeneratorType5.create(this.level, biomeSource, overworldGeneratorSettings);
		}
	}

	@Nullable
	@Override
	public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
		for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); i++) {
			for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); j++) {
				BlockPos blockPos = this.getValidSpawnPosition(i, j, bl);
				if (blockPos != null) {
					return blockPos;
				}
			}
		}

		return null;
	}

	@Nullable
	@Override
	public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, 0, j);
		Biome biome = this.level.getBiome(mutableBlockPos);
		BlockState blockState = biome.getSurfaceBuilderConfig().getTopMaterial();
		if (bl && !blockState.getBlock().is(BlockTags.VALID_SPAWN)) {
			return null;
		} else {
			LevelChunk levelChunk = this.level.getChunk(i >> 4, j >> 4);
			int k = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 15, j & 15);
			if (k < 0) {
				return null;
			} else if (levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 15, j & 15) > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 15, j & 15)) {
				return null;
			} else {
				for (int l = k + 1; l >= 0; l--) {
					mutableBlockPos.set(i, l, j);
					BlockState blockState2 = this.level.getBlockState(mutableBlockPos);
					if (!blockState2.getFluidState().isEmpty()) {
						break;
					}

					if (blockState2.equals(blockState)) {
						return mutableBlockPos.above().immutable();
					}
				}

				return null;
			}
		}
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		double d = Mth.frac((double)l / 24000.0 - 0.25);
		double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
		return (float)(d * 2.0 + e) / 3.0F;
	}

	@Override
	public boolean isNaturalDimension() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(int i, float f) {
		return Vec3.fromRGB24(i).multiply((double)(f * 0.94F + 0.06F), (double)(f * 0.94F + 0.06F), (double)(f * 0.91F + 0.09F));
	}

	@Override
	public boolean mayRespawn() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return false;
	}
}
