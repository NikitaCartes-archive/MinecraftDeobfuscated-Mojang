package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
	final String group;
	final CraftingBookCategory category;
	final ItemStack result;
	final List<Ingredient> ingredients;
	@Nullable
	private PlacementInfo placementInfo;

	public ShapelessRecipe(String string, CraftingBookCategory craftingBookCategory, ItemStack itemStack, List<Ingredient> list) {
		this.group = string;
		this.category = craftingBookCategory;
		this.result = itemStack;
		this.ingredients = list;
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
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return this.result;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.create(this.ingredients);
		}

		return this.placementInfo;
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		if (craftingInput.ingredientCount() != this.ingredients.size()) {
			return false;
		} else {
			return craftingInput.size() == 1 && this.ingredients.size() == 1
				? ((Ingredient)this.ingredients.getFirst()).test(craftingInput.getItem(0))
				: craftingInput.stackedContents().canCraft(this, null);
		}
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		return this.result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= this.ingredients.size();
	}

	public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
		private static final MapCodec<ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(shapelessRecipe -> shapelessRecipe.group),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(shapelessRecipe -> shapelessRecipe.category),
						ItemStack.STRICT_CODEC.fieldOf("result").forGetter(shapelessRecipe -> shapelessRecipe.result),
						Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(shapelessRecipe -> shapelessRecipe.ingredients)
					)
					.apply(instance, ShapelessRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			shapelessRecipe -> shapelessRecipe.group,
			CraftingBookCategory.STREAM_CODEC,
			shapelessRecipe -> shapelessRecipe.category,
			ItemStack.STREAM_CODEC,
			shapelessRecipe -> shapelessRecipe.result,
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
			shapelessRecipe -> shapelessRecipe.ingredients,
			ShapelessRecipe::new
		);

		@Override
		public MapCodec<ShapelessRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
