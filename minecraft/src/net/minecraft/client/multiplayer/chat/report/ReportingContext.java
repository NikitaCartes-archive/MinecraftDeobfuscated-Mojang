package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class ReportingContext {
	private static final int LOG_CAPACITY = 1024;
	private final AbuseReportSender sender;
	private final ReportEnvironment environment;
	private final ChatLog chatLog;
	@Nullable
	private Report draftReport;

	public ReportingContext(AbuseReportSender abuseReportSender, ReportEnvironment reportEnvironment, ChatLog chatLog) {
		this.sender = abuseReportSender;
		this.environment = reportEnvironment;
		this.chatLog = chatLog;
	}

	public static ReportingContext create(ReportEnvironment reportEnvironment, UserApiService userApiService) {
		ChatLog chatLog = new ChatLog(1024);
		AbuseReportSender abuseReportSender = AbuseReportSender.create(reportEnvironment, userApiService);
		return new ReportingContext(abuseReportSender, reportEnvironment, chatLog);
	}

	public void draftReportHandled(Minecraft minecraft, Screen screen, Runnable runnable, boolean bl) {
		if (this.draftReport != null) {
			Report report = this.draftReport.copy();
			minecraft.setScreen(
				new ConfirmScreen(
					blx -> {
						this.setReportDraft(null);
						if (blx) {
							minecraft.setScreen(report.createScreen(screen, this));
						} else {
							runnable.run();
						}
					},
					Component.translatable(bl ? "gui.abuseReport.draft.quittotitle.title" : "gui.abuseReport.draft.title"),
					Component.translatable(bl ? "gui.abuseReport.draft.quittotitle.content" : "gui.abuseReport.draft.content"),
					Component.translatable("gui.abuseReport.draft.edit"),
					Component.translatable("gui.abuseReport.draft.discard")
				)
			);
		} else {
			runnable.run();
		}
	}

	public AbuseReportSender sender() {
		return this.sender;
	}

	public ChatLog chatLog() {
		return this.chatLog;
	}

	public boolean matches(ReportEnvironment reportEnvironment) {
		return Objects.equals(this.environment, reportEnvironment);
	}

	public void setReportDraft(@Nullable Report report) {
		this.draftReport = report;
	}

	public boolean hasDraftReport() {
		return this.draftReport != null;
	}

	public boolean hasDraftReportFor(UUID uUID) {
		return this.hasDraftReport() && this.draftReport.isReportedPlayer(uUID);
	}
}
