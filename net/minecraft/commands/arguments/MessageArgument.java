/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.SignedArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import org.jetbrains.annotations.Nullable;

public class MessageArgument
implements SignedArgument<Message> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

    public static MessageArgument message() {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Message message = commandContext.getArgument(string, Message.class);
        return message.resolveComponent(commandContext.getSource());
    }

    public static void resolveChatMessage(CommandContext<CommandSourceStack> commandContext, String string, Consumer<PlayerChatMessage> consumer) throws CommandSyntaxException {
        Message message = commandContext.getArgument(string, Message.class);
        CommandSourceStack commandSourceStack = commandContext.getSource();
        Component component = message.resolveComponent(commandSourceStack);
        CommandSigningContext commandSigningContext = commandSourceStack.getSigningContext();
        PlayerChatMessage playerChatMessage = commandSigningContext.getArgument(string);
        if (playerChatMessage != null) {
            MessageArgument.resolveSignedMessage(consumer, commandSourceStack, playerChatMessage.withUnsignedContent(component));
        } else {
            MessageArgument.resolveDisguisedMessage(consumer, commandSourceStack, PlayerChatMessage.system(message.text).withUnsignedContent(component));
        }
    }

    private static void resolveSignedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        CompletableFuture<FilteredText> completableFuture = MessageArgument.filterPlainText(commandSourceStack, playerChatMessage);
        CompletableFuture<Component> completableFuture2 = minecraftServer.getChatDecorator().decorate(commandSourceStack.getPlayer(), playerChatMessage.decoratedContent());
        commandSourceStack.getChatMessageChainer().append(executor -> CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync(void_ -> {
            PlayerChatMessage playerChatMessage2 = playerChatMessage.withUnsignedContent((Component)completableFuture2.join()).filter(((FilteredText)completableFuture.join()).mask());
            consumer.accept(playerChatMessage2);
        }, executor));
    }

    private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        CompletableFuture<Component> completableFuture = minecraftServer.getChatDecorator().decorate(commandSourceStack.getPlayer(), playerChatMessage.decoratedContent());
        commandSourceStack.getChatMessageChainer().append(executor -> completableFuture.thenAcceptAsync(component -> consumer.accept(playerChatMessage.withUnsignedContent((Component)component)), executor));
    }

    private static CompletableFuture<FilteredText> filterPlainText(CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
        ServerPlayer serverPlayer = commandSourceStack.getPlayer();
        if (serverPlayer != null && playerChatMessage.hasSignatureFrom(serverPlayer.getUUID())) {
            return serverPlayer.getTextFilter().processStreamMessage(playerChatMessage.signedContent());
        }
        return CompletableFuture.completedFuture(FilteredText.passThrough(playerChatMessage.signedContent()));
    }

    @Override
    public Message parse(StringReader stringReader) throws CommandSyntaxException {
        return Message.parseText(stringReader, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class Message {
        final String text;
        private final Part[] parts;

        public Message(String string, Part[] parts) {
            this.text = string;
            this.parts = parts;
        }

        public String getText() {
            return this.text;
        }

        public Part[] getParts() {
            return this.parts;
        }

        Component resolveComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            return this.toComponent(commandSourceStack, commandSourceStack.hasPermission(2));
        }

        public Component toComponent(CommandSourceStack commandSourceStack, boolean bl) throws CommandSyntaxException {
            if (this.parts.length == 0 || !bl) {
                return Component.literal(this.text);
            }
            MutableComponent mutableComponent = Component.literal(this.text.substring(0, this.parts[0].getStart()));
            int i = this.parts[0].getStart();
            for (Part part : this.parts) {
                Component component = part.toComponent(commandSourceStack);
                if (i < part.getStart()) {
                    mutableComponent.append(this.text.substring(i, part.getStart()));
                }
                if (component != null) {
                    mutableComponent.append(component);
                }
                i = part.getEnd();
            }
            if (i < this.text.length()) {
                mutableComponent.append(this.text.substring(i));
            }
            return mutableComponent;
        }

        public static Message parseText(StringReader stringReader, boolean bl) throws CommandSyntaxException {
            String string = stringReader.getString().substring(stringReader.getCursor(), stringReader.getTotalLength());
            if (!bl) {
                stringReader.setCursor(stringReader.getTotalLength());
                return new Message(string, new Part[0]);
            }
            ArrayList<Part> list = Lists.newArrayList();
            int i = stringReader.getCursor();
            while (stringReader.canRead()) {
                if (stringReader.peek() == '@') {
                    EntitySelector entitySelector;
                    int j = stringReader.getCursor();
                    try {
                        EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
                        entitySelector = entitySelectorParser.parse();
                    } catch (CommandSyntaxException commandSyntaxException) {
                        if (commandSyntaxException.getType() == EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE || commandSyntaxException.getType() == EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                            stringReader.setCursor(j + 1);
                            continue;
                        }
                        throw commandSyntaxException;
                    }
                    list.add(new Part(j - i, stringReader.getCursor() - i, entitySelector));
                    continue;
                }
                stringReader.skip();
            }
            return new Message(string, list.toArray(new Part[0]));
        }
    }

    public static class Part {
        private final int start;
        private final int end;
        private final EntitySelector selector;

        public Part(int i, int j, EntitySelector entitySelector) {
            this.start = i;
            this.end = j;
            this.selector = entitySelector;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public EntitySelector getSelector() {
            return this.selector;
        }

        @Nullable
        public Component toComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            return EntitySelector.joinNames(this.selector.findEntities(commandSourceStack));
        }
    }
}

