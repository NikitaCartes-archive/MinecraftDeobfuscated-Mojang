package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;

public interface RecipeDisplay {
	Codec<RecipeDisplay> CODEC = BuiltInRegistries.RECIPE_DISPLAY.byNameCodec().dispatch(RecipeDisplay::type, RecipeDisplay.Type::codec);
	StreamCodec<RegistryFriendlyByteBuf, RecipeDisplay> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_DISPLAY)
		.dispatch(RecipeDisplay::type, RecipeDisplay.Type::streamCodec);

	SlotDisplay result();

	SlotDisplay craftingStation();

	RecipeDisplay.Type<? extends RecipeDisplay> type();

	default boolean isEnabled(FeatureFlagSet featureFlagSet) {
		return this.result().isEnabled(featureFlagSet) && this.craftingStation().isEnabled(featureFlagSet);
	}

	public static record Type<T extends RecipeDisplay>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
	}
}
