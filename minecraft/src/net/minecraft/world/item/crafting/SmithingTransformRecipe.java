package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;

	public SmithingTransformRecipe(Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, ItemStack itemStack) {
		this.template = ingredient;
		this.base = ingredient2;
		this.addition = ingredient3;
		this.result = itemStack;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.template.test(container.getItem(0)) && this.base.test(container.getItem(1)) && this.addition.test(container.getItem(2));
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		ItemStack itemStack = this.result.copy();
		CompoundTag compoundTag = container.getItem(1).getTag();
		if (compoundTag != null) {
			itemStack.setTag(compoundTag.copy());
		}

		return itemStack;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return this.result;
	}

	@Override
	public boolean isTemplateIngredient(ItemStack itemStack) {
		return this.template.test(itemStack);
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
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMITHING_TRANSFORM;
	}

	@Override
	public boolean isIncomplete() {
		return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
	}

	public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
		private static final Codec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Ingredient.CODEC.fieldOf("template").forGetter(smithingTransformRecipe -> smithingTransformRecipe.template),
						Ingredient.CODEC.fieldOf("base").forGetter(smithingTransformRecipe -> smithingTransformRecipe.base),
						Ingredient.CODEC.fieldOf("addition").forGetter(smithingTransformRecipe -> smithingTransformRecipe.addition),
						CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(smithingTransformRecipe -> smithingTransformRecipe.result)
					)
					.apply(instance, SmithingTransformRecipe::new)
		);

		@Override
		public Codec<SmithingTransformRecipe> codec() {
			return CODEC;
		}

		public SmithingTransformRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient ingredient2 = Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient ingredient3 = Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack itemStack = friendlyByteBuf.readItem();
			return new SmithingTransformRecipe(ingredient, ingredient2, ingredient3, itemStack);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, SmithingTransformRecipe smithingTransformRecipe) {
			smithingTransformRecipe.template.toNetwork(friendlyByteBuf);
			smithingTransformRecipe.base.toNetwork(friendlyByteBuf);
			smithingTransformRecipe.addition.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(smithingTransformRecipe.result);
		}
	}
}
