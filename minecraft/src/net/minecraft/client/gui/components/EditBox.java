package net.minecraft.client.gui.components;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class EditBox extends AbstractWidget implements Widget, GuiEventListener {
	private final Font font;
	private String value = "";
	private int maxLength = 32;
	private int frame;
	private boolean bordered = true;
	private boolean canLoseFocus = true;
	private boolean isEditable = true;
	private boolean shiftPressed;
	private int displayPos;
	private int cursorPos;
	private int highlightPos;
	private int textColor = 14737632;
	private int textColorUneditable = 7368816;
	private String suggestion;
	private Consumer<String> responder;
	private Predicate<String> filter = Predicates.alwaysTrue();
	private BiFunction<String, Integer, String> formatter = (stringx, integer) -> stringx;

	public EditBox(Font font, int i, int j, int k, int l, String string) {
		this(font, i, j, k, l, null, string);
	}

	public EditBox(Font font, int i, int j, int k, int l, @Nullable EditBox editBox, String string) {
		super(i, j, k, l, string);
		this.font = font;
		if (editBox != null) {
			this.setValue(editBox.getValue());
		}
	}

	public void setResponder(Consumer<String> consumer) {
		this.responder = consumer;
	}

	public void setFormatter(BiFunction<String, Integer, String> biFunction) {
		this.formatter = biFunction;
	}

	public void tick() {
		this.frame++;
	}

	@Override
	protected String getNarrationMessage() {
		String string = this.getMessage();
		return string.isEmpty() ? "" : I18n.get("gui.narrate.editBox", string, this.value);
	}

	public void setValue(String string) {
		if (this.filter.test(string)) {
			if (string.length() > this.maxLength) {
				this.value = string.substring(0, this.maxLength);
			} else {
				this.value = string;
			}

			this.moveCursorToEnd();
			this.setHighlightPos(this.cursorPos);
			this.onValueChange(string);
		}
	}

	public String getValue() {
		return this.value;
	}

	public String getHighlighted() {
		int i = this.cursorPos < this.highlightPos ? this.cursorPos : this.highlightPos;
		int j = this.cursorPos < this.highlightPos ? this.highlightPos : this.cursorPos;
		return this.value.substring(i, j);
	}

	public void setFilter(Predicate<String> predicate) {
		this.filter = predicate;
	}

	public void insertText(String string) {
		String string2 = "";
		String string3 = SharedConstants.filterText(string);
		int i = this.cursorPos < this.highlightPos ? this.cursorPos : this.highlightPos;
		int j = this.cursorPos < this.highlightPos ? this.highlightPos : this.cursorPos;
		int k = this.maxLength - this.value.length() - (i - j);
		if (!this.value.isEmpty()) {
			string2 = string2 + this.value.substring(0, i);
		}

		int l;
		if (k < string3.length()) {
			string2 = string2 + string3.substring(0, k);
			l = k;
		} else {
			string2 = string2 + string3;
			l = string3.length();
		}

		if (!this.value.isEmpty() && j < this.value.length()) {
			string2 = string2 + this.value.substring(j);
		}

		if (this.filter.test(string2)) {
			this.value = string2;
			this.setCursorPosition(i + l);
			this.setHighlightPos(this.cursorPos);
			this.onValueChange(this.value);
		}
	}

	private void onValueChange(String string) {
		if (this.responder != null) {
			this.responder.accept(string);
		}

		this.nextNarration = Util.getMillis() + 500L;
	}

	private void deleteText(int i) {
		if (Screen.hasControlDown()) {
			this.deleteWords(i);
		} else {
			this.deleteChars(i);
		}
	}

	public void deleteWords(int i) {
		if (!this.value.isEmpty()) {
			if (this.highlightPos != this.cursorPos) {
				this.insertText("");
			} else {
				this.deleteChars(this.getWordPosition(i) - this.cursorPos);
			}
		}
	}

	public void deleteChars(int i) {
		if (!this.value.isEmpty()) {
			if (this.highlightPos != this.cursorPos) {
				this.insertText("");
			} else {
				boolean bl = i < 0;
				int j = bl ? this.cursorPos + i : this.cursorPos;
				int k = bl ? this.cursorPos : this.cursorPos + i;
				String string = "";
				if (j >= 0) {
					string = this.value.substring(0, j);
				}

				if (k < this.value.length()) {
					string = string + this.value.substring(k);
				}

				if (this.filter.test(string)) {
					this.value = string;
					if (bl) {
						this.moveCursor(i);
					}

					this.onValueChange(this.value);
				}
			}
		}
	}

	public int getWordPosition(int i) {
		return this.getWordPosition(i, this.getCursorPosition());
	}

	private int getWordPosition(int i, int j) {
		return this.getWordPosition(i, j, true);
	}

	private int getWordPosition(int i, int j, boolean bl) {
		int k = j;
		boolean bl2 = i < 0;
		int l = Math.abs(i);

		for (int m = 0; m < l; m++) {
			if (!bl2) {
				int n = this.value.length();
				k = this.value.indexOf(32, k);
				if (k == -1) {
					k = n;
				} else {
					while (bl && k < n && this.value.charAt(k) == ' ') {
						k++;
					}
				}
			} else {
				while (bl && k > 0 && this.value.charAt(k - 1) == ' ') {
					k--;
				}

				while (k > 0 && this.value.charAt(k - 1) != ' ') {
					k--;
				}
			}
		}

		return k;
	}

	public void moveCursor(int i) {
		this.moveCursorTo(this.cursorPos + i);
	}

	public void moveCursorTo(int i) {
		this.setCursorPosition(i);
		if (!this.shiftPressed) {
			this.setHighlightPos(this.cursorPos);
		}

		this.onValueChange(this.value);
	}

	public void setCursorPosition(int i) {
		this.cursorPos = Mth.clamp(i, 0, this.value.length());
	}

	public void moveCursorToStart() {
		this.moveCursorTo(0);
	}

	public void moveCursorToEnd() {
		this.moveCursorTo(this.value.length());
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (!this.canConsumeInput()) {
			return false;
		} else {
			this.shiftPressed = Screen.hasShiftDown();
			if (Screen.isSelectAll(i)) {
				this.moveCursorToEnd();
				this.setHighlightPos(0);
				return true;
			} else if (Screen.isCopy(i)) {
				Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
				return true;
			} else if (Screen.isPaste(i)) {
				if (this.isEditable) {
					this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
				}

				return true;
			} else if (Screen.isCut(i)) {
				Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
				if (this.isEditable) {
					this.insertText("");
				}

				return true;
			} else {
				switch (i) {
					case 259:
						if (this.isEditable) {
							this.shiftPressed = false;
							this.deleteText(-1);
							this.shiftPressed = Screen.hasShiftDown();
						}

						return true;
					case 260:
					case 264:
					case 265:
					case 266:
					case 267:
					default:
						return false;
					case 261:
						if (this.isEditable) {
							this.shiftPressed = false;
							this.deleteText(1);
							this.shiftPressed = Screen.hasShiftDown();
						}

						return true;
					case 262:
						if (Screen.hasControlDown()) {
							this.moveCursorTo(this.getWordPosition(1));
						} else {
							this.moveCursor(1);
						}

						return true;
					case 263:
						if (Screen.hasControlDown()) {
							this.moveCursorTo(this.getWordPosition(-1));
						} else {
							this.moveCursor(-1);
						}

						return true;
					case 268:
						this.moveCursorToStart();
						return true;
					case 269:
						this.moveCursorToEnd();
						return true;
				}
			}
		}
	}

	public boolean canConsumeInput() {
		return this.isVisible() && this.isFocused() && this.isEditable();
	}

	@Override
	public boolean charTyped(char c, int i) {
		if (!this.canConsumeInput()) {
			return false;
		} else if (SharedConstants.isAllowedChatCharacter(c)) {
			if (this.isEditable) {
				this.insertText(Character.toString(c));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (!this.isVisible()) {
			return false;
		} else {
			boolean bl = d >= (double)this.x && d < (double)(this.x + this.width) && e >= (double)this.y && e < (double)(this.y + this.height);
			if (this.canLoseFocus) {
				this.setFocus(bl);
			}

			if (this.isFocused() && bl && i == 0) {
				int j = Mth.floor(d) - this.x;
				if (this.bordered) {
					j -= 4;
				}

				String string = this.font.substrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
				this.moveCursorTo(this.font.substrByWidth(string, j).length() + this.displayPos);
				return true;
			} else {
				return false;
			}
		}
	}

	public void setFocus(boolean bl) {
		super.setFocused(bl);
	}

	@Override
	public void renderButton(int i, int j, float f) {
		if (this.isVisible()) {
			if (this.isBordered()) {
				fill(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
				fill(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
			}

			int k = this.isEditable ? this.textColor : this.textColorUneditable;
			int l = this.cursorPos - this.displayPos;
			int m = this.highlightPos - this.displayPos;
			String string = this.font.substrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
			boolean bl = l >= 0 && l <= string.length();
			boolean bl2 = this.isFocused() && this.frame / 6 % 2 == 0 && bl;
			int n = this.bordered ? this.x + 4 : this.x;
			int o = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
			int p = n;
			if (m > string.length()) {
				m = string.length();
			}

			if (!string.isEmpty()) {
				String string2 = bl ? string.substring(0, l) : string;
				p = this.font.drawShadow((String)this.formatter.apply(string2, this.displayPos), (float)n, (float)o, k);
			}

			boolean bl3 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
			int q = p;
			if (!bl) {
				q = l > 0 ? n + this.width : n;
			} else if (bl3) {
				q = p - 1;
				p--;
			}

			if (!string.isEmpty() && bl && l < string.length()) {
				this.font.drawShadow((String)this.formatter.apply(string.substring(l), this.cursorPos), (float)p, (float)o, k);
			}

			if (!bl3 && this.suggestion != null) {
				this.font.drawShadow(this.suggestion, (float)(q - 1), (float)o, -8355712);
			}

			if (bl2) {
				if (bl3) {
					GuiComponent.fill(q, o - 1, q + 1, o + 1 + 9, -3092272);
				} else {
					this.font.drawShadow("_", (float)q, (float)o, k);
				}
			}

			if (m != l) {
				int r = n + this.font.width(string.substring(0, m));
				this.renderHighlight(q, o - 1, r - 1, o + 1 + 9);
			}
		}
	}

	private void renderHighlight(int i, int j, int k, int l) {
		if (i < k) {
			int m = i;
			i = k;
			k = m;
		}

		if (j < l) {
			int m = j;
			j = l;
			l = m;
		}

		if (k > this.x + this.width) {
			k = this.x + this.width;
		}

		if (i > this.x + this.width) {
			i = this.x + this.width;
		}

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		GlStateManager.color4f(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture();
		GlStateManager.enableColorLogicOp();
		GlStateManager.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
		bufferBuilder.vertex((double)i, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)j, 0.0).endVertex();
		bufferBuilder.vertex((double)i, (double)j, 0.0).endVertex();
		tesselator.end();
		GlStateManager.disableColorLogicOp();
		GlStateManager.enableTexture();
	}

	public void setMaxLength(int i) {
		this.maxLength = i;
		if (this.value.length() > i) {
			this.value = this.value.substring(0, i);
			this.onValueChange(this.value);
		}
	}

	private int getMaxLength() {
		return this.maxLength;
	}

	public int getCursorPosition() {
		return this.cursorPos;
	}

	private boolean isBordered() {
		return this.bordered;
	}

	public void setBordered(boolean bl) {
		this.bordered = bl;
	}

	public void setTextColor(int i) {
		this.textColor = i;
	}

	public void setTextColorUneditable(int i) {
		this.textColorUneditable = i;
	}

	@Override
	public boolean changeFocus(boolean bl) {
		return this.visible && this.isEditable ? super.changeFocus(bl) : false;
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return this.visible && d >= (double)this.x && d < (double)(this.x + this.width) && e >= (double)this.y && e < (double)(this.y + this.height);
	}

	@Override
	protected void onFocusedChanged(boolean bl) {
		if (bl) {
			this.frame = 0;
		}
	}

	private boolean isEditable() {
		return this.isEditable;
	}

	public void setEditable(boolean bl) {
		this.isEditable = bl;
	}

	public int getInnerWidth() {
		return this.isBordered() ? this.width - 8 : this.width;
	}

	public void setHighlightPos(int i) {
		int j = this.value.length();
		this.highlightPos = Mth.clamp(i, 0, j);
		if (this.font != null) {
			if (this.displayPos > j) {
				this.displayPos = j;
			}

			int k = this.getInnerWidth();
			String string = this.font.substrByWidth(this.value.substring(this.displayPos), k);
			int l = string.length() + this.displayPos;
			if (this.highlightPos == this.displayPos) {
				this.displayPos = this.displayPos - this.font.substrByWidth(this.value, k, true).length();
			}

			if (this.highlightPos > l) {
				this.displayPos = this.displayPos + (this.highlightPos - l);
			} else if (this.highlightPos <= this.displayPos) {
				this.displayPos = this.displayPos - (this.displayPos - this.highlightPos);
			}

			this.displayPos = Mth.clamp(this.displayPos, 0, j);
		}
	}

	public void setCanLoseFocus(boolean bl) {
		this.canLoseFocus = bl;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean bl) {
		this.visible = bl;
	}

	public void setSuggestion(@Nullable String string) {
		this.suggestion = string;
	}

	public int getScreenX(int i) {
		return i > this.value.length() ? this.x : this.x + this.font.width(this.value.substring(0, i));
	}

	public void setX(int i) {
		this.x = i;
	}
}
