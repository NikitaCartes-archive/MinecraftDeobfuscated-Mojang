package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
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
	public ItemStack getResultItem(RegistryAccess registryAccess) {
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

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		return this.pattern.matches(craftingContainer);
	}

	public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		return this.getResultItem(registryAccess).copy();
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
		public static final Codec<ShapedRecipe> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(shapedRecipe -> shapedRecipe.group),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(shapedRecipe -> shapedRecipe.category),
						ShapedRecipePattern.MAP_CODEC.forGetter(shapedRecipe -> shapedRecipe.pattern),
						ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(shapedRecipe -> shapedRecipe.result),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(shapedRecipe -> shapedRecipe.showNotification)
					)
					.apply(instance, ShapedRecipe::new)
		);

		@Override
		public Codec<ShapedRecipe> codec() {
			return CODEC;
		}

		public ShapedRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			String string = friendlyByteBuf.readUtf();
			CraftingBookCategory craftingBookCategory = friendlyByteBuf.readEnum(CraftingBookCategory.class);
			ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.fromNetwork(friendlyByteBuf);
			ItemStack itemStack = friendlyByteBuf.readItem();
			boolean bl = friendlyByteBuf.readBoolean();
			return new ShapedRecipe(string, craftingBookCategory, shapedRecipePattern, itemStack, bl);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, ShapedRecipe shapedRecipe) {
			friendlyByteBuf.writeUtf(shapedRecipe.group);
			friendlyByteBuf.writeEnum(shapedRecipe.category);
			shapedRecipe.pattern.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(shapedRecipe.result);
			friendlyByteBuf.writeBoolean(shapedRecipe.showNotification);
		}
	}
}
