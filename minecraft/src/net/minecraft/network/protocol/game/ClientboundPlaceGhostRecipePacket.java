package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ClientboundPlaceGhostRecipePacket implements Packet<ClientGamePacketListener> {
	private final int containerId;
	private final ResourceLocation recipe;

	public ClientboundPlaceGhostRecipePacket(int i, RecipeHolder<?> recipeHolder) {
		this.containerId = i;
		this.recipe = recipeHolder.id();
	}

	public ClientboundPlaceGhostRecipePacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readByte();
		this.recipe = friendlyByteBuf.readResourceLocation();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeResourceLocation(this.recipe);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlaceRecipe(this);
	}

	public ResourceLocation getRecipe() {
		return this.recipe;
	}

	public int getContainerId() {
		return this.containerId;
	}
}
