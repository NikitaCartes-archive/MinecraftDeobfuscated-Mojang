package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class BlockPlacerType<P extends BlockPlacer> {
	public static final BlockPlacerType<SimpleBlockPlacer> SIMPLE_BLOCK_PLACER = register("simple_block_placer", SimpleBlockPlacer.CODEC);
	public static final BlockPlacerType<DoublePlantPlacer> DOUBLE_PLANT_PLACER = register("double_plant_placer", DoublePlantPlacer.CODEC);
	public static final BlockPlacerType<ColumnPlacer> COLUMN_PLACER = register("column_placer", ColumnPlacer.CODEC);
	private final Codec<P> codec;

	private static <P extends BlockPlacer> BlockPlacerType<P> register(String string, Codec<P> codec) {
		return Registry.register(Registry.BLOCK_PLACER_TYPES, string, new BlockPlacerType<>(codec));
	}

	private BlockPlacerType(Codec<P> codec) {
		this.codec = codec;
	}

	public Codec<P> codec() {
		return this.codec;
	}
}
