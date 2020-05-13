package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RandomScatteredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanRuinFeature extends RandomScatteredFeature<OceanRuinConfiguration> {
	public OceanRuinFeature(Function<Dynamic<?>, ? extends OceanRuinConfiguration> function) {
		super(function);
	}

	@Override
	public String getFeatureName() {
		return "Ocean_Ruin";
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	@Override
	protected int getSpacing(ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getOceanRuinSpacing();
	}

	@Override
	protected int getSeparation(ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getOceanRuinSeparation();
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return OceanRuinFeature.OceanRuinStart::new;
	}

	@Override
	protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
		return 14357621;
	}

	public static class OceanRuinStart extends StructureStart {
		public OceanRuinStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			OceanRuinConfiguration oceanRuinConfiguration = chunkGenerator.getStructureConfiguration(biome, Feature.OCEAN_RUIN);
			int k = i * 16;
			int l = j * 16;
			BlockPos blockPos = new BlockPos(k, 90, l);
			Rotation rotation = Rotation.getRandom(this.random);
			OceanRuinPieces.addPieces(structureManager, blockPos, rotation, this.pieces, this.random, oceanRuinConfiguration);
			this.calculateBoundingBox();
		}
	}

	public static enum Type {
		WARM("warm"),
		COLD("cold");

		private static final Map<String, OceanRuinFeature.Type> BY_NAME = (Map<String, OceanRuinFeature.Type>)Arrays.stream(values())
			.collect(Collectors.toMap(OceanRuinFeature.Type::getName, type -> type));
		private final String name;

		private Type(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		public static OceanRuinFeature.Type byName(String string) {
			return (OceanRuinFeature.Type)BY_NAME.get(string);
		}
	}
}
