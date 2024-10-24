package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

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
		return applyTrim(provider, smithingRecipeInput.base(), smithingRecipeInput.addition(), smithingRecipeInput.template());
	}

	public static ItemStack applyTrim(HolderLookup.Provider provider, ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3) {
		Optional<Holder.Reference<TrimMaterial>> optional = TrimMaterials.getFromIngredient(provider, itemStack2);
		Optional<Holder.Reference<TrimPattern>> optional2 = TrimPatterns.getFromTemplate(provider, itemStack3);
		if (optional.isPresent() && optional2.isPresent()) {
			ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
			if (armorTrim != null && armorTrim.hasPatternAndMaterial((Holder<TrimPattern>)optional2.get(), (Holder<TrimMaterial>)optional.get())) {
				return ItemStack.EMPTY;
			} else {
				ItemStack itemStack4 = itemStack.copyWithCount(1);
				itemStack4.set(DataComponents.TRIM, new ArmorTrim((Holder<TrimMaterial>)optional.get(), (Holder<TrimPattern>)optional2.get()));
				return itemStack4;
			}
		} else {
			return ItemStack.EMPTY;
		}
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
	public RecipeSerializer<SmithingTrimRecipe> getSerializer() {
		return RecipeSerializer.SMITHING_TRIM;
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
		SlotDisplay slotDisplay = Ingredient.optionalIngredientToDisplay(this.base);
		SlotDisplay slotDisplay2 = Ingredient.optionalIngredientToDisplay(this.addition);
		SlotDisplay slotDisplay3 = Ingredient.optionalIngredientToDisplay(this.template);
		return List.of(
			new SmithingRecipeDisplay(
				slotDisplay3,
				slotDisplay,
				slotDisplay2,
				new SlotDisplay.SmithingTrimDemoSlotDisplay(slotDisplay, slotDisplay2, slotDisplay3),
				new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
			)
		);
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
