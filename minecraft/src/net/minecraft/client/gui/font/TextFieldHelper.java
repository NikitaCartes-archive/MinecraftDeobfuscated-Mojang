package net.minecraft.client.gui.font;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TextFieldHelper {
	private final Supplier<String> getMessageFn;
	private final Consumer<String> setMessageFn;
	private final Supplier<String> getClipboardFn;
	private final Consumer<String> setClipboardFn;
	private final Predicate<String> stringValidator;
	private int cursorPos;
	private int selectionPos;

	public TextFieldHelper(
		Supplier<String> supplier, Consumer<String> consumer, Supplier<String> supplier2, Consumer<String> consumer2, Predicate<String> predicate
	) {
		this.getMessageFn = supplier;
		this.setMessageFn = consumer;
		this.getClipboardFn = supplier2;
		this.setClipboardFn = consumer2;
		this.stringValidator = predicate;
		this.setCursorToEnd();
	}

	public static Supplier<String> createClipboardGetter(Minecraft minecraft) {
		return () -> getClipboardContents(minecraft);
	}

	public static String getClipboardContents(Minecraft minecraft) {
		return ChatFormatting.stripFormatting(minecraft.keyboardHandler.getClipboard().replaceAll("\\r", ""));
	}

	public static Consumer<String> createClipboardSetter(Minecraft minecraft) {
		return string -> setClipboardContents(minecraft, string);
	}

	public static void setClipboardContents(Minecraft minecraft, String string) {
		minecraft.keyboardHandler.setClipboard(string);
	}

	public boolean charTyped(char c) {
		if (SharedConstants.isAllowedChatCharacter(c)) {
			this.insertText((String)this.getMessageFn.get(), Character.toString(c));
		}

		return true;
	}

	public boolean keyPressed(int i) {
		if (Screen.isSelectAll(i)) {
			this.selectAll();
			return true;
		} else if (Screen.isCopy(i)) {
			this.copy();
			return true;
		} else if (Screen.isPaste(i)) {
			this.paste();
			return true;
		} else if (Screen.isCut(i)) {
			this.cut();
			return true;
		} else if (i == 259) {
			this.removeCharsFromCursor(-1);
			return true;
		} else {
			if (i == 261) {
				this.removeCharsFromCursor(1);
			} else {
				if (i == 263) {
					if (Screen.hasControlDown()) {
						this.moveByWords(-1, Screen.hasShiftDown());
					} else {
						this.moveByChars(-1, Screen.hasShiftDown());
					}

					return true;
				}

				if (i == 262) {
					if (Screen.hasControlDown()) {
						this.moveByWords(1, Screen.hasShiftDown());
					} else {
						this.moveByChars(1, Screen.hasShiftDown());
					}

					return true;
				}

				if (i == 268) {
					this.setCursorToStart(Screen.hasShiftDown());
					return true;
				}

				if (i == 269) {
					this.setCursorToEnd(Screen.hasShiftDown());
					return true;
				}
			}

			return false;
		}
	}

	private int clampToMsgLength(int i) {
		return Mth.clamp(i, 0, ((String)this.getMessageFn.get()).length());
	}

	private void insertText(String string, String string2) {
		if (this.selectionPos != this.cursorPos) {
			string = this.deleteSelection(string);
		}

		this.cursorPos = Mth.clamp(this.cursorPos, 0, string.length());
		String string3 = new StringBuilder(string).insert(this.cursorPos, string2).toString();
		if (this.stringValidator.test(string3)) {
			this.setMessageFn.accept(string3);
			this.selectionPos = this.cursorPos = Math.min(string3.length(), this.cursorPos + string2.length());
		}
	}

	public void insertText(String string) {
		this.insertText((String)this.getMessageFn.get(), string);
	}

	private void resetSelectionIfNeeded(boolean bl) {
		if (!bl) {
			this.selectionPos = this.cursorPos;
		}
	}

	public void moveByChars(int i) {
		this.moveByChars(i, false);
	}

	public void moveByChars(int i, boolean bl) {
		this.cursorPos = Util.offsetByCodepoints((String)this.getMessageFn.get(), this.cursorPos, i);
		this.resetSelectionIfNeeded(bl);
	}

	public void moveByWords(int i) {
		this.moveByWords(i, false);
	}

	public void moveByWords(int i, boolean bl) {
		this.cursorPos = StringSplitter.getWordPosition((String)this.getMessageFn.get(), i, this.cursorPos, true);
		this.resetSelectionIfNeeded(bl);
	}

	public void removeCharsFromCursor(int i) {
		String string = (String)this.getMessageFn.get();
		if (!string.isEmpty()) {
			String string2;
			if (this.selectionPos != this.cursorPos) {
				string2 = this.deleteSelection(string);
			} else {
				int j = Util.offsetByCodepoints(string, this.cursorPos, i);
				int k = Math.min(j, this.cursorPos);
				int l = Math.max(j, this.cursorPos);
				string2 = new StringBuilder(string).delete(k, l).toString();
				if (i < 0) {
					this.selectionPos = this.cursorPos = k;
				}
			}

			this.setMessageFn.accept(string2);
		}
	}

	public void cut() {
		String string = (String)this.getMessageFn.get();
		this.setClipboardFn.accept(this.getSelected(string));
		this.setMessageFn.accept(this.deleteSelection(string));
	}

	public void paste() {
		this.insertText((String)this.getMessageFn.get(), (String)this.getClipboardFn.get());
		this.selectionPos = this.cursorPos;
	}

	public void copy() {
		this.setClipboardFn.accept(this.getSelected((String)this.getMessageFn.get()));
	}

	public void selectAll() {
		this.selectionPos = 0;
		this.cursorPos = ((String)this.getMessageFn.get()).length();
	}

	private String getSelected(String string) {
		int i = Math.min(this.cursorPos, this.selectionPos);
		int j = Math.max(this.cursorPos, this.selectionPos);
		return string.substring(i, j);
	}

	private String deleteSelection(String string) {
		if (this.selectionPos == this.cursorPos) {
			return string;
		} else {
			int i = Math.min(this.cursorPos, this.selectionPos);
			int j = Math.max(this.cursorPos, this.selectionPos);
			String string2 = string.substring(0, i) + string.substring(j);
			this.selectionPos = this.cursorPos = i;
			return string2;
		}
	}

	public void setCursorToStart() {
		this.setCursorToStart(false);
	}

	private void setCursorToStart(boolean bl) {
		this.cursorPos = 0;
		this.resetSelectionIfNeeded(bl);
	}

	public void setCursorToEnd() {
		this.setCursorToEnd(false);
	}

	private void setCursorToEnd(boolean bl) {
		this.cursorPos = ((String)this.getMessageFn.get()).length();
		this.resetSelectionIfNeeded(bl);
	}

	public int getCursorPos() {
		return this.cursorPos;
	}

	public void setCursorPos(int i) {
		this.setCursorPos(i, true);
	}

	public void setCursorPos(int i, boolean bl) {
		this.cursorPos = this.clampToMsgLength(i);
		this.resetSelectionIfNeeded(bl);
	}

	public int getSelectionPos() {
		return this.selectionPos;
	}

	public void setSelectionPos(int i) {
		this.selectionPos = this.clampToMsgLength(i);
	}

	public void setSelectionRange(int i, int j) {
		int k = ((String)this.getMessageFn.get()).length();
		this.cursorPos = Mth.clamp(i, 0, k);
		this.selectionPos = Mth.clamp(j, 0, k);
	}

	public boolean isSelecting() {
		return this.cursorPos != this.selectionPos;
	}
}
