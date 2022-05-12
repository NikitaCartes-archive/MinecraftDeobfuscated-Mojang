/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ChatDecorator {
    public static final ChatDecorator PLAIN = (serverPlayer, component) -> component;

    @Deprecated
    public static ChatDecorator testRainbowChat() {
        return (serverPlayer, component) -> {
            String string = component.getString().trim();
            int i = string.length();
            float f = Math.nextDown(1.0f) * (float)i;
            MutableComponent mutableComponent = Component.literal(String.valueOf(string.charAt(0))).withStyle(Style.EMPTY.withColor(Mth.hsvToRgb(Math.nextDown(1.0f), 1.0f, 1.0f)));
            for (int j = 1; j < i; ++j) {
                mutableComponent.append(Component.literal(String.valueOf(string.charAt(j))).withStyle(Style.EMPTY.withColor(Mth.hsvToRgb((float)j / f, 1.0f, 1.0f))));
            }
            return mutableComponent;
        };
    }

    public Component decorate(@Nullable ServerPlayer var1, Component var2);

    default public PlayerChatMessage decorate(@Nullable ServerPlayer serverPlayer, Component component, MessageSignature messageSignature, boolean bl) {
        Component component2 = this.decorate(serverPlayer, component);
        if (component.equals(component2)) {
            return PlayerChatMessage.signed(component, messageSignature);
        }
        if (!bl) {
            return PlayerChatMessage.signed(component, messageSignature).withUnsignedContent(component2);
        }
        return PlayerChatMessage.signed(component2, messageSignature);
    }

    default public PlayerChatMessage decorate(@Nullable ServerPlayer serverPlayer, PlayerChatMessage playerChatMessage) {
        return this.decorate(serverPlayer, playerChatMessage.signedContent(), playerChatMessage.signature(), false);
    }
}

