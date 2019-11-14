package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ChatScreen extends Screen {
	private String historyBuffer = "";
	private int historyPos = -1;
	protected EditBox input;
	private String initial = "";
	private CommandSuggestions commandSuggestions;

	public ChatScreen(String string) {
		super(NarratorChatListener.NO_TITLE);
		this.initial = string;
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
		this.input = new EditBox(this.font, 4, this.height - 12, this.width - 4, 12, I18n.get("chat.editBox")) {
			@Override
			protected String getNarrationMessage() {
				return super.getNarrationMessage() + ChatScreen.this.commandSuggestions.getNarrationMessage();
			}
		};
		this.input.setMaxLength(256);
		this.input.setBordered(false);
		this.input.setValue(this.initial);
		this.input.setResponder(this::onEdited);
		this.children.add(this.input);
		this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
		this.commandSuggestions.updateCommandInfo();
		this.setInitialFocus(this.input);
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.input.getValue();
		this.init(minecraft, i, j);
		this.setChatLine(string);
		this.commandSuggestions.updateCommandInfo();
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
		this.commandSuggestions.setAllowSuggestions(!string2.equals(this.initial));
		this.commandSuggestions.updateCommandInfo();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.commandSuggestions.keyPressed(i, j, k)) {
			return true;
		} else if (super.keyPressed(i, j, k)) {
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

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		if (f > 1.0) {
			f = 1.0;
		}

		if (f < -1.0) {
			f = -1.0;
		}

		if (this.commandSuggestions.mouseScrolled(f)) {
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
		if (this.commandSuggestions.mouseClicked((double)((int)d), (double)((int)e), i)) {
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
				this.commandSuggestions.setAllowSuggestions(false);
				this.historyPos = j;
			}
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.setFocused(this.input);
		this.input.setFocus(true);
		fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
		this.input.render(i, j, f);
		this.commandSuggestions.render(i, j);
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

	private void setChatLine(String string) {
		this.input.setValue(string);
	}
}
