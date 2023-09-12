package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
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
	public ItemStack getResultItem(RegistryAccess registryAccess) {
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
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		return this.result.copy();
	}

	public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
		private static final MapCodec<ItemStack> RESULT_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(ItemStack::getItem), Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)
					)
					.apply(instance, ItemStack::new)
		);
		final SingleItemRecipe.Serializer.SingleItemMaker<T> factory;
		private final Codec<T> codec;

		protected Serializer(SingleItemRecipe.Serializer.SingleItemMaker<T> singleItemMaker) {
			this.factory = singleItemMaker;
			this.codec = RecordCodecBuilder.create(
				instance -> instance.group(
							ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(singleItemRecipe -> singleItemRecipe.group),
							Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(singleItemRecipe -> singleItemRecipe.ingredient),
							RESULT_CODEC.forGetter(singleItemRecipe -> singleItemRecipe.result)
						)
						.apply(instance, singleItemMaker::create)
			);
		}

		@Override
		public Codec<T> codec() {
			return this.codec;
		}

		public T fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			String string = friendlyByteBuf.readUtf();
			Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack itemStack = friendlyByteBuf.readItem();
			return this.factory.create(string, ingredient, itemStack);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, T singleItemRecipe) {
			friendlyByteBuf.writeUtf(singleItemRecipe.group);
			singleItemRecipe.ingredient.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(singleItemRecipe.result);
		}

		interface SingleItemMaker<T extends SingleItemRecipe> {
			T create(String string, Ingredient ingredient, ItemStack itemStack);
		}
	}
}
