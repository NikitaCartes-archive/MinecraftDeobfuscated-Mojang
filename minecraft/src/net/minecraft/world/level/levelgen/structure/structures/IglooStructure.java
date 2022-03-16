package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class IglooStructure extends Structure {
	public static final Codec<IglooStructure> CODEC = RecordCodecBuilder.create(instance -> codec(instance).apply(instance, IglooStructure::new));

	public IglooStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
		super(holderSet, map, decoration, bl);
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		return onTopOfChunkCenter(
			generationContext, Heightmap.Types.WORLD_SURFACE_WG, structurePiecesBuilder -> this.generatePieces(structurePiecesBuilder, generationContext)
		);
	}

	private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		ChunkPos chunkPos = generationContext.chunkPos();
		WorldgenRandom worldgenRandom = generationContext.random();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 90, chunkPos.getMinBlockZ());
		Rotation rotation = Rotation.getRandom(worldgenRandom);
		IglooPieces.addPieces(generationContext.structureTemplateManager(), blockPos, rotation, structurePiecesBuilder, worldgenRandom);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.IGLOO;
	}
}
