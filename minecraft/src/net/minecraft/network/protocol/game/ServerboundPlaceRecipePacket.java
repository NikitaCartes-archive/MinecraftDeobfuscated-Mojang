package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ServerboundPlaceRecipePacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundPlaceRecipePacket> STREAM_CODEC = Packet.codec(
		ServerboundPlaceRecipePacket::write, ServerboundPlaceRecipePacket::new
	);
	private final int containerId;
	private final ResourceLocation recipe;
	private final boolean useMaxItems;

	public ServerboundPlaceRecipePacket(int i, RecipeHolder<?> recipeHolder, boolean bl) {
		this.containerId = i;
		this.recipe = recipeHolder.id();
		this.useMaxItems = bl;
	}

	private ServerboundPlaceRecipePacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readContainerId();
		this.recipe = friendlyByteBuf.readResourceLocation();
		this.useMaxItems = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeContainerId(this.containerId);
		friendlyByteBuf.writeResourceLocation(this.recipe);
		friendlyByteBuf.writeBoolean(this.useMaxItems);
	}

	@Override
	public PacketType<ServerboundPlaceRecipePacket> type() {
		return GamePacketTypes.SERVERBOUND_PLACE_RECIPE;
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

	public boolean isUseMaxItems() {
		return this.useMaxItems;
	}
}
