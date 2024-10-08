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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

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
	public Optional<Ingredient> templateIngredient() {
		return this.template;
	}

	@Override
	public Optional<Ingredient> baseIngredient() {
		return this.base;
	}

	@Override
	public Optional<Ingredient> additionIngredient() {
		return this.addition;
	}

	@Override
	public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
		return RecipeSerializer.SMITHING_TRANSFORM;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
		}

		return this.placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new SmithingRecipeDisplay(
				Ingredient.optionalIngredientToDisplay(this.template),
				Ingredient.optionalIngredientToDisplay(this.base),
				Ingredient.optionalIngredientToDisplay(this.addition),
				new SlotDisplay.ItemStackSlotDisplay(this.result),
				new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
			)
		);
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
