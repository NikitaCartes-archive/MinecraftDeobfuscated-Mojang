package net.minecraft.client.gui.components;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;

@Environment(EnvType.CLIENT)
public class MultilineTextField {
	public static final int NO_CHARACTER_LIMIT = Integer.MAX_VALUE;
	private static final int LINE_SEEK_PIXEL_BIAS = 2;
	private final Font font;
	private final List<MultilineTextField.StringView> displayLines = Lists.<MultilineTextField.StringView>newArrayList();
	private String value;
	private int cursor;
	private int selectCursor;
	private boolean selecting;
	private int characterLimit = Integer.MAX_VALUE;
	private final int width;
	private Consumer<String> valueListener = string -> {
	};
	private Runnable cursorListener = () -> {
	};

	public MultilineTextField(Font font, int i) {
		this.font = font;
		this.width = i;
		this.setValue("");
	}

	public int characterLimit() {
		return this.characterLimit;
	}

	public void setCharacterLimit(int i) {
		if (i < 0) {
			throw new IllegalArgumentException("Character limit cannot be negative");
		} else {
			this.characterLimit = i;
		}
	}

	public boolean hasCharacterLimit() {
		return this.characterLimit != Integer.MAX_VALUE;
	}

	public void setValueListener(Consumer<String> consumer) {
		this.valueListener = consumer;
	}

	public void setCursorListener(Runnable runnable) {
		this.cursorListener = runnable;
	}

	public void setValue(String string) {
		this.value = this.truncateFullText(string);
		this.cursor = this.value.length();
		this.selectCursor = this.cursor;
		this.onValueChange();
	}

	public String value() {
		return this.value;
	}

	public void insertText(String string) {
		if (!string.isEmpty() || this.hasSelection()) {
			String string2 = this.truncateInsertionText(SharedConstants.filterText(string, true));
			MultilineTextField.StringView stringView = this.getSelected();
			this.value = new StringBuilder(this.value).replace(stringView.beginIndex, stringView.endIndex, string2).toString();
			this.cursor = stringView.beginIndex + string2.length();
			this.selectCursor = this.cursor;
			this.onValueChange();
		}
	}

	public void deleteText(int i) {
		if (!this.hasSelection()) {
			this.selectCursor = Mth.clamp(this.cursor + i, 0, this.value.length());
		}

		this.insertText("");
	}

	public int cursor() {
		return this.cursor;
	}

	public void setSelecting(boolean bl) {
		this.selecting = bl;
	}

	public MultilineTextField.StringView getSelected() {
		return new MultilineTextField.StringView(Math.min(this.selectCursor, this.cursor), Math.max(this.selectCursor, this.cursor));
	}

	public int getLineCount() {
		return this.displayLines.size();
	}

	public int getLineAtCursor() {
		for (int i = 0; i < this.displayLines.size(); i++) {
			MultilineTextField.StringView stringView = (MultilineTextField.StringView)this.displayLines.get(i);
			if (this.cursor >= stringView.beginIndex && this.cursor <= stringView.endIndex) {
				return i;
			}
		}

		return -1;
	}

	public MultilineTextField.StringView getLineView(int i) {
		return (MultilineTextField.StringView)this.displayLines.get(Mth.clamp(i, 0, this.displayLines.size() - 1));
	}

	public void seekCursor(Whence whence, int i) {
		switch (whence) {
			case ABSOLUTE:
				this.cursor = i;
				break;
			case RELATIVE:
				this.cursor += i;
				break;
			case END:
				this.cursor = this.value.length() + i;
		}

		this.cursor = Mth.clamp(this.cursor, 0, this.value.length());
		this.cursorListener.run();
		if (!this.selecting) {
			this.selectCursor = this.cursor;
		}
	}

	public void seekCursorLine(int i) {
		if (i != 0) {
			int j = this.font.width(this.value.substring(this.getCursorLineView().beginIndex, this.cursor)) + 2;
			MultilineTextField.StringView stringView = this.getCursorLineView(i);
			int k = this.font.plainSubstrByWidth(this.value.substring(stringView.beginIndex, stringView.endIndex), j).length();
			this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex + k);
		}
	}

	public void seekCursorToPoint(double d, double e) {
		int i = Mth.floor(d);
		int j = Mth.floor(e / 9.0);
		MultilineTextField.StringView stringView = (MultilineTextField.StringView)this.displayLines.get(Mth.clamp(j, 0, this.displayLines.size() - 1));
		int k = this.font.plainSubstrByWidth(this.value.substring(stringView.beginIndex, stringView.endIndex), i).length();
		this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex + k);
	}

	public boolean keyPressed(int i) {
		this.selecting = Screen.hasShiftDown();
		if (Screen.isSelectAll(i)) {
			this.cursor = this.value.length();
			this.selectCursor = 0;
			return true;
		} else if (Screen.isCopy(i)) {
			Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
			return true;
		} else if (Screen.isPaste(i)) {
			this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
			return true;
		} else if (Screen.isCut(i)) {
			Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
			this.insertText("");
			return true;
		} else {
			switch (i) {
				case 257:
				case 335:
					this.insertText("\n");
					return true;
				case 259:
					if (Screen.hasControlDown()) {
						MultilineTextField.StringView stringView = this.getPreviousWord();
						this.deleteText(stringView.beginIndex - this.cursor);
					} else {
						this.deleteText(-1);
					}

					return true;
				case 261:
					if (Screen.hasControlDown()) {
						MultilineTextField.StringView stringView = this.getNextWord();
						this.deleteText(stringView.beginIndex - this.cursor);
					} else {
						this.deleteText(1);
					}

					return true;
				case 262:
					if (Screen.hasControlDown()) {
						MultilineTextField.StringView stringView = this.getNextWord();
						this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex);
					} else {
						this.seekCursor(Whence.RELATIVE, 1);
					}

					return true;
				case 263:
					if (Screen.hasControlDown()) {
						MultilineTextField.StringView stringView = this.getPreviousWord();
						this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex);
					} else {
						this.seekCursor(Whence.RELATIVE, -1);
					}

					return true;
				case 264:
					if (!Screen.hasControlDown()) {
						this.seekCursorLine(1);
					}

					return true;
				case 265:
					if (!Screen.hasControlDown()) {
						this.seekCursorLine(-1);
					}

					return true;
				case 266:
					this.seekCursor(Whence.ABSOLUTE, 0);
					return true;
				case 267:
					this.seekCursor(Whence.END, 0);
					return true;
				case 268:
					if (Screen.hasControlDown()) {
						this.seekCursor(Whence.ABSOLUTE, 0);
					} else {
						this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().beginIndex);
					}

					return true;
				case 269:
					if (Screen.hasControlDown()) {
						this.seekCursor(Whence.END, 0);
					} else {
						this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().endIndex);
					}

					return true;
				default:
					return false;
			}
		}
	}

	public Iterable<MultilineTextField.StringView> iterateLines() {
		return this.displayLines;
	}

	public boolean hasSelection() {
		return this.selectCursor != this.cursor;
	}

	@VisibleForTesting
	public String getSelectedText() {
		MultilineTextField.StringView stringView = this.getSelected();
		return this.value.substring(stringView.beginIndex, stringView.endIndex);
	}

	private MultilineTextField.StringView getCursorLineView() {
		return this.getCursorLineView(0);
	}

	private MultilineTextField.StringView getCursorLineView(int i) {
		int j = this.getLineAtCursor();
		if (j < 0) {
			throw new IllegalStateException("Cursor is not within text (cursor = " + this.cursor + ", length = " + this.value.length() + ")");
		} else {
			return (MultilineTextField.StringView)this.displayLines.get(Mth.clamp(j + i, 0, this.displayLines.size() - 1));
		}
	}

	@VisibleForTesting
	public MultilineTextField.StringView getPreviousWord() {
		if (this.value.isEmpty()) {
			return MultilineTextField.StringView.EMPTY;
		} else {
			int i = Mth.clamp(this.cursor, 0, this.value.length() - 1);

			while (i > 0 && Character.isWhitespace(this.value.charAt(i - 1))) {
				i--;
			}

			while (i > 0 && !Character.isWhitespace(this.value.charAt(i - 1))) {
				i--;
			}

			return new MultilineTextField.StringView(i, this.getWordEndPosition(i));
		}
	}

	@VisibleForTesting
	public MultilineTextField.StringView getNextWord() {
		if (this.value.isEmpty()) {
			return MultilineTextField.StringView.EMPTY;
		} else {
			int i = Mth.clamp(this.cursor, 0, this.value.length() - 1);

			while (i < this.value.length() && !Character.isWhitespace(this.value.charAt(i))) {
				i++;
			}

			while (i < this.value.length() && Character.isWhitespace(this.value.charAt(i))) {
				i++;
			}

			return new MultilineTextField.StringView(i, this.getWordEndPosition(i));
		}
	}

	private int getWordEndPosition(int i) {
		int j = i;

		while (j < this.value.length() && !Character.isWhitespace(this.value.charAt(j))) {
			j++;
		}

		return j;
	}

	private void onValueChange() {
		this.reflowDisplayLines();
		this.valueListener.accept(this.value);
		this.cursorListener.run();
	}

	private void reflowDisplayLines() {
		this.displayLines.clear();
		if (this.value.isEmpty()) {
			this.displayLines.add(MultilineTextField.StringView.EMPTY);
		} else {
			this.font
				.getSplitter()
				.splitLines(this.value, this.width, Style.EMPTY, false, (style, i, j) -> this.displayLines.add(new MultilineTextField.StringView(i, j)));
			if (this.value.charAt(this.value.length() - 1) == '\n') {
				this.displayLines.add(new MultilineTextField.StringView(this.value.length(), this.value.length()));
			}
		}
	}

	private String truncateFullText(String string) {
		return this.hasCharacterLimit() ? StringUtil.truncateStringIfNecessary(string, this.characterLimit, false) : string;
	}

	private String truncateInsertionText(String string) {
		if (this.hasCharacterLimit()) {
			int i = this.characterLimit - this.value.length();
			return StringUtil.truncateStringIfNecessary(string, i, false);
		} else {
			return string;
		}
	}

	@Environment(EnvType.CLIENT)
	protected static record StringView(int beginIndex, int endIndex) {
		static final MultilineTextField.StringView EMPTY = new MultilineTextField.StringView(0, 0);
	}
}
