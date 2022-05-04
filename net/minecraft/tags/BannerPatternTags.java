/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternTags {
    public static final TagKey<BannerPattern> NO_ITEM_REQUIRED = BannerPatternTags.create("no_item_required");
    public static final TagKey<BannerPattern> PATTERN_ITEM_FLOWER = BannerPatternTags.create("pattern_item/flower");
    public static final TagKey<BannerPattern> PATTERN_ITEM_CREEPER = BannerPatternTags.create("pattern_item/creeper");
    public static final TagKey<BannerPattern> PATTERN_ITEM_SKULL = BannerPatternTags.create("pattern_item/skull");
    public static final TagKey<BannerPattern> PATTERN_ITEM_MOJANG = BannerPatternTags.create("pattern_item/mojang");
    public static final TagKey<BannerPattern> PATTERN_ITEM_GLOBE = BannerPatternTags.create("pattern_item/globe");
    public static final TagKey<BannerPattern> PATTERN_ITEM_PIGLIN = BannerPatternTags.create("pattern_item/piglin");

    private BannerPatternTags() {
    }

    private static TagKey<BannerPattern> create(String string) {
        return TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(string));
    }
}

