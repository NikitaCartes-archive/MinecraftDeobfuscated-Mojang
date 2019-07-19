package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundPlaceRecipePacket implements Packet<ServerGamePacketListener> {
	private int containerId;
	private ResourceLocation recipe;
	private boolean shiftDown;

	public ServerboundPlaceRecipePacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundPlaceRecipePacket(int i, Recipe<?> recipe, boolean bl) {
		this.containerId = i;
		this.recipe = recipe.getId();
		this.shiftDown = bl;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.containerId = friendlyByteBuf.readByte();
		this.recipe = friendlyByteBuf.readResourceLocation();
		this.shiftDown = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeResourceLocation(this.recipe);
		friendlyByteBuf.writeBoolean(this.shiftDown);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePlaceRecipe(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public ResourceLocation getRecipe() {
		return this.recipe;
	}

	public boolean isShiftDown() {
		return this.shiftDown;
	}
}
