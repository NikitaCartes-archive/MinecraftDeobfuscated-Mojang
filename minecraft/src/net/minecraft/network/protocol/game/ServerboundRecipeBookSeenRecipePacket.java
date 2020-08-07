package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundRecipeBookSeenRecipePacket implements Packet<ServerGamePacketListener> {
	private ResourceLocation recipe;

	public ServerboundRecipeBookSeenRecipePacket() {
	}

	public ServerboundRecipeBookSeenRecipePacket(Recipe<?> recipe) {
		this.recipe = recipe.getId();
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.recipe = friendlyByteBuf.readResourceLocation();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeResourceLocation(this.recipe);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleRecipeBookSeenRecipePacket(this);
	}

	public ResourceLocation getRecipe() {
		return this.recipe;
	}
}
