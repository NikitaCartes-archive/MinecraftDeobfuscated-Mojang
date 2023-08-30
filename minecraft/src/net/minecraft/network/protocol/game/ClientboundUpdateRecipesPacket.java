package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
	private final List<RecipeHolder<?>> recipes;

	public ClientboundUpdateRecipesPacket(Collection<RecipeHolder<?>> collection) {
		this.recipes = Lists.<RecipeHolder<?>>newArrayList(collection);
	}

	public ClientboundUpdateRecipesPacket(FriendlyByteBuf friendlyByteBuf) {
		this.recipes = friendlyByteBuf.readList(ClientboundUpdateRecipesPacket::fromNetwork);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.recipes, ClientboundUpdateRecipesPacket::toNetwork);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateRecipes(this);
	}

	public List<RecipeHolder<?>> getRecipes() {
		return this.recipes;
	}

	private static RecipeHolder<?> fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
		ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
		Recipe<?> recipe = ((RecipeSerializer)BuiltInRegistries.RECIPE_SERIALIZER
				.getOptional(resourceLocation)
				.orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + resourceLocation)))
			.fromNetwork(friendlyByteBuf);
		return new RecipeHolder<>(resourceLocation2, recipe);
	}

	public static <T extends Recipe<?>> void toNetwork(FriendlyByteBuf friendlyByteBuf, RecipeHolder<?> recipeHolder) {
		friendlyByteBuf.writeResourceLocation(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipeHolder.value().getSerializer()));
		friendlyByteBuf.writeResourceLocation(recipeHolder.id());
		((RecipeSerializer<Recipe<?>>)recipeHolder.value().getSerializer()).toNetwork(friendlyByteBuf, recipeHolder.value());
	}
}
