/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record GuiMessage(int addedTime, Component content, @Nullable MessageSignature headerSignature, @Nullable GuiMessageTag tag) {
    @Nullable
    public MessageSignature headerSignature() {
        return this.headerSignature;
    }

    @Nullable
    public GuiMessageTag tag() {
        return this.tag;
    }

    @Environment(value=EnvType.CLIENT)
    public record Line(int addedTime, FormattedCharSequence content, @Nullable GuiMessageTag tag, boolean endOfEntry) {
        @Nullable
        public GuiMessageTag tag() {
            return this.tag;
        }
    }
}

