package net.minecraft.client.gui.screens;

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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

@Environment(EnvType.CLIENT)
public class ChatScreen extends Screen {
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
	private String historyBuffer = "";
	private int historyPos = -1;
	protected EditBox input;
	private String initial = "";
	protected final List<String> commandUsage = Lists.<String>newArrayList();
	protected int commandUsagePosition;
	protected int commandUsageWidth;
	private ParseResults<SharedSuggestionProvider> currentParse;
	private CompletableFuture<Suggestions> pendingSuggestions;
	private ChatScreen.SuggestionsList suggestions;
	private boolean hasEdits;
	private boolean keepSuggestions;

	public ChatScreen(String string) {
		super(NarratorChatListener.NO_TITLE);
		this.initial = string;
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
		this.input = new EditBox(this.font, 4, this.height - 12, this.width - 4, 12, I18n.get("chat.editBox"));
		this.input.setMaxLength(256);
		this.input.setBordered(false);
		this.input.setValue(this.initial);
		this.input.setFormatter(this::formatChat);
		this.input.setResponder(this::onEdited);
		this.children.add(this.input);
		this.updateCommandInfo();
		this.setInitialFocus(this.input);
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.input.getValue();
		this.init(minecraft, i, j);
		this.setChatLine(string);
		this.updateCommandInfo();
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		this.minecraft.gui.getChat().resetChatScroll();
	}

	@Override
	public void tick() {
		this.input.tick();
	}

	private void onEdited(String string) {
		String string2 = this.input.getValue();
		this.hasEdits = !string2.equals(this.initial);
		this.updateCommandInfo();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.suggestions != null && this.suggestions.keyPressed(i, j, k)) {
			return true;
		} else {
			if (i == 258) {
				this.hasEdits = true;
				this.showSuggestions();
			}

			if (super.keyPressed(i, j, k)) {
				return true;
			} else if (i == 256) {
				this.minecraft.setScreen(null);
				return true;
			} else if (i == 257 || i == 335) {
				String string = this.input.getValue().trim();
				if (!string.isEmpty()) {
					this.sendMessage(string);
				}

				this.minecraft.setScreen(null);
				return true;
			} else if (i == 265) {
				this.moveInHistory(-1);
				return true;
			} else if (i == 264) {
				this.moveInHistory(1);
				return true;
			} else if (i == 266) {
				this.minecraft.gui.getChat().scrollChat((double)(this.minecraft.gui.getChat().getLinesPerPage() - 1));
				return true;
			} else if (i == 267) {
				this.minecraft.gui.getChat().scrollChat((double)(-this.minecraft.gui.getChat().getLinesPerPage() + 1));
				return true;
			} else {
				return false;
			}
		}
	}

	public void showSuggestions() {
		if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
			int i = 0;
			Suggestions suggestions = (Suggestions)this.pendingSuggestions.join();
			if (!suggestions.getList().isEmpty()) {
				for (Suggestion suggestion : suggestions.getList()) {
					i = Math.max(i, this.font.width(suggestion.getText()));
				}

				int j = Mth.clamp(this.input.getScreenX(suggestions.getRange().getStart()), 0, this.width - i);
				this.suggestions = new ChatScreen.SuggestionsList(j, this.height - 12, i, suggestions);
			}
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

	private void updateCommandInfo() {
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
		if (stringReader.canRead() && stringReader.peek() == '/') {
			stringReader.skip();
			CommandDispatcher<SharedSuggestionProvider> commandDispatcher = this.minecraft.player.connection.getCommands();
			if (this.currentParse == null) {
				this.currentParse = commandDispatcher.parse(stringReader, this.minecraft.player.connection.getSuggestionsProvider());
			}

			int i = this.input.getCursorPosition();
			if (i >= 1 && (this.suggestions == null || !this.keepSuggestions)) {
				this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.currentParse, i);
				this.pendingSuggestions.thenRun(() -> {
					if (this.pendingSuggestions.isDone()) {
						this.updateUsageInfo();
					}
				});
			}
		} else {
			int i = getLastWordIndex(string);
			Collection<String> collection = this.minecraft.player.connection.getSuggestionsProvider().getOnlinePlayerNames();
			this.pendingSuggestions = SharedSuggestionProvider.suggest(collection, new SuggestionsBuilder(string, i));
		}
	}

	private void updateUsageInfo() {
		if (((Suggestions)this.pendingSuggestions.join()).isEmpty()
			&& !this.currentParse.getExceptions().isEmpty()
			&& this.input.getCursorPosition() == this.input.getValue().length()) {
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
		}

		this.commandUsagePosition = 0;
		this.commandUsageWidth = this.width;
		if (this.commandUsage.isEmpty()) {
			this.fillNodeUsage(ChatFormatting.GRAY);
		}

		this.suggestions = null;
		if (this.hasEdits && this.minecraft.options.autoSuggestions) {
			this.showSuggestions();
		}
	}

	private String formatChat(String string, int i) {
		return this.currentParse != null ? formatText(this.currentParse, string, i) : string;
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

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		if (f > 1.0) {
			f = 1.0;
		}

		if (f < -1.0) {
			f = -1.0;
		}

		if (this.suggestions != null && this.suggestions.mouseScrolled(f)) {
			return true;
		} else {
			if (!hasShiftDown()) {
				f *= 7.0;
			}

			this.minecraft.gui.getChat().scrollChat(f);
			return true;
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.suggestions != null && this.suggestions.mouseClicked((int)d, (int)e, i)) {
			return true;
		} else {
			if (i == 0) {
				Component component = this.minecraft.gui.getChat().getClickedComponentAt(d, e);
				if (component != null && this.handleComponentClicked(component)) {
					return true;
				}
			}

			return this.input.mouseClicked(d, e, i) ? true : super.mouseClicked(d, e, i);
		}
	}

	@Override
	protected void insertText(String string, boolean bl) {
		if (bl) {
			this.input.setValue(string);
		} else {
			this.input.insertText(string);
		}
	}

	public void moveInHistory(int i) {
		int j = this.historyPos + i;
		int k = this.minecraft.gui.getChat().getRecentChat().size();
		j = Mth.clamp(j, 0, k);
		if (j != this.historyPos) {
			if (j == k) {
				this.historyPos = k;
				this.input.setValue(this.historyBuffer);
			} else {
				if (this.historyPos == k) {
					this.historyBuffer = this.input.getValue();
				}

				this.input.setValue((String)this.minecraft.gui.getChat().getRecentChat().get(j));
				this.suggestions = null;
				this.historyPos = j;
				this.hasEdits = false;
			}
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.setFocused(this.input);
		this.input.setFocus(true);
		fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
		this.input.render(i, j, f);
		if (this.suggestions != null) {
			this.suggestions.render(i, j);
		} else {
			int k = 0;

			for (String string : this.commandUsage) {
				fill(
					this.commandUsagePosition - 1,
					this.height - 14 - 13 - 12 * k,
					this.commandUsagePosition + this.commandUsageWidth + 1,
					this.height - 2 - 13 - 12 * k,
					-16777216
				);
				this.font.drawShadow(string, (float)this.commandUsagePosition, (float)(this.height - 14 - 13 + 2 - 12 * k), -1);
				k++;
			}
		}

		Component component = this.minecraft.gui.getChat().getClickedComponentAt((double)i, (double)j);
		if (component != null && component.getStyle().getHoverEvent() != null) {
			this.renderComponentHoverEffect(component, i, j);
		}

		super.render(i, j, f);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
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
			this.commandUsagePosition = Mth.clamp(this.input.getScreenX(suggestionContext.startPos), 0, this.width - i);
			this.commandUsageWidth = i;
		}
	}

	@Nullable
	private static String calculateSuggestionSuffix(String string, String string2) {
		return string2.startsWith(string) ? string2.substring(string.length()) : null;
	}

	private void setChatLine(String string) {
		this.input.setValue(string);
	}

	@Environment(EnvType.CLIENT)
	class SuggestionsList {
		private final Rect2i rect;
		private final Suggestions suggestions;
		private final String originalContents;
		private int offset;
		private int current;
		private Vec2 lastMouse = Vec2.ZERO;
		private boolean tabCycles;

		private SuggestionsList(int i, int j, int k, Suggestions suggestions) {
			this.rect = new Rect2i(i - 1, j - 3 - Math.min(suggestions.getList().size(), 10) * 12, k + 1, Math.min(suggestions.getList().size(), 10) * 12);
			this.suggestions = suggestions;
			this.originalContents = ChatScreen.this.input.getValue();
			this.select(0);
		}

		public void render(int i, int j) {
			int k = Math.min(this.suggestions.getList().size(), 10);
			int l = -5592406;
			boolean bl = this.offset > 0;
			boolean bl2 = this.suggestions.getList().size() > this.offset + k;
			boolean bl3 = bl || bl2;
			boolean bl4 = this.lastMouse.x != (float)i || this.lastMouse.y != (float)j;
			if (bl4) {
				this.lastMouse = new Vec2((float)i, (float)j);
			}

			if (bl3) {
				GuiComponent.fill(this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), -805306368);
				GuiComponent.fill(
					this.rect.getX(),
					this.rect.getY() + this.rect.getHeight(),
					this.rect.getX() + this.rect.getWidth(),
					this.rect.getY() + this.rect.getHeight() + 1,
					-805306368
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
				GuiComponent.fill(this.rect.getX(), this.rect.getY() + 12 * n, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * n + 12, -805306368);
				if (i > this.rect.getX() && i < this.rect.getX() + this.rect.getWidth() && j > this.rect.getY() + 12 * n && j < this.rect.getY() + 12 * n + 12) {
					if (bl4) {
						this.select(n + this.offset);
					}

					bl5 = true;
				}

				ChatScreen.this.font
					.drawShadow(suggestion.getText(), (float)(this.rect.getX() + 1), (float)(this.rect.getY() + 2 + 12 * n), n + this.offset == this.current ? -256 : -5592406);
			}

			if (bl5) {
				Message message = ((Suggestion)this.suggestions.getList().get(this.current)).getTooltip();
				if (message != null) {
					ChatScreen.this.renderTooltip(ComponentUtils.fromMessage(message).getColoredString(), i, j);
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
				ChatScreen.this.minecraft.mouseHandler.xpos()
					* (double)ChatScreen.this.minecraft.window.getGuiScaledWidth()
					/ (double)ChatScreen.this.minecraft.window.getScreenWidth()
			);
			int j = (int)(
				ChatScreen.this.minecraft.mouseHandler.ypos()
					* (double)ChatScreen.this.minecraft.window.getGuiScaledHeight()
					/ (double)ChatScreen.this.minecraft.window.getScreenHeight()
			);
			if (this.rect.contains(i, j)) {
				this.offset = Mth.clamp((int)((double)this.offset - d), 0, Math.max(this.suggestions.getList().size() - 10, 0));
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
			int k = this.offset + 10 - 1;
			if (this.current < j) {
				this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestions.getList().size() - 10, 0));
			} else if (this.current > k) {
				this.offset = Mth.clamp(this.current + 1 - 10, 0, Math.max(this.suggestions.getList().size() - 10, 0));
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
			ChatScreen.this.input.setSuggestion(ChatScreen.calculateSuggestionSuffix(ChatScreen.this.input.getValue(), suggestion.apply(this.originalContents)));
		}

		public void useSuggestion() {
			Suggestion suggestion = (Suggestion)this.suggestions.getList().get(this.current);
			ChatScreen.this.keepSuggestions = true;
			ChatScreen.this.setChatLine(suggestion.apply(this.originalContents));
			int i = suggestion.getRange().getStart() + suggestion.getText().length();
			ChatScreen.this.input.setCursorPosition(i);
			ChatScreen.this.input.setHighlightPos(i);
			this.select(this.current);
			ChatScreen.this.keepSuggestions = false;
			this.tabCycles = true;
		}

		public void hide() {
			ChatScreen.this.suggestions = null;
		}
	}
}
