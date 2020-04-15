package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.AxisAlignedLinearPosTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class BastionTreasureRoomPools {
	public static void bootstrap() {
	}

	static {
		ImmutableList<StructureProcessor> immutableList = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.15F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState())
				)
			)
		);
		ImmutableList<StructureProcessor> immutableList2 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.35F),
						AlwaysTrueTest.INSTANCE,
						Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.1F),
						AlwaysTrueTest.INSTANCE,
						Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		);
		ImmutableList<StructureProcessor> immutableList3 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState())
				)
			)
		);
		ImmutableList<StructureProcessor> immutableList4 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(
						AlwaysTrueTest.INSTANCE, AlwaysTrueTest.INSTANCE, new AxisAlignedLinearPosTest(0.0F, 0.05F, 0, 100, Direction.Axis.Y), Blocks.AIR.defaultBlockState()
					)
				)
			)
		);
		ImmutableList<StructureProcessor> immutableList5 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.MAGMA_BLOCK, 0.75F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, 0.15F),
						AlwaysTrueTest.INSTANCE,
						Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/starters"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/big_air_full", immutableList2), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/bases"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/bases/lava_basin", immutableList2), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/stairs"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/stairs/lower_stairs", immutableList2), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/bases/centers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_1", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_2", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_3", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/brains"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/brains/center_brain", immutableList2), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/walls"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/walls/lava_wall", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/entrance_wall", immutableList3), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/walls/outer"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/top_corner", immutableList3), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/mid_corner", immutableList3), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/bottom_corner", immutableList3), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/outer_wall", immutableList3), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/medium_outer_wall", immutableList3), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/tall_outer_wall", immutableList3), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/walls/bottom"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_1", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_2", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_3", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/walls/mid"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/walls/mid/wall_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/mid/wall_1", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/mid/wall_2", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/walls/top"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/walls/top/main_entrance", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/top/wall_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/walls/top/wall_1", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/connectors"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/connectors/center_to_wall_middle", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/connectors/center_to_wall_top", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/connectors/center_to_wall_top_entrance", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/entrances"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/entrances/entrance_0", immutableList2), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/ramparts"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/ramparts/mid_wall_main", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/ramparts/mid_wall_side", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/ramparts/bottom_wall_0", immutableList5), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/ramparts/top_wall", immutableList4), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/ramparts/lava_basin_side", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/ramparts/lava_basin_main", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/corners/bottom"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/corners/bottom/corner_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/corners/bottom/corner_1", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/corners/edges"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/corners/edges/bottom", immutableList3), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/corners/edges/middle", immutableList3), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/corners/edges/top", immutableList3), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/corners/middle"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/corners/middle/corner_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/corners/middle/corner_1", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/corners/top"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/corners/top/corner_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/corners/top/corner_1", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/extensions/large_pool"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/fire_room", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_1", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_2", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_3", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/roofed_bridge", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/extensions/small_pool"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/fire_room", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_1", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_2", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_3", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/extensions/houses"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/house_0", immutableList2), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/extensions/house_1", immutableList2), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/treasure/roofs"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/treasure/roofs/wall_roof", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/roofs/corner_roof", immutableList), 1),
						Pair.of(new SinglePoolElement("bastion/treasure/roofs/center_roof", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}
}
