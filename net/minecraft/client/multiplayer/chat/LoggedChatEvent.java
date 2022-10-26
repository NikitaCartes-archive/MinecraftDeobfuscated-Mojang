/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.util.StringRepresentable;

@Environment(value=EnvType.CLIENT)
public interface LoggedChatEvent {
    public static final Codec<LoggedChatEvent> CODEC = StringRepresentable.fromEnum(Type::values).dispatch(LoggedChatEvent::type, Type::codec);

    public Type type();

    @Environment(value=EnvType.CLIENT)
    public static enum Type implements StringRepresentable
    {
        PLAYER("player", () -> LoggedChatMessage.Player.CODEC),
        SYSTEM("system", () -> LoggedChatMessage.System.CODEC);

        private final String serializedName;
        private final Supplier<Codec<? extends LoggedChatEvent>> codec;

        private Type(String string2, Supplier<Codec<? extends LoggedChatEvent>> supplier) {
            this.serializedName = string2;
            this.codec = supplier;
        }

        private Codec<? extends LoggedChatEvent> codec() {
            return this.codec.get();
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}

