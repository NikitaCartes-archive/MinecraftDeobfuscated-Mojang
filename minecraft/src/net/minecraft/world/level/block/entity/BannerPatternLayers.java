package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import org.slf4j.Logger;

public record BannerPatternLayers(List<BannerPatternLayers.Layer> layers) {
	static final Logger LOGGER = LogUtils.getLogger();
	public static final BannerPatternLayers EMPTY = new BannerPatternLayers(List.of());
	public static final Codec<BannerPatternLayers> CODEC = BannerPatternLayers.Layer.CODEC.listOf().xmap(BannerPatternLayers::new, BannerPatternLayers::layers);
	public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers> STREAM_CODEC = BannerPatternLayers.Layer.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(BannerPatternLayers::new, BannerPatternLayers::layers);

	public BannerPatternLayers removeLast() {
		return new BannerPatternLayers(List.copyOf(this.layers.subList(0, this.layers.size() - 1)));
	}

	public static class Builder {
		private final ImmutableList.Builder<BannerPatternLayers.Layer> layers = ImmutableList.builder();

		@Deprecated
		public BannerPatternLayers.Builder addIfRegistered(HolderGetter<BannerPattern> holderGetter, ResourceKey<BannerPattern> resourceKey, DyeColor dyeColor) {
			Optional<Holder.Reference<BannerPattern>> optional = holderGetter.get(resourceKey);
			if (optional.isEmpty()) {
				BannerPatternLayers.LOGGER.warn("Unable to find banner pattern with id: '{}'", resourceKey.location());
				return this;
			} else {
				return this.add((Holder<BannerPattern>)optional.get(), dyeColor);
			}
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
						BannerPattern.CODEC.fieldOf("pattern").forGetter(BannerPatternLayers.Layer::pattern),
						DyeColor.CODEC.fieldOf("color").forGetter(BannerPatternLayers.Layer::color)
					)
					.apply(instance, BannerPatternLayers.Layer::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers.Layer> STREAM_CODEC = StreamCodec.composite(
			BannerPattern.STREAM_CODEC, BannerPatternLayers.Layer::pattern, DyeColor.STREAM_CODEC, BannerPatternLayers.Layer::color, BannerPatternLayers.Layer::new
		);

		public MutableComponent description() {
			String string = this.pattern.value().translationKey();
			return Component.translatable(string + "." + this.color.getName());
		}
	}
}
