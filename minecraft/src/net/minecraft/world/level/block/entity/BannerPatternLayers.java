package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;

public record BannerPatternLayers(List<BannerPatternLayers.Layer> layers) {
	public static final BannerPatternLayers EMPTY = new BannerPatternLayers(List.of());
	public static final Codec<BannerPatternLayers> CODEC = BannerPatternLayers.Layer.CODEC.listOf().xmap(BannerPatternLayers::new, BannerPatternLayers::layers);
	public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers> STREAM_CODEC = BannerPatternLayers.Layer.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(BannerPatternLayers::new, BannerPatternLayers::layers);

	public BannerPatternLayers withBase(DyeColor dyeColor) {
		return new BannerPatternLayers.Builder().add(BannerPatterns.BASE, dyeColor).addAll(this).build();
	}

	public BannerPatternLayers removeLast() {
		return new BannerPatternLayers(List.copyOf(this.layers.subList(0, this.layers.size() - 1)));
	}

	public static class Builder {
		private final ImmutableList.Builder<BannerPatternLayers.Layer> layers = ImmutableList.builder();

		public BannerPatternLayers.Builder add(ResourceKey<BannerPattern> resourceKey, DyeColor dyeColor) {
			return this.add(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(resourceKey), dyeColor);
		}

		public BannerPatternLayers.Builder add(Holder<BannerPattern> holder, DyeColor dyeColor) {
			return this.add(new BannerPatternLayers.Layer(holder, dyeColor));
		}

		public BannerPatternLayers.Builder add(BannerPatternLayers.Layer layer) {
			this.layers.add(layer);
			return this;
		}

		public BannerPatternLayers.Builder addAll(BannerPatternLayers bannerPatternLayers) {
			this.layers.addAll(bannerPatternLayers.layers);
			return this;
		}

		public BannerPatternLayers build() {
			return new BannerPatternLayers(this.layers.build());
		}
	}

	public static record Layer(Holder<BannerPattern> pattern, DyeColor color) {
		public static final Codec<BannerPatternLayers.Layer> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.BANNER_PATTERN.holderByNameCodec().fieldOf("pattern").forGetter(BannerPatternLayers.Layer::pattern),
						DyeColor.CODEC.fieldOf("color").forGetter(BannerPatternLayers.Layer::color)
					)
					.apply(instance, BannerPatternLayers.Layer::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers.Layer> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.BANNER_PATTERN),
			BannerPatternLayers.Layer::pattern,
			DyeColor.STREAM_CODEC,
			BannerPatternLayers.Layer::color,
			BannerPatternLayers.Layer::new
		);
	}
}
