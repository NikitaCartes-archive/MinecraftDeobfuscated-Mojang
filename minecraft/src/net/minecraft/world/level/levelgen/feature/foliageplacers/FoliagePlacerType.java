package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class FoliagePlacerType<P extends FoliagePlacer> {
	public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = register(
		"blob_foliage_placer", BlobFoliagePlacer::new, BlobFoliagePlacer::random
	);
	public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = register(
		"spruce_foliage_placer", SpruceFoliagePlacer::new, SpruceFoliagePlacer::random
	);
	public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = register(
		"pine_foliage_placer", PineFoliagePlacer::new, PineFoliagePlacer::random
	);
	public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = register(
		"acacia_foliage_placer", AcaciaFoliagePlacer::new, AcaciaFoliagePlacer::random
	);
	private final Function<Dynamic<?>, P> deserializer;
	private final Function<Random, P> randomProvider;

	private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String string, Function<Dynamic<?>, P> function, Function<Random, P> function2) {
		return Registry.register(Registry.FOLIAGE_PLACER_TYPES, string, new FoliagePlacerType<>(function, function2));
	}

	private FoliagePlacerType(Function<Dynamic<?>, P> function, Function<Random, P> function2) {
		this.deserializer = function;
		this.randomProvider = function2;
	}

	public P deserialize(Dynamic<?> dynamic) {
		return (P)this.deserializer.apply(dynamic);
	}

	public P random(Random random) {
		return (P)this.randomProvider.apply(random);
	}
}
