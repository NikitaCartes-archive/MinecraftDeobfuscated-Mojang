package net.minecraft.data.loot.packs;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public class UpdateOneTwentyBuiltInLootTables {
	private static final Set<ResourceLocation> LOCATIONS = Sets.<ResourceLocation>newHashSet();
	private static final Set<ResourceLocation> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);
	public static final ResourceLocation DESERT_WELL_ARCHAEOLOGY = register("archaeology/desert_well");
	public static final ResourceLocation DESERT_PYRAMID_ARCHAEOLOGY = register("archaeology/desert_pyramid");

	private static ResourceLocation register(String string) {
		return register(new ResourceLocation(string));
	}

	private static ResourceLocation register(ResourceLocation resourceLocation) {
		if (LOCATIONS.add(resourceLocation)) {
			return resourceLocation;
		} else {
			throw new IllegalArgumentException(resourceLocation + " is already a registered built-in loot table");
		}
	}

	public static Set<ResourceLocation> all() {
		return IMMUTABLE_LOCATIONS;
	}
}
