/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatLogSegmenter {
    private final Function<ChatLog.Entry<LoggedChatMessage>, MessageType> typeFunction;
    private final List<ChatLog.Entry<LoggedChatMessage>> messages = new ArrayList<ChatLog.Entry<LoggedChatMessage>>();
    @Nullable
    private MessageType segmentType;

    public ChatLogSegmenter(Function<ChatLog.Entry<LoggedChatMessage>, MessageType> function) {
        this.typeFunction = function;
    }

    public boolean accept(ChatLog.Entry<LoggedChatMessage> entry) {
        MessageType messageType = this.typeFunction.apply(entry);
        if (this.segmentType == null || messageType == this.segmentType) {
            this.segmentType = messageType;
            this.messages.add(entry);
            return true;
        }
        return false;
    }

    @Nullable
    public Results build() {
        if (!this.messages.isEmpty() && this.segmentType != null) {
            return new Results(this.messages, this.segmentType);
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum MessageType {
        REPORTABLE,
        CONTEXT;


        public boolean foldable() {
            return this == CONTEXT;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Results(List<ChatLog.Entry<LoggedChatMessage>> messages, MessageType type) {
    }
}

