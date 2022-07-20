/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportChatMessageBody;
import com.mojang.authlib.minecraft.report.ReportChatMessageContent;
import com.mojang.authlib.minecraft.report.ReportChatMessageHeader;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.LoggedChatMessageLink;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
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
    public CannotBuildReason checkBuildable() {
        if (this.reportedMessages.isEmpty()) {
            return CannotBuildReason.NO_REPORTED_MESSAGES;
        }
        if (this.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
            return CannotBuildReason.TOO_MANY_MESSAGES;
        }
        if (this.reason == null) {
            return CannotBuildReason.NO_REASON;
        }
        if (this.comments.length() > this.limits.maxOpinionCommentsLength()) {
            return CannotBuildReason.COMMENTS_TOO_LONG;
        }
        return null;
    }

    public Either<Result, CannotBuildReason> build(ReportingContext reportingContext) {
        CannotBuildReason cannotBuildReason = this.checkBuildable();
        if (cannotBuildReason != null) {
            return Either.right(cannotBuildReason);
        }
        String string = Objects.requireNonNull(this.reason).backendName();
        ReportEvidence reportEvidence = this.buildEvidence(reportingContext.chatLog());
        if (reportEvidence.messages.size() > this.limits.maxEvidenceMessageCount()) {
            return Either.right(CannotBuildReason.TOO_MANY_MESSAGES);
        }
        ReportedEntity reportedEntity = new ReportedEntity(this.reportedProfileId);
        AbuseReport abuseReport = new AbuseReport(this.comments, string, reportEvidence, reportedEntity, this.createdAt);
        return Either.left(new Result(this.reportId, abuseReport));
    }

    private ReportEvidence buildEvidence(ChatLog chatLog) {
        Int2ObjectRBTreeMap int2ObjectSortedMap = new Int2ObjectRBTreeMap();
        this.reportedMessages.forEach(i -> {
            Int2ObjectMap<LoggedChatMessage.Player> int2ObjectMap = ChatReportBuilder.collectReferencedContext(chatLog, i, this.limits);
            ObjectOpenHashSet set = new ObjectOpenHashSet();
            for (Int2ObjectMap.Entry entry2 : Int2ObjectMaps.fastIterable(int2ObjectMap)) {
                int j = entry2.getIntKey();
                LoggedChatMessage.Player player = (LoggedChatMessage.Player)entry2.getValue();
                int2ObjectSortedMap.put(j, this.buildReportedChatMessage(j, player));
                set.add(player.profileId());
            }
            for (UUID uUID : set) {
                this.chainForPlayer(chatLog, int2ObjectMap, uUID).forEach(entry -> {
                    LoggedChatMessageLink loggedChatMessageLink = (LoggedChatMessageLink)entry.event();
                    if (loggedChatMessageLink instanceof LoggedChatMessage.Player) {
                        LoggedChatMessage.Player player = (LoggedChatMessage.Player)loggedChatMessageLink;
                        int2ObjectSortedMap.putIfAbsent(entry.id(), this.buildReportedChatMessage(entry.id(), player));
                    } else {
                        int2ObjectSortedMap.putIfAbsent(entry.id(), this.buildReportedChatHeader(loggedChatMessageLink));
                    }
                });
            }
        });
        return new ReportEvidence(new ArrayList<ReportChatMessage>(int2ObjectSortedMap.values()));
    }

    private Stream<ChatLog.Entry<LoggedChatMessageLink>> chainForPlayer(ChatLog chatLog, Int2ObjectMap<LoggedChatMessage.Player> int2ObjectMap, UUID uUID) {
        int i = Integer.MAX_VALUE;
        int j = Integer.MIN_VALUE;
        for (Int2ObjectMap.Entry entry2 : Int2ObjectMaps.fastIterable(int2ObjectMap)) {
            LoggedChatMessage.Player player = (LoggedChatMessage.Player)entry2.getValue();
            if (!player.profileId().equals(uUID)) continue;
            int k = entry2.getIntKey();
            i = Math.min(i, k);
            j = Math.max(j, k);
        }
        return chatLog.selectBetween(i, j).entries().map(entry -> entry.tryCast(LoggedChatMessageLink.class)).filter(Objects::nonNull).filter(entry -> ((LoggedChatMessageLink)entry.event()).header().sender().equals(uUID));
    }

    private static Int2ObjectMap<LoggedChatMessage.Player> collectReferencedContext(ChatLog chatLog, int i, AbuseReportLimits abuseReportLimits) {
        int j2 = abuseReportLimits.leadingContextMessageCount() + 1;
        Int2ObjectOpenHashMap<LoggedChatMessage.Player> int2ObjectMap = new Int2ObjectOpenHashMap<LoggedChatMessage.Player>();
        ChatReportBuilder.walkMessageReferenceGraph(chatLog, i, (j, player) -> {
            int2ObjectMap.put(j, player);
            return int2ObjectMap.size() < j2;
        });
        ChatReportBuilder.trailingContext(chatLog, i, abuseReportLimits.trailingContextMessageCount()).forEach(entry -> int2ObjectMap.put(entry.id(), (LoggedChatMessage.Player)entry.event()));
        return int2ObjectMap;
    }

    private static Stream<ChatLog.Entry<LoggedChatMessage.Player>> trailingContext(ChatLog chatLog, int i, int j) {
        return chatLog.selectAfter(chatLog.after(i)).entries().map(entry -> entry.tryCast(LoggedChatMessage.Player.class)).filter(Objects::nonNull).limit(j);
    }

    private static void walkMessageReferenceGraph(ChatLog chatLog, int i, ReferencedMessageVisitor referencedMessageVisitor) {
        IntArrayPriorityQueue intPriorityQueue = new IntArrayPriorityQueue(IntComparators.OPPOSITE_COMPARATOR);
        intPriorityQueue.enqueue(i);
        IntOpenHashSet intSet = new IntOpenHashSet();
        intSet.add(i);
        while (!intPriorityQueue.isEmpty()) {
            int j = intPriorityQueue.dequeueInt();
            Object object = chatLog.lookup(j);
            if (!(object instanceof LoggedChatMessage.Player)) continue;
            LoggedChatMessage.Player player = (LoggedChatMessage.Player)object;
            if (!referencedMessageVisitor.accept(j, player)) break;
            object = ChatReportBuilder.messageReferences(chatLog, j, player.message()).iterator();
            while (object.hasNext()) {
                int k = (Integer)object.next();
                if (!intSet.add(k)) continue;
                intPriorityQueue.enqueue(k);
            }
        }
    }

    private static IntCollection messageReferences(ChatLog chatLog, int i, PlayerChatMessage playerChatMessage) {
        Set set = playerChatMessage.signedBody().lastSeen().entries().stream().map(LastSeenMessages.Entry::lastSignature).collect(Collectors.toCollection(ObjectOpenHashSet::new));
        MessageSignature messageSignature = playerChatMessage.signedHeader().previousSignature();
        if (messageSignature != null) {
            set.add(messageSignature);
        }
        IntArrayList intList = new IntArrayList();
        Iterator iterator = chatLog.selectBefore(i).entries().iterator();
        while (iterator.hasNext() && !set.isEmpty()) {
            LoggedChatMessage.Player player;
            ChatLog.Entry entry = (ChatLog.Entry)iterator.next();
            Object t = entry.event();
            if (!(t instanceof LoggedChatMessage.Player) || !set.remove((player = (LoggedChatMessage.Player)t).headerSignature())) continue;
            intList.add(entry.id());
        }
        return intList;
    }

    private ReportChatMessage buildReportedChatMessage(int i, LoggedChatMessage.Player player) {
        PlayerChatMessage playerChatMessage = player.message();
        SignedMessageBody signedMessageBody = playerChatMessage.signedBody();
        Instant instant = playerChatMessage.timeStamp();
        long l = playerChatMessage.salt();
        ByteBuffer byteBuffer = playerChatMessage.headerSignature().asByteBuffer();
        ByteBuffer byteBuffer2 = Util.mapNullable(playerChatMessage.signedHeader().previousSignature(), MessageSignature::asByteBuffer);
        ByteBuffer byteBuffer3 = ByteBuffer.wrap(signedMessageBody.hash().asBytes());
        ReportChatMessageContent reportChatMessageContent = new ReportChatMessageContent(playerChatMessage.signedContent().plain(), playerChatMessage.signedContent().isDecorated() ? ChatReportBuilder.encodeComponent(playerChatMessage.signedContent().decorated()) : null);
        String string = playerChatMessage.unsignedContent().map(ChatReportBuilder::encodeComponent).orElse(null);
        List<ReportChatMessageBody.LastSeenSignature> list = signedMessageBody.lastSeen().entries().stream().map(entry -> new ReportChatMessageBody.LastSeenSignature(entry.profileId(), entry.lastSignature().asByteBuffer())).toList();
        return new ReportChatMessage(new ReportChatMessageHeader(byteBuffer2, player.profileId(), byteBuffer3, byteBuffer), new ReportChatMessageBody(instant, l, list, reportChatMessageContent), string, this.isReported(i));
    }

    private ReportChatMessage buildReportedChatHeader(LoggedChatMessageLink loggedChatMessageLink) {
        ByteBuffer byteBuffer = loggedChatMessageLink.headerSignature().asByteBuffer();
        ByteBuffer byteBuffer2 = Util.mapNullable(loggedChatMessageLink.header().previousSignature(), MessageSignature::asByteBuffer);
        return new ReportChatMessage(new ReportChatMessageHeader(byteBuffer2, loggedChatMessageLink.header().sender(), ByteBuffer.wrap(loggedChatMessageLink.bodyDigest()), byteBuffer), null, null, false);
    }

    private static String encodeComponent(Component component) {
        return Component.Serializer.toStableJson(component);
    }

    public ChatReportBuilder copy() {
        ChatReportBuilder chatReportBuilder = new ChatReportBuilder(this.reportId, this.createdAt, this.reportedProfileId, this.limits);
        chatReportBuilder.reportedMessages.addAll(this.reportedMessages);
        chatReportBuilder.comments = this.comments;
        chatReportBuilder.reason = this.reason;
        return chatReportBuilder;
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

    @Environment(value=EnvType.CLIENT)
    static interface ReferencedMessageVisitor {
        public boolean accept(int var1, LoggedChatMessage.Player var2);
    }
}

