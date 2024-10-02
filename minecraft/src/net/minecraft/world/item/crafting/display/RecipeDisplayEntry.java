package net.minecraft.world.item.crafting.display;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BasicRecipeBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;

public record RecipeDisplayEntry(
	RecipeDisplayId id, RecipeDisplay display, OptionalInt group, BasicRecipeBookCategory category, Optional<List<Ingredient>> craftingRequirements
) {
	public static final StreamCodec<RegistryFriendlyByteBuf, RecipeDisplayEntry> STREAM_CODEC = StreamCodec.composite(
		RecipeDisplayId.STREAM_CODEC,
		RecipeDisplayEntry::id,
		RecipeDisplay.STREAM_CODEC,
		RecipeDisplayEntry::display,
		ByteBufCodecs.OPTIONAL_VAR_INT,
		RecipeDisplayEntry::group,
		BasicRecipeBookCategory.STREAM_CODEC,
		RecipeDisplayEntry::category,
		Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
		RecipeDisplayEntry::craftingRequirements,
		RecipeDisplayEntry::new
	);

	public List<ItemStack> resultItems(SlotDisplay.ResolutionContext resolutionContext) {
		return this.display.result().resolveForStacks(resolutionContext);
	}

	public boolean canCraft(StackedItemContents stackedItemContents) {
		if (this.craftingRequirements.isEmpty()) {
			return false;
		} else {
			List<StackedContents.IngredientInfo<Holder<Item>>> list = ((List)this.craftingRequirements.get()).stream().map(PlacementInfo::ingredientToContents).toList();
			return stackedItemContents.canCraft(list, null);
		}
	}
}
