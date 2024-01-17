package net.minecraft.network.protocol.game;

import java.util.Collection;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateRecipesPacket> STREAM_CODEC = StreamCodec.composite(
		RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()),
		clientboundUpdateRecipesPacket -> clientboundUpdateRecipesPacket.recipes,
		ClientboundUpdateRecipesPacket::new
	);
	private final List<RecipeHolder<?>> recipes;

	public ClientboundUpdateRecipesPacket(Collection<RecipeHolder<?>> collection) {
		this.recipes = List.copyOf(collection);
	}

	@Override
	public PacketType<ClientboundUpdateRecipesPacket> type() {
		return GamePacketTypes.CLIENTBOUND_UPDATE_RECIPES;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateRecipes(this);
	}

	public List<RecipeHolder<?>> getRecipes() {
		return this.recipes;
	}
}
