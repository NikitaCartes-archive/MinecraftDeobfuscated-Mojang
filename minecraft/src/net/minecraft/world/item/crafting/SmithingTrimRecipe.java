package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SmithingTrimRecipe implements SmithingRecipe {
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;

	public SmithingTrimRecipe(Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3) {
		this.template = ingredient;
		this.base = ingredient2;
		this.addition = ingredient3;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.template.test(container.getItem(0)) && this.base.test(container.getItem(1)) && this.addition.test(container.getItem(2));
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		ItemStack itemStack = container.getItem(1);
		if (this.base.test(itemStack)) {
			Optional<Holder.Reference<TrimMaterial>> optional = TrimMaterials.getFromIngredient(registryAccess, container.getItem(2));
			Optional<Holder.Reference<TrimPattern>> optional2 = TrimPatterns.getFromTemplate(registryAccess, container.getItem(0));
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
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		ItemStack itemStack = new ItemStack(Items.IRON_CHESTPLATE);
		Optional<Holder.Reference<TrimPattern>> optional = registryAccess.registryOrThrow(Registries.TRIM_PATTERN).holders().findFirst();
		Optional<Holder.Reference<TrimMaterial>> optional2 = registryAccess.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(TrimMaterials.REDSTONE);
		if (optional.isPresent() && optional2.isPresent()) {
			itemStack.set(DataComponents.TRIM, new ArmorTrim((Holder<TrimMaterial>)optional2.get(), (Holder<TrimPattern>)optional.get()));
		}

		return itemStack;
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
		return RecipeSerializer.SMITHING_TRIM;
	}

	@Override
	public boolean isIncomplete() {
		return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
	}

	public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
		private static final Codec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Ingredient.CODEC.fieldOf("template").forGetter(smithingTrimRecipe -> smithingTrimRecipe.template),
						Ingredient.CODEC.fieldOf("base").forGetter(smithingTrimRecipe -> smithingTrimRecipe.base),
						Ingredient.CODEC.fieldOf("addition").forGetter(smithingTrimRecipe -> smithingTrimRecipe.addition)
					)
					.apply(instance, SmithingTrimRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.of(
			SmithingTrimRecipe.Serializer::toNetwork, SmithingTrimRecipe.Serializer::fromNetwork
		);

		@Override
		public Codec<SmithingTrimRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static SmithingTrimRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
			Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
			Ingredient ingredient3 = Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf);
			return new SmithingTrimRecipe(ingredient, ingredient2, ingredient3);
		}

		private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, SmithingTrimRecipe smithingTrimRecipe) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, smithingTrimRecipe.template);
			Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, smithingTrimRecipe.base);
			Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, smithingTrimRecipe.addition);
		}
	}
}
