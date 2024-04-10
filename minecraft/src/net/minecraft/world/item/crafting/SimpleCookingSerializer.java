package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
	private final AbstractCookingRecipe.Factory<T> factory;
	private final MapCodec<T> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

	public SimpleCookingSerializer(AbstractCookingRecipe.Factory<T> factory, int i) {
		this.factory = factory;
		this.codec = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(abstractCookingRecipe -> abstractCookingRecipe.group),
						CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(abstractCookingRecipe -> abstractCookingRecipe.category),
						Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(abstractCookingRecipe -> abstractCookingRecipe.ingredient),
						ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(abstractCookingRecipe -> abstractCookingRecipe.result),
						Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(abstractCookingRecipe -> abstractCookingRecipe.experience),
						Codec.INT.fieldOf("cookingtime").orElse(i).forGetter(abstractCookingRecipe -> abstractCookingRecipe.cookingTime)
					)
					.apply(instance, factory::create)
		);
		this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
	}

	@Override
	public MapCodec<T> codec() {
		return this.codec;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return this.streamCodec;
	}

	private T fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		String string = registryFriendlyByteBuf.readUtf();
		CookingBookCategory cookingBookCategory = registryFriendlyByteBuf.readEnum(CookingBookCategory.class);
		Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
		ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
		float f = registryFriendlyByteBuf.readFloat();
		int i = registryFriendlyByteBuf.readVarInt();
		return this.factory.create(string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	private void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, T abstractCookingRecipe) {
		registryFriendlyByteBuf.writeUtf(abstractCookingRecipe.group);
		registryFriendlyByteBuf.writeEnum(abstractCookingRecipe.category());
		Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, abstractCookingRecipe.ingredient);
		ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, abstractCookingRecipe.result);
		registryFriendlyByteBuf.writeFloat(abstractCookingRecipe.experience);
		registryFriendlyByteBuf.writeVarInt(abstractCookingRecipe.cookingTime);
	}

	public AbstractCookingRecipe create(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		return this.factory.create(string, cookingBookCategory, ingredient, itemStack, f, i);
	}
}
