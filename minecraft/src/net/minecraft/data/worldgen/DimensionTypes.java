package net.minecraft.data.worldgen;

import java.util.OptionalLong;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionTypes {
	public static void bootstrap(BootstapContext<DimensionType> bootstapContext) {
		bootstapContext.register(
			BuiltinDimensionTypes.OVERWORLD,
			new DimensionType(
				OptionalLong.empty(),
				true,
				false,
				false,
				true,
				1.0,
				true,
				false,
				-64,
				384,
				384,
				BlockTags.INFINIBURN_OVERWORLD,
				BuiltinDimensionTypes.OVERWORLD_EFFECTS,
				0.0F,
				new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)
			)
		);
		bootstapContext.register(
			BuiltinDimensionTypes.NETHER,
			new DimensionType(
				OptionalLong.of(18000L),
				false,
				true,
				true,
				false,
				8.0,
				false,
				true,
				0,
				256,
				128,
				BlockTags.INFINIBURN_NETHER,
				BuiltinDimensionTypes.NETHER_EFFECTS,
				0.1F,
				new DimensionType.MonsterSettings(true, false, ConstantInt.of(11), 15)
			)
		);
		bootstapContext.register(
			BuiltinDimensionTypes.END,
			new DimensionType(
				OptionalLong.of(6000L),
				false,
				false,
				false,
				false,
				1.0,
				false,
				false,
				0,
				256,
				256,
				BlockTags.INFINIBURN_END,
				BuiltinDimensionTypes.END_EFFECTS,
				0.0F,
				new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)
			)
		);
		bootstapContext.register(
			BuiltinDimensionTypes.OVERWORLD_CAVES,
			new DimensionType(
				OptionalLong.empty(),
				true,
				true,
				false,
				true,
				1.0,
				true,
				false,
				-64,
				384,
				384,
				BlockTags.INFINIBURN_OVERWORLD,
				BuiltinDimensionTypes.OVERWORLD_EFFECTS,
				0.0F,
				new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)
			)
		);
	}
}
