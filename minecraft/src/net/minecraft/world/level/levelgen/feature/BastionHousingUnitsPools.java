package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class BastionHousingUnitsPools {
	public static void bootstrap() {
	}

	static {
		ImmutableList<StructureProcessor> immutableList = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/base"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/air_base", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/center_pieces"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/center_pieces/center_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/center_pieces/center_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/center_pieces/center_2", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/pathways"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/pathways/pathway_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/pathways/pathway_wall_0", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/walls/wall_bases"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/walls/wall_base", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/walls/connected_wall", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/stages/stage_0"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_0_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_0_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_0_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_0_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/stages/stage_1"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_1_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_1_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_1_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_1_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/stages/rot/stage_1"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/stages/rot/stage_1_0", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/stages/stage_2"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_2_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_2_1", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/stages/stage_3"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_3_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_3_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_3_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/stages/stage_3_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/fillers/stage_0"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/fillers/stage_0", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/edges"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/edges/edge_0", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/wall_units"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/wall_units/unit_0", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/edge_wall_units"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/wall_units/edge_0_large", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/ramparts"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/units/ramparts/ramparts_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/ramparts/ramparts_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/units/ramparts/ramparts_2", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/large_ramparts"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/ramparts/ramparts_0", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/units/rampart_plates"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/units/rampart_plates/plate_0", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}
}
