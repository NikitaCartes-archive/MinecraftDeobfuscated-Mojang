package net.minecraft.client.gui.font;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TextFieldHelper {
	private final Minecraft minecraft;
	private final Font font;
	private final Supplier<String> getMessageFn;
	private final Consumer<String> setMessageFn;
	private final int maxWidth;
	private int cursorPos;
	private int selectionPos;

	public TextFieldHelper(Minecraft minecraft, Supplier<String> supplier, Consumer<String> consumer, int i) {
		this.minecraft = minecraft;
		this.font = minecraft.font;
		this.getMessageFn = supplier;
		this.setMessageFn = consumer;
		this.maxWidth = i;
		this.setEnd();
	}

	public boolean charTyped(char c) {
		if (SharedConstants.isAllowedChatCharacter(c)) {
			this.insertText(Character.toString(c));
		}

		return true;
	}

	private void insertText(String string) {
		if (this.selectionPos != this.cursorPos) {
			this.deleteSelection();
		}

		String string2 = (String)this.getMessageFn.get();
		this.cursorPos = Mth.clamp(this.cursorPos, 0, string2.length());
		String string3 = new StringBuilder(string2).insert(this.cursorPos, string).toString();
		if (this.font.width(string3) <= this.maxWidth) {
			this.setMessageFn.accept(string3);
			this.selectionPos = this.cursorPos = Math.min(string3.length(), this.cursorPos + string.length());
		}
	}

	public boolean keyPressed(int i) {
		String string = (String)this.getMessageFn.get();
		if (Screen.isSelectAll(i)) {
			this.selectionPos = 0;
			this.cursorPos = string.length();
			return true;
		} else if (Screen.isCopy(i)) {
			this.minecraft.keyboardHandler.setClipboard(this.getSelected());
			return true;
		} else if (Screen.isPaste(i)) {
			this.insertText(SharedConstants.filterText(ChatFormatting.stripFormatting(this.minecraft.keyboardHandler.getClipboard().replaceAll("\\r", ""))));
			this.selectionPos = this.cursorPos;
			return true;
		} else if (Screen.isCut(i)) {
			this.minecraft.keyboardHandler.setClipboard(this.getSelected());
			this.deleteSelection();
			return true;
		} else if (i == 259) {
			if (!string.isEmpty()) {
				if (this.selectionPos != this.cursorPos) {
					this.deleteSelection();
				} else if (this.cursorPos > 0) {
					string = new StringBuilder(string).deleteCharAt(Math.max(0, this.cursorPos - 1)).toString();
					this.selectionPos = this.cursorPos = Math.max(0, this.cursorPos - 1);
					this.setMessageFn.accept(string);
				}
			}

			return true;
		} else if (i == 261) {
			if (!string.isEmpty()) {
				if (this.selectionPos != this.cursorPos) {
					this.deleteSelection();
				} else if (this.cursorPos < string.length()) {
					string = new StringBuilder(string).deleteCharAt(Math.max(0, this.cursorPos)).toString();
					this.setMessageFn.accept(string);
				}
			}

			return true;
		} else if (i == 263) {
			int j = this.font.isBidirectional() ? 1 : -1;
			if (Screen.hasControlDown()) {
				this.cursorPos = this.font.getWordPosition(string, j, this.cursorPos, true);
			} else {
				this.cursorPos = Math.max(0, Math.min(string.length(), this.cursorPos + j));
			}

			if (!Screen.hasShiftDown()) {
				this.selectionPos = this.cursorPos;
			}

			return true;
		} else if (i == 262) {
			int jx = this.font.isBidirectional() ? -1 : 1;
			if (Screen.hasControlDown()) {
				this.cursorPos = this.font.getWordPosition(string, jx, this.cursorPos, true);
			} else {
				this.cursorPos = Math.max(0, Math.min(string.length(), this.cursorPos + jx));
			}

			if (!Screen.hasShiftDown()) {
				this.selectionPos = this.cursorPos;
			}

			return true;
		} else if (i == 268) {
			this.cursorPos = 0;
			if (!Screen.hasShiftDown()) {
				this.selectionPos = this.cursorPos;
			}

			return true;
		} else if (i == 269) {
			this.cursorPos = ((String)this.getMessageFn.get()).length();
			if (!Screen.hasShiftDown()) {
				this.selectionPos = this.cursorPos;
			}

			return true;
		} else {
			return false;
		}
	}

	private String getSelected() {
		String string = (String)this.getMessageFn.get();
		int i = Math.min(this.cursorPos, this.selectionPos);
		int j = Math.max(this.cursorPos, this.selectionPos);
		return string.substring(i, j);
	}

	private void deleteSelection() {
		if (this.selectionPos != this.cursorPos) {
			String string = (String)this.getMessageFn.get();
			int i = Math.min(this.cursorPos, this.selectionPos);
			int j = Math.max(this.cursorPos, this.selectionPos);
			String string2 = string.substring(0, i) + string.substring(j);
			this.cursorPos = i;
			this.selectionPos = this.cursorPos;
			this.setMessageFn.accept(string2);
		}
	}

	public void setEnd() {
		this.selectionPos = this.cursorPos = ((String)this.getMessageFn.get()).length();
	}

	public int getCursorPos() {
		return this.cursorPos;
	}

	public int getSelectionPos() {
		return this.selectionPos;
	}
}
