package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener> {
	private ItemStack book;
	private boolean signing;
	private InteractionHand hand;

	public ServerboundEditBookPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundEditBookPacket(ItemStack itemStack, boolean bl, InteractionHand interactionHand) {
		this.book = itemStack.copy();
		this.signing = bl;
		this.hand = interactionHand;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.book = friendlyByteBuf.readItem();
		this.signing = friendlyByteBuf.readBoolean();
		this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeItem(this.book);
		friendlyByteBuf.writeBoolean(this.signing);
		friendlyByteBuf.writeEnum(this.hand);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleEditBook(this);
	}

	public ItemStack getBook() {
		return this.book;
	}

	public boolean isSigning() {
		return this.signing;
	}

	public InteractionHand getHand() {
		return this.hand;
	}
}
