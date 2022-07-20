/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import java.util.Map;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.Nullable;

public interface CommandSigningContext {
    public static final CommandSigningContext ANONYMOUS = new CommandSigningContext(){

        @Override
        @Nullable
        public PlayerChatMessage getArgument(String string) {
            return null;
        }
    };

    @Nullable
    public PlayerChatMessage getArgument(String var1);

    public record SignedArguments(Map<String, PlayerChatMessage> arguments) implements CommandSigningContext
    {
        @Override
        @Nullable
        public PlayerChatMessage getArgument(String string) {
            return this.arguments.get(string);
        }
    }
}

