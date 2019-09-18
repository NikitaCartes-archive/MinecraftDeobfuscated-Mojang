package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.phys.Vec2;

@Environment(EnvType.CLIENT)
public abstract class AbstractCommandBlockEditScreen extends Screen {
	protected EditBox commandEdit;
	protected EditBox previousEdit;
	protected Button doneButton;
	protected Button cancelButton;
	protected Button outputButton;
	protected boolean trackOutput;
	protected final List<String> commandUsage = Lists.<String>newArrayList();
	protected int commandUsagePosition;
	protected int commandUsageWidth;
	protected ParseResults<SharedSuggestionProvider> currentParse;
	protected CompletableFuture<Suggestions> pendingSuggestions;
	protected AbstractCommandBlockEditScreen.SuggestionsList suggestions;
	private boolean keepSuggestions;

	public AbstractCommandBlockEditScreen() {
		super(NarratorChatListener.NO_TITLE);
	}

	@Override
	public void tick() {
		this.commandEdit.tick();
	}

	abstract BaseCommandBlock getCommandBlock();

	abstract int getPreviousY();

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, I18n.get("gui.done"), button -> this.onDone()));
		this.cancelButton = this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.get("gui.cancel"), button -> this.onClose()));
		this.outputButton = this.addButton(new Button(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, "O", button -> {
			BaseCommandBlock baseCommandBlock = this.getCommandBlock();
			baseCommandBlock.setTrackOutput(!baseCommandBlock.isTrackOutput());
			this.updateCommandOutput();
		}));
		this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, I18n.get("advMode.command"));
		this.commandEdit.setMaxLength(32500);
		this.commandEdit.setFormatter(this::formatChat);
		this.commandEdit.setResponder(this::onEdited);
		this.children.add(this.commandEdit);
		this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, I18n.get("advMode.previousOutput"));
		this.previousEdit.setMaxLength(32500);
		this.previousEdit.setEditable(false);
		this.previousEdit.setValue("-");
		this.children.add(this.previousEdit);
		this.setInitialFocus(this.commandEdit);
		this.commandEdit.setFocus(true);
		this.updateCommandInfo();
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.commandEdit.getValue();
		this.init(minecraft, i, j);
		this.setChatLine(string);
		this.updateCommandInfo();
	}

	protected void updateCommandOutput() {
		if (this.getCommandBlock().isTrackOutput()) {
			this.outputButton.setMessage("O");
			this.previousEdit.setValue(this.getCommandBlock().getLastOutput().getString());
		} else {
			this.outputButton.setMessage("X");
			this.previousEdit.setValue("-");
		}
	}

	protected void onDone() {
		BaseCommandBlock baseCommandBlock = this.getCommandBlock();
		this.populateAndSendPacket(baseCommandBlock);
		if (!baseCommandBlock.isTrackOutput()) {
			baseCommandBlock.setLastOutput(null);
		}

		this.minecraft.setScreen(null);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	protected abstract void populateAndSendPacket(BaseCommandBlock baseCommandBlock);

	@Override
	public void onClose() {
		this.getCommandBlock().setTrackOutput(this.trackOutput);
		this.minecraft.setScreen(null);
	}

	private void onEdited(String string) {
		this.updateCommandInfo();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.suggestions != null && this.suggestions.keyPressed(i, j, k)) {
			return true;
		} else if (this.getFocused() == this.commandEdit && i == 258) {
			this.showSuggestions();
			return true;
		} else if (super.keyPressed(i, j, k)) {
			return true;
		} else if (i != 257 && i != 335) {
			if (i == 258 && this.getFocused() == this.commandEdit) {
				this.showSuggestions();
			}

			return false;
		} else {
			this.onDone();
			return true;
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		return this.suggestions != null && this.suggestions.mouseScrolled(Mth.clamp(f, -1.0, 1.0)) ? true : super.mouseScrolled(d, e, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return this.suggestions != null && this.suggestions.mouseClicked((int)d, (int)e, i) ? true : super.mouseClicked(d, e, i);
	}

	protected void updateCommandInfo() {
		String string = this.commandEdit.getValue();
		if (this.currentParse != null && !this.currentParse.getReader().getString().equals(string)) {
			this.currentParse = null;
		}

		if (!this.keepSuggestions) {
			this.commandEdit.setSuggestion(null);
			this.suggestions = null;
		}

		this.commandUsage.clear();
		CommandDispatcher<SharedSuggestionProvider> commandDispatcher = this.minecraft.player.connection.getCommands();
		StringReader stringReader = new StringReader(string);
		if (stringReader.canRead() && stringReader.peek() == '/') {
			stringReader.skip();
		}

		int i = stringReader.getCursor();
		if (this.currentParse == null) {
			this.currentParse = commandDispatcher.parse(stringReader, this.minecraft.player.connection.getSuggestionsProvider());
		}

		int j = this.commandEdit.getCursorPosition();
		if (j >= i && (this.suggestions == null || !this.keepSuggestions)) {
			this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.currentParse, j);
			this.pendingSuggestions.thenRun(() -> {
				if (this.pendingSuggestions.isDone()) {
					this.updateUsageInfo();
				}
			});
		}
	}

	private void updateUsageInfo() {
		if (((Suggestions)this.pendingSuggestions.join()).isEmpty()
			&& !this.currentParse.getExceptions().isEmpty()
			&& this.commandEdit.getCursorPosition() == this.commandEdit.getValue().length()) {
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
		if (this.minecraft.options.autoSuggestions) {
			this.showSuggestions();
		}
	}

	private String formatChat(String string, int i) {
		return this.currentParse != null ? ChatScreen.formatText(this.currentParse, string, i) : string;
	}

	private void fillNodeUsage(ChatFormatting chatFormatting) {
		CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = this.currentParse.getContext();
		SuggestionContext<SharedSuggestionProvider> suggestionContext = commandContextBuilder.findSuggestionContext(this.commandEdit.getCursorPosition());
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
			this.commandUsagePosition = Mth.clamp(
				this.commandEdit.getScreenX(suggestionContext.startPos), 0, this.commandEdit.getScreenX(0) + this.commandEdit.getInnerWidth() - i
			);
			this.commandUsageWidth = i;
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, I18n.get("advMode.setCommand"), this.width / 2, 20, 16777215);
		this.drawString(this.font, I18n.get("advMode.command"), this.width / 2 - 150, 40, 10526880);
		this.commandEdit.render(i, j, f);
		int k = 75;
		if (!this.previousEdit.getValue().isEmpty()) {
			k += 5 * 9 + 1 + this.getPreviousY() - 135;
			this.drawString(this.font, I18n.get("advMode.previousOutput"), this.width / 2 - 150, k + 4, 10526880);
			this.previousEdit.render(i, j, f);
		}

		super.render(i, j, f);
		if (this.suggestions != null) {
			this.suggestions.render(i, j);
		} else {
			k = 0;

			for (String string : this.commandUsage) {
				fill(this.commandUsagePosition - 1, 72 + 12 * k, this.commandUsagePosition + this.commandUsageWidth + 1, 84 + 12 * k, Integer.MIN_VALUE);
				this.font.drawShadow(string, (float)this.commandUsagePosition, (float)(74 + 12 * k), -1);
				k++;
			}
		}
	}

	public void showSuggestions() {
		if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
			Suggestions suggestions = (Suggestions)this.pendingSuggestions.join();
			if (!suggestions.isEmpty()) {
				int i = 0;

				for (Suggestion suggestion : suggestions.getList()) {
					i = Math.max(i, this.font.width(suggestion.getText()));
				}

				int j = Mth.clamp(this.commandEdit.getScreenX(suggestions.getRange().getStart()), 0, this.commandEdit.getScreenX(0) + this.commandEdit.getInnerWidth() - i);
				this.suggestions = new AbstractCommandBlockEditScreen.SuggestionsList(j, 72, i, suggestions);
			}
		}
	}

	protected void setChatLine(String string) {
		this.commandEdit.setValue(string);
	}

	@Nullable
	private static String calculateSuggestionSuffix(String string, String string2) {
		return string2.startsWith(string) ? string2.substring(string.length()) : null;
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
			this.rect = new Rect2i(i - 1, j, k + 1, Math.min(suggestions.getList().size(), 7) * 12);
			this.suggestions = suggestions;
			this.originalContents = AbstractCommandBlockEditScreen.this.commandEdit.getValue();
			this.select(0);
		}

		public void render(int i, int j) {
			int k = Math.min(this.suggestions.getList().size(), 7);
			int l = Integer.MIN_VALUE;
			int m = -5592406;
			boolean bl = this.offset > 0;
			boolean bl2 = this.suggestions.getList().size() > this.offset + k;
			boolean bl3 = bl || bl2;
			boolean bl4 = this.lastMouse.x != (float)i || this.lastMouse.y != (float)j;
			if (bl4) {
				this.lastMouse = new Vec2((float)i, (float)j);
			}

			if (bl3) {
				GuiComponent.fill(this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), Integer.MIN_VALUE);
				GuiComponent.fill(
					this.rect.getX(),
					this.rect.getY() + this.rect.getHeight(),
					this.rect.getX() + this.rect.getWidth(),
					this.rect.getY() + this.rect.getHeight() + 1,
					Integer.MIN_VALUE
				);
				if (bl) {
					for (int n = 0; n < this.rect.getWidth(); n++) {
						if (n % 2 == 0) {
							GuiComponent.fill(this.rect.getX() + n, this.rect.getY() - 1, this.rect.getX() + n + 1, this.rect.getY(), -1);
						}
					}
				}

				if (bl2) {
					for (int nx = 0; nx < this.rect.getWidth(); nx++) {
						if (nx % 2 == 0) {
							GuiComponent.fill(
								this.rect.getX() + nx, this.rect.getY() + this.rect.getHeight(), this.rect.getX() + nx + 1, this.rect.getY() + this.rect.getHeight() + 1, -1
							);
						}
					}
				}
			}

			boolean bl5 = false;

			for (int o = 0; o < k; o++) {
				Suggestion suggestion = (Suggestion)this.suggestions.getList().get(o + this.offset);
				GuiComponent.fill(this.rect.getX(), this.rect.getY() + 12 * o, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * o + 12, Integer.MIN_VALUE);
				if (i > this.rect.getX() && i < this.rect.getX() + this.rect.getWidth() && j > this.rect.getY() + 12 * o && j < this.rect.getY() + 12 * o + 12) {
					if (bl4) {
						this.select(o + this.offset);
					}

					bl5 = true;
				}

				AbstractCommandBlockEditScreen.this.font
					.drawShadow(suggestion.getText(), (float)(this.rect.getX() + 1), (float)(this.rect.getY() + 2 + 12 * o), o + this.offset == this.current ? -256 : -5592406);
			}

			if (bl5) {
				Message message = ((Suggestion)this.suggestions.getList().get(this.current)).getTooltip();
				if (message != null) {
					AbstractCommandBlockEditScreen.this.renderTooltip(ComponentUtils.fromMessage(message).getColoredString(), i, j);
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
				AbstractCommandBlockEditScreen.this.minecraft.mouseHandler.xpos()
					* (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getGuiScaledWidth()
					/ (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getScreenWidth()
			);
			int j = (int)(
				AbstractCommandBlockEditScreen.this.minecraft.mouseHandler.ypos()
					* (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getGuiScaledHeight()
					/ (double)AbstractCommandBlockEditScreen.this.minecraft.getWindow().getScreenHeight()
			);
			if (this.rect.contains(i, j)) {
				this.offset = Mth.clamp((int)((double)this.offset - d), 0, Math.max(this.suggestions.getList().size() - 7, 0));
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
			int k = this.offset + 7 - 1;
			if (this.current < j) {
				this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestions.getList().size() - 7, 0));
			} else if (this.current > k) {
				this.offset = Mth.clamp(this.current - 7, 0, Math.max(this.suggestions.getList().size() - 7, 0));
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
			AbstractCommandBlockEditScreen.this.commandEdit
				.setSuggestion(
					AbstractCommandBlockEditScreen.calculateSuggestionSuffix(
						AbstractCommandBlockEditScreen.this.commandEdit.getValue(), suggestion.apply(this.originalContents)
					)
				);
		}

		public void useSuggestion() {
			Suggestion suggestion = (Suggestion)this.suggestions.getList().get(this.current);
			AbstractCommandBlockEditScreen.this.keepSuggestions = true;
			AbstractCommandBlockEditScreen.this.setChatLine(suggestion.apply(this.originalContents));
			int i = suggestion.getRange().getStart() + suggestion.getText().length();
			AbstractCommandBlockEditScreen.this.commandEdit.setCursorPosition(i);
			AbstractCommandBlockEditScreen.this.commandEdit.setHighlightPos(i);
			this.select(this.current);
			AbstractCommandBlockEditScreen.this.keepSuggestions = false;
			this.tabCycles = true;
		}

		public void hide() {
			AbstractCommandBlockEditScreen.this.suggestions = null;
		}
	}
}
