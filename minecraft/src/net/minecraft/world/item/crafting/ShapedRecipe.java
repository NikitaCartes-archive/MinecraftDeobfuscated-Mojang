package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe {
	final ShapedRecipePattern pattern;
	final ItemStack result;
	final String group;
	final CraftingBookCategory category;
	final boolean showNotification;
	@Nullable
	private PlacementInfo placementInfo;

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
	public RecipeSerializer<? extends ShapedRecipe> getSerializer() {
		return RecipeSerializer.SHAPED_RECIPE;
	}

	@Override
	public String group() {
		return this.group;
	}

	@Override
	public CraftingBookCategory category() {
		return this.category;
	}

	@VisibleForTesting
	public List<Optional<Ingredient>> getIngredients() {
		return this.pattern.ingredients();
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
		}

		return this.placementInfo;
	}

	@Override
	public boolean showNotification() {
		return this.showNotification;
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		return this.pattern.matches(craftingInput);
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		return this.result.copy();
	}

	public int getWidth() {
		return this.pattern.width();
	}

	public int getHeight() {
		return this.pattern.height();
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new ShapedCraftingRecipeDisplay(
				this.pattern.width(),
				this.pattern.height(),
				this.pattern.ingredients().stream().map(optional -> (SlotDisplay)optional.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
				new SlotDisplay.ItemStackSlotDisplay(this.result),
				new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
			)
		);
	}

	public static class Serializer implements RecipeSerializer<ShapedRecipe> {
		public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(shapedRecipe -> shapedRecipe.group),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(shapedRecipe -> shapedRecipe.category),
						ShapedRecipePattern.MAP_CODEC.forGetter(shapedRecipe -> shapedRecipe.pattern),
						ItemStack.STRICT_CODEC.fieldOf("result").forGetter(shapedRecipe -> shapedRecipe.result),
						Codec.BOOL.optionalFieldOf("show_notification", Boolean.valueOf(true)).forGetter(shapedRecipe -> shapedRecipe.showNotification)
					)
					.apply(instance, ShapedRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> STREAM_CODEC = StreamCodec.of(
			ShapedRecipe.Serializer::toNetwork, ShapedRecipe.Serializer::fromNetwork
		);

		@Override
		public MapCodec<ShapedRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static ShapedRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			String string = registryFriendlyByteBuf.readUtf();
			CraftingBookCategory craftingBookCategory = registryFriendlyByteBuf.readEnum(CraftingBookCategory.class);
			ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.STREAM_CODEC.decode(registryFriendlyByteBuf);
			ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
			boolean bl = registryFriendlyByteBuf.readBoolean();
			return new ShapedRecipe(string, craftingBookCategory, shapedRecipePattern, itemStack, bl);
		}

		private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, ShapedRecipe shapedRecipe) {
			registryFriendlyByteBuf.writeUtf(shapedRecipe.group);
			registryFriendlyByteBuf.writeEnum(shapedRecipe.category);
			ShapedRecipePattern.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.pattern);
			ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.result);
			registryFriendlyByteBuf.writeBoolean(shapedRecipe.showNotification);
		}
	}
}
