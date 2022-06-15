/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.LoggedChat;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatLogSegmenter {
    private final Function<LoggedChat.WithId, MessageType> typeFunction;
    private final List<LoggedChat.WithId> messages = new ArrayList<LoggedChat.WithId>();
    @Nullable
    private MessageType segmentType;

    public ChatLogSegmenter(Function<LoggedChat.WithId, MessageType> function) {
        this.typeFunction = function;
    }

    public boolean accept(LoggedChat.WithId withId) {
        MessageType messageType = this.typeFunction.apply(withId);
        if (this.segmentType == null || messageType == this.segmentType) {
            this.segmentType = messageType;
            this.messages.add(withId);
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
    public record Results(List<LoggedChat.WithId> messages, MessageType type) {
    }
}

