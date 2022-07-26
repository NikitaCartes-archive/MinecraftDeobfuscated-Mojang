package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import org.slf4j.Logger;

public class RateKickingConnection extends Connection {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component EXCEED_REASON = Component.translatable("disconnect.exceeded_packet_rate");
	private final int rateLimitPacketsPerSecond;

	public RateKickingConnection(int i) {
		super(PacketFlow.SERVERBOUND);
		this.rateLimitPacketsPerSecond = i;
	}

	@Override
	protected void tickSecond() {
		super.tickSecond();
		float f = this.getAverageReceivedPackets();
		if (f > (float)this.rateLimitPacketsPerSecond) {
			LOGGER.warn("Player exceeded rate-limit (sent {} packets per second)", f);
			this.send(new ClientboundDisconnectPacket(EXCEED_REASON), PacketSendListener.thenRun(() -> this.disconnect(EXCEED_REASON)));
			this.setReadOnly();
		}
	}
}
