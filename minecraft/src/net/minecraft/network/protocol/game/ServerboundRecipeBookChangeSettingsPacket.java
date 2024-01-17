package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.inventory.RecipeBookType;

public class ServerboundRecipeBookChangeSettingsPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundRecipeBookChangeSettingsPacket> STREAM_CODEC = Packet.codec(
		ServerboundRecipeBookChangeSettingsPacket::write, ServerboundRecipeBookChangeSettingsPacket::new
	);
	private final RecipeBookType bookType;
	private final boolean isOpen;
	private final boolean isFiltering;

	public ServerboundRecipeBookChangeSettingsPacket(RecipeBookType recipeBookType, boolean bl, boolean bl2) {
		this.bookType = recipeBookType;
		this.isOpen = bl;
		this.isFiltering = bl2;
	}

	private ServerboundRecipeBookChangeSettingsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.bookType = friendlyByteBuf.readEnum(RecipeBookType.class);
		this.isOpen = friendlyByteBuf.readBoolean();
		this.isFiltering = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.bookType);
		friendlyByteBuf.writeBoolean(this.isOpen);
		friendlyByteBuf.writeBoolean(this.isFiltering);
	}

	@Override
	public PacketType<ServerboundRecipeBookChangeSettingsPacket> type() {
		return GamePacketTypes.SERVERBOUND_RECIPE_BOOK_CHANGE_SETTINGS;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleRecipeBookChangeSettingsPacket(this);
	}

	public RecipeBookType getBookType() {
		return this.bookType;
	}

	public boolean isOpen() {
		return this.isOpen;
	}

	public boolean isFiltering() {
		return this.isFiltering;
	}
}
