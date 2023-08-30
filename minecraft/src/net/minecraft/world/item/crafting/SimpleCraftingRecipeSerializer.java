package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
	private final SimpleCraftingRecipeSerializer.Factory<T> constructor;
	private final Codec<T> codec;

	public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> factory) {
		this.constructor = factory;
		this.codec = RecordCodecBuilder.create(
			instance -> instance.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category))
					.apply(instance, factory::create)
		);
	}

	@Override
	public Codec<T> codec() {
		return this.codec;
	}

	public T fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		CraftingBookCategory craftingBookCategory = friendlyByteBuf.readEnum(CraftingBookCategory.class);
		return this.constructor.create(craftingBookCategory);
	}

	public void toNetwork(FriendlyByteBuf friendlyByteBuf, T craftingRecipe) {
		friendlyByteBuf.writeEnum(craftingRecipe.category());
	}

	@FunctionalInterface
	public interface Factory<T extends CraftingRecipe> {
		T create(CraftingBookCategory craftingBookCategory);
	}
}
