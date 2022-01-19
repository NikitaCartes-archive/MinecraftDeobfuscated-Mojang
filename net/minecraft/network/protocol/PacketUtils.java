/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T packetListener, ServerLevel serverLevel) throws RunningOnDifferentThreadException {
        PacketUtils.ensureRunningOnSameThread(packet, packetListener, serverLevel.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T packetListener, BlockableEventLoop<?> blockableEventLoop) throws RunningOnDifferentThreadException {
        if (!blockableEventLoop.isSameThread()) {
            blockableEventLoop.executeIfPossible(() -> {
                if (packetListener.getConnection().isConnected()) {
                    try {
                        packet.handle(packetListener);
                    } catch (Exception exception) {
                        if (packetListener.shouldPropagateHandlingExceptions()) {
                            throw exception;
                        }
                        LOGGER.error("Failed to handle packet {}, suppressing error", (Object)packet, (Object)exception);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)packet);
                }
            });
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
}

