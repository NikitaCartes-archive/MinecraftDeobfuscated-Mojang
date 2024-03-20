package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record MapDecorationType(ResourceLocation assetId, boolean showOnItemFrame, int mapColor, boolean explorationMapElement, boolean trackCount) {
	public static final int NO_MAP_COLOR = -1;
	public static final Codec<Holder<MapDecorationType>> CODEC = BuiltInRegistries.MAP_DECORATION_TYPE.holderByNameCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MapDecorationType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.MAP_DECORATION_TYPE);

	public boolean hasMapColor() {
		return this.mapColor != -1;
	}
}
