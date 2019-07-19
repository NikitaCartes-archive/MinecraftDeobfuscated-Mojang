package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
	private List<Recipe<?>> recipes;

	public ClientboundUpdateRecipesPacket() {
	}

	public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> collection) {
		this.recipes = Lists.<Recipe<?>>newArrayList(collection);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateRecipes(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.recipes = Lists.<Recipe<?>>newArrayList();
		int i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			this.recipes.add(fromNetwork(friendlyByteBuf));
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.recipes.size());

		for (Recipe<?> recipe : this.recipes) {
			toNetwork(recipe, friendlyByteBuf);
		}
	}

	@Environment(EnvType.CLIENT)
	public List<Recipe<?>> getRecipes() {
		return this.recipes;
	}

	public static Recipe<?> fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
		ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
		return ((RecipeSerializer)Registry.RECIPE_SERIALIZER
				.getOptional(resourceLocation)
				.orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + resourceLocation)))
			.fromNetwork(resourceLocation2, friendlyByteBuf);
	}

	public static <T extends Recipe<?>> void toNetwork(T recipe, FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(recipe.getSerializer()));
		friendlyByteBuf.writeResourceLocation(recipe.getId());
		((RecipeSerializer<T>)recipe.getSerializer()).toNetwork(friendlyByteBuf, recipe);
	}
}
