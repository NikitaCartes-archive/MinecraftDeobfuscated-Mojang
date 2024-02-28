package net.minecraft.world.item.armortrim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record TrimPattern(ResourceLocation assetId, Holder<Item> templateItem, Component description, boolean decal) {
	public static final Codec<TrimPattern> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("asset_id").forGetter(TrimPattern::assetId),
					RegistryFixedCodec.create(Registries.ITEM).fieldOf("template_item").forGetter(TrimPattern::templateItem),
					ComponentSerialization.CODEC.fieldOf("description").forGetter(TrimPattern::description),
					Codec.BOOL.fieldOf("decal").orElse(false).forGetter(TrimPattern::decal)
				)
				.apply(instance, TrimPattern::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, TrimPattern> DIRECT_STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC,
		TrimPattern::assetId,
		ByteBufCodecs.holderRegistry(Registries.ITEM),
		TrimPattern::templateItem,
		ComponentSerialization.STREAM_CODEC,
		TrimPattern::description,
		ByteBufCodecs.BOOL,
		TrimPattern::decal,
		TrimPattern::new
	);
	public static final Codec<Holder<TrimPattern>> CODEC = RegistryFileCodec.create(Registries.TRIM_PATTERN, DIRECT_CODEC);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<TrimPattern>> STREAM_CODEC = ByteBufCodecs.holder(Registries.TRIM_PATTERN, DIRECT_STREAM_CODEC);

	public Component copyWithStyle(Holder<TrimMaterial> holder) {
		return this.description.copy().withStyle(holder.value().description().getStyle());
	}
}
