package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class RootPlacerType<P extends RootPlacer> {
	public static final RootPlacerType<MangroveRootPlacer> MANGROVE_ROOT_PLACER = register("mangrove_root_placer", MangroveRootPlacer.CODEC);
	private final MapCodec<P> codec;

	private static <P extends RootPlacer> RootPlacerType<P> register(String string, MapCodec<P> mapCodec) {
		return Registry.register(BuiltInRegistries.ROOT_PLACER_TYPE, string, new RootPlacerType<>(mapCodec));
	}

	private RootPlacerType(MapCodec<P> mapCodec) {
		this.codec = mapCodec;
	}

	public MapCodec<P> codec() {
		return this.codec;
	}
}
