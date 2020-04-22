package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class FoliagePlacerType<P extends FoliagePlacer> {
	public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = register("blob_foliage_placer", BlobFoliagePlacer::new);
	public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = register("spruce_foliage_placer", SpruceFoliagePlacer::new);
	public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = register("pine_foliage_placer", PineFoliagePlacer::new);
	public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = register("acacia_foliage_placer", AcaciaFoliagePlacer::new);
	public static final FoliagePlacerType<BushFoliagePlacer> BUSH_FOLIAGE_PLACER = register("bush_foliage_placer", BushFoliagePlacer::new);
	public static final FoliagePlacerType<FancyFoliagePlacer> FANCY_FOLIAGE_PLACER = register("fancy_foliage_placer", FancyFoliagePlacer::new);
	public static final FoliagePlacerType<MegaJungleFoliagePlacer> MEGA_JUNGLE_FOLIAGE_PLACER = register("jungle_foliage_placer", MegaJungleFoliagePlacer::new);
	public static final FoliagePlacerType<MegaPineFoliagePlacer> MEGA_PINE_FOLIAGE_PLACER = register("mega_pine_foliage_placer", MegaPineFoliagePlacer::new);
	public static final FoliagePlacerType<DarkOakFoliagePlacer> DARK_OAK_FOLIAGE_PLACER = register("dark_oak_foliage_placer", DarkOakFoliagePlacer::new);
	private final Function<Dynamic<?>, P> deserializer;

	private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String string, Function<Dynamic<?>, P> function) {
		return Registry.register(Registry.FOLIAGE_PLACER_TYPES, string, new FoliagePlacerType<>(function));
	}

	private FoliagePlacerType(Function<Dynamic<?>, P> function) {
		this.deserializer = function;
	}

	public P deserialize(Dynamic<?> dynamic) {
		return (P)this.deserializer.apply(dynamic);
	}
}
