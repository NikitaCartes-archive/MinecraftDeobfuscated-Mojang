package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundPlaceGhostRecipePacket implements Packet<ClientGamePacketListener> {
	private final int containerId;
	private final ResourceLocation recipe;

	public ClientboundPlaceGhostRecipePacket(int i, Recipe<?> recipe) {
		this.containerId = i;
		this.recipe = recipe.getId();
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

	@Environment(EnvType.CLIENT)
	public ResourceLocation getRecipe() {
		return this.recipe;
	}

	@Environment(EnvType.CLIENT)
	public int getContainerId() {
		return this.containerId;
	}
}
