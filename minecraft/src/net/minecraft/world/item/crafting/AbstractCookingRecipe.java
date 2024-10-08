package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public abstract class AbstractCookingRecipe extends SingleItemRecipe {
	private final CookingBookCategory category;
	private final float experience;
	private final int cookingTime;

	public AbstractCookingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(string, ingredient, itemStack);
		this.category = cookingBookCategory;
		this.experience = f;
		this.cookingTime = i;
	}

	@Override
	public abstract RecipeSerializer<? extends AbstractCookingRecipe> getSerializer();

	@Override
	public abstract RecipeType<? extends AbstractCookingRecipe> getType();

	public float experience() {
		return this.experience;
	}

	public int cookingTime() {
		return this.cookingTime;
	}

	public CookingBookCategory category() {
		return this.category;
	}

	protected abstract Item furnaceIcon();

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new FurnaceRecipeDisplay(
				this.input().display(),
				SlotDisplay.AnyFuel.INSTANCE,
				new SlotDisplay.ItemStackSlotDisplay(this.result()),
				new SlotDisplay.ItemSlotDisplay(this.furnaceIcon()),
				this.cookingTime,
				this.experience
			)
		);
	}

	@FunctionalInterface
	public interface Factory<T extends AbstractCookingRecipe> {
		T create(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i);
	}

	public static class Serializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
		private final MapCodec<T> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

		public Serializer(AbstractCookingRecipe.Factory<T> factory, int i) {
			this.codec = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
							Codec.STRING.optionalFieldOf("group", "").forGetter(SingleItemRecipe::group),
							CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(AbstractCookingRecipe::category),
							Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
							ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(SingleItemRecipe::result),
							Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(AbstractCookingRecipe::experience),
							Codec.INT.fieldOf("cookingtime").orElse(i).forGetter(AbstractCookingRecipe::cookingTime)
						)
						.apply(instance, factory::create)
			);
			this.streamCodec = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8,
				SingleItemRecipe::group,
				CookingBookCategory.STREAM_CODEC,
				AbstractCookingRecipe::category,
				Ingredient.CONTENTS_STREAM_CODEC,
				SingleItemRecipe::input,
				ItemStack.STREAM_CODEC,
				SingleItemRecipe::result,
				ByteBufCodecs.FLOAT,
				AbstractCookingRecipe::experience,
				ByteBufCodecs.INT,
				AbstractCookingRecipe::cookingTime,
				factory::create
			);
		}

		@Override
		public MapCodec<T> codec() {
			return this.codec;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
			return this.streamCodec;
		}
	}
}
