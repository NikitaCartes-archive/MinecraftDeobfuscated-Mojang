package net.minecraft.client.multiplayer.chat.report;

import com.google.common.collect.Lists;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class ChatReport extends Report {
	final IntSet reportedMessages = new IntOpenHashSet();

	ChatReport(UUID uUID, Instant instant, UUID uUID2) {
		super(uUID, instant, uUID2);
	}

	public void toggleReported(int i, AbuseReportLimits abuseReportLimits) {
		if (this.reportedMessages.contains(i)) {
			this.reportedMessages.remove(i);
		} else if (this.reportedMessages.size() < abuseReportLimits.maxReportedMessageCount()) {
			this.reportedMessages.add(i);
		}
	}

	public ChatReport copy() {
		ChatReport chatReport = new ChatReport(this.reportId, this.createdAt, this.reportedProfileId);
		chatReport.reportedMessages.addAll(this.reportedMessages);
		chatReport.comments = this.comments;
		chatReport.reason = this.reason;
		return chatReport;
	}

	@Override
	public Screen createScreen(Screen screen, ReportingContext reportingContext) {
		return new ChatReportScreen(screen, reportingContext, this);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder extends Report.Builder<ChatReport> {
		public Builder(ChatReport chatReport, AbuseReportLimits abuseReportLimits) {
			super(chatReport, abuseReportLimits);
		}

		public Builder(UUID uUID, AbuseReportLimits abuseReportLimits) {
			super(new ChatReport(UUID.randomUUID(), Instant.now(), uUID), abuseReportLimits);
		}

		public IntSet reportedMessages() {
			return this.report.reportedMessages;
		}

		public void toggleReported(int i) {
			this.report.toggleReported(i, this.limits);
		}

		public boolean isReported(int i) {
			return this.report.reportedMessages.contains(i);
		}

		@Override
		public boolean hasContent() {
			return StringUtils.isNotEmpty(this.comments()) || !this.reportedMessages().isEmpty() || this.reason() != null;
		}

		@Nullable
		@Override
		public Report.CannotBuildReason checkBuildable() {
			if (this.report.reportedMessages.isEmpty()) {
				return Report.CannotBuildReason.NO_REPORTED_MESSAGES;
			} else if (this.report.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
				return Report.CannotBuildReason.TOO_MANY_MESSAGES;
			} else if (this.report.reason == null) {
				return Report.CannotBuildReason.NO_REASON;
			} else {
				return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? Report.CannotBuildReason.COMMENT_TOO_LONG : null;
			}
		}

		@Override
		public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext reportingContext) {
			Report.CannotBuildReason cannotBuildReason = this.checkBuildable();
			if (cannotBuildReason != null) {
				return Either.right(cannotBuildReason);
			} else {
				String string = ((ReportReason)Objects.requireNonNull(this.report.reason)).backendName();
				ReportEvidence reportEvidence = this.buildEvidence(reportingContext);
				ReportedEntity reportedEntity = new ReportedEntity(this.report.reportedProfileId);
				AbuseReport abuseReport = AbuseReport.chat(this.report.comments, string, reportEvidence, reportedEntity, this.report.createdAt);
				return Either.left(new Report.Result(this.report.reportId, ReportType.CHAT, abuseReport));
			}
		}

		private ReportEvidence buildEvidence(ReportingContext reportingContext) {
			List<ReportChatMessage> list = new ArrayList();
			ChatReportContextBuilder chatReportContextBuilder = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
			chatReportContextBuilder.collectAllContext(
				reportingContext.chatLog(), this.report.reportedMessages, (i, player) -> list.add(this.buildReportedChatMessage(player, this.isReported(i)))
			);
			return new ReportEvidence(Lists.reverse(list));
		}

		private ReportChatMessage buildReportedChatMessage(LoggedChatMessage.Player player, boolean bl) {
			SignedMessageLink signedMessageLink = player.message().link();
			SignedMessageBody signedMessageBody = player.message().signedBody();
			List<ByteBuffer> list = signedMessageBody.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
			ByteBuffer byteBuffer = Optionull.map(player.message().signature(), MessageSignature::asByteBuffer);
			return new ReportChatMessage(
				signedMessageLink.index(),
				signedMessageLink.sender(),
				signedMessageLink.sessionId(),
				signedMessageBody.timeStamp(),
				signedMessageBody.salt(),
				list,
				signedMessageBody.content(),
				byteBuffer,
				bl
			);
		}

		public ChatReport.Builder copy() {
			return new ChatReport.Builder(this.report.copy(), this.limits);
		}
	}
}
