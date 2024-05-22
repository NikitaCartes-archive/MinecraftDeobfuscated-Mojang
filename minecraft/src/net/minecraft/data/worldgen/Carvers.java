package net.minecraft.data.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class Carvers {
	public static final ResourceKey<ConfiguredWorldCarver<?>> CAVE = createKey("cave");
	public static final ResourceKey<ConfiguredWorldCarver<?>> CAVE_EXTRA_UNDERGROUND = createKey("cave_extra_underground");
	public static final ResourceKey<ConfiguredWorldCarver<?>> CANYON = createKey("canyon");
	public static final ResourceKey<ConfiguredWorldCarver<?>> NETHER_CAVE = createKey("nether_cave");

	private static ResourceKey<ConfiguredWorldCarver<?>> createKey(String string) {
		return ResourceKey.create(Registries.CONFIGURED_CARVER, ResourceLocation.withDefaultNamespace(string));
	}

	public static void bootstrap(BootstrapContext<ConfiguredWorldCarver<?>> bootstrapContext) {
		HolderGetter<Block> holderGetter = bootstrapContext.lookup(Registries.BLOCK);
		bootstrapContext.register(
			CAVE,
			WorldCarver.CAVE
				.configured(
					new CaveCarverConfiguration(
						0.15F,
						UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(180)),
						UniformFloat.of(0.1F, 0.9F),
						VerticalAnchor.aboveBottom(8),
						CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
						holderGetter.getOrThrow(BlockTags.OVERWORLD_CARVER_REPLACEABLES),
						UniformFloat.of(0.7F, 1.4F),
						UniformFloat.of(0.8F, 1.3F),
						UniformFloat.of(-1.0F, -0.4F)
					)
				)
		);
		bootstrapContext.register(
			CAVE_EXTRA_UNDERGROUND,
			WorldCarver.CAVE
				.configured(
					new CaveCarverConfiguration(
						0.07F,
						UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(47)),
						UniformFloat.of(0.1F, 0.9F),
						VerticalAnchor.aboveBottom(8),
						CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()),
						holderGetter.getOrThrow(BlockTags.OVERWORLD_CARVER_REPLACEABLES),
						UniformFloat.of(0.7F, 1.4F),
						UniformFloat.of(0.8F, 1.3F),
						UniformFloat.of(-1.0F, -0.4F)
					)
				)
		);
		bootstrapContext.register(
			CANYON,
			WorldCarver.CANYON
				.configured(
					new CanyonCarverConfiguration(
						0.01F,
						UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(67)),
						ConstantFloat.of(3.0F),
						VerticalAnchor.aboveBottom(8),
						CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
						holderGetter.getOrThrow(BlockTags.OVERWORLD_CARVER_REPLACEABLES),
						UniformFloat.of(-0.125F, 0.125F),
						new CanyonCarverConfiguration.CanyonShapeConfiguration(
							UniformFloat.of(0.75F, 1.0F), TrapezoidFloat.of(0.0F, 6.0F, 2.0F), 3, UniformFloat.of(0.75F, 1.0F), 1.0F, 0.0F
						)
					)
				)
		);
		bootstrapContext.register(
			NETHER_CAVE,
			WorldCarver.NETHER_CAVE
				.configured(
					new CaveCarverConfiguration(
						0.2F,
						UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.belowTop(1)),
						ConstantFloat.of(0.5F),
						VerticalAnchor.aboveBottom(10),
						holderGetter.getOrThrow(BlockTags.NETHER_CARVER_REPLACEABLES),
						ConstantFloat.of(1.0F),
						ConstantFloat.of(1.0F),
						ConstantFloat.of(-0.7F)
					)
				)
		);
	}
}
