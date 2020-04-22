package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class TrunkPlacerType<P extends TrunkPlacer> {
	public static final TrunkPlacerType<StraightTrunkPlacer> STRAIGHT_TRUNK_PLACER = register("straight_trunk_placer", StraightTrunkPlacer::new);
	public static final TrunkPlacerType<ForkingTrunkPlacer> FORKING_TRUNK_PLACER = register("forking_trunk_placer", ForkingTrunkPlacer::new);
	public static final TrunkPlacerType<GiantTrunkPlacer> GIANT_TRUNK_PLACER = register("giant_trunk_placer", GiantTrunkPlacer::new);
	public static final TrunkPlacerType<MegaJungleTrunkPlacer> MEGA_JUNGLE_TRUNK_PLACER = register("mega_jungle_trunk_placer", MegaJungleTrunkPlacer::new);
	public static final TrunkPlacerType<DarkOakTrunkPlacer> DARK_OAK_TRUNK_PLACER = register("dark_oak_trunk_placer", DarkOakTrunkPlacer::new);
	public static final TrunkPlacerType<FancyTrunkPlacer> FANCY_TRUNK_PLACER = register("fancy_trunk_placer", FancyTrunkPlacer::new);
	private final Function<Dynamic<?>, P> deserializer;

	private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String string, Function<Dynamic<?>, P> function) {
		return Registry.register(Registry.TRUNK_PLACER_TYPES, string, new TrunkPlacerType<>(function));
	}

	private TrunkPlacerType(Function<Dynamic<?>, P> function) {
		this.deserializer = function;
	}

	public P deserialize(Dynamic<?> dynamic) {
		return (P)this.deserializer.apply(dynamic);
	}
}
