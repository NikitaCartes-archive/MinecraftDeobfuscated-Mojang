package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternItem extends Item {
	private final TagKey<BannerPattern> bannerPattern;

	public BannerPatternItem(TagKey<BannerPattern> tagKey, Item.Properties properties) {
		super(properties);
		this.bannerPattern = tagKey;
	}

	public TagKey<BannerPattern> getBannerPattern() {
		return this.bannerPattern;
	}
}
