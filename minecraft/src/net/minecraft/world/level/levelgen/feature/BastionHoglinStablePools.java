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

public class BastionHoglinStablePools {
	public static void bootstrap() {
	}

	static {
		ImmutableList<StructureProcessor> immutableList = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		);
		ImmutableList<StructureProcessor> immutableList2 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/origin"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/hoglin_stable/air_base", ImmutableList.of()), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/starting_pieces"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/starting_stairs_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/starting_stairs_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/starting_stairs_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/starting_stairs_3", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/starting_stairs_4", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/mirrored_starting_pieces"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/stairs_0_mirrored", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/stairs_1_mirrored", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/stairs_2_mirrored", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/stairs_3_mirrored", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/starting_pieces/stairs_4_mirrored", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/wall_bases"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/hoglin_stable/walls/wall_base", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/walls"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/walls/side_wall_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/walls/side_wall_1", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/stairs"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_1_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_1_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_1_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_1_3", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_1_4", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_2_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_2_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_2_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_2_3", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_2_4", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_3_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_3_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_3_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_3_3", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/stairs/stairs_3_4", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/small_stables/inner"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/inner_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/inner_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/inner_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/inner_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/small_stables/outer"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/outer_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/outer_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/outer_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/small_stables/outer_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/large_stables/inner"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/inner_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/inner_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/inner_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/inner_3", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/inner_4", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/large_stables/outer"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/outer_0", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/outer_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/outer_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/outer_3", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/large_stables/outer_4", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/posts"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/posts/stair_post", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/posts/end_post", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/ramparts"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/ramparts/ramparts_1", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/ramparts/ramparts_2", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/hoglin_stable/ramparts/ramparts_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/rampart_plates"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/hoglin_stable/rampart_plates/rampart_plate_1", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/hoglin_stable/connectors"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/hoglin_stable/connectors/end_post_connector", immutableList), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}
}
