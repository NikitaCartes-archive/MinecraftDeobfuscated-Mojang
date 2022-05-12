/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.Crypt;

public interface CommandSigningContext {
    public static final CommandSigningContext NONE = (commandContext, string, component) -> PlayerChatMessage.unsigned(component);

    public PlayerChatMessage signArgument(CommandContext<CommandSourceStack> var1, String var2, Component var3) throws CommandSyntaxException;

    public record PlainArguments(UUID sender, Instant timeStamp, ArgumentSignatures argumentSignatures) implements CommandSigningContext
    {
        @Override
        public PlayerChatMessage signArgument(CommandContext<CommandSourceStack> commandContext, String string, Component component) {
            Crypt.SaltSignaturePair saltSignaturePair = this.argumentSignatures.get(string);
            if (saltSignaturePair != null) {
                return PlayerChatMessage.signed(component, new MessageSignature(this.sender, this.timeStamp, saltSignaturePair));
            }
            return PlayerChatMessage.unsigned(component);
        }
    }
}

