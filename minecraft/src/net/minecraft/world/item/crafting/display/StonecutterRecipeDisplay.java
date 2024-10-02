package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record StonecutterRecipeDisplay(SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
	public static final MapCodec<StonecutterRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					SlotDisplay.CODEC.fieldOf("result").forGetter(StonecutterRecipeDisplay::result),
					SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(StonecutterRecipeDisplay::craftingStation)
				)
				.apply(instance, StonecutterRecipeDisplay::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, StonecutterRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
		SlotDisplay.STREAM_CODEC,
		StonecutterRecipeDisplay::result,
		SlotDisplay.STREAM_CODEC,
		StonecutterRecipeDisplay::craftingStation,
		StonecutterRecipeDisplay::new
	);
	public static final RecipeDisplay.Type<StonecutterRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public RecipeDisplay.Type<StonecutterRecipeDisplay> type() {
		return TYPE;
	}
}
