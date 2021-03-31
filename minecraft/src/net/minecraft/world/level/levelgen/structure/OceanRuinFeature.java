package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanRuinFeature extends StructureFeature<OceanRuinConfiguration> {
	public OceanRuinFeature(Codec<OceanRuinConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<OceanRuinConfiguration> getStartFactory() {
		return OceanRuinFeature.OceanRuinStart::new;
	}

	public static class OceanRuinStart extends StructureStart<OceanRuinConfiguration> {
		public OceanRuinStart(StructureFeature<OceanRuinConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			OceanRuinConfiguration oceanRuinConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 90, chunkPos.getMinBlockZ());
			Rotation rotation = Rotation.getRandom(this.random);
			OceanRuinPieces.addPieces(structureManager, blockPos, rotation, this, this.random, oceanRuinConfiguration);
		}
	}

	public static enum Type implements StringRepresentable {
		WARM("warm"),
		COLD("cold");

		public static final Codec<OceanRuinFeature.Type> CODEC = StringRepresentable.fromEnum(OceanRuinFeature.Type::values, OceanRuinFeature.Type::byName);
		private static final Map<String, OceanRuinFeature.Type> BY_NAME = (Map<String, OceanRuinFeature.Type>)Arrays.stream(values())
			.collect(Collectors.toMap(OceanRuinFeature.Type::getName, type -> type));
		private final String name;

		private Type(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		@Nullable
		public static OceanRuinFeature.Type byName(String string) {
			return (OceanRuinFeature.Type)BY_NAME.get(string);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
