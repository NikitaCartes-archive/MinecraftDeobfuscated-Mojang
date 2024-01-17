package net.minecraft.world.item.crafting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
	public static final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<?>> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, RecipeHolder::id, Recipe.STREAM_CODEC, RecipeHolder::value, RecipeHolder::new
	);

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof RecipeHolder<?> recipeHolder && this.id.equals(recipeHolder.id)) {
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
