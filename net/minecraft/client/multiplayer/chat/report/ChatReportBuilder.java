/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportContextBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatReportBuilder {
    private final ChatReport report;
    private final AbuseReportLimits limits;

    public ChatReportBuilder(ChatReport chatReport, AbuseReportLimits abuseReportLimits) {
        this.report = chatReport;
        this.limits = abuseReportLimits;
    }

    public ChatReportBuilder(UUID uUID, AbuseReportLimits abuseReportLimits) {
        this.report = new ChatReport(UUID.randomUUID(), Instant.now(), uUID);
        this.limits = abuseReportLimits;
    }

    public ChatReport report() {
        return this.report;
    }

    public UUID reportedProfileId() {
        return this.report.reportedProfileId;
    }

    public IntSet reportedMessages() {
        return this.report.reportedMessages;
    }

    public String comments() {
        return this.report.comments;
    }

    public void setComments(String string) {
        this.report.comments = string;
    }

    @Nullable
    public ReportReason reason() {
        return this.report.reason;
    }

    public void setReason(ReportReason reportReason) {
        this.report.reason = reportReason;
    }

    public void toggleReported(int i) {
        this.report.toggleReported(i, this.limits);
    }

    public boolean isReported(int i) {
        return this.report.reportedMessages.contains(i);
    }

    public boolean hasContent() {
        return StringUtils.isNotEmpty(this.comments()) || !this.reportedMessages().isEmpty() || this.reason() != null;
    }

    @Nullable
    public CannotBuildReason checkBuildable() {
        if (this.report.reportedMessages.isEmpty()) {
            return CannotBuildReason.NO_REPORTED_MESSAGES;
        }
        if (this.report.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
            return CannotBuildReason.TOO_MANY_MESSAGES;
        }
        if (this.report.reason == null) {
            return CannotBuildReason.NO_REASON;
        }
        if (this.report.comments.length() > this.limits.maxOpinionCommentsLength()) {
            return CannotBuildReason.COMMENTS_TOO_LONG;
        }
        return null;
    }

    public Either<Result, CannotBuildReason> build(ReportingContext reportingContext) {
        CannotBuildReason cannotBuildReason = this.checkBuildable();
        if (cannotBuildReason != null) {
            return Either.right(cannotBuildReason);
        }
        String string = Objects.requireNonNull(this.report.reason).backendName();
        ReportEvidence reportEvidence = this.buildEvidence(reportingContext.chatLog());
        ReportedEntity reportedEntity = new ReportedEntity(this.report.reportedProfileId);
        AbuseReport abuseReport = new AbuseReport(this.report.comments, string, reportEvidence, reportedEntity, this.report.createdAt);
        return Either.left(new Result(this.report.reportId, abuseReport));
    }

    private ReportEvidence buildEvidence(ChatLog chatLog) {
        ArrayList list = new ArrayList();
        ChatReportContextBuilder chatReportContextBuilder = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
        chatReportContextBuilder.collectAllContext(chatLog, this.report.reportedMessages, (i, player) -> list.add(this.buildReportedChatMessage(player, this.isReported(i))));
        return new ReportEvidence(Lists.reverse(list));
    }

    private ReportChatMessage buildReportedChatMessage(LoggedChatMessage.Player player, boolean bl) {
        SignedMessageLink signedMessageLink = player.message().link();
        SignedMessageBody signedMessageBody = player.message().signedBody();
        List<ByteBuffer> list = signedMessageBody.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
        ByteBuffer byteBuffer = Optionull.map(player.message().signature(), MessageSignature::asByteBuffer);
        return new ReportChatMessage(signedMessageLink.index(), signedMessageLink.sender(), signedMessageLink.sessionId(), signedMessageBody.timeStamp(), signedMessageBody.salt(), list, signedMessageBody.content(), byteBuffer, bl);
    }

    public ChatReportBuilder copy() {
        return new ChatReportBuilder(this.report.copy(), this.limits);
    }

    @Environment(value=EnvType.CLIENT)
    public class ChatReport {
        final UUID reportId;
        final Instant createdAt;
        final UUID reportedProfileId;
        final IntSet reportedMessages = new IntOpenHashSet();
        String comments = "";
        @Nullable
        ReportReason reason;

        ChatReport(UUID uUID, Instant instant, UUID uUID2) {
            this.reportId = uUID;
            this.createdAt = instant;
            this.reportedProfileId = uUID2;
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

        public boolean isReportedPlayer(UUID uUID) {
            return uUID.equals(this.reportedProfileId);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record CannotBuildReason(Component message) {
        public static final CannotBuildReason NO_REASON = new CannotBuildReason(Component.translatable("gui.chatReport.send.no_reason"));
        public static final CannotBuildReason NO_REPORTED_MESSAGES = new CannotBuildReason(Component.translatable("gui.chatReport.send.no_reported_messages"));
        public static final CannotBuildReason TOO_MANY_MESSAGES = new CannotBuildReason(Component.translatable("gui.chatReport.send.too_many_messages"));
        public static final CannotBuildReason COMMENTS_TOO_LONG = new CannotBuildReason(Component.translatable("gui.chatReport.send.comments_too_long"));
    }

    @Environment(value=EnvType.CLIENT)
    public record Result(UUID id, AbuseReport report) {
    }
}

