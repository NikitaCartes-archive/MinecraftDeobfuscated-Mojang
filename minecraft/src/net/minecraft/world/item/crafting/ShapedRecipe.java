package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe {
	final ShapedRecipePattern pattern;
	final ItemStack result;
	final String group;
	final CraftingBookCategory category;
	final boolean showNotification;

	public ShapedRecipe(String string, CraftingBookCategory craftingBookCategory, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack, boolean bl) {
		this.group = string;
		this.category = craftingBookCategory;
		this.pattern = shapedRecipePattern;
		this.result = itemStack;
		this.showNotification = bl;
	}

	public ShapedRecipe(String string, CraftingBookCategory craftingBookCategory, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack) {
		this(string, craftingBookCategory, shapedRecipePattern, itemStack, true);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SHAPED_RECIPE;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public CraftingBookCategory category() {
		return this.category;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return this.result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.pattern.ingredients();
	}

	@Override
	public boolean showNotification() {
		return this.showNotification;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i >= this.pattern.width() && j >= this.pattern.height();
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		return this.pattern.matches(craftingInput);
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		return this.getResultItem(provider).copy();
	}

	public int getWidth() {
		return this.pattern.width();
	}

	public int getHeight() {
		return this.pattern.height();
	}

	@Override
	public boolean isIncomplete() {
		NonNullList<Ingredient> nonNullList = this.getIngredients();
		return nonNullList.isEmpty() || nonNullList.stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.getItems().length == 0);
	}

	public static class Serializer implements RecipeSerializer<ShapedRecipe> {
		public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(shapedRecipe -> shapedRecipe.group),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(shapedRecipe -> shapedRecipe.category),
						ShapedRecipePattern.MAP_CODEC.forGetter(shapedRecipe -> shapedRecipe.pattern),
						ItemStack.STRICT_CODEC.fieldOf("result").forGetter(shapedRecipe -> shapedRecipe.result),
						Codec.BOOL.optionalFieldOf("show_notification", Boolean.valueOf(true)).forGetter(shapedRecipe -> shapedRecipe.showNotification)
					)
					.apply(instance, ShapedRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> STREAM_CODEC = StreamCodec.of(
			ShapedRecipe.Serializer::toNetwork, ShapedRecipe.Serializer::fromNetwork
		);

		@Override
		public MapCodec<ShapedRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static ShapedRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			String string = registryFriendlyByteBuf.readUtf();
			CraftingBookCategory craftingBookCategory = registryFriendlyByteBuf.readEnum(CraftingBookCategory.class);
			ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.STREAM_CODEC.decode(registryFriendlyByteBuf);
			ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
			boolean bl = registryFriendlyByteBuf.readBoolean();
			return new ShapedRecipe(string, craftingBookCategory, shapedRecipePattern, itemStack, bl);
		}

		private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, ShapedRecipe shapedRecipe) {
			registryFriendlyByteBuf.writeUtf(shapedRecipe.group);
			registryFriendlyByteBuf.writeEnum(shapedRecipe.category);
			ShapedRecipePattern.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.pattern);
			ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.result);
			registryFriendlyByteBuf.writeBoolean(shapedRecipe.showNotification);
		}
	}
}
