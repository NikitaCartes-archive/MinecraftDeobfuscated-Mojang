package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.QuartPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
	public static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(
		new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3),
		new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
		new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
		new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5),
		new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
	);

	public NetherFortressFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, PieceGeneratorSupplier.simple(NetherFortressFeature::checkLocation, NetherFortressFeature::generatePieces));
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
		return worldgenRandom.nextInt(5) >= 2
			? false
			: context.validBiome()
				.test(
					context.chunkGenerator()
						.getNoiseBiome(QuartPos.fromBlock(context.chunkPos().getMiddleBlockX()), QuartPos.fromBlock(64), QuartPos.fromBlock(context.chunkPos().getMiddleBlockZ()))
				);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<NoneFeatureConfiguration> context) {
		NetherBridgePieces.StartPiece startPiece = new NetherBridgePieces.StartPiece(
			context.random(), context.chunkPos().getBlockX(2), context.chunkPos().getBlockZ(2)
		);
		structurePiecesBuilder.addPiece(startPiece);
		startPiece.addChildren(startPiece, structurePiecesBuilder, context.random());
		List<StructurePiece> list = startPiece.pendingChildren;

		while (!list.isEmpty()) {
			int i = context.random().nextInt(list.size());
			StructurePiece structurePiece = (StructurePiece)list.remove(i);
			structurePiece.addChildren(startPiece, structurePiecesBuilder, context.random());
		}

		structurePiecesBuilder.moveInsideHeights(context.random(), 48, 70);
	}
}
