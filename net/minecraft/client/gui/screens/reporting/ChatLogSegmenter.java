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
public class ChatLogSegmenter<T extends LoggedChatMessage> {
    private final Function<ChatLog.Entry<T>, MessageType> typeFunction;
    private final List<ChatLog.Entry<T>> messages = new ArrayList<ChatLog.Entry<T>>();
    @Nullable
    private MessageType segmentType;

    public ChatLogSegmenter(Function<ChatLog.Entry<T>, MessageType> function) {
        this.typeFunction = function;
    }

    public boolean accept(ChatLog.Entry<T> entry) {
        MessageType messageType = this.typeFunction.apply(entry);
        if (this.segmentType == null || messageType == this.segmentType) {
            this.segmentType = messageType;
            this.messages.add(entry);
            return true;
        }
        return false;
    }

    @Nullable
    public Results<T> build() {
        if (!this.messages.isEmpty() && this.segmentType != null) {
            return new Results<T>(this.messages, this.segmentType);
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
    public record Results<T extends LoggedChatMessage>(List<ChatLog.Entry<T>> messages, MessageType type) {
    }
}

