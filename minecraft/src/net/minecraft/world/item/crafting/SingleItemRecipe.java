package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class SingleItemRecipe implements Recipe<Container> {
	protected final Ingredient ingredient;
	protected final ItemStack result;
	private final RecipeType<?> type;
	private final RecipeSerializer<?> serializer;
	protected final String group;

	public SingleItemRecipe(RecipeType<?> recipeType, RecipeSerializer<?> recipeSerializer, String string, Ingredient ingredient, ItemStack itemStack) {
		this.type = recipeType;
		this.serializer = recipeSerializer;
		this.group = string;
		this.ingredient = ingredient;
		this.result = itemStack;
	}

	@Override
	public RecipeType<?> getType() {
		return this.type;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return this.serializer;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return this.result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nonNullList = NonNullList.create();
		nonNullList.add(this.ingredient);
		return nonNullList;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return true;
	}

	@Override
	public ItemStack assemble(Container container, HolderLookup.Provider provider) {
		return this.result.copy();
	}

	public interface Factory<T extends SingleItemRecipe> {
		T create(String string, Ingredient ingredient, ItemStack itemStack);
	}

	public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
		final SingleItemRecipe.Factory<T> factory;
		private final MapCodec<T> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

		protected Serializer(SingleItemRecipe.Factory<T> factory) {
			this.factory = factory;
			this.codec = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
							Codec.STRING.optionalFieldOf("group", "").forGetter(singleItemRecipe -> singleItemRecipe.group),
							Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(singleItemRecipe -> singleItemRecipe.ingredient),
							ItemStack.CODEC.fieldOf("result").forGetter(singleItemRecipe -> singleItemRecipe.result)
						)
						.apply(instance, factory::create)
			);
			this.streamCodec = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8,
				singleItemRecipe -> singleItemRecipe.group,
				Ingredient.CONTENTS_STREAM_CODEC,
				singleItemRecipe -> singleItemRecipe.ingredient,
				ItemStack.STREAM_CODEC,
				singleItemRecipe -> singleItemRecipe.result,
				factory::create
			);
		}

		@Override
		public MapCodec<T> codec() {
			return this.codec;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
			return this.streamCodec;
		}
	}
}
