/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.SignedMessageChain;

public interface CommandSigningContext {
    public static CommandSigningContext anonymous() {
        final MessageSigner messageSigner = MessageSigner.system();
        return new CommandSigningContext(){

            @Override
            public SignedArgument getArgument(String string) {
                return SignedArgument.UNSIGNED;
            }

            @Override
            public MessageSigner argumentSigner() {
                return messageSigner;
            }

            @Override
            public SignedMessageChain.Decoder decoder() {
                return SignedMessageChain.Decoder.UNSIGNED;
            }
        };
    }

    public SignedArgument getArgument(String var1);

    public MessageSigner argumentSigner();

    public SignedMessageChain.Decoder decoder();

    public record SignedArguments(SignedMessageChain.Decoder decoder, MessageSigner argumentSigner, ArgumentSignatures argumentSignatures, boolean signedPreview, LastSeenMessages lastSeenMessages) implements CommandSigningContext
    {
        @Override
        public SignedArgument getArgument(String string) {
            return new SignedArgument(this.argumentSignatures.get(string), this.signedPreview, this.lastSeenMessages);
        }
    }

    public record SignedArgument(MessageSignature signature, boolean signedPreview, LastSeenMessages lastSeenMessages) {
        public static final SignedArgument UNSIGNED = new SignedArgument(MessageSignature.EMPTY, false, LastSeenMessages.EMPTY);
    }
}

