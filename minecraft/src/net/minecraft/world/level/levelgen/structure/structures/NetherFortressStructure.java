package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFortressStructure extends Structure {
	public static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(
		new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3),
		new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
		new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
		new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5),
		new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
	);
	public static final Codec<NetherFortressStructure> CODEC = RecordCodecBuilder.create(instance -> codec(instance).apply(instance, NetherFortressStructure::new));

	public NetherFortressStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
		super(holderSet, map, decoration, bl);
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		ChunkPos chunkPos = generationContext.chunkPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 64, chunkPos.getMinBlockZ());
		return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> generatePieces(structurePiecesBuilder, generationContext)));
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		NetherFortressPieces.StartPiece startPiece = new NetherFortressPieces.StartPiece(
			generationContext.random(), generationContext.chunkPos().getBlockX(2), generationContext.chunkPos().getBlockZ(2)
		);
		structurePiecesBuilder.addPiece(startPiece);
		startPiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
		List<StructurePiece> list = startPiece.pendingChildren;

		while (!list.isEmpty()) {
			int i = generationContext.random().nextInt(list.size());
			StructurePiece structurePiece = (StructurePiece)list.remove(i);
			structurePiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
		}

		structurePiecesBuilder.moveInsideHeights(generationContext.random(), 48, 70);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.FORTRESS;
	}
}
