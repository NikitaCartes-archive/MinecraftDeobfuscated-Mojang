/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.SignedArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MessageArgument
implements SignedArgument<Message> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
    static final Logger LOGGER = LogUtils.getLogger();

    public static MessageArgument message() {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Message message = commandContext.getArgument(string, Message.class);
        return message.resolveComponent(commandContext.getSource());
    }

    public static ChatMessage getChatMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Message message = commandContext.getArgument(string, Message.class);
        Component component = message.resolveComponent(commandContext.getSource());
        CommandSigningContext commandSigningContext = commandContext.getSource().getSigningContext();
        CommandSigningContext.SignedArgument signedArgument = commandSigningContext.getArgument(string);
        return new ChatMessage(message.text, component, signedArgument);
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
    public String getSignableText(Message message) {
        return message.getText();
    }

    @Override
    public CompletableFuture<Component> resolvePreview(CommandSourceStack commandSourceStack, Message message) throws CommandSyntaxException {
        return message.resolveDecoratedComponent(commandSourceStack);
    }

    @Override
    public Class<Message> getValueType() {
        return Message.class;
    }

    static void logResolutionFailure(CommandSourceStack commandSourceStack, CompletableFuture<?> completableFuture) {
        completableFuture.exceptionally(throwable -> {
            LOGGER.error("Encountered unexpected exception while resolving chat message argument from '{}'", (Object)commandSourceStack.getDisplayName().getString(), throwable);
            return null;
        });
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

        CompletableFuture<Component> resolveDecoratedComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            Component component = this.resolveComponent(commandSourceStack);
            CompletableFuture<Component> completableFuture = commandSourceStack.getServer().getChatDecorator().decorate(commandSourceStack.getPlayer(), component);
            MessageArgument.logResolutionFailure(commandSourceStack, completableFuture);
            return completableFuture;
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

    public record ChatMessage(String plain, Component formatted, CommandSigningContext.SignedArgument signedArgument) {
        public void resolve(CommandSourceStack commandSourceStack, Consumer<FilteredText<PlayerChatMessage>> consumer) {
            commandSourceStack.getChatMessageChainer().append(() -> ((CompletableFuture)((CompletableFuture)this.filterPlainText(commandSourceStack, this.plain).thenComposeAsync(filteredText -> {
                FilteredText<Component> filteredText2 = this.rebuildMessageIfNeeded(commandSourceStack, (FilteredText<String>)filteredText);
                return this.resolveFiltered(commandSourceStack, (FilteredText<String>)filteredText, filteredText2);
            }, (Executor)commandSourceStack.getServer())).thenApply(filteredText -> {
                PlayerChatMessage playerChatMessage = (PlayerChatMessage)filteredText.raw();
                if (playerChatMessage.hasExpiredServer(Instant.now())) {
                    LOGGER.warn("{} sent expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)commandSourceStack.getDisplayName().getString(), (Object)playerChatMessage.signedContent().plain().getString());
                }
                return filteredText;
            })).thenAcceptAsync(consumer, (Executor)commandSourceStack.getServer()));
        }

        private FilteredText<Component> rebuildMessageIfNeeded(CommandSourceStack commandSourceStack, FilteredText<String> filteredText) {
            return filteredText.mapWithEquality(string -> this.formatted, string -> this.rebuildFilteredMessage(commandSourceStack, (String)string));
        }

        @Nullable
        private Component rebuildFilteredMessage(CommandSourceStack commandSourceStack, String string) {
            try {
                Message message = Message.parseText(new StringReader(string), true);
                return message.resolveComponent(commandSourceStack);
            } catch (CommandSyntaxException commandSyntaxException) {
                return null;
            }
        }

        private CompletableFuture<FilteredText<PlayerChatMessage>> resolveFiltered(CommandSourceStack commandSourceStack, FilteredText<String> filteredText, FilteredText<Component> filteredText22) {
            ChatDecorator chatDecorator = commandSourceStack.getServer().getChatDecorator();
            ServerPlayer serverPlayer = commandSourceStack.getPlayer();
            CommandSigningContext commandSigningContext = commandSourceStack.getSigningContext();
            SignedMessageChain.Decoder decoder = commandSigningContext.decoder();
            MessageSigner messageSigner = commandSigningContext.argumentSigner();
            SignedMessageChain.Link link = new SignedMessageChain.Link(this.signedArgument.signature());
            if (this.signedArgument.signedPreview()) {
                return chatDecorator.decorateFiltered(serverPlayer, filteredText22).thenApply(filteredText2 -> decoder.unpack(link, messageSigner, ChatMessageContent.fromFiltered(filteredText, filteredText2), this.signedArgument.lastSeenMessages()));
            }
            FilteredText<PlayerChatMessage> filteredText3 = decoder.unpack(link, messageSigner, ChatMessageContent.fromFiltered(filteredText), this.signedArgument.lastSeenMessages());
            return chatDecorator.decorateFiltered(serverPlayer, filteredText22).thenApply(filteredText2 -> ChatDecorator.attachUnsignedDecoration(filteredText3, filteredText2));
        }

        private CompletableFuture<FilteredText<String>> filterPlainText(CommandSourceStack commandSourceStack, String string) {
            ServerPlayer serverPlayer = commandSourceStack.getPlayer();
            if (serverPlayer != null) {
                return serverPlayer.getTextFilter().processStreamMessage(string);
            }
            return CompletableFuture.completedFuture(FilteredText.passThrough(string));
        }

        public void consume(CommandSourceStack commandSourceStack) {
            if (!commandSourceStack.getSigningContext().argumentSigner().isSystem()) {
                this.resolve(commandSourceStack, filteredText -> {
                    PlayerList playerList = commandSourceStack.getServer().getPlayerList();
                    playerList.broadcastMessageHeader((PlayerChatMessage)filteredText.raw(), Set.of());
                });
            }
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

