package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;

public class SmithingTrimRecipe implements SmithingRecipe {
	final Optional<Ingredient> template;
	final Optional<Ingredient> base;
	final Optional<Ingredient> addition;
	@Nullable
	private PlacementInfo placementInfo;

	public SmithingTrimRecipe(Optional<Ingredient> optional, Optional<Ingredient> optional2, Optional<Ingredient> optional3) {
		this.template = optional;
		this.base = optional2;
		this.addition = optional3;
	}

	public ItemStack assemble(SmithingRecipeInput smithingRecipeInput, HolderLookup.Provider provider) {
		ItemStack itemStack = smithingRecipeInput.base();
		if (Ingredient.testOptionalIngredient(this.base, itemStack)) {
			Optional<Holder.Reference<TrimMaterial>> optional = TrimMaterials.getFromIngredient(provider, smithingRecipeInput.addition());
			Optional<Holder.Reference<TrimPattern>> optional2 = TrimPatterns.getFromTemplate(provider, smithingRecipeInput.template());
			if (optional.isPresent() && optional2.isPresent()) {
				ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
				if (armorTrim != null && armorTrim.hasPatternAndMaterial((Holder<TrimPattern>)optional2.get(), (Holder<TrimMaterial>)optional.get())) {
					return ItemStack.EMPTY;
				}

				ItemStack itemStack2 = itemStack.copyWithCount(1);
				itemStack2.set(DataComponents.TRIM, new ArmorTrim((Holder<TrimMaterial>)optional.get(), (Holder<TrimPattern>)optional2.get()));
				return itemStack2;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		ItemStack itemStack = new ItemStack(Items.IRON_CHESTPLATE);
		Optional<Holder.Reference<TrimPattern>> optional = provider.lookupOrThrow(Registries.TRIM_PATTERN).listElements().findFirst();
		Optional<Holder.Reference<TrimMaterial>> optional2 = provider.lookupOrThrow(Registries.TRIM_MATERIAL).get(TrimMaterials.REDSTONE);
		if (optional.isPresent() && optional2.isPresent()) {
			itemStack.set(DataComponents.TRIM, new ArmorTrim((Holder<TrimMaterial>)optional2.get(), (Holder<TrimPattern>)optional.get()));
		}

		return itemStack;
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
		return RecipeSerializer.SMITHING_TRIM;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
		}

		return this.placementInfo;
	}

	public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
		private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Ingredient.CODEC.optionalFieldOf("template").forGetter(smithingTrimRecipe -> smithingTrimRecipe.template),
						Ingredient.CODEC.optionalFieldOf("base").forGetter(smithingTrimRecipe -> smithingTrimRecipe.base),
						Ingredient.CODEC.optionalFieldOf("addition").forGetter(smithingTrimRecipe -> smithingTrimRecipe.addition)
					)
					.apply(instance, SmithingTrimRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
			smithingTrimRecipe -> smithingTrimRecipe.template,
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
			smithingTrimRecipe -> smithingTrimRecipe.base,
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
			smithingTrimRecipe -> smithingTrimRecipe.addition,
			SmithingTrimRecipe::new
		);

		@Override
		public MapCodec<SmithingTrimRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
