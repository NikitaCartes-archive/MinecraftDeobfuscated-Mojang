package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;

public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
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
