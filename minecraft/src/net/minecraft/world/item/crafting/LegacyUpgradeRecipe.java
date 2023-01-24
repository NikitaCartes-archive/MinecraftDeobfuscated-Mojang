package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Deprecated(
	forRemoval = true
)
public class LegacyUpgradeRecipe implements SmithingRecipe {
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;
	private final ResourceLocation id;

	public LegacyUpgradeRecipe(ResourceLocation resourceLocation, Ingredient ingredient, Ingredient ingredient2, ItemStack itemStack) {
		this.id = resourceLocation;
		this.base = ingredient;
		this.addition = ingredient2;
		this.result = itemStack;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.base.test(container.getItem(0)) && this.addition.test(container.getItem(1));
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		ItemStack itemStack = this.result.copy();
		CompoundTag compoundTag = container.getItem(0).getTag();
		if (compoundTag != null) {
			itemStack.setTag(compoundTag.copy());
		}

		return itemStack;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return this.result;
	}

	@Override
	public boolean isTemplateIngredient(ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean isBaseIngredient(ItemStack itemStack) {
		return this.base.test(itemStack);
	}

	@Override
	public boolean isAdditionIngredient(ItemStack itemStack) {
		return this.addition.test(itemStack);
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMITHING;
	}

	@Override
	public boolean isIncomplete() {
		return Stream.of(this.base, this.addition).anyMatch(ingredient -> ingredient.getItems().length == 0);
	}

	public static class Serializer implements RecipeSerializer<LegacyUpgradeRecipe> {
		public LegacyUpgradeRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
			Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "base"));
			Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "addition"));
			ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
			return new LegacyUpgradeRecipe(resourceLocation, ingredient, ingredient2, itemStack);
		}

		public LegacyUpgradeRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
			Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient ingredient2 = Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack itemStack = friendlyByteBuf.readItem();
			return new LegacyUpgradeRecipe(resourceLocation, ingredient, ingredient2, itemStack);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, LegacyUpgradeRecipe legacyUpgradeRecipe) {
			legacyUpgradeRecipe.base.toNetwork(friendlyByteBuf);
			legacyUpgradeRecipe.addition.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(legacyUpgradeRecipe.result);
		}
	}
}
