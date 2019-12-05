package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

@Environment(EnvType.CLIENT)
public class CommandSuggestions {
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
	private final Minecraft minecraft;
	private final Screen screen;
	private final EditBox input;
	private final Font font;
	private final boolean commandsOnly;
	private final boolean onlyShowIfCursorPastError;
	private final int lineStartOffset;
	private final int suggestionLineLimit;
	private final boolean anchorToBottom;
	private final int fillColor;
	private final List<String> commandUsage = Lists.<String>newArrayList();
	private int commandUsagePosition;
	private int commandUsageWidth;
	private ParseResults<SharedSuggestionProvider> currentParse;
	private CompletableFuture<Suggestions> pendingSuggestions;
	private CommandSuggestions.SuggestionsList suggestions;
	private boolean allowSuggestions;
	private boolean keepSuggestions;

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
		} else if (this.screen.getFocused() == this.input && i == 258) {
			this.showSuggestions(true);
			return true;
		} else {
			return false;
		}
	}

	public boolean mouseScrolled(double d) {
		return this.suggestions != null && this.suggestions.mouseScrolled(Mth.clamp(d, -1.0, 1.0));
	}

	public boolean mouseClicked(double d, double e, int i) {
		return this.suggestions != null && this.suggestions.mouseClicked((int)d, (int)e, i);
	}

	public void showSuggestions(boolean bl) {
		if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
			Suggestions suggestions = (Suggestions)this.pendingSuggestions.join();
			if (!suggestions.isEmpty()) {
				int i = 0;

				for (Suggestion suggestion : suggestions.getList()) {
					i = Math.max(i, this.font.width(suggestion.getText()));
				}

				int j = Mth.clamp(this.input.getScreenX(suggestions.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
				int k = this.anchorToBottom ? this.screen.height - 12 : 72;
				this.suggestions = new CommandSuggestions.SuggestionsList(j, k, i, suggestions, bl);
			}
		}
	}

	public void updateCommandInfo() {
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
		boolean bl = stringReader.canRead() && stringReader.peek() == '/';
		if (bl) {
			stringReader.skip();
		}

		boolean bl2 = this.commandsOnly || bl;
		int i = this.input.getCursorPosition();
		if (bl2) {
			CommandDispatcher<SharedSuggestionProvider> commandDispatcher = this.minecraft.player.connection.getCommands();
			if (this.currentParse == null) {
				this.currentParse = commandDispatcher.parse(stringReader, this.minecraft.player.connection.getSuggestionsProvider());
			}

			int j = this.onlyShowIfCursorPastError ? stringReader.getCursor() : 1;
			if (i >= j && (this.suggestions == null || !this.keepSuggestions)) {
				this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.currentParse, i);
				this.pendingSuggestions.thenRun(() -> {
					if (this.pendingSuggestions.isDone()) {
						this.updateUsageInfo();
					}
				});
			}
		} else {
			String string2 = string.substring(0, i);
			int j = getLastWordIndex(string2);
			Collection<String> collection = this.minecraft.player.connection.getSuggestionsProvider().getOnlinePlayerNames();
			this.pendingSuggestions = SharedSuggestionProvider.suggest(collection, new SuggestionsBuilder(string2, j));
		}
	}

	private static int getLastWordIndex(String string) {
		if (Strings.isNullOrEmpty(string)) {
			return 0;
		} else {
			int i = 0;
			Matcher matcher = WHITESPACE_PATTERN.matcher(string);

			while (matcher.find()) {
				i = matcher.end();
			}

			return i;
		}
	}

	public void updateUsageInfo() {
		if (this.input.getCursorPosition() == this.input.getValue().length()) {
			if (((Suggestions)this.pendingSuggestions.join()).isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
				int i = 0;

				for (Entry<CommandNode<SharedSuggestionProvider>, CommandSyntaxException> entry : this.currentParse.getExceptions().entrySet()) {
					CommandSyntaxException commandSyntaxException = (CommandSyntaxException)entry.getValue();
					if (commandSyntaxException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
						i++;
					} else {
						this.commandUsage.add(commandSyntaxException.getMessage());
					}
				}

				if (i > 0) {
					this.commandUsage.add(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create().getMessage());
				}
			} else if (this.currentParse.getReader().canRead()) {
				this.commandUsage.add(Commands.getParseException(this.currentParse).getMessage());
			}
		}

		this.commandUsagePosition = 0;
		this.commandUsageWidth = this.screen.width;
		if (this.commandUsage.isEmpty()) {
			this.fillNodeUsage(ChatFormatting.GRAY);
		}

		this.suggestions = null;
		if (this.allowSuggestions && this.minecraft.options.autoSuggestions) {
			this.showSuggestions(false);
		}
	}

	private void fillNodeUsage(ChatFormatting chatFormatting) {
		CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = this.currentParse.getContext();
		SuggestionContext<SharedSuggestionProvider> suggestionContext = commandContextBuilder.findSuggestionContext(this.input.getCursorPosition());
		Map<CommandNode<SharedSuggestionProvider>, String> map = this.minecraft
			.player
			.connection
			.getCommands()
			.getSmartUsage(suggestionContext.parent, this.minecraft.player.connection.getSuggestionsProvider());
		List<String> list = Lists.<String>newArrayList();
		int i = 0;

		for (Entry<CommandNode<SharedSuggestionProvider>, String> entry : map.entrySet()) {
			if (!(entry.getKey() instanceof LiteralCommandNode)) {
				list.add(chatFormatting + (String)entry.getValue());
				i = Math.max(i, this.font.width((String)entry.getValue()));
			}
		}

		if (!list.isEmpty()) {
			this.commandUsage.addAll(list);
			this.commandUsagePosition = Mth.clamp(this.input.getScreenX(suggestionContext.startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
			this.commandUsageWidth = i;
		}
	}

	private String formatChat(String string, int i) {
		return this.currentParse != null ? formatText(this.currentParse, string, i) : string;
	}

	@Nullable
	private static String calculateSuggestionSuffix(String string, String string2) {
		return string2.startsWith(string) ? string2.substring(string.length()) : null;
	}

	public static String formatText(ParseResults<SharedSuggestionProvider> parseResults, String string, int i) {
		ChatFormatting[] chatFormattings = new ChatFormatting[]{
			ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD
		};
		String string2 = ChatFormatting.GRAY.toString();
		StringBuilder stringBuilder = new StringBuilder(string2);
		int j = 0;
		int k = -1;
		CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = parseResults.getContext().getLastChild();

		for (ParsedArgument<SharedSuggestionProvider, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
			if (++k >= chatFormattings.length) {
				k = 0;
			}

			int l = Math.max(parsedArgument.getRange().getStart() - i, 0);
			if (l >= string.length()) {
				break;
			}

			int m = Math.min(parsedArgument.getRange().getEnd() - i, string.length());
			if (m > 0) {
				stringBuilder.append(string, j, l);
				stringBuilder.append(chatFormattings[k]);
				stringBuilder.append(string, l, m);
				stringBuilder.append(string2);
				j = m;
			}
		}

		if (parseResults.getReader().canRead()) {
			int n = Math.max(parseResults.getReader().getCursor() - i, 0);
			if (n < string.length()) {
				int o = Math.min(n + parseResults.getReader().getRemainingLength(), string.length());
				stringBuilder.append(string, j, n);
				stringBuilder.append(ChatFormatting.RED);
				stringBuilder.append(string, n, o);
				j = o;
			}
		}

		stringBuilder.append(string, j, string.length());
		return stringBuilder.toString();
	}

	public void render(int i, int j) {
		if (this.suggestions != null) {
			this.suggestions.render(i, j);
		} else {
			int k = 0;

			for (String string : this.commandUsage) {
				int l = this.anchorToBottom ? this.screen.height - 14 - 13 - 12 * k : 72 + 12 * k;
				GuiComponent.fill(this.commandUsagePosition - 1, l, this.commandUsagePosition + this.commandUsageWidth + 1, l + 12, this.fillColor);
				this.font.drawShadow(string, (float)this.commandUsagePosition, (float)(l + 2), -1);
				k++;
			}
		}
	}

	public String getNarrationMessage() {
		return this.suggestions != null ? "\n" + this.suggestions.getNarrationMessage() : "";
	}

	@Environment(EnvType.CLIENT)
	public class SuggestionsList {
		private final Rect2i rect;
		private final Suggestions suggestions;
		private final String originalContents;
		private int offset;
		private int current;
		private Vec2 lastMouse = Vec2.ZERO;
		private boolean tabCycles;
		private int lastNarratedEntry;

		private SuggestionsList(int i, int j, int k, Suggestions suggestions, boolean bl) {
			int l = i - 1;
			int m = CommandSuggestions.this.anchorToBottom ? j - 3 - Math.min(suggestions.getList().size(), CommandSuggestions.this.suggestionLineLimit) * 12 : j;
			this.rect = new Rect2i(l, m, k + 1, Math.min(suggestions.getList().size(), CommandSuggestions.this.suggestionLineLimit) * 12);
			this.suggestions = suggestions;
			this.originalContents = CommandSuggestions.this.input.getValue();
			this.lastNarratedEntry = bl ? -1 : 0;
			this.select(0);
		}

		public void render(int i, int j) {
			int k = Math.min(this.suggestions.getList().size(), CommandSuggestions.this.suggestionLineLimit);
			int l = -5592406;
			boolean bl = this.offset > 0;
			boolean bl2 = this.suggestions.getList().size() > this.offset + k;
			boolean bl3 = bl || bl2;
			boolean bl4 = this.lastMouse.x != (float)i || this.lastMouse.y != (float)j;
			if (bl4) {
				this.lastMouse = new Vec2((float)i, (float)j);
			}

			if (bl3) {
				GuiComponent.fill(this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), CommandSuggestions.this.fillColor);
				GuiComponent.fill(
					this.rect.getX(),
					this.rect.getY() + this.rect.getHeight(),
					this.rect.getX() + this.rect.getWidth(),
					this.rect.getY() + this.rect.getHeight() + 1,
					CommandSuggestions.this.fillColor
				);
				if (bl) {
					for (int m = 0; m < this.rect.getWidth(); m++) {
						if (m % 2 == 0) {
							GuiComponent.fill(this.rect.getX() + m, this.rect.getY() - 1, this.rect.getX() + m + 1, this.rect.getY(), -1);
						}
					}
				}

				if (bl2) {
					for (int mx = 0; mx < this.rect.getWidth(); mx++) {
						if (mx % 2 == 0) {
							GuiComponent.fill(
								this.rect.getX() + mx, this.rect.getY() + this.rect.getHeight(), this.rect.getX() + mx + 1, this.rect.getY() + this.rect.getHeight() + 1, -1
							);
						}
					}
				}
			}

			boolean bl5 = false;

			for (int n = 0; n < k; n++) {
				Suggestion suggestion = (Suggestion)this.suggestions.getList().get(n + this.offset);
				GuiComponent.fill(
					this.rect.getX(), this.rect.getY() + 12 * n, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * n + 12, CommandSuggestions.this.fillColor
				);
				if (i > this.rect.getX() && i < this.rect.getX() + this.rect.getWidth() && j > this.rect.getY() + 12 * n && j < this.rect.getY() + 12 * n + 12) {
					if (bl4) {
						this.select(n + this.offset);
					}

					bl5 = true;
				}

				CommandSuggestions.this.font
					.drawShadow(suggestion.getText(), (float)(this.rect.getX() + 1), (float)(this.rect.getY() + 2 + 12 * n), n + this.offset == this.current ? -256 : -5592406);
			}

			if (bl5) {
				Message message = ((Suggestion)this.suggestions.getList().get(this.current)).getTooltip();
				if (message != null) {
					CommandSuggestions.this.screen.renderTooltip(ComponentUtils.fromMessage(message).getColoredString(), i, j);
				}
			}
		}

		public boolean mouseClicked(int i, int j, int k) {
			if (!this.rect.contains(i, j)) {
				return false;
			} else {
				int l = (j - this.rect.getY()) / 12 + this.offset;
				if (l >= 0 && l < this.suggestions.getList().size()) {
					this.select(l);
					this.useSuggestion();
				}

				return true;
			}
		}

		public boolean mouseScrolled(double d) {
			int i = (int)(
				CommandSuggestions.this.minecraft.mouseHandler.xpos()
					* (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledWidth()
					/ (double)CommandSuggestions.this.minecraft.getWindow().getScreenWidth()
			);
			int j = (int)(
				CommandSuggestions.this.minecraft.mouseHandler.ypos()
					* (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledHeight()
					/ (double)CommandSuggestions.this.minecraft.getWindow().getScreenHeight()
			);
			if (this.rect.contains(i, j)) {
				this.offset = Mth.clamp((int)((double)this.offset - d), 0, Math.max(this.suggestions.getList().size() - CommandSuggestions.this.suggestionLineLimit, 0));
				return true;
			} else {
				return false;
			}
		}

		public boolean keyPressed(int i, int j, int k) {
			if (i == 265) {
				this.cycle(-1);
				this.tabCycles = false;
				return true;
			} else if (i == 264) {
				this.cycle(1);
				this.tabCycles = false;
				return true;
			} else if (i == 258) {
				if (this.tabCycles) {
					this.cycle(Screen.hasShiftDown() ? -1 : 1);
				}

				this.useSuggestion();
				return true;
			} else if (i == 256) {
				this.hide();
				return true;
			} else {
				return false;
			}
		}

		public void cycle(int i) {
			this.select(this.current + i);
			int j = this.offset;
			int k = this.offset + CommandSuggestions.this.suggestionLineLimit - 1;
			if (this.current < j) {
				this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestions.getList().size() - CommandSuggestions.this.suggestionLineLimit, 0));
			} else if (this.current > k) {
				this.offset = Mth.clamp(
					this.current + CommandSuggestions.this.lineStartOffset - CommandSuggestions.this.suggestionLineLimit,
					0,
					Math.max(this.suggestions.getList().size() - CommandSuggestions.this.suggestionLineLimit, 0)
				);
			}
		}

		public void select(int i) {
			this.current = i;
			if (this.current < 0) {
				this.current = this.current + this.suggestions.getList().size();
			}

			if (this.current >= this.suggestions.getList().size()) {
				this.current = this.current - this.suggestions.getList().size();
			}

			Suggestion suggestion = (Suggestion)this.suggestions.getList().get(this.current);
			CommandSuggestions.this.input
				.setSuggestion(CommandSuggestions.calculateSuggestionSuffix(CommandSuggestions.this.input.getValue(), suggestion.apply(this.originalContents)));
			if (NarratorChatListener.INSTANCE.isActive() && this.lastNarratedEntry != this.current) {
				NarratorChatListener.INSTANCE.sayNow(this.getNarrationMessage());
			}
		}

		public void useSuggestion() {
			Suggestion suggestion = (Suggestion)this.suggestions.getList().get(this.current);
			CommandSuggestions.this.keepSuggestions = true;
			CommandSuggestions.this.input.setValue(suggestion.apply(this.originalContents));
			int i = suggestion.getRange().getStart() + suggestion.getText().length();
			CommandSuggestions.this.input.setCursorPosition(i);
			CommandSuggestions.this.input.setHighlightPos(i);
			this.select(this.current);
			CommandSuggestions.this.keepSuggestions = false;
			this.tabCycles = true;
		}

		private String getNarrationMessage() {
			this.lastNarratedEntry = this.current;
			List<Suggestion> list = this.suggestions.getList();
			Suggestion suggestion = (Suggestion)list.get(this.current);
			Message message = suggestion.getTooltip();
			return message != null
				? I18n.get("narration.suggestion.tooltip", this.current + 1, list.size(), suggestion.getText(), message.getString())
				: I18n.get("narration.suggestion", this.current + 1, list.size(), suggestion.getText());
		}

		public void hide() {
			CommandSuggestions.this.suggestions = null;
		}
	}
}