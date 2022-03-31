package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanRuinStructure extends Structure {
	public static final Codec<OceanRuinStructure> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					settingsCodec(instance),
					OceanRuinStructure.Type.CODEC.fieldOf("biome_temp").forGetter(oceanRuinStructure -> oceanRuinStructure.biomeTemp),
					Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter(oceanRuinStructure -> oceanRuinStructure.largeProbability),
					Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter(oceanRuinStructure -> oceanRuinStructure.clusterProbability)
				)
				.apply(instance, OceanRuinStructure::new)
	);
	public final OceanRuinStructure.Type biomeTemp;
	public final float largeProbability;
	public final float clusterProbability;

	public OceanRuinStructure(Structure.StructureSettings structureSettings, OceanRuinStructure.Type type, float f, float g) {
		super(structureSettings);
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

		public static final Codec<OceanRuinStructure.Type> CODEC = StringRepresentable.fromEnum(OceanRuinStructure.Type::values);
		private final String name;

		private Type(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
