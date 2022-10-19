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
import net.minecraft.Util;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;

@Environment(EnvType.CLIENT)
public class ChatReportBuilder {
	private final UUID reportId;
	private final Instant createdAt;
	private final UUID reportedProfileId;
	private final AbuseReportLimits limits;
	private final IntSet reportedMessages = new IntOpenHashSet();
	private String comments = "";
	@Nullable
	private ReportReason reason;

	private ChatReportBuilder(UUID uUID, Instant instant, UUID uUID2, AbuseReportLimits abuseReportLimits) {
		this.reportId = uUID;
		this.createdAt = instant;
		this.reportedProfileId = uUID2;
		this.limits = abuseReportLimits;
	}

	public ChatReportBuilder(UUID uUID, AbuseReportLimits abuseReportLimits) {
		this(UUID.randomUUID(), Instant.now(), uUID, abuseReportLimits);
	}

	public void setComments(String string) {
		this.comments = string;
	}

	public void setReason(ReportReason reportReason) {
		this.reason = reportReason;
	}

	public void toggleReported(int i) {
		if (this.reportedMessages.contains(i)) {
			this.reportedMessages.remove(i);
		} else if (this.reportedMessages.size() < this.limits.maxReportedMessageCount()) {
			this.reportedMessages.add(i);
		}
	}

	public UUID reportedProfileId() {
		return this.reportedProfileId;
	}

	public IntSet reportedMessages() {
		return this.reportedMessages;
	}

	public String comments() {
		return this.comments;
	}

	@Nullable
	public ReportReason reason() {
		return this.reason;
	}

	public boolean isReported(int i) {
		return this.reportedMessages.contains(i);
	}

	@Nullable
	public ChatReportBuilder.CannotBuildReason checkBuildable() {
		if (this.reportedMessages.isEmpty()) {
			return ChatReportBuilder.CannotBuildReason.NO_REPORTED_MESSAGES;
		} else if (this.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
			return ChatReportBuilder.CannotBuildReason.TOO_MANY_MESSAGES;
		} else if (this.reason == null) {
			return ChatReportBuilder.CannotBuildReason.NO_REASON;
		} else {
			return this.comments.length() > this.limits.maxOpinionCommentsLength() ? ChatReportBuilder.CannotBuildReason.COMMENTS_TOO_LONG : null;
		}
	}

	public Either<ChatReportBuilder.Result, ChatReportBuilder.CannotBuildReason> build(ReportingContext reportingContext) {
		ChatReportBuilder.CannotBuildReason cannotBuildReason = this.checkBuildable();
		if (cannotBuildReason != null) {
			return Either.right(cannotBuildReason);
		} else {
			String string = ((ReportReason)Objects.requireNonNull(this.reason)).backendName();
			ReportEvidence reportEvidence = this.buildEvidence(reportingContext.chatLog());
			ReportedEntity reportedEntity = new ReportedEntity(this.reportedProfileId);
			AbuseReport abuseReport = new AbuseReport(this.comments, string, reportEvidence, reportedEntity, this.createdAt);
			return Either.left(new ChatReportBuilder.Result(this.reportId, abuseReport));
		}
	}

	private ReportEvidence buildEvidence(ChatLog chatLog) {
		List<ReportChatMessage> list = new ArrayList();
		ChatReportContextBuilder chatReportContextBuilder = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
		chatReportContextBuilder.collectAllContext(chatLog, this.reportedMessages, (i, player) -> list.add(this.buildReportedChatMessage(player, this.isReported(i))));
		return new ReportEvidence(Lists.reverse(list));
	}

	private ReportChatMessage buildReportedChatMessage(LoggedChatMessage.Player player, boolean bl) {
		SignedMessageLink signedMessageLink = player.message().link();
		SignedMessageBody signedMessageBody = player.message().signedBody();
		List<ByteBuffer> list = signedMessageBody.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
		ByteBuffer byteBuffer = Util.mapNullable(player.message().signature(), MessageSignature::asByteBuffer);
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

	public ChatReportBuilder copy() {
		ChatReportBuilder chatReportBuilder = new ChatReportBuilder(this.reportId, this.createdAt, this.reportedProfileId, this.limits);
		chatReportBuilder.reportedMessages.addAll(this.reportedMessages);
		chatReportBuilder.comments = this.comments;
		chatReportBuilder.reason = this.reason;
		return chatReportBuilder;
	}

	@Environment(EnvType.CLIENT)
	public static record CannotBuildReason(Component message) {
		public static final ChatReportBuilder.CannotBuildReason NO_REASON = new ChatReportBuilder.CannotBuildReason(
			Component.translatable("gui.chatReport.send.no_reason")
		);
		public static final ChatReportBuilder.CannotBuildReason NO_REPORTED_MESSAGES = new ChatReportBuilder.CannotBuildReason(
			Component.translatable("gui.chatReport.send.no_reported_messages")
		);
		public static final ChatReportBuilder.CannotBuildReason TOO_MANY_MESSAGES = new ChatReportBuilder.CannotBuildReason(
			Component.translatable("gui.chatReport.send.too_many_messages")
		);
		public static final ChatReportBuilder.CannotBuildReason COMMENTS_TOO_LONG = new ChatReportBuilder.CannotBuildReason(
			Component.translatable("gui.chatReport.send.comments_too_long")
		);
	}

	@Environment(EnvType.CLIENT)
	public static record Result(UUID id, AbuseReport report) {
	}
}
