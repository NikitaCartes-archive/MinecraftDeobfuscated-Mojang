package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;

public class ClientboundAddVibrationSignalPacket implements Packet<ClientGamePacketListener> {
	private final VibrationPath vibrationPath;

	public ClientboundAddVibrationSignalPacket(VibrationPath vibrationPath) {
		this.vibrationPath = vibrationPath;
	}

	public ClientboundAddVibrationSignalPacket(FriendlyByteBuf friendlyByteBuf) {
		this.vibrationPath = VibrationPath.read(friendlyByteBuf);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		VibrationPath.write(friendlyByteBuf, this.vibrationPath);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddVibrationSignal(this);
	}

	public VibrationPath getVibrationPath() {
		return this.vibrationPath;
	}
}
