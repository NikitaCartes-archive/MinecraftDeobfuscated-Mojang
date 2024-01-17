package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
	private final SimpleCraftingRecipeSerializer.Factory<T> constructor;
	private final Codec<T> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

	public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> factory) {
		this.constructor = factory;
		this.codec = RecordCodecBuilder.create(
			instance -> instance.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category))
					.apply(instance, factory::create)
		);
		this.streamCodec = StreamCodec.composite(CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category, factory::create);
	}

	@Override
	public Codec<T> codec() {
		return this.codec;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return this.streamCodec;
	}

	@FunctionalInterface
	public interface Factory<T extends CraftingRecipe> {
		T create(CraftingBookCategory craftingBookCategory);
	}
}
