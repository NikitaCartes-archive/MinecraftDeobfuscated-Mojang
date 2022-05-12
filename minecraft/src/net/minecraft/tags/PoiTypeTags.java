package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public class PoiTypeTags {
	public static final TagKey<PoiType> ACQUIRABLE_JOB_SITE = create("acquirable_job_site");
	public static final TagKey<PoiType> VILLAGE = create("village");
	public static final TagKey<PoiType> BEE_HOME = create("bee_home");

	private PoiTypeTags() {
	}

	private static TagKey<PoiType> create(String string) {
		return TagKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, new ResourceLocation(string));
	}
}
