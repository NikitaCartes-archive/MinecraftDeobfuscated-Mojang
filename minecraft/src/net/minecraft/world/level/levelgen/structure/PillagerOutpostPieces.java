package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.ListPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostPieces {
	public static void addPieces(
		ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos blockPos, List<StructurePiece> list, WorldgenRandom worldgenRandom
	) {
		JigsawPlacement.addPieces(
			new ResourceLocation("pillager_outpost/base_plates"),
			7,
			PillagerOutpostPieces.PillagerOutpostPiece::new,
			chunkGenerator,
			structureManager,
			blockPos,
			list,
			worldgenRandom,
			true,
			true
		);
	}

	public static void bootstrap() {
	}

	static {
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("pillager_outpost/base_plates"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new LegacySinglePoolElement("pillager_outpost/base_plate"), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("pillager_outpost/towers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(
							new ListPoolElement(
								ImmutableList.of(
									new LegacySinglePoolElement("pillager_outpost/watchtower"),
									new LegacySinglePoolElement("pillager_outpost/watchtower_overgrown", ImmutableList.of(new BlockRotProcessor(0.05F)))
								)
							),
							1
						)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("pillager_outpost/feature_plates"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new LegacySinglePoolElement("pillager_outpost/feature_plate"), 1)),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("pillager_outpost/features"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new LegacySinglePoolElement("pillager_outpost/feature_cage1"), 1),
						Pair.of(new LegacySinglePoolElement("pillager_outpost/feature_cage2"), 1),
						Pair.of(new LegacySinglePoolElement("pillager_outpost/feature_logs"), 1),
						Pair.of(new LegacySinglePoolElement("pillager_outpost/feature_tent1"), 1),
						Pair.of(new LegacySinglePoolElement("pillager_outpost/feature_tent2"), 1),
						Pair.of(new LegacySinglePoolElement("pillager_outpost/feature_targets"), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 6)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}

	public static class PillagerOutpostPiece extends PoolElementStructurePiece {
		public PillagerOutpostPiece(
			StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox
		) {
			super(StructurePieceType.PILLAGER_OUTPOST, structureManager, structurePoolElement, blockPos, i, rotation, boundingBox);
		}

		public PillagerOutpostPiece(StructureManager structureManager, CompoundTag compoundTag) {
			super(structureManager, compoundTag, StructurePieceType.PILLAGER_OUTPOST);
		}
	}
}
