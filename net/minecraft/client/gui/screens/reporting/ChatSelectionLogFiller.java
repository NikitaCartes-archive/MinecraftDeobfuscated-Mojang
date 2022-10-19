/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.reporting;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportContextBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatSelectionLogFiller {
    private final ChatLog log;
    private final ChatReportContextBuilder contextBuilder;
    private final Predicate<LoggedChatMessage.Player> canReport;
    private int eventId;
    private int missedCount;
    @Nullable
    private PlayerChatMessage lastMessage;

    public ChatSelectionLogFiller(ReportingContext reportingContext, Predicate<LoggedChatMessage.Player> predicate) {
        this.log = reportingContext.chatLog();
        this.contextBuilder = new ChatReportContextBuilder(reportingContext.sender().reportLimits().leadingContextMessageCount());
        this.canReport = predicate;
        this.eventId = this.log.end();
    }

    public void fillNextPage(int i, Output output) {
        LoggedChatEvent loggedChatEvent;
        int j = 0;
        while (j < i && (loggedChatEvent = this.log.lookup(this.eventId)) != null) {
            LoggedChatMessage.Player player;
            int k = this.eventId--;
            if (!(loggedChatEvent instanceof LoggedChatMessage.Player) || (player = (LoggedChatMessage.Player)loggedChatEvent).message().equals(this.lastMessage)) continue;
            if (this.acceptMessage(player)) {
                if (this.missedCount > 0) {
                    output.acceptDivider(Component.translatable("gui.chatSelection.fold", this.missedCount));
                    this.missedCount = 0;
                }
                output.acceptMessage(k, player);
                ++j;
            } else {
                ++this.missedCount;
            }
            this.lastMessage = player.message();
        }
    }

    private boolean acceptMessage(LoggedChatMessage.Player player) {
        PlayerChatMessage playerChatMessage = player.message();
        boolean bl = this.contextBuilder.acceptContext(playerChatMessage);
        if (this.canReport.test(player)) {
            this.contextBuilder.trackContext(playerChatMessage);
            return true;
        }
        return bl;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Output {
        public void acceptMessage(int var1, LoggedChatMessage.Player var2);

        public void acceptDivider(Component var1);
    }
}

