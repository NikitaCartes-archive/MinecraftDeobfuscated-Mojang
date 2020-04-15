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

public class BastionSharedPools {
	public static final ProcessorRule GILDED_BLACKSTONE_REPLACEMENT_RULE = new ProcessorRule(
		new RandomBlockMatchTest(Blocks.BLACKSTONE, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.GILDED_BLACKSTONE.defaultBlockState()
	);

	public static void bootstrap() {
	}

	static {
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/mobs/piglin"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/mobs/melee_piglin"), 1),
						Pair.of(new SinglePoolElement("bastion/mobs/sword_piglin"), 4),
						Pair.of(new SinglePoolElement("bastion/mobs/crossbow_piglin"), 4),
						Pair.of(new SinglePoolElement("bastion/mobs/empty"), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/mobs/hoglin"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/mobs/hoglin"), 2), Pair.of(new SinglePoolElement("bastion/mobs/empty"), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/blocks/gold"),
					new ResourceLocation("empty"),
					ImmutableList.of(Pair.of(new SinglePoolElement("bastion/blocks/air"), 3), Pair.of(new SinglePoolElement("bastion/blocks/gold"), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("bastion/mobs/piglin_melee"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						Pair.of(new SinglePoolElement("bastion/mobs/melee_piglin_always"), 1),
						Pair.of(new SinglePoolElement("bastion/mobs/melee_piglin"), 5),
						Pair.of(new SinglePoolElement("bastion/mobs/sword_piglin"), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}
}
