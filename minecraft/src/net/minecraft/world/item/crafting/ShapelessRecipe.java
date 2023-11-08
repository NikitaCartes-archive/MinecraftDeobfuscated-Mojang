package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
	final String group;
	final CraftingBookCategory category;
	final ItemStack result;
	final NonNullList<Ingredient> ingredients;

	public ShapelessRecipe(String string, CraftingBookCategory craftingBookCategory, ItemStack itemStack, NonNullList<Ingredient> nonNullList) {
		this.group = string;
		this.category = craftingBookCategory;
		this.result = itemStack;
		this.ingredients = nonNullList;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SHAPELESS_RECIPE;
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
		return this.ingredients;
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		StackedContents stackedContents = new StackedContents();
		int i = 0;

		for (int j = 0; j < craftingContainer.getContainerSize(); j++) {
			ItemStack itemStack = craftingContainer.getItem(j);
			if (!itemStack.isEmpty()) {
				i++;
				stackedContents.accountStack(itemStack, 1);
			}
		}

		return i == this.ingredients.size() && stackedContents.canCraft(this, null);
	}

	public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		return this.result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= this.ingredients.size();
	}

	public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
		private static final Codec<ShapelessRecipe> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(shapelessRecipe -> shapelessRecipe.group),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(shapelessRecipe -> shapelessRecipe.category),
						ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(shapelessRecipe -> shapelessRecipe.result),
						Ingredient.CODEC_NONEMPTY
							.listOf()
							.fieldOf("ingredients")
							.flatXmap(
								list -> {
									Ingredient[] ingredients = (Ingredient[])list.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
									if (ingredients.length == 0) {
										return DataResult.error(() -> "No ingredients for shapeless recipe");
									} else {
										return ingredients.length > 9
											? DataResult.error(() -> "Too many ingredients for shapeless recipe")
											: DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
									}
								},
								DataResult::success
							)
							.forGetter(shapelessRecipe -> shapelessRecipe.ingredients)
					)
					.apply(instance, ShapelessRecipe::new)
		);

		@Override
		public Codec<ShapelessRecipe> codec() {
			return CODEC;
		}

		public ShapelessRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			String string = friendlyByteBuf.readUtf();
			CraftingBookCategory craftingBookCategory = friendlyByteBuf.readEnum(CraftingBookCategory.class);
			int i = friendlyByteBuf.readVarInt();
			NonNullList<Ingredient> nonNullList = NonNullList.withSize(i, Ingredient.EMPTY);

			for (int j = 0; j < nonNullList.size(); j++) {
				nonNullList.set(j, Ingredient.fromNetwork(friendlyByteBuf));
			}

			ItemStack itemStack = friendlyByteBuf.readItem();
			return new ShapelessRecipe(string, craftingBookCategory, itemStack, nonNullList);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, ShapelessRecipe shapelessRecipe) {
			friendlyByteBuf.writeUtf(shapelessRecipe.group);
			friendlyByteBuf.writeEnum(shapelessRecipe.category);
			friendlyByteBuf.writeVarInt(shapelessRecipe.ingredients.size());

			for (Ingredient ingredient : shapelessRecipe.ingredients) {
				ingredient.toNetwork(friendlyByteBuf);
			}

			friendlyByteBuf.writeItem(shapelessRecipe.result);
		}
	}
}
