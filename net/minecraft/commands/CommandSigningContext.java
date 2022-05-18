/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.Crypt;

public interface CommandSigningContext {
    public static final CommandSigningContext NONE = string -> MessageSignature.unsigned();

    public MessageSignature getArgumentSignature(String var1);

    default public boolean signedArgumentPreview(String string) {
        return false;
    }

    public record SignedArguments(UUID sender, Instant timeStamp, ArgumentSignatures argumentSignatures, boolean signedPreview) implements CommandSigningContext
    {
        @Override
        public MessageSignature getArgumentSignature(String string) {
            Crypt.SaltSignaturePair saltSignaturePair = this.argumentSignatures.get(string);
            if (saltSignaturePair != null) {
                return new MessageSignature(this.sender, this.timeStamp, saltSignaturePair);
            }
            return MessageSignature.unsigned();
        }

        @Override
        public boolean signedArgumentPreview(String string) {
            return this.signedPreview;
        }
    }
}

