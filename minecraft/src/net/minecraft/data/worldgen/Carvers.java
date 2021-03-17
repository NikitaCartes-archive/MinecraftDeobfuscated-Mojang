package net.minecraft.data.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.TrapezoidFloat;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class Carvers {
	public static final ConfiguredWorldCarver<CarverConfiguration> CAVE = register(
		"cave", WorldCarver.CAVE.configured(new CarverConfiguration(0.33333334F, CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState())))
	);
	public static final ConfiguredWorldCarver<CanyonCarverConfiguration> CANYON = register(
		"canyon",
		WorldCarver.CANYON
			.configured(
				new CanyonCarverConfiguration(
					0.02F,
					CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
					VerticalAnchor.absolute(10),
					VerticalAnchor.absolute(67),
					UniformInt.fixed(3),
					UniformFloat.of(0.75F, 0.25F),
					UniformFloat.of(-0.125F, 0.25F),
					TrapezoidFloat.of(0.0F, 6.0F, 2.0F),
					3,
					UniformFloat.of(0.75F, 0.25F),
					1.0F,
					0.0F
				)
			)
	);
	public static final ConfiguredWorldCarver<CarverConfiguration> OCEAN_CAVE = register(
		"ocean_cave", WorldCarver.CAVE.configured(new CarverConfiguration(0.14285715F, CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState())))
	);
	public static final ConfiguredWorldCarver<CarverConfiguration> NETHER_CAVE = register(
		"nether_cave", WorldCarver.NETHER_CAVE.configured(new CarverConfiguration(0.2F))
	);
	public static final ConfiguredWorldCarver<CanyonCarverConfiguration> CRACK = register(
		"crack",
		WorldCarver.CANYON
			.configured(
				new CanyonCarverConfiguration(
					0.00125F,
					CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()),
					VerticalAnchor.absolute(40),
					VerticalAnchor.absolute(80),
					UniformInt.of(6, 2),
					UniformFloat.of(0.5F, 0.5F),
					UniformFloat.of(-0.125F, 0.25F),
					UniformFloat.of(0.0F, 1.0F),
					6,
					UniformFloat.of(0.25F, 0.75F),
					0.0F,
					5.0F
				)
			)
	);

	private static <WC extends CarverConfiguration> ConfiguredWorldCarver<WC> register(String string, ConfiguredWorldCarver<WC> configuredWorldCarver) {
		return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_CARVER, string, configuredWorldCarver);
	}
}
