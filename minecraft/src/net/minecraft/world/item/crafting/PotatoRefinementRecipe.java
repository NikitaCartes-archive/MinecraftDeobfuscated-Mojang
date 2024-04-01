package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PotatoRefinementRecipe implements Recipe<Container> {
	private final RecipeType<?> type = RecipeType.POTATO_REFINEMENT;
	private final String group;
	private final CookingBookCategory category;
	final Ingredient ingredient;
	final Ingredient bottleIngredient;
	final ItemStack result;
	final float experience;
	protected final int refinementTime;

	public PotatoRefinementRecipe(Ingredient ingredient, Ingredient ingredient2, ItemStack itemStack, float f, int i) {
		this("", CookingBookCategory.MISC, ingredient, ingredient2, itemStack, f, i);
	}

	private PotatoRefinementRecipe(
		String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, Ingredient ingredient2, ItemStack itemStack, float f, int i
	) {
		this.group = string;
		this.category = cookingBookCategory;
		this.ingredient = ingredient;
		this.bottleIngredient = ingredient2;
		this.result = itemStack;
		this.experience = f;
		this.refinementTime = i;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.ingredient.test(container.getItem(0)) && this.bottleIngredient.test(container.getItem(2));
	}

	@Override
	public ItemStack assemble(Container container, HolderLookup.Provider provider) {
		return this.result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return this.result;
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.POTATO_REFINERY);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.POTATO_REFINEMENT_RECIPE;
	}

	@Override
	public RecipeType<?> getType() {
		return this.type;
	}

	public int getRefinementTime() {
		return this.refinementTime;
	}

	public static class Serializer implements RecipeSerializer<PotatoRefinementRecipe> {
		private static final Codec<PotatoRefinementRecipe> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Ingredient.CODEC.fieldOf("ingredient").forGetter(potatoRefinementRecipe -> potatoRefinementRecipe.ingredient),
						Ingredient.CODEC.fieldOf("bottle_ingredient").forGetter(potatoRefinementRecipe -> potatoRefinementRecipe.bottleIngredient),
						ItemStack.CODEC.fieldOf("result").forGetter(potatoRefinementRecipe -> potatoRefinementRecipe.result),
						Codec.FLOAT.fieldOf("experience").forGetter(potatoRefinementRecipe -> potatoRefinementRecipe.experience),
						Codec.INT.fieldOf("refinement_time").forGetter(potatoRefinementRecipe -> potatoRefinementRecipe.refinementTime)
					)
					.apply(instance, PotatoRefinementRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, PotatoRefinementRecipe> STREAM_CODEC = StreamCodec.of(
			PotatoRefinementRecipe.Serializer::toNetwork, PotatoRefinementRecipe.Serializer::fromNetwork
		);

		@Override
		public Codec<PotatoRefinementRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, PotatoRefinementRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static PotatoRefinementRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
			Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
			ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
			float f = registryFriendlyByteBuf.readFloat();
			int i = registryFriendlyByteBuf.readInt();
			return new PotatoRefinementRecipe(ingredient, ingredient2, itemStack, f, i);
		}

		private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, PotatoRefinementRecipe potatoRefinementRecipe) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, potatoRefinementRecipe.ingredient);
			Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, potatoRefinementRecipe.bottleIngredient);
			ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, potatoRefinementRecipe.result);
			registryFriendlyByteBuf.writeFloat(potatoRefinementRecipe.experience);
			registryFriendlyByteBuf.writeInt(potatoRefinementRecipe.refinementTime);
		}
	}
}
