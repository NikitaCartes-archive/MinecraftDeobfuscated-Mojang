package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanRuinStructure extends Structure {
	public static final Codec<OceanRuinStructure> CODEC = RecordCodecBuilder.create(
		instance -> codec(instance)
				.<OceanRuinStructure.Type, float, float>and(
					instance.group(
						OceanRuinStructure.Type.CODEC.fieldOf("biome_temp").forGetter(oceanRuinStructure -> oceanRuinStructure.biomeTemp),
						Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter(oceanRuinStructure -> oceanRuinStructure.largeProbability),
						Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter(oceanRuinStructure -> oceanRuinStructure.clusterProbability)
					)
				)
				.apply(instance, OceanRuinStructure::new)
	);
	public final OceanRuinStructure.Type biomeTemp;
	public final float largeProbability;
	public final float clusterProbability;

	public OceanRuinStructure(
		HolderSet<Biome> holderSet,
		Map<MobCategory, StructureSpawnOverride> map,
		GenerationStep.Decoration decoration,
		boolean bl,
		OceanRuinStructure.Type type,
		float f,
		float g
	) {
		super(holderSet, map, decoration, bl);
		this.biomeTemp = type;
		this.largeProbability = f;
		this.clusterProbability = g;
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		return onTopOfChunkCenter(
			generationContext, Heightmap.Types.OCEAN_FLOOR_WG, structurePiecesBuilder -> this.generatePieces(structurePiecesBuilder, generationContext)
		);
	}

	private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		BlockPos blockPos = new BlockPos(generationContext.chunkPos().getMinBlockX(), 90, generationContext.chunkPos().getMinBlockZ());
		Rotation rotation = Rotation.getRandom(generationContext.random());
		OceanRuinPieces.addPieces(generationContext.structureTemplateManager(), blockPos, rotation, structurePiecesBuilder, generationContext.random(), this);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.OCEAN_RUIN;
	}

	public static enum Type implements StringRepresentable {
		WARM("warm"),
		COLD("cold");

		public static final Codec<OceanRuinStructure.Type> CODEC = StringRepresentable.fromEnum(OceanRuinStructure.Type::values, OceanRuinStructure.Type::byName);
		private static final Map<String, OceanRuinStructure.Type> BY_NAME = (Map<String, OceanRuinStructure.Type>)Arrays.stream(values())
			.collect(Collectors.toMap(OceanRuinStructure.Type::getName, type -> type));
		private final String name;

		private Type(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		@Nullable
		public static OceanRuinStructure.Type byName(String string) {
			return (OceanRuinStructure.Type)BY_NAME.get(string);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
