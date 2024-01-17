package net.minecraft.advancements;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record AdvancementHolder(ResourceLocation id, Advancement value) {
	public static final StreamCodec<RegistryFriendlyByteBuf, AdvancementHolder> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, AdvancementHolder::id, Advancement.STREAM_CODEC, AdvancementHolder::value, AdvancementHolder::new
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, List<AdvancementHolder>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof AdvancementHolder advancementHolder && this.id.equals(advancementHolder.id)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public String toString() {
		return this.id.toString();
	}
}
