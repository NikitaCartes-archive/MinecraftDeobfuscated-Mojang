package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.Crypt;

@Environment(EnvType.CLIENT)
public class ChatReportBuilder {
	private static final String REPORT_TYPE_CHAT = "CHAT";
	private static final int CONTEXT_FRONT = 2;
	private static final int CONTEXT_BACK = 4;
	private final UUID id;
	private final Instant createdAt;
	private final UUID reportedProfileId;
	private final AbuseReportLimits limits;
	private final IntSet reportedMessages = new IntOpenHashSet();
	private String comments = "";
	@Nullable
	private ReportReason reason;

	private ChatReportBuilder(UUID uUID, Instant instant, UUID uUID2, AbuseReportLimits abuseReportLimits) {
		this.id = uUID;
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
			if (reportEvidence.messages.size() > this.limits.maxEvidenceMessageCount()) {
				return Either.right(ChatReportBuilder.CannotBuildReason.TOO_MANY_MESSAGES);
			} else {
				ReportedEntity reportedEntity = new ReportedEntity(this.reportedProfileId);
				AbuseReport abuseReport = new AbuseReport("CHAT", this.comments, string, reportEvidence, reportedEntity, this.createdAt);
				return Either.left(new ChatReportBuilder.Result(this.id, abuseReport));
			}
		}
	}

	private ReportEvidence buildEvidence(ChatLog chatLog) {
		IntSortedSet intSortedSet = new IntRBTreeSet();
		this.reportedMessages.forEach(i -> {
			IntStream intStream = this.selectContextMessages(chatLog, i);
			intStream.forEach(intSortedSet::add);
		});
		List<ReportChatMessage> list = intSortedSet.intStream()
			.mapToObj(i -> chatLog.lookup(i) instanceof LoggedChat.Player player ? this.buildReportedChatMessage(i, player) : null)
			.filter(Objects::nonNull)
			.toList();
		return new ReportEvidence(list);
	}

	private ReportChatMessage buildReportedChatMessage(int i, LoggedChat.Player player) {
		PlayerChatMessage playerChatMessage = player.message();
		Instant instant = playerChatMessage.signature().timeStamp();
		Crypt.SaltSignaturePair saltSignaturePair = playerChatMessage.signature().saltSignature();
		long l = saltSignaturePair.salt();
		String string = saltSignaturePair.isValid() ? encodeSignature(saltSignaturePair.signature()) : null;
		String string2 = encodeComponent(playerChatMessage.signedContent());
		String string3 = (String)playerChatMessage.unsignedContent().map(ChatReportBuilder::encodeComponent).orElse(null);
		return new ReportChatMessage(player.profileId(), instant, l, string, string2, string3, this.isReported(i));
	}

	private static String encodeComponent(Component component) {
		return Component.Serializer.toStableJson(component);
	}

	private static String encodeSignature(byte[] bs) {
		return Base64.getEncoder().encodeToString(bs);
	}

	private IntStream selectContextMessages(ChatLog chatLog, int i) {
		int j = chatLog.offsetClamped(i, -4);
		int k = chatLog.offsetClamped(i, 2);
		return chatLog.selectBetween(j, k).ids();
	}

	public ChatReportBuilder copy() {
		ChatReportBuilder chatReportBuilder = new ChatReportBuilder(this.id, this.createdAt, this.reportedProfileId, this.limits);
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
