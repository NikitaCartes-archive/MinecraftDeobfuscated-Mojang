package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;

public class ClientboundAddVibrationSignalPacket implements Packet<ClientGamePacketListener> {
	private VibrationPath vibrationPath;

	public ClientboundAddVibrationSignalPacket() {
	}

	public ClientboundAddVibrationSignalPacket(VibrationPath vibrationPath) {
		this.vibrationPath = vibrationPath;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.vibrationPath = VibrationPath.read(friendlyByteBuf);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		VibrationPath.write(friendlyByteBuf, this.vibrationPath);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddVibrationSignal(this);
	}

	@Environment(EnvType.CLIENT)
	public VibrationPath getVibrationPath() {
		return this.vibrationPath;
	}
}
