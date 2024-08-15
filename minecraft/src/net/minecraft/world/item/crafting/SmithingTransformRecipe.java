package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class SmithingTransformRecipe implements SmithingRecipe {
	final Optional<Ingredient> template;
	final Optional<Ingredient> base;
	final Optional<Ingredient> addition;
	final ItemStack result;
	@Nullable
	private PlacementInfo placementInfo;

	public SmithingTransformRecipe(Optional<Ingredient> optional, Optional<Ingredient> optional2, Optional<Ingredient> optional3, ItemStack itemStack) {
		this.template = optional;
		this.base = optional2;
		this.addition = optional3;
		this.result = itemStack;
	}

	public ItemStack assemble(SmithingRecipeInput smithingRecipeInput, HolderLookup.Provider provider) {
		ItemStack itemStack = smithingRecipeInput.base().transmuteCopy(this.result.getItem(), this.result.getCount());
		itemStack.applyComponents(this.result.getComponentsPatch());
		return itemStack;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return this.result;
	}

	@Override
	public boolean isTemplateIngredient(ItemStack itemStack) {
		return Ingredient.testOptionalIngredient(this.template, itemStack);
	}

	@Override
	public boolean isBaseIngredient(ItemStack itemStack) {
		return Ingredient.testOptionalIngredient(this.base, itemStack);
	}

	@Override
	public boolean isAdditionIngredient(ItemStack itemStack) {
		return Ingredient.testOptionalIngredient(this.addition, itemStack);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMITHING_TRANSFORM;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
		}

		return this.placementInfo;
	}

	public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
		private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Ingredient.CODEC.optionalFieldOf("template").forGetter(smithingTransformRecipe -> smithingTransformRecipe.template),
						Ingredient.CODEC.optionalFieldOf("base").forGetter(smithingTransformRecipe -> smithingTransformRecipe.base),
						Ingredient.CODEC.optionalFieldOf("addition").forGetter(smithingTransformRecipe -> smithingTransformRecipe.addition),
						ItemStack.STRICT_CODEC.fieldOf("result").forGetter(smithingTransformRecipe -> smithingTransformRecipe.result)
					)
					.apply(instance, SmithingTransformRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
			smithingTransformRecipe -> smithingTransformRecipe.template,
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
			smithingTransformRecipe -> smithingTransformRecipe.base,
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
			smithingTransformRecipe -> smithingTransformRecipe.addition,
			ItemStack.STREAM_CODEC,
			smithingTransformRecipe -> smithingTransformRecipe.result,
			SmithingTransformRecipe::new
		);

		@Override
		public MapCodec<SmithingTransformRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
