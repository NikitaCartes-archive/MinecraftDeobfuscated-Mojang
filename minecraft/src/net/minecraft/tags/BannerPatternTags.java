package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternTags {
	public static final TagKey<BannerPattern> NO_ITEM_REQUIRED = create("no_item_required");
	public static final TagKey<BannerPattern> PATTERN_ITEM_FLOWER = create("pattern_item/flower");
	public static final TagKey<BannerPattern> PATTERN_ITEM_CREEPER = create("pattern_item/creeper");
	public static final TagKey<BannerPattern> PATTERN_ITEM_SKULL = create("pattern_item/skull");
	public static final TagKey<BannerPattern> PATTERN_ITEM_MOJANG = create("pattern_item/mojang");
	public static final TagKey<BannerPattern> PATTERN_ITEM_GLOBE = create("pattern_item/globe");
	public static final TagKey<BannerPattern> PATTERN_ITEM_PIGLIN = create("pattern_item/piglin");
	public static final TagKey<BannerPattern> PATTERN_ITEM_FLOW = create("pattern_item/flow");
	public static final TagKey<BannerPattern> PATTERN_ITEM_GUSTER = create("pattern_item/guster");
	public static final TagKey<BannerPattern> PATTERN_ITEM_FIELD_MASONED = create("pattern_item/field_masoned");
	public static final TagKey<BannerPattern> PATTERN_ITEM_BORDURE_INDENTED = create("pattern_item/bordure_indented");

	private BannerPatternTags() {
	}

	private static TagKey<BannerPattern> create(String string) {
		return TagKey.create(Registries.BANNER_PATTERN, ResourceLocation.withDefaultNamespace(string));
	}
}
