package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener> {
	private BlockHitResult blockHit;
	private InteractionHand hand;

	public ServerboundUseItemOnPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundUseItemOnPacket(InteractionHand interactionHand, BlockHitResult blockHitResult) {
		this.hand = interactionHand;
		this.blockHit = blockHitResult;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
		this.blockHit = friendlyByteBuf.readBlockHitResult();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.hand);
		friendlyByteBuf.writeBlockHitResult(this.blockHit);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleUseItemOn(this);
	}

	public InteractionHand getHand() {
		return this.hand;
	}

	public BlockHitResult getHitResult() {
		return this.blockHit;
	}
}
