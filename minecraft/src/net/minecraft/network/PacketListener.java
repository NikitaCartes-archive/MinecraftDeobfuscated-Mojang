package net.minecraft.network;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketUtils;

public interface PacketListener {
	PacketFlow flow();

	ConnectionProtocol protocol();

	void onDisconnect(DisconnectionDetails disconnectionDetails);

	default void onPacketError(Packet packet, Exception exception) throws ReportedException {
		throw PacketUtils.makeReportedException(exception, packet, this);
	}

	default DisconnectionDetails createDisconnectionInfo(Component component, Throwable throwable) {
		return new DisconnectionDetails(component);
	}

	boolean isAcceptingMessages();

	default boolean shouldHandleMessage(Packet<?> packet) {
		return this.isAcceptingMessages();
	}

	default void fillCrashReport(CrashReport crashReport) {
		CrashReportCategory crashReportCategory = crashReport.addCategory("Connection");
		crashReportCategory.setDetail("Protocol", (CrashReportDetail<String>)(() -> this.protocol().id()));
		crashReportCategory.setDetail("Flow", (CrashReportDetail<String>)(() -> this.flow().toString()));
		this.fillListenerSpecificCrashDetails(crashReport, crashReportCategory);
	}

	default void fillListenerSpecificCrashDetails(CrashReport crashReport, CrashReportCategory crashReportCategory) {
	}
}
