package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public abstract class ServerCommonPacketListenerImpl implements ServerCommonPacketListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int LATENCY_CHECK_INTERVAL = 15000;
	private static final Component TIMEOUT_DISCONNECTION_MESSAGE = Component.translatable("disconnect.timeout");
	protected final MinecraftServer server;
	protected final Connection connection;
	private long keepAliveTime;
	private boolean keepAlivePending;
	private long keepAliveChallenge;
	private int latency;

	public ServerCommonPacketListenerImpl(MinecraftServer minecraftServer, Connection connection, int i) {
		this.server = minecraftServer;
		this.connection = connection;
		this.keepAliveTime = Util.getMillis();
		this.latency = i;
	}

	@Override
	public void onDisconnect(Component component) {
		if (this.isSingleplayerOwner()) {
			LOGGER.info("Stopping singleplayer server as player logged out");
			this.server.halt(false);
		}
	}

	@Override
	public void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket) {
		if (this.keepAlivePending && serverboundKeepAlivePacket.getId() == this.keepAliveChallenge) {
			int i = (int)(Util.getMillis() - this.keepAliveTime);
			this.latency = (this.latency * 3 + i) / 4;
			this.keepAlivePending = false;
		} else if (!this.isSingleplayerOwner()) {
			this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
		}
	}

	@Override
	public void handlePong(ServerboundPongPacket serverboundPongPacket) {
	}

	@Override
	public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
	}

	@Override
	public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundResourcePackPacket, this, this.server);
		if (serverboundResourcePackPacket.getAction() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
			LOGGER.info("Disconnecting {} due to resource pack rejection", this.playerProfile().getName());
			this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
		}
	}

	protected void keepConnectionAlive() {
		this.server.getProfiler().push("keepAlive");
		long l = Util.getMillis();
		if (l - this.keepAliveTime >= 15000L) {
			if (this.keepAlivePending) {
				this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
			} else {
				this.keepAlivePending = true;
				this.keepAliveTime = l;
				this.keepAliveChallenge = l;
				this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
			}
		}

		this.server.getProfiler().pop();
	}

	public void send(Packet<?> packet) {
		this.send(packet, null, true);
	}

	public void sendNoFlush(Packet<?> packet) {
		this.send(packet, null, false);
	}

	public void flush() {
		this.connection.flushChannel();
	}

	public void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener, boolean bl) {
		try {
			this.connection.send(packet, packetSendListener, bl);
		} catch (Throwable var7) {
			CrashReport crashReport = CrashReport.forThrowable(var7, "Sending packet");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Packet being sent");
			crashReportCategory.setDetail("Packet class", (CrashReportDetail<String>)(() -> packet.getClass().getCanonicalName()));
			throw new ReportedException(crashReport);
		}
	}

	public void disconnect(Component component) {
		this.connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> this.connection.disconnect(component)));
		this.connection.setReadOnly();
		this.server.executeBlocking(this.connection::handleDisconnection);
	}

	protected boolean isSingleplayerOwner() {
		return this.server.isSingleplayerOwner(this.playerProfile());
	}

	protected abstract GameProfile playerProfile();

	@VisibleForDebug
	public GameProfile getOwner() {
		return this.playerProfile();
	}

	public int latency() {
		return this.latency;
	}
}
