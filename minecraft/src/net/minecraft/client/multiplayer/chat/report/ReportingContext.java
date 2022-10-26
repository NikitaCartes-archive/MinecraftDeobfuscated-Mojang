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
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class ReportingContext {
	private static final int LOG_CAPACITY = 1024;
	private final AbuseReportSender sender;
	private final ReportEnvironment environment;
	private final ChatLog chatLog;
	@Nullable
	private ChatReportBuilder.ChatReport chatReportDraft;

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

	public boolean draftReportHandled(Minecraft minecraft, @Nullable Screen screen, boolean bl) {
		if (this.chatReportDraft != null) {
			ChatReportBuilder.ChatReport chatReport = this.chatReportDraft.copy();
			minecraft.setScreen(
				new ConfirmScreen(
					blx -> {
						this.setChatReportDraft(null);
						if (blx) {
							minecraft.setScreen(new ChatReportScreen(screen, this, chatReport));
						} else {
							minecraft.setScreen(screen);
						}
					},
					Component.translatable(bl ? "gui.chatReport.draft.quittotitle.title" : "gui.chatReport.draft.title"),
					Component.translatable(bl ? "gui.chatReport.draft.quittotitle.content" : "gui.chatReport.draft.content"),
					Component.translatable("gui.chatReport.draft.edit"),
					Component.translatable("gui.chatReport.draft.discard")
				)
			);
			return false;
		} else {
			return true;
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

	public void setChatReportDraft(@Nullable ChatReportBuilder.ChatReport chatReport) {
		this.chatReportDraft = chatReport;
	}

	public boolean hasDraftReport() {
		return this.chatReportDraft != null;
	}

	public boolean hasDraftReportFor(UUID uUID) {
		return this.hasDraftReport() && this.chatReportDraft.isReportedPlayer(uUID);
	}
}
