package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.ListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class BookEditScreen extends Screen {
	private final Player owner;
	private final ItemStack book;
	private boolean isModified;
	private boolean isSigning;
	private int frameTick;
	private int currentPage;
	private final List<String> pages = Lists.<String>newArrayList();
	private String title = "";
	private int cursorPos;
	private int selectionPos;
	private long lastClickTime;
	private int lastIndex = -1;
	private PageButton forwardButton;
	private PageButton backButton;
	private Button doneButton;
	private Button signButton;
	private Button finalizeButton;
	private Button cancelButton;
	private final InteractionHand hand;

	public BookEditScreen(Player player, ItemStack itemStack, InteractionHand interactionHand) {
		super(NarratorChatListener.NO_TITLE);
		this.owner = player;
		this.book = itemStack;
		this.hand = interactionHand;
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null) {
			ListTag listTag = compoundTag.getList("pages", 8).copy();

			for (int i = 0; i < listTag.size(); i++) {
				this.pages.add(listTag.getString(i));
			}
		}

		if (this.pages.isEmpty()) {
			this.pages.add("");
		}
	}

	private int getNumPages() {
		return this.pages.size();
	}

	@Override
	public void tick() {
		super.tick();
		this.frameTick++;
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.signButton = this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, I18n.get("book.signButton"), button -> {
			this.isSigning = true;
			this.updateButtonVisibility();
		}));
		this.doneButton = this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, I18n.get("gui.done"), button -> {
			this.minecraft.setScreen(null);
			this.saveChanges(false);
		}));
		this.finalizeButton = this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, I18n.get("book.finalizeButton"), button -> {
			if (this.isSigning) {
				this.saveChanges(true);
				this.minecraft.setScreen(null);
			}
		}));
		this.cancelButton = this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, I18n.get("gui.cancel"), button -> {
			if (this.isSigning) {
				this.isSigning = false;
			}

			this.updateButtonVisibility();
		}));
		int i = (this.width - 192) / 2;
		int j = 2;
		this.forwardButton = this.addButton(new PageButton(i + 116, 159, true, button -> this.pageForward(), true));
		this.backButton = this.addButton(new PageButton(i + 43, 159, false, button -> this.pageBack(), true));
		this.updateButtonVisibility();
	}

	private String filterText(String string) {
		StringBuilder stringBuilder = new StringBuilder();

		for (char c : string.toCharArray()) {
			if (c != 167 && c != 127) {
				stringBuilder.append(c);
			}
		}

		return stringBuilder.toString();
	}

	private void pageBack() {
		if (this.currentPage > 0) {
			this.currentPage--;
			this.cursorPos = 0;
			this.selectionPos = this.cursorPos;
		}

		this.updateButtonVisibility();
	}

	private void pageForward() {
		if (this.currentPage < this.getNumPages() - 1) {
			this.currentPage++;
			this.cursorPos = 0;
			this.selectionPos = this.cursorPos;
		} else {
			this.appendPageToBook();
			if (this.currentPage < this.getNumPages() - 1) {
				this.currentPage++;
			}

			this.cursorPos = 0;
			this.selectionPos = this.cursorPos;
		}

		this.updateButtonVisibility();
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void updateButtonVisibility() {
		this.backButton.visible = !this.isSigning && this.currentPage > 0;
		this.forwardButton.visible = !this.isSigning;
		this.doneButton.visible = !this.isSigning;
		this.signButton.visible = !this.isSigning;
		this.cancelButton.visible = this.isSigning;
		this.finalizeButton.visible = this.isSigning;
		this.finalizeButton.active = !this.title.trim().isEmpty();
	}

	private void eraseEmptyTrailingPages() {
		ListIterator<String> listIterator = this.pages.listIterator(this.pages.size());

		while (listIterator.hasPrevious() && ((String)listIterator.previous()).isEmpty()) {
			listIterator.remove();
		}
	}

	private void saveChanges(boolean bl) {
		if (this.isModified) {
			this.eraseEmptyTrailingPages();
			ListTag listTag = new ListTag();
			this.pages.stream().map(StringTag::new).forEach(listTag::add);
			if (!this.pages.isEmpty()) {
				this.book.addTagElement("pages", listTag);
			}

			if (bl) {
				this.book.addTagElement("author", new StringTag(this.owner.getGameProfile().getName()));
				this.book.addTagElement("title", new StringTag(this.title.trim()));
			}

			this.minecraft.getConnection().send(new ServerboundEditBookPacket(this.book, bl, this.hand));
		}
	}

	private void appendPageToBook() {
		if (this.getNumPages() < 100) {
			this.pages.add("");
			this.isModified = true;
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else {
			return this.isSigning ? this.titleKeyPressed(i, j, k) : this.bookKeyPressed(i, j, k);
		}
	}

	@Override
	public boolean charTyped(char c, int i) {
		if (super.charTyped(c, i)) {
			return true;
		} else if (this.isSigning) {
			if (this.title.length() < 16 && SharedConstants.isAllowedChatCharacter(c)) {
				this.title = this.title + Character.toString(c);
				this.updateButtonVisibility();
				this.isModified = true;
				return true;
			} else {
				return false;
			}
		} else if (SharedConstants.isAllowedChatCharacter(c)) {
			this.insertText(Character.toString(c));
			return true;
		} else {
			return false;
		}
	}

	private boolean bookKeyPressed(int i, int j, int k) {
		String string = this.getCurrentPageText();
		if (Screen.isSelectAll(i)) {
			this.selectionPos = 0;
			this.cursorPos = string.length();
			return true;
		} else if (Screen.isCopy(i)) {
			this.minecraft.keyboardHandler.setClipboard(this.getSelected());
			return true;
		} else if (Screen.isPaste(i)) {
			this.insertText(this.filterText(ChatFormatting.stripFormatting(this.minecraft.keyboardHandler.getClipboard().replaceAll("\\r", ""))));
			this.selectionPos = this.cursorPos;
			return true;
		} else if (Screen.isCut(i)) {
			this.minecraft.keyboardHandler.setClipboard(this.getSelected());
			this.deleteSelection();
			return true;
		} else {
			switch (i) {
				case 257:
				case 335:
					this.insertText("\n");
					return true;
				case 259:
					this.keyBackspace(string);
					return true;
				case 261:
					this.keyDelete(string);
					return true;
				case 262:
					this.keyRight(string);
					return true;
				case 263:
					this.keyLeft(string);
					return true;
				case 264:
					this.keyDown(string);
					return true;
				case 265:
					this.keyUp(string);
					return true;
				case 266:
					this.backButton.onPress();
					return true;
				case 267:
					this.forwardButton.onPress();
					return true;
				case 268:
					this.keyHome(string);
					return true;
				case 269:
					this.keyEnd(string);
					return true;
				default:
					return false;
			}
		}
	}

	private void keyBackspace(String string) {
		if (!string.isEmpty()) {
			if (this.selectionPos != this.cursorPos) {
				this.deleteSelection();
			} else if (this.cursorPos > 0) {
				String string2 = new StringBuilder(string).deleteCharAt(Math.max(0, this.cursorPos - 1)).toString();
				this.setCurrentPageText(string2);
				this.cursorPos = Math.max(0, this.cursorPos - 1);
				this.selectionPos = this.cursorPos;
			}
		}
	}

	private void keyDelete(String string) {
		if (!string.isEmpty()) {
			if (this.selectionPos != this.cursorPos) {
				this.deleteSelection();
			} else if (this.cursorPos < string.length()) {
				String string2 = new StringBuilder(string).deleteCharAt(Math.max(0, this.cursorPos)).toString();
				this.setCurrentPageText(string2);
			}
		}
	}

	private void keyLeft(String string) {
		int i = this.font.isBidirectional() ? 1 : -1;
		if (Screen.hasControlDown()) {
			this.cursorPos = this.font.getWordPosition(string, i, this.cursorPos, true);
		} else {
			this.cursorPos = Math.max(0, this.cursorPos + i);
		}

		if (!Screen.hasShiftDown()) {
			this.selectionPos = this.cursorPos;
		}
	}

	private void keyRight(String string) {
		int i = this.font.isBidirectional() ? -1 : 1;
		if (Screen.hasControlDown()) {
			this.cursorPos = this.font.getWordPosition(string, i, this.cursorPos, true);
		} else {
			this.cursorPos = Math.min(string.length(), this.cursorPos + i);
		}

		if (!Screen.hasShiftDown()) {
			this.selectionPos = this.cursorPos;
		}
	}

	private void keyUp(String string) {
		if (!string.isEmpty()) {
			BookEditScreen.Pos2i pos2i = this.getPositionAtIndex(string, this.cursorPos);
			if (pos2i.y == 0) {
				this.cursorPos = 0;
				if (!Screen.hasShiftDown()) {
					this.selectionPos = this.cursorPos;
				}
			} else {
				int i = this.getIndexAtPosition(string, new BookEditScreen.Pos2i(pos2i.x + this.getWidthAt(string, this.cursorPos) / 3, pos2i.y - 9));
				if (i >= 0) {
					this.cursorPos = i;
					if (!Screen.hasShiftDown()) {
						this.selectionPos = this.cursorPos;
					}
				}
			}
		}
	}

	private void keyDown(String string) {
		if (!string.isEmpty()) {
			BookEditScreen.Pos2i pos2i = this.getPositionAtIndex(string, this.cursorPos);
			int i = this.font.wordWrapHeight(string + "" + ChatFormatting.BLACK + "_", 114);
			if (pos2i.y + 9 == i) {
				this.cursorPos = string.length();
				if (!Screen.hasShiftDown()) {
					this.selectionPos = this.cursorPos;
				}
			} else {
				int j = this.getIndexAtPosition(string, new BookEditScreen.Pos2i(pos2i.x + this.getWidthAt(string, this.cursorPos) / 3, pos2i.y + 9));
				if (j >= 0) {
					this.cursorPos = j;
					if (!Screen.hasShiftDown()) {
						this.selectionPos = this.cursorPos;
					}
				}
			}
		}
	}

	private void keyHome(String string) {
		this.cursorPos = this.getIndexAtPosition(string, new BookEditScreen.Pos2i(0, this.getPositionAtIndex(string, this.cursorPos).y));
		if (!Screen.hasShiftDown()) {
			this.selectionPos = this.cursorPos;
		}
	}

	private void keyEnd(String string) {
		this.cursorPos = this.getIndexAtPosition(string, new BookEditScreen.Pos2i(113, this.getPositionAtIndex(string, this.cursorPos).y));
		if (!Screen.hasShiftDown()) {
			this.selectionPos = this.cursorPos;
		}
	}

	private void deleteSelection() {
		if (this.selectionPos != this.cursorPos) {
			String string = this.getCurrentPageText();
			int i = Math.min(this.cursorPos, this.selectionPos);
			int j = Math.max(this.cursorPos, this.selectionPos);
			String string2 = string.substring(0, i) + string.substring(j);
			this.cursorPos = i;
			this.selectionPos = this.cursorPos;
			this.setCurrentPageText(string2);
		}
	}

	private int getWidthAt(String string, int i) {
		return (int)this.font.charWidth(string.charAt(Mth.clamp(i, 0, string.length() - 1)));
	}

	private boolean titleKeyPressed(int i, int j, int k) {
		switch (i) {
			case 257:
			case 335:
				if (!this.title.isEmpty()) {
					this.saveChanges(true);
					this.minecraft.setScreen(null);
				}

				return true;
			case 259:
				if (!this.title.isEmpty()) {
					this.title = this.title.substring(0, this.title.length() - 1);
					this.updateButtonVisibility();
				}

				return true;
			default:
				return false;
		}
	}

	private String getCurrentPageText() {
		return this.currentPage >= 0 && this.currentPage < this.pages.size() ? (String)this.pages.get(this.currentPage) : "";
	}

	private void setCurrentPageText(String string) {
		if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
			this.pages.set(this.currentPage, string);
			this.isModified = true;
		}
	}

	private void insertText(String string) {
		if (this.selectionPos != this.cursorPos) {
			this.deleteSelection();
		}

		String string2 = this.getCurrentPageText();
		this.cursorPos = Mth.clamp(this.cursorPos, 0, string2.length());
		String string3 = new StringBuilder(string2).insert(this.cursorPos, string).toString();
		int i = this.font.wordWrapHeight(string3 + "" + ChatFormatting.BLACK + "_", 114);
		if (i <= 128 && string3.length() < 1024) {
			this.setCurrentPageText(string3);
			this.selectionPos = this.cursorPos = Math.min(this.getCurrentPageText().length(), this.cursorPos + string.length());
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.setFocused(null);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(BookViewScreen.BOOK_LOCATION);
		int k = (this.width - 192) / 2;
		int l = 2;
		this.blit(k, 2, 0, 0, 192, 192);
		if (this.isSigning) {
			String string = this.title;
			if (this.frameTick / 6 % 2 == 0) {
				string = string + "" + ChatFormatting.BLACK + "_";
			} else {
				string = string + "" + ChatFormatting.GRAY + "_";
			}

			String string2 = I18n.get("book.editTitle");
			int m = this.strWidth(string2);
			this.font.draw(string2, (float)(k + 36 + (114 - m) / 2), 34.0F, 0);
			int n = this.strWidth(string);
			this.font.draw(string, (float)(k + 36 + (114 - n) / 2), 50.0F, 0);
			String string3 = I18n.get("book.byAuthor", this.owner.getName().getString());
			int o = this.strWidth(string3);
			this.font.draw(ChatFormatting.DARK_GRAY + string3, (float)(k + 36 + (114 - o) / 2), 60.0F, 0);
			String string4 = I18n.get("book.finalizeWarning");
			this.font.drawWordWrap(string4, k + 36, 82, 114, 0);
		} else {
			String string = I18n.get("book.pageIndicator", this.currentPage + 1, this.getNumPages());
			String string2 = this.getCurrentPageText();
			int m = this.strWidth(string);
			this.font.draw(string, (float)(k - m + 192 - 44), 18.0F, 0);
			this.font.drawWordWrap(string2, k + 36, 32, 114, 0);
			this.renderSelection(string2);
			if (this.frameTick / 6 % 2 == 0) {
				BookEditScreen.Pos2i pos2i = this.getPositionAtIndex(string2, this.cursorPos);
				if (this.font.isBidirectional()) {
					this.handleBidi(pos2i);
					pos2i.x = pos2i.x - 4;
				}

				this.convertLocalToScreen(pos2i);
				if (this.cursorPos < string2.length()) {
					GuiComponent.fill(pos2i.x, pos2i.y - 1, pos2i.x + 1, pos2i.y + 9, -16777216);
				} else {
					this.font.draw("_", (float)pos2i.x, (float)pos2i.y, 0);
				}
			}
		}

		super.render(i, j, f);
	}

	private int strWidth(String string) {
		return this.font.width(this.font.isBidirectional() ? this.font.bidirectionalShaping(string) : string);
	}

	private int strIndexAtWidth(String string, int i) {
		return this.font.indexAtWidth(string, i);
	}

	private String getSelected() {
		String string = this.getCurrentPageText();
		int i = Math.min(this.cursorPos, this.selectionPos);
		int j = Math.max(this.cursorPos, this.selectionPos);
		return string.substring(i, j);
	}

	private void renderSelection(String string) {
		if (this.selectionPos != this.cursorPos) {
			int i = Math.min(this.cursorPos, this.selectionPos);
			int j = Math.max(this.cursorPos, this.selectionPos);
			String string2 = string.substring(i, j);
			int k = this.font.getWordPosition(string, 1, j, true);
			String string3 = string.substring(i, k);
			BookEditScreen.Pos2i pos2i = this.getPositionAtIndex(string, i);
			BookEditScreen.Pos2i pos2i2 = new BookEditScreen.Pos2i(pos2i.x, pos2i.y + 9);

			while (!string2.isEmpty()) {
				int l = this.strIndexAtWidth(string3, 114 - pos2i.x);
				if (string2.length() <= l) {
					pos2i2.x = pos2i.x + this.strWidth(string2);
					this.renderHighlight(pos2i, pos2i2);
					break;
				}

				l = Math.min(l, string2.length() - 1);
				String string4 = string2.substring(0, l);
				char c = string2.charAt(l);
				boolean bl = c == ' ' || c == '\n';
				string2 = ChatFormatting.getLastColors(string4) + string2.substring(l + (bl ? 1 : 0));
				string3 = ChatFormatting.getLastColors(string4) + string3.substring(l + (bl ? 1 : 0));
				pos2i2.x = pos2i.x + this.strWidth(string4 + " ");
				this.renderHighlight(pos2i, pos2i2);
				pos2i.x = 0;
				pos2i.y = pos2i.y + 9;
				pos2i2.y = pos2i2.y + 9;
			}
		}
	}

	private void renderHighlight(BookEditScreen.Pos2i pos2i, BookEditScreen.Pos2i pos2i2) {
		BookEditScreen.Pos2i pos2i3 = new BookEditScreen.Pos2i(pos2i.x, pos2i.y);
		BookEditScreen.Pos2i pos2i4 = new BookEditScreen.Pos2i(pos2i2.x, pos2i2.y);
		if (this.font.isBidirectional()) {
			this.handleBidi(pos2i3);
			this.handleBidi(pos2i4);
			int i = pos2i4.x;
			pos2i4.x = pos2i3.x;
			pos2i3.x = i;
		}

		this.convertLocalToScreen(pos2i3);
		this.convertLocalToScreen(pos2i4);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		GlStateManager.color4f(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture();
		GlStateManager.enableColorLogicOp();
		GlStateManager.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
		bufferBuilder.vertex((double)pos2i3.x, (double)pos2i4.y, 0.0).endVertex();
		bufferBuilder.vertex((double)pos2i4.x, (double)pos2i4.y, 0.0).endVertex();
		bufferBuilder.vertex((double)pos2i4.x, (double)pos2i3.y, 0.0).endVertex();
		bufferBuilder.vertex((double)pos2i3.x, (double)pos2i3.y, 0.0).endVertex();
		tesselator.end();
		GlStateManager.disableColorLogicOp();
		GlStateManager.enableTexture();
	}

	private BookEditScreen.Pos2i getPositionAtIndex(String string, int i) {
		BookEditScreen.Pos2i pos2i = new BookEditScreen.Pos2i();
		int j = 0;
		int k = 0;

		for (String string2 = string; !string2.isEmpty(); k = j) {
			int l = this.strIndexAtWidth(string2, 114);
			if (string2.length() <= l) {
				String string3 = string2.substring(0, Math.min(Math.max(i - k, 0), string2.length()));
				pos2i.x = pos2i.x + this.strWidth(string3);
				break;
			}

			String string3 = string2.substring(0, l);
			char c = string2.charAt(l);
			boolean bl = c == ' ' || c == '\n';
			string2 = ChatFormatting.getLastColors(string3) + string2.substring(l + (bl ? 1 : 0));
			j += string3.length() + (bl ? 1 : 0);
			if (j - 1 >= i) {
				String string4 = string3.substring(0, Math.min(Math.max(i - k, 0), string3.length()));
				pos2i.x = pos2i.x + this.strWidth(string4);
				break;
			}

			pos2i.y = pos2i.y + 9;
		}

		return pos2i;
	}

	private void handleBidi(BookEditScreen.Pos2i pos2i) {
		if (this.font.isBidirectional()) {
			pos2i.x = 114 - pos2i.x;
		}
	}

	private void convertScreenToLocal(BookEditScreen.Pos2i pos2i) {
		pos2i.x = pos2i.x - (this.width - 192) / 2 - 36;
		pos2i.y = pos2i.y - 32;
	}

	private void convertLocalToScreen(BookEditScreen.Pos2i pos2i) {
		pos2i.x = pos2i.x + (this.width - 192) / 2 + 36;
		pos2i.y = pos2i.y + 32;
	}

	private int indexInLine(String string, int i) {
		if (i < 0) {
			return 0;
		} else {
			float f = 0.0F;
			boolean bl = false;
			String string2 = string + " ";

			for (int j = 0; j < string2.length(); j++) {
				char c = string2.charAt(j);
				float g = this.font.charWidth(c);
				if (c == 167 && j < string2.length() - 1) {
					c = string2.charAt(++j);
					if (c == 'l' || c == 'L') {
						bl = true;
					} else if (c == 'r' || c == 'R') {
						bl = false;
					}

					g = 0.0F;
				}

				float h = f;
				f += g;
				if (bl && g > 0.0F) {
					f++;
				}

				if ((float)i >= h && (float)i < f) {
					return j;
				}
			}

			return (float)i >= f ? string2.length() - 1 : -1;
		}
	}

	private int getIndexAtPosition(String string, BookEditScreen.Pos2i pos2i) {
		int i = 16 * 9;
		if (pos2i.y > i) {
			return -1;
		} else {
			int j = Integer.MIN_VALUE;
			int k = 9;
			int l = 0;

			for (String string2 = string; !string2.isEmpty() && j < i; k += 9) {
				int m = this.strIndexAtWidth(string2, 114);
				if (m < string2.length()) {
					String string3 = string2.substring(0, m);
					if (pos2i.y >= j && pos2i.y < k) {
						int n = this.indexInLine(string3, pos2i.x);
						return n < 0 ? -1 : l + n;
					}

					char c = string2.charAt(m);
					boolean bl = c == ' ' || c == '\n';
					string2 = ChatFormatting.getLastColors(string3) + string2.substring(m + (bl ? 1 : 0));
					l += string3.length() + (bl ? 1 : 0);
				} else if (pos2i.y >= j && pos2i.y < k) {
					int o = this.indexInLine(string2, pos2i.x);
					return o < 0 ? -1 : l + o;
				}

				j = k;
			}

			return string.length();
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (i == 0) {
			long l = Util.getMillis();
			String string = this.getCurrentPageText();
			if (!string.isEmpty()) {
				BookEditScreen.Pos2i pos2i = new BookEditScreen.Pos2i((int)d, (int)e);
				this.convertScreenToLocal(pos2i);
				this.handleBidi(pos2i);
				int j = this.getIndexAtPosition(string, pos2i);
				if (j >= 0) {
					if (j != this.lastIndex || l - this.lastClickTime >= 250L) {
						this.cursorPos = j;
						if (!Screen.hasShiftDown()) {
							this.selectionPos = this.cursorPos;
						}
					} else if (this.selectionPos == this.cursorPos) {
						this.selectionPos = this.font.getWordPosition(string, -1, j, false);
						this.cursorPos = this.font.getWordPosition(string, 1, j, false);
					} else {
						this.selectionPos = 0;
						this.cursorPos = this.getCurrentPageText().length();
					}
				}

				this.lastIndex = j;
			}

			this.lastClickTime = l;
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (i == 0 && this.currentPage >= 0 && this.currentPage < this.pages.size()) {
			String string = (String)this.pages.get(this.currentPage);
			BookEditScreen.Pos2i pos2i = new BookEditScreen.Pos2i((int)d, (int)e);
			this.convertScreenToLocal(pos2i);
			this.handleBidi(pos2i);
			int j = this.getIndexAtPosition(string, pos2i);
			if (j >= 0) {
				this.cursorPos = j;
			}
		}

		return super.mouseDragged(d, e, i, f, g);
	}

	@Environment(EnvType.CLIENT)
	class Pos2i {
		private int x;
		private int y;

		Pos2i() {
		}

		Pos2i(int i, int j) {
			this.x = i;
			this.y = j;
		}
	}
}
