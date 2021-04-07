package net.minecraft.data.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class Carvers {
	public static final ConfiguredWorldCarver<CaveCarverConfiguration> OLD_CAVE = register(
		"old_cave",
		WorldCarver.CAVE
			.configured(
				new CaveCarverConfiguration(
					0.14285715F,
					BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8),
					ConstantFloat.of(0.5F),
					VerticalAnchor.aboveBottom(11),
					CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(-0.7F)
				)
			)
	);
	public static final ConfiguredWorldCarver<CaveCarverConfiguration> CAVE = register(
		"cave",
		WorldCarver.CAVE
			.configured(
				new CaveCarverConfiguration(
					0.33333334F,
					UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(126)),
					UniformFloat.of(0.1F, 0.9F),
					VerticalAnchor.aboveBottom(9),
					CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
					UniformFloat.of(0.3F, 1.8F),
					UniformFloat.of(0.5F, 1.8F),
					UniformFloat.of(-1.0F, 0.0F)
				)
			)
	);
	public static final ConfiguredWorldCarver<CanyonCarverConfiguration> OLD_CANYON = register(
		"old_canyon",
		WorldCarver.CANYON
			.configured(
				new CanyonCarverConfiguration(
					0.02F,
					BiasedToBottomHeight.of(VerticalAnchor.absolute(20), VerticalAnchor.absolute(67), 8),
					ConstantFloat.of(3.0F),
					VerticalAnchor.aboveBottom(11),
					CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
					UniformFloat.of(-0.125F, 0.125F),
					new CanyonCarverConfiguration.CanyonShapeConfiguration(
						UniformFloat.of(0.75F, 1.0F), TrapezoidFloat.of(0.0F, 6.0F, 2.0F), 3, UniformFloat.of(0.75F, 1.0F), 1.0F, 0.0F
					)
				)
			)
	);
	public static final ConfiguredWorldCarver<CanyonCarverConfiguration> CANYON = register(
		"canyon",
		WorldCarver.CANYON
			.configured(
				new CanyonCarverConfiguration(
					0.02F,
					UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(67)),
					ConstantFloat.of(3.0F),
					VerticalAnchor.aboveBottom(9),
					CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
					UniformFloat.of(-0.125F, 0.125F),
					new CanyonCarverConfiguration.CanyonShapeConfiguration(
						UniformFloat.of(0.75F, 1.0F), TrapezoidFloat.of(0.0F, 6.0F, 2.0F), 3, UniformFloat.of(0.75F, 1.0F), 1.0F, 0.0F
					)
				)
			)
	);
	public static final ConfiguredWorldCarver<CaveCarverConfiguration> OLD_OCEAN_CAVE = register(
		"old_ocean_cave",
		WorldCarver.CAVE
			.configured(
				new CaveCarverConfiguration(
					0.06666667F,
					BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8),
					ConstantFloat.of(0.5F),
					VerticalAnchor.aboveBottom(11),
					CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(-0.7F)
				)
			)
	);
	public static final ConfiguredWorldCarver<CaveCarverConfiguration> OCEAN_CAVE = register(
		"ocean_cave",
		WorldCarver.CAVE
			.configured(
				new CaveCarverConfiguration(
					0.14285715F,
					BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8),
					UniformFloat.of(0.1F, 0.9F),
					VerticalAnchor.aboveBottom(9),
					CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(1.0F),
					UniformFloat.of(-1.0F, 0.0F)
				)
			)
	);
	public static final ConfiguredWorldCarver<CanyonCarverConfiguration> OLD_UNDERWATER_CANYON = register(
		"old_underwater_canyon",
		WorldCarver.UNDERWATER_CANYON
			.configured(
				new CanyonCarverConfiguration(
					0.02F,
					BiasedToBottomHeight.of(VerticalAnchor.absolute(20), VerticalAnchor.absolute(67), 8),
					ConstantFloat.of(3.0F),
					VerticalAnchor.aboveBottom(11),
					CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
					UniformFloat.of(-0.125F, 0.125F),
					new CanyonCarverConfiguration.CanyonShapeConfiguration(
						UniformFloat.of(0.75F, 1.0F), TrapezoidFloat.of(0.0F, 6.0F, 2.0F), 3, UniformFloat.of(0.75F, 1.0F), 1.0F, 0.0F
					)
				)
			)
	);
	public static final ConfiguredWorldCarver<CaveCarverConfiguration> OLD_UNDERWATER_CAVE = register(
		"old_underwater_cave",
		WorldCarver.UNDERWATER_CAVE
			.configured(
				new CaveCarverConfiguration(
					0.06666667F,
					BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8),
					ConstantFloat.of(0.5F),
					VerticalAnchor.aboveBottom(11),
					CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(-0.7F)
				)
			)
	);
	public static final ConfiguredWorldCarver<CaveCarverConfiguration> NETHER_CAVE = register(
		"nether_cave",
		WorldCarver.NETHER_CAVE
			.configured(
				new CaveCarverConfiguration(
					0.2F,
					UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.belowTop(1)),
					ConstantFloat.of(0.5F),
					VerticalAnchor.aboveBottom(11),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(1.0F),
					ConstantFloat.of(-0.7F)
				)
			)
	);
	public static final ConfiguredWorldCarver<CanyonCarverConfiguration> CRACK = register(
		"crack",
		WorldCarver.CANYON
			.configured(
				new CanyonCarverConfiguration(
					0.00125F,
					UniformHeight.of(VerticalAnchor.absolute(40), VerticalAnchor.absolute(80)),
					UniformFloat.of(6.0F, 8.0F),
					VerticalAnchor.aboveBottom(9),
					CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()),
					UniformFloat.of(-0.125F, 0.125F),
					new CanyonCarverConfiguration.CanyonShapeConfiguration(
						UniformFloat.of(0.5F, 1.0F), UniformFloat.of(0.0F, 1.0F), 6, UniformFloat.of(0.25F, 1.0F), 0.0F, 5.0F
					)
				)
			)
	);

	private static <WC extends CarverConfiguration> ConfiguredWorldCarver<WC> register(String string, ConfiguredWorldCarver<WC> configuredWorldCarver) {
		return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_CARVER, string, configuredWorldCarver);
	}
}
