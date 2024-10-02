package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public record ClientboundPlaceGhostRecipePacket(int containerId, RecipeDisplay recipeDisplay) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlaceGhostRecipePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.CONTAINER_ID,
		ClientboundPlaceGhostRecipePacket::containerId,
		RecipeDisplay.STREAM_CODEC,
		ClientboundPlaceGhostRecipePacket::recipeDisplay,
		ClientboundPlaceGhostRecipePacket::new
	);

	@Override
	public PacketType<ClientboundPlaceGhostRecipePacket> type() {
		return GamePacketTypes.CLIENTBOUND_PLACE_GHOST_RECIPE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlaceRecipe(this);
	}
}
