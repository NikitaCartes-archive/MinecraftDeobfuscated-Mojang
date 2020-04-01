package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;

public class VillageConfiguration implements FeatureConfiguration {
	private static final List<String> STARTS = ImmutableList.of(
		"village/plains/town_centers", "village/desert/town_centers", "village/savanna/town_centers", "village/snowy/town_centers", "village/taiga/town_centers"
	);
	public final ResourceLocation startPool;
	public final int size;

	public VillageConfiguration(String string, int i) {
		this.startPool = new ResourceLocation(string);
		this.size = i;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("start_pool"),
					dynamicOps.createString(this.startPool.toString()),
					dynamicOps.createString("size"),
					dynamicOps.createInt(this.size)
				)
			)
		);
	}

	public static <T> VillageConfiguration deserialize(Dynamic<T> dynamic) {
		String string = dynamic.get("start_pool").asString("");
		int i = dynamic.get("size").asInt(6);
		return new VillageConfiguration(string, i);
	}

	public static VillageConfiguration random(Random random) {
		return new VillageConfiguration((String)STARTS.get(random.nextInt(STARTS.size())), random.nextInt(10));
	}
}
