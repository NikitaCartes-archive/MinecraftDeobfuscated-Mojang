package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SimpleRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
	private final Function<ResourceLocation, T> constructor;

	public SimpleRecipeSerializer(Function<ResourceLocation, T> function) {
		this.constructor = function;
	}

	@Override
	public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
		return (T)this.constructor.apply(resourceLocation);
	}

	@Override
	public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		return (T)this.constructor.apply(resourceLocation);
	}

	@Override
	public void toNetwork(FriendlyByteBuf friendlyByteBuf, T recipe) {
	}
}
