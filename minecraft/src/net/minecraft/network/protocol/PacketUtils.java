package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.network.PacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T packetListener, ServerLevel serverLevel) throws RunningOnDifferentThreadException {
		ensureRunningOnSameThread(packet, packetListener, serverLevel.getServer());
	}

	public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T packetListener, BlockableEventLoop<?> blockableEventLoop) throws RunningOnDifferentThreadException {
		if (!blockableEventLoop.isSameThread()) {
			blockableEventLoop.executeIfPossible(() -> {
				if (packetListener.isAcceptingMessages()) {
					try {
						packet.handle(packetListener);
					} catch (Exception var3) {
						if (packetListener.shouldPropagateHandlingExceptions()) {
							throw var3;
						}

						LOGGER.error("Failed to handle packet {}, suppressing error", packet, var3);
					}
				} else {
					LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
				}
			});
			throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
		}
	}
}
