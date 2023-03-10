/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CommandSuggestions {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private static final Style UNPARSED_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final Style LITERAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final List<Style> ARGUMENT_STYLES = Stream.of(ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());
    final Minecraft minecraft;
    final Screen screen;
    final EditBox input;
    final Font font;
    private final boolean commandsOnly;
    private final boolean onlyShowIfCursorPastError;
    final int lineStartOffset;
    final int suggestionLineLimit;
    final boolean anchorToBottom;
    final int fillColor;
    private final List<FormattedCharSequence> commandUsage = Lists.newArrayList();
    private int commandUsagePosition;
    private int commandUsageWidth;
    @Nullable
    private ParseResults<SharedSuggestionProvider> currentParse;
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Nullable
    private SuggestionsList suggestions;
    private boolean allowSuggestions;
    boolean keepSuggestions;

    public CommandSuggestions(Minecraft minecraft, Screen screen, EditBox editBox, Font font, boolean bl, boolean bl2, int i, int j, boolean bl3, int k) {
        this.minecraft = minecraft;
        this.screen = screen;
        this.input = editBox;
        this.font = font;
        this.commandsOnly = bl;
        this.onlyShowIfCursorPastError = bl2;
        this.lineStartOffset = i;
        this.suggestionLineLimit = j;
        this.anchorToBottom = bl3;
        this.fillColor = k;
        editBox.setFormatter(this::formatChat);
    }

    public void setAllowSuggestions(boolean bl) {
        this.allowSuggestions = bl;
        if (!bl) {
            this.suggestions = null;
        }
    }

    public boolean keyPressed(int i, int j, int k) {
        if (this.suggestions != null && this.suggestions.keyPressed(i, j, k)) {
            return true;
        }
        if (this.screen.getFocused() == this.input && i == 258) {
            this.showSuggestions(true);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double d) {
        return this.suggestions != null && this.suggestions.mouseScrolled(Mth.clamp(d, -1.0, 1.0));
    }

    public boolean mouseClicked(double d, double e, int i) {
        return this.suggestions != null && this.suggestions.mouseClicked((int)d, (int)e, i);
    }

    public void showSuggestions(boolean bl) {
        Suggestions suggestions;
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone() && !(suggestions = this.pendingSuggestions.join()).isEmpty()) {
            int i = 0;
            for (Suggestion suggestion : suggestions.getList()) {
                i = Math.max(i, this.font.width(suggestion.getText()));
            }
            int j = Mth.clamp(this.input.getScreenX(suggestions.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
            int k = this.anchorToBottom ? this.screen.height - 12 : 72;
            this.suggestions = new SuggestionsList(j, k, i, this.sortSuggestions(suggestions), bl);
        }
    }

    public void hide() {
        this.suggestions = null;
    }

    private List<Suggestion> sortSuggestions(Suggestions suggestions) {
        String string = this.input.getValue().substring(0, this.input.getCursorPosition());
        int i = CommandSuggestions.getLastWordIndex(string);
        String string2 = string.substring(i).toLowerCase(Locale.ROOT);
        ArrayList<Suggestion> list = Lists.newArrayList();
        ArrayList<Suggestion> list2 = Lists.newArrayList();
        for (Suggestion suggestion : suggestions.getList()) {
            if (suggestion.getText().startsWith(string2) || suggestion.getText().startsWith("minecraft:" + string2)) {
                list.add(suggestion);
                continue;
            }
            list2.add(suggestion);
        }
        list.addAll(list2);
        return list;
    }

    public void updateCommandInfo() {
        boolean bl;
        String string = this.input.getValue();
        if (this.currentParse != null && !this.currentParse.getReader().getString().equals(string)) {
            this.currentParse = null;
        }
        if (!this.keepSuggestions) {
            this.input.setSuggestion(null);
            this.suggestions = null;
        }
        this.commandUsage.clear();
        StringReader stringReader = new StringReader(string);
        boolean bl2 = bl = stringReader.canRead() && stringReader.peek() == '/';
        if (bl) {
            stringReader.skip();
        }
        boolean bl22 = this.commandsOnly || bl;
        int i = this.input.getCursorPosition();
        if (bl22) {
            int j;
            CommandDispatcher<SharedSuggestionProvider> commandDispatcher = this.minecraft.player.connection.getCommands();
            if (this.currentParse == null) {
                this.currentParse = commandDispatcher.parse(stringReader, (SharedSuggestionProvider)this.minecraft.player.connection.getSuggestionsProvider());
            }
            int n = j = this.onlyShowIfCursorPastError ? stringReader.getCursor() : 1;
            if (!(i < j || this.suggestions != null && this.keepSuggestions)) {
                this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.currentParse, i);
                this.pendingSuggestions.thenRun(() -> {
                    if (!this.pendingSuggestions.isDone()) {
                        return;
                    }
                    this.updateUsageInfo();
                });
            }
        } else {
            String string2 = string.substring(0, i);
            int j = CommandSuggestions.getLastWordIndex(string2);
            Collection<String> collection = this.minecraft.player.connection.getSuggestionsProvider().getCustomTabSugggestions();
            this.pendingSuggestions = SharedSuggestionProvider.suggest(collection, new SuggestionsBuilder(string2, j));
        }
    }

    private static int getLastWordIndex(String string) {
        if (Strings.isNullOrEmpty(string)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = WHITESPACE_PATTERN.matcher(string);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }

    private static FormattedCharSequence getExceptionMessage(CommandSyntaxException commandSyntaxException) {
        Component component = ComponentUtils.fromMessage(commandSyntaxException.getRawMessage());
        String string = commandSyntaxException.getContext();
        if (string == null) {
            return component.getVisualOrderText();
        }
        return Component.translatable("command.context.parse_error", component, commandSyntaxException.getCursor(), string).getVisualOrderText();
    }

    private void updateUsageInfo() {
        if (this.input.getCursorPosition() == this.input.getValue().length()) {
            if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
                int i = 0;
                for (Map.Entry<CommandNode<SharedSuggestionProvider>, CommandSyntaxException> entry : this.currentParse.getExceptions().entrySet()) {
                    CommandSyntaxException commandSyntaxException = entry.getValue();
                    if (commandSyntaxException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                        ++i;
                        continue;
                    }
                    this.commandUsage.add(CommandSuggestions.getExceptionMessage(commandSyntaxException));
                }
                if (i > 0) {
                    this.commandUsage.add(CommandSuggestions.getExceptionMessage(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
                }
            } else if (this.currentParse.getReader().canRead()) {
                this.commandUsage.add(CommandSuggestions.getExceptionMessage(Commands.getParseException(this.currentParse)));
            }
        }
        this.commandUsagePosition = 0;
        this.commandUsageWidth = this.screen.width;
        if (this.commandUsage.isEmpty()) {
            this.fillNodeUsage(ChatFormatting.GRAY);
        }
        this.suggestions = null;
        if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get().booleanValue()) {
            this.showSuggestions(false);
        }
    }

    private void fillNodeUsage(ChatFormatting chatFormatting) {
        CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = this.currentParse.getContext();
        SuggestionContext<SharedSuggestionProvider> suggestionContext = commandContextBuilder.findSuggestionContext(this.input.getCursorPosition());
        Map<CommandNode<SharedSuggestionProvider>, String> map = this.minecraft.player.connection.getCommands().getSmartUsage(suggestionContext.parent, this.minecraft.player.connection.getSuggestionsProvider());
        ArrayList<FormattedCharSequence> list = Lists.newArrayList();
        int i = 0;
        Style style = Style.EMPTY.withColor(chatFormatting);
        for (Map.Entry<CommandNode<SharedSuggestionProvider>, String> entry : map.entrySet()) {
            if (entry.getKey() instanceof LiteralCommandNode) continue;
            list.add(FormattedCharSequence.forward(entry.getValue(), style));
            i = Math.max(i, this.font.width(entry.getValue()));
        }
        if (!list.isEmpty()) {
            this.commandUsage.addAll(list);
            this.commandUsagePosition = Mth.clamp(this.input.getScreenX(suggestionContext.startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
            this.commandUsageWidth = i;
        }
    }

    private FormattedCharSequence formatChat(String string, int i) {
        if (this.currentParse != null) {
            return CommandSuggestions.formatText(this.currentParse, string, i);
        }
        return FormattedCharSequence.forward(string, Style.EMPTY);
    }

    @Nullable
    static String calculateSuggestionSuffix(String string, String string2) {
        if (string2.startsWith(string)) {
            return string2.substring(string.length());
        }
        return null;
    }

    private static FormattedCharSequence formatText(ParseResults<SharedSuggestionProvider> parseResults, String string, int i) {
        int n;
        ArrayList<FormattedCharSequence> list = Lists.newArrayList();
        int j = 0;
        int k = -1;
        CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = parseResults.getContext().getLastChild();
        for (ParsedArgument<SharedSuggestionProvider, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
            int l;
            if (++k >= ARGUMENT_STYLES.size()) {
                k = 0;
            }
            if ((l = Math.max(parsedArgument.getRange().getStart() - i, 0)) >= string.length()) break;
            int m = Math.min(parsedArgument.getRange().getEnd() - i, string.length());
            if (m <= 0) continue;
            list.add(FormattedCharSequence.forward(string.substring(j, l), LITERAL_STYLE));
            list.add(FormattedCharSequence.forward(string.substring(l, m), ARGUMENT_STYLES.get(k)));
            j = m;
        }
        if (parseResults.getReader().canRead() && (n = Math.max(parseResults.getReader().getCursor() - i, 0)) < string.length()) {
            int o = Math.min(n + parseResults.getReader().getRemainingLength(), string.length());
            list.add(FormattedCharSequence.forward(string.substring(j, n), LITERAL_STYLE));
            list.add(FormattedCharSequence.forward(string.substring(n, o), UNPARSED_STYLE));
            j = o;
        }
        list.add(FormattedCharSequence.forward(string.substring(j), LITERAL_STYLE));
        return FormattedCharSequence.composite(list);
    }

    public void render(PoseStack poseStack, int i, int j) {
        if (!this.renderSuggestions(poseStack, i, j)) {
            this.renderUsage(poseStack);
        }
    }

    public boolean renderSuggestions(PoseStack poseStack, int i, int j) {
        if (this.suggestions != null) {
            this.suggestions.render(poseStack, i, j);
            return true;
        }
        return false;
    }

    public void renderUsage(PoseStack poseStack) {
        int i = 0;
        for (FormattedCharSequence formattedCharSequence : this.commandUsage) {
            int j = this.anchorToBottom ? this.screen.height - 14 - 13 - 12 * i : 72 + 12 * i;
            GuiComponent.fill(poseStack, this.commandUsagePosition - 1, j, this.commandUsagePosition + this.commandUsageWidth + 1, j + 12, this.fillColor);
            this.font.drawShadow(poseStack, formattedCharSequence, (float)this.commandUsagePosition, (float)(j + 2), -1);
            ++i;
        }
    }

    public Component getNarrationMessage() {
        if (this.suggestions != null) {
            return CommonComponents.NEW_LINE.copy().append(this.suggestions.getNarrationMessage());
        }
        return CommonComponents.EMPTY;
    }

    @Environment(value=EnvType.CLIENT)
    public class SuggestionsList {
        private final Rect2i rect;
        private final String originalContents;
        private final List<Suggestion> suggestionList;
        private int offset;
        private int current;
        private Vec2 lastMouse = Vec2.ZERO;
        private boolean tabCycles;
        private int lastNarratedEntry;

        SuggestionsList(int i, int j, int k, List<Suggestion> list, boolean bl) {
            int l = i - 1;
            int m = CommandSuggestions.this.anchorToBottom ? j - 3 - Math.min(list.size(), CommandSuggestions.this.suggestionLineLimit) * 12 : j;
            this.rect = new Rect2i(l, m, k + 1, Math.min(list.size(), CommandSuggestions.this.suggestionLineLimit) * 12);
            this.originalContents = CommandSuggestions.this.input.getValue();
            this.lastNarratedEntry = bl ? -1 : 0;
            this.suggestionList = list;
            this.select(0);
        }

        public void render(PoseStack poseStack, int i, int j) {
            Message message;
            boolean bl4;
            int k = Math.min(this.suggestionList.size(), CommandSuggestions.this.suggestionLineLimit);
            int l = -5592406;
            boolean bl = this.offset > 0;
            boolean bl2 = this.suggestionList.size() > this.offset + k;
            boolean bl3 = bl || bl2;
            boolean bl5 = bl4 = this.lastMouse.x != (float)i || this.lastMouse.y != (float)j;
            if (bl4) {
                this.lastMouse = new Vec2(i, j);
            }
            if (bl3) {
                int m;
                GuiComponent.fill(poseStack, this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), CommandSuggestions.this.fillColor);
                GuiComponent.fill(poseStack, this.rect.getX(), this.rect.getY() + this.rect.getHeight(), this.rect.getX() + this.rect.getWidth(), this.rect.getY() + this.rect.getHeight() + 1, CommandSuggestions.this.fillColor);
                if (bl) {
                    for (m = 0; m < this.rect.getWidth(); ++m) {
                        if (m % 2 != 0) continue;
                        GuiComponent.fill(poseStack, this.rect.getX() + m, this.rect.getY() - 1, this.rect.getX() + m + 1, this.rect.getY(), -1);
                    }
                }
                if (bl2) {
                    for (m = 0; m < this.rect.getWidth(); ++m) {
                        if (m % 2 != 0) continue;
                        GuiComponent.fill(poseStack, this.rect.getX() + m, this.rect.getY() + this.rect.getHeight(), this.rect.getX() + m + 1, this.rect.getY() + this.rect.getHeight() + 1, -1);
                    }
                }
            }
            boolean bl52 = false;
            for (int n = 0; n < k; ++n) {
                Suggestion suggestion = this.suggestionList.get(n + this.offset);
                GuiComponent.fill(poseStack, this.rect.getX(), this.rect.getY() + 12 * n, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * n + 12, CommandSuggestions.this.fillColor);
                if (i > this.rect.getX() && i < this.rect.getX() + this.rect.getWidth() && j > this.rect.getY() + 12 * n && j < this.rect.getY() + 12 * n + 12) {
                    if (bl4) {
                        this.select(n + this.offset);
                    }
                    bl52 = true;
                }
                CommandSuggestions.this.font.drawShadow(poseStack, suggestion.getText(), (float)(this.rect.getX() + 1), (float)(this.rect.getY() + 2 + 12 * n), n + this.offset == this.current ? -256 : -5592406);
            }
            if (bl52 && (message = this.suggestionList.get(this.current).getTooltip()) != null) {
                CommandSuggestions.this.screen.renderTooltip(poseStack, ComponentUtils.fromMessage(message), i, j);
            }
        }

        public boolean mouseClicked(int i, int j, int k) {
            if (!this.rect.contains(i, j)) {
                return false;
            }
            int l = (j - this.rect.getY()) / 12 + this.offset;
            if (l >= 0 && l < this.suggestionList.size()) {
                this.select(l);
                this.useSuggestion();
            }
            return true;
        }

        public boolean mouseScrolled(double d) {
            int j;
            int i = (int)(CommandSuggestions.this.minecraft.mouseHandler.xpos() * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledWidth() / (double)CommandSuggestions.this.minecraft.getWindow().getScreenWidth());
            if (this.rect.contains(i, j = (int)(CommandSuggestions.this.minecraft.mouseHandler.ypos() * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledHeight() / (double)CommandSuggestions.this.minecraft.getWindow().getScreenHeight()))) {
                this.offset = Mth.clamp((int)((double)this.offset - d), 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
                return true;
            }
            return false;
        }

        public boolean keyPressed(int i, int j, int k) {
            if (i == 265) {
                this.cycle(-1);
                this.tabCycles = false;
                return true;
            }
            if (i == 264) {
                this.cycle(1);
                this.tabCycles = false;
                return true;
            }
            if (i == 258) {
                if (this.tabCycles) {
                    this.cycle(Screen.hasShiftDown() ? -1 : 1);
                }
                this.useSuggestion();
                return true;
            }
            if (i == 256) {
                CommandSuggestions.this.hide();
                return true;
            }
            return false;
        }

        public void cycle(int i) {
            this.select(this.current + i);
            int j = this.offset;
            int k = this.offset + CommandSuggestions.this.suggestionLineLimit - 1;
            if (this.current < j) {
                this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
            } else if (this.current > k) {
                this.offset = Mth.clamp(this.current + CommandSuggestions.this.lineStartOffset - CommandSuggestions.this.suggestionLineLimit, 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
            }
        }

        public void select(int i) {
            this.current = i;
            if (this.current < 0) {
                this.current += this.suggestionList.size();
            }
            if (this.current >= this.suggestionList.size()) {
                this.current -= this.suggestionList.size();
            }
            Suggestion suggestion = this.suggestionList.get(this.current);
            CommandSuggestions.this.input.setSuggestion(CommandSuggestions.calculateSuggestionSuffix(CommandSuggestions.this.input.getValue(), suggestion.apply(this.originalContents)));
            if (this.lastNarratedEntry != this.current) {
                CommandSuggestions.this.minecraft.getNarrator().sayNow(this.getNarrationMessage());
            }
        }

        public void useSuggestion() {
            Suggestion suggestion = this.suggestionList.get(this.current);
            CommandSuggestions.this.keepSuggestions = true;
            CommandSuggestions.this.input.setValue(suggestion.apply(this.originalContents));
            int i = suggestion.getRange().getStart() + suggestion.getText().length();
            CommandSuggestions.this.input.setCursorPosition(i);
            CommandSuggestions.this.input.setHighlightPos(i);
            this.select(this.current);
            CommandSuggestions.this.keepSuggestions = false;
            this.tabCycles = true;
        }

        Component getNarrationMessage() {
            this.lastNarratedEntry = this.current;
            Suggestion suggestion = this.suggestionList.get(this.current);
            Message message = suggestion.getTooltip();
            if (message != null) {
                return Component.translatable("narration.suggestion.tooltip", this.current + 1, this.suggestionList.size(), suggestion.getText(), message);
            }
            return Component.translatable("narration.suggestion", this.current + 1, this.suggestionList.size(), suggestion.getText());
        }
    }
}

