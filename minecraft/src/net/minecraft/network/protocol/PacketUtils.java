package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
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
				if (packetListener.shouldHandleMessage(packet)) {
					try {
						packet.handle(packetListener);
					} catch (Exception var4) {
						if (var4 instanceof ReportedException reportedException && reportedException.getCause() instanceof OutOfMemoryError) {
							throw makeReportedException(var4, packet, packetListener);
						}

						packetListener.onPacketError(packet, var4);
					}
				} else {
					LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
				}
			});
			throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
		}
	}

	public static <T extends PacketListener> ReportedException makeReportedException(Exception exception, Packet<T> packet, T packetListener) {
		if (exception instanceof ReportedException reportedException) {
			fillCrashReport(reportedException.getReport(), packetListener, packet);
			return reportedException;
		} else {
			CrashReport crashReport = CrashReport.forThrowable(exception, "Main thread packet handler");
			fillCrashReport(crashReport, packetListener, packet);
			return new ReportedException(crashReport);
		}
	}

	private static <T extends PacketListener> void fillCrashReport(CrashReport crashReport, T packetListener, Packet<T> packet) {
		CrashReportCategory crashReportCategory = crashReport.addCategory("Incoming Packet");
		crashReportCategory.setDetail("Type", (CrashReportDetail<String>)(() -> packet.type().toString()));
		crashReportCategory.setDetail("Is Terminal", (CrashReportDetail<String>)(() -> Boolean.toString(packet.isTerminal())));
		crashReportCategory.setDetail("Is Skippable", (CrashReportDetail<String>)(() -> Boolean.toString(packet.isSkippable())));
		packetListener.fillCrashReport(crashReport);
	}
}
