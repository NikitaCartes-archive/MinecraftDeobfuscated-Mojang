package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.function.LongFunction;
import net.minecraft.Util;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public class Layers implements LayerBiomes {
	protected static final int WARM_ID = 1;
	protected static final int MEDIUM_ID = 2;
	protected static final int COLD_ID = 3;
	protected static final int ICE_ID = 4;
	protected static final int SPECIAL_MASK = 3840;
	protected static final int SPECIAL_SHIFT = 8;
	private static final Int2IntMap CATEGORIES = Util.make(new Int2IntOpenHashMap(), int2IntOpenHashMap -> {
		register(int2IntOpenHashMap, Layers.Category.BEACH, 16);
		register(int2IntOpenHashMap, Layers.Category.BEACH, 26);
		register(int2IntOpenHashMap, Layers.Category.DESERT, 2);
		register(int2IntOpenHashMap, Layers.Category.DESERT, 17);
		register(int2IntOpenHashMap, Layers.Category.DESERT, 130);
		register(int2IntOpenHashMap, Layers.Category.EXTREME_HILLS, 131);
		register(int2IntOpenHashMap, Layers.Category.EXTREME_HILLS, 162);
		register(int2IntOpenHashMap, Layers.Category.EXTREME_HILLS, 20);
		register(int2IntOpenHashMap, Layers.Category.EXTREME_HILLS, 3);
		register(int2IntOpenHashMap, Layers.Category.EXTREME_HILLS, 34);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 27);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 28);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 29);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 157);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 132);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 4);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 155);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 156);
		register(int2IntOpenHashMap, Layers.Category.FOREST, 18);
		register(int2IntOpenHashMap, Layers.Category.ICY, 140);
		register(int2IntOpenHashMap, Layers.Category.ICY, 13);
		register(int2IntOpenHashMap, Layers.Category.ICY, 12);
		register(int2IntOpenHashMap, Layers.Category.JUNGLE, 168);
		register(int2IntOpenHashMap, Layers.Category.JUNGLE, 169);
		register(int2IntOpenHashMap, Layers.Category.JUNGLE, 21);
		register(int2IntOpenHashMap, Layers.Category.JUNGLE, 23);
		register(int2IntOpenHashMap, Layers.Category.JUNGLE, 22);
		register(int2IntOpenHashMap, Layers.Category.JUNGLE, 149);
		register(int2IntOpenHashMap, Layers.Category.JUNGLE, 151);
		register(int2IntOpenHashMap, Layers.Category.MESA, 37);
		register(int2IntOpenHashMap, Layers.Category.MESA, 165);
		register(int2IntOpenHashMap, Layers.Category.MESA, 167);
		register(int2IntOpenHashMap, Layers.Category.MESA, 166);
		register(int2IntOpenHashMap, Layers.Category.BADLANDS_PLATEAU, 39);
		register(int2IntOpenHashMap, Layers.Category.BADLANDS_PLATEAU, 38);
		register(int2IntOpenHashMap, Layers.Category.MUSHROOM, 14);
		register(int2IntOpenHashMap, Layers.Category.MUSHROOM, 15);
		register(int2IntOpenHashMap, Layers.Category.NONE, 25);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 46);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 49);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 50);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 48);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 24);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 47);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 10);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 45);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 0);
		register(int2IntOpenHashMap, Layers.Category.OCEAN, 44);
		register(int2IntOpenHashMap, Layers.Category.PLAINS, 1);
		register(int2IntOpenHashMap, Layers.Category.PLAINS, 129);
		register(int2IntOpenHashMap, Layers.Category.RIVER, 11);
		register(int2IntOpenHashMap, Layers.Category.RIVER, 7);
		register(int2IntOpenHashMap, Layers.Category.SAVANNA, 35);
		register(int2IntOpenHashMap, Layers.Category.SAVANNA, 36);
		register(int2IntOpenHashMap, Layers.Category.SAVANNA, 163);
		register(int2IntOpenHashMap, Layers.Category.SAVANNA, 164);
		register(int2IntOpenHashMap, Layers.Category.SWAMP, 6);
		register(int2IntOpenHashMap, Layers.Category.SWAMP, 134);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 160);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 161);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 32);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 33);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 30);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 31);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 158);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 5);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 19);
		register(int2IntOpenHashMap, Layers.Category.TAIGA, 133);
	});

	private static <T extends Area, C extends BigContext<T>> AreaFactory<T> zoom(
		long l, AreaTransformer1 areaTransformer1, AreaFactory<T> areaFactory, int i, LongFunction<C> longFunction
	) {
		AreaFactory<T> areaFactory2 = areaFactory;

		for (int j = 0; j < i; j++) {
			areaFactory2 = areaTransformer1.run((BigContext<T>)longFunction.apply(l + (long)j), areaFactory2);
		}

		return areaFactory2;
	}

	private static <T extends Area, C extends BigContext<T>> AreaFactory<T> getDefaultLayer(boolean bl, int i, int j, LongFunction<C> longFunction) {
		AreaFactory<T> areaFactory = IslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(1L));
		areaFactory = ZoomLayer.FUZZY.run((BigContext<T>)longFunction.apply(2000L), areaFactory);
		areaFactory = AddIslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(1L), areaFactory);
		areaFactory = ZoomLayer.NORMAL.run((BigContext<T>)longFunction.apply(2001L), areaFactory);
		areaFactory = AddIslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(2L), areaFactory);
		areaFactory = AddIslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(50L), areaFactory);
		areaFactory = AddIslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(70L), areaFactory);
		areaFactory = RemoveTooMuchOceanLayer.INSTANCE.run((BigContext<T>)longFunction.apply(2L), areaFactory);
		AreaFactory<T> areaFactory2 = OceanLayer.INSTANCE.run((BigContext<T>)longFunction.apply(2L));
		areaFactory2 = zoom(2001L, ZoomLayer.NORMAL, areaFactory2, 6, longFunction);
		areaFactory = AddSnowLayer.INSTANCE.run((BigContext<T>)longFunction.apply(2L), areaFactory);
		areaFactory = AddIslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(3L), areaFactory);
		areaFactory = AddEdgeLayer.CoolWarm.INSTANCE.run((BigContext<T>)longFunction.apply(2L), areaFactory);
		areaFactory = AddEdgeLayer.HeatIce.INSTANCE.run((BigContext<T>)longFunction.apply(2L), areaFactory);
		areaFactory = AddEdgeLayer.IntroduceSpecial.INSTANCE.run((BigContext<T>)longFunction.apply(3L), areaFactory);
		areaFactory = ZoomLayer.NORMAL.run((BigContext<T>)longFunction.apply(2002L), areaFactory);
		areaFactory = ZoomLayer.NORMAL.run((BigContext<T>)longFunction.apply(2003L), areaFactory);
		areaFactory = AddIslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(4L), areaFactory);
		areaFactory = AddMushroomIslandLayer.INSTANCE.run((BigContext<T>)longFunction.apply(5L), areaFactory);
		areaFactory = AddDeepOceanLayer.INSTANCE.run((BigContext<T>)longFunction.apply(4L), areaFactory);
		areaFactory = zoom(1000L, ZoomLayer.NORMAL, areaFactory, 0, longFunction);
		AreaFactory<T> areaFactory3 = zoom(1000L, ZoomLayer.NORMAL, areaFactory, 0, longFunction);
		areaFactory3 = RiverInitLayer.INSTANCE.run((BigContext)longFunction.apply(100L), areaFactory3);
		AreaFactory<T> areaFactory4 = new BiomeInitLayer(bl).run((BigContext<T>)longFunction.apply(200L), areaFactory);
		areaFactory4 = RareBiomeLargeLayer.INSTANCE.run((BigContext)longFunction.apply(1001L), areaFactory4);
		areaFactory4 = zoom(1000L, ZoomLayer.NORMAL, areaFactory4, 2, longFunction);
		areaFactory4 = BiomeEdgeLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4);
		AreaFactory<T> areaFactory5 = zoom(1000L, ZoomLayer.NORMAL, areaFactory3, 2, longFunction);
		areaFactory4 = RegionHillsLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4, areaFactory5);
		areaFactory3 = zoom(1000L, ZoomLayer.NORMAL, areaFactory3, 2, longFunction);
		areaFactory3 = zoom(1000L, ZoomLayer.NORMAL, areaFactory3, j, longFunction);
		areaFactory3 = RiverLayer.INSTANCE.run((BigContext)longFunction.apply(1L), areaFactory3);
		areaFactory3 = SmoothLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory3);
		areaFactory4 = RareBiomeSpotLayer.INSTANCE.run((BigContext)longFunction.apply(1001L), areaFactory4);

		for (int k = 0; k < i; k++) {
			areaFactory4 = ZoomLayer.NORMAL.run((BigContext)longFunction.apply((long)(1000 + k)), areaFactory4);
			if (k == 0) {
				areaFactory4 = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(3L), areaFactory4);
			}

			if (k == 1 || i == 1) {
				areaFactory4 = ShoreLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4);
			}
		}

		areaFactory4 = SmoothLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4);
		areaFactory4 = RiverMixerLayer.INSTANCE.run((BigContext)longFunction.apply(100L), areaFactory4, areaFactory3);
		return OceanMixerLayer.INSTANCE.run((BigContext<T>)longFunction.apply(100L), areaFactory4, areaFactory2);
	}

	public static Layer getDefaultLayer(long l, boolean bl, int i, int j) {
		int k = 25;
		AreaFactory<LazyArea> areaFactory = getDefaultLayer(bl, i, j, m -> new LazyAreaContext(25, l, m));
		return new Layer(areaFactory);
	}

	public static boolean isSame(int i, int j) {
		return i == j ? true : CATEGORIES.get(i) == CATEGORIES.get(j);
	}

	private static void register(Int2IntOpenHashMap int2IntOpenHashMap, Layers.Category category, int i) {
		int2IntOpenHashMap.put(i, category.ordinal());
	}

	protected static boolean isOcean(int i) {
		return i == 44 || i == 45 || i == 0 || i == 46 || i == 10 || i == 47 || i == 48 || i == 24 || i == 49 || i == 50;
	}

	protected static boolean isShallowOcean(int i) {
		return i == 44 || i == 45 || i == 0 || i == 46 || i == 10;
	}

	static enum Category {
		NONE,
		TAIGA,
		EXTREME_HILLS,
		JUNGLE,
		MESA,
		BADLANDS_PLATEAU,
		PLAINS,
		SAVANNA,
		ICY,
		BEACH,
		FOREST,
		OCEAN,
		DESERT,
		RIVER,
		SWAMP,
		MUSHROOM;
	}
}
