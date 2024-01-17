package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ServerboundRecipeBookSeenRecipePacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundRecipeBookSeenRecipePacket> STREAM_CODEC = Packet.codec(
		ServerboundRecipeBookSeenRecipePacket::write, ServerboundRecipeBookSeenRecipePacket::new
	);
	private final ResourceLocation recipe;

	public ServerboundRecipeBookSeenRecipePacket(RecipeHolder<?> recipeHolder) {
		this.recipe = recipeHolder.id();
	}

	private ServerboundRecipeBookSeenRecipePacket(FriendlyByteBuf friendlyByteBuf) {
		this.recipe = friendlyByteBuf.readResourceLocation();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(this.recipe);
	}

	@Override
	public PacketType<ServerboundRecipeBookSeenRecipePacket> type() {
		return GamePacketTypes.SERVERBOUND_RECIPE_BOOK_SEEN_RECIPE;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleRecipeBookSeenRecipePacket(this);
	}

	public ResourceLocation getRecipe() {
		return this.recipe;
	}
}
