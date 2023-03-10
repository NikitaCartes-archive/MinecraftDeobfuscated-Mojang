/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BookEditScreen
extends Screen {
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 128;
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    private static final Component EDIT_TITLE_LABEL = Component.translatable("book.editTitle");
    private static final Component FINALIZE_WARNING_LABEL = Component.translatable("book.finalizeWarning");
    private static final FormattedCharSequence BLACK_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.BLACK));
    private static final FormattedCharSequence GRAY_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.GRAY));
    private final Player owner;
    private final ItemStack book;
    private boolean isModified;
    private boolean isSigning;
    private int frameTick;
    private int currentPage;
    private final List<String> pages = Lists.newArrayList();
    private String title = "";
    private final TextFieldHelper pageEdit = new TextFieldHelper(this::getCurrentPageText, this::setCurrentPageText, this::getClipboard, this::setClipboard, string -> string.length() < 1024 && this.font.wordWrapHeight((String)string, 114) <= 128);
    private final TextFieldHelper titleEdit = new TextFieldHelper(() -> this.title, string -> {
        this.title = string;
    }, this::getClipboard, this::setClipboard, string -> string.length() < 16);
    private long lastClickTime;
    private int lastIndex = -1;
    private PageButton forwardButton;
    private PageButton backButton;
    private Button doneButton;
    private Button signButton;
    private Button finalizeButton;
    private Button cancelButton;
    private final InteractionHand hand;
    @Nullable
    private DisplayCache displayCache = DisplayCache.EMPTY;
    private Component pageMsg = CommonComponents.EMPTY;
    private final Component ownerText;

    public BookEditScreen(Player player, ItemStack itemStack, InteractionHand interactionHand) {
        super(GameNarrator.NO_TITLE);
        this.owner = player;
        this.book = itemStack;
        this.hand = interactionHand;
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null) {
            BookViewScreen.loadPages(compoundTag, this.pages::add);
        }
        if (this.pages.isEmpty()) {
            this.pages.add("");
        }
        this.ownerText = Component.translatable("book.byAuthor", player.getName()).withStyle(ChatFormatting.DARK_GRAY);
    }

    private void setClipboard(String string) {
        if (this.minecraft != null) {
            TextFieldHelper.setClipboardContents(this.minecraft, string);
        }
    }

    private String getClipboard() {
        return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
    }

    private int getNumPages() {
        return this.pages.size();
    }

    @Override
    public void tick() {
        super.tick();
        ++this.frameTick;
    }

    @Override
    protected void init() {
        this.clearDisplayCache();
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), button -> {
            this.isSigning = true;
            this.updateButtonVisibility();
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.minecraft.setScreen(null);
            this.saveChanges(false);
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        this.finalizeButton = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), button -> {
            if (this.isSigning) {
                this.saveChanges(true);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            if (this.isSigning) {
                this.isSigning = false;
            }
            this.updateButtonVisibility();
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        int i = (this.width - 192) / 2;
        int j = 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, button -> this.pageForward(), true));
        this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, button -> this.pageBack(), true));
        this.updateButtonVisibility();
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }
        this.updateButtonVisibility();
        this.clearDisplayCacheAfterPageChange();
    }

    private void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        } else {
            this.appendPageToBook();
            if (this.currentPage < this.getNumPages() - 1) {
                ++this.currentPage;
            }
        }
        this.updateButtonVisibility();
        this.clearDisplayCacheAfterPageChange();
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
        while (listIterator.hasPrevious() && listIterator.previous().isEmpty()) {
            listIterator.remove();
        }
    }

    private void saveChanges(boolean bl) {
        if (!this.isModified) {
            return;
        }
        this.eraseEmptyTrailingPages();
        this.updateLocalCopy(bl);
        int i = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : 40;
        this.minecraft.getConnection().send(new ServerboundEditBookPacket(i, this.pages, bl ? Optional.of(this.title.trim()) : Optional.empty()));
    }

    private void updateLocalCopy(boolean bl) {
        ListTag listTag = new ListTag();
        this.pages.stream().map(StringTag::valueOf).forEach(listTag::add);
        if (!this.pages.isEmpty()) {
            this.book.addTagElement("pages", listTag);
        }
        if (bl) {
            this.book.addTagElement("author", StringTag.valueOf(this.owner.getGameProfile().getName()));
            this.book.addTagElement("title", StringTag.valueOf(this.title.trim()));
        }
    }

    private void appendPageToBook() {
        if (this.getNumPages() >= 100) {
            return;
        }
        this.pages.add("");
        this.isModified = true;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (this.isSigning) {
            return this.titleKeyPressed(i, j, k);
        }
        boolean bl = this.bookKeyPressed(i, j, k);
        if (bl) {
            this.clearDisplayCache();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (super.charTyped(c, i)) {
            return true;
        }
        if (this.isSigning) {
            boolean bl = this.titleEdit.charTyped(c);
            if (bl) {
                this.updateButtonVisibility();
                this.isModified = true;
                return true;
            }
            return false;
        }
        if (SharedConstants.isAllowedChatCharacter(c)) {
            this.pageEdit.insertText(Character.toString(c));
            this.clearDisplayCache();
            return true;
        }
        return false;
    }

    private boolean bookKeyPressed(int i, int j, int k) {
        if (Screen.isSelectAll(i)) {
            this.pageEdit.selectAll();
            return true;
        }
        if (Screen.isCopy(i)) {
            this.pageEdit.copy();
            return true;
        }
        if (Screen.isPaste(i)) {
            this.pageEdit.paste();
            return true;
        }
        if (Screen.isCut(i)) {
            this.pageEdit.cut();
            return true;
        }
        TextFieldHelper.CursorStep cursorStep = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
        switch (i) {
            case 259: {
                this.pageEdit.removeFromCursor(-1, cursorStep);
                return true;
            }
            case 261: {
                this.pageEdit.removeFromCursor(1, cursorStep);
                return true;
            }
            case 257: 
            case 335: {
                this.pageEdit.insertText("\n");
                return true;
            }
            case 263: {
                this.pageEdit.moveBy(-1, Screen.hasShiftDown(), cursorStep);
                return true;
            }
            case 262: {
                this.pageEdit.moveBy(1, Screen.hasShiftDown(), cursorStep);
                return true;
            }
            case 265: {
                this.keyUp();
                return true;
            }
            case 264: {
                this.keyDown();
                return true;
            }
            case 266: {
                this.backButton.onPress();
                return true;
            }
            case 267: {
                this.forwardButton.onPress();
                return true;
            }
            case 268: {
                this.keyHome();
                return true;
            }
            case 269: {
                this.keyEnd();
                return true;
            }
        }
        return false;
    }

    private void keyUp() {
        this.changeLine(-1);
    }

    private void keyDown() {
        this.changeLine(1);
    }

    private void changeLine(int i) {
        int j = this.pageEdit.getCursorPos();
        int k = this.getDisplayCache().changeLine(j, i);
        this.pageEdit.setCursorPos(k, Screen.hasShiftDown());
    }

    private void keyHome() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToStart(Screen.hasShiftDown());
        } else {
            int i = this.pageEdit.getCursorPos();
            int j = this.getDisplayCache().findLineStart(i);
            this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
        }
    }

    private void keyEnd() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToEnd(Screen.hasShiftDown());
        } else {
            DisplayCache displayCache = this.getDisplayCache();
            int i = this.pageEdit.getCursorPos();
            int j = displayCache.findLineEnd(i);
            this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
        }
    }

    private boolean titleKeyPressed(int i, int j, int k) {
        switch (i) {
            case 259: {
                this.titleEdit.removeCharsFromCursor(-1);
                this.updateButtonVisibility();
                this.isModified = true;
                return true;
            }
            case 257: 
            case 335: {
                if (!this.title.isEmpty()) {
                    this.saveChanges(true);
                    this.minecraft.setScreen(null);
                }
                return true;
            }
        }
        return false;
    }

    private String getCurrentPageText() {
        if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            return this.pages.get(this.currentPage);
        }
        return "";
    }

    private void setCurrentPageText(String string) {
        if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            this.pages.set(this.currentPage, string);
            this.isModified = true;
            this.clearDisplayCache();
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.setFocused(null);
        RenderSystem.setShaderTexture(0, BookViewScreen.BOOK_LOCATION);
        int k = (this.width - 192) / 2;
        int l = 2;
        BookEditScreen.blit(poseStack, k, 2, 0, 0, 192, 192);
        if (this.isSigning) {
            boolean bl = this.frameTick / 6 % 2 == 0;
            FormattedCharSequence formattedCharSequence = FormattedCharSequence.composite(FormattedCharSequence.forward(this.title, Style.EMPTY), bl ? BLACK_CURSOR : GRAY_CURSOR);
            int m = this.font.width(EDIT_TITLE_LABEL);
            this.font.draw(poseStack, EDIT_TITLE_LABEL, (float)(k + 36 + (114 - m) / 2), 34.0f, 0);
            int n = this.font.width(formattedCharSequence);
            this.font.draw(poseStack, formattedCharSequence, (float)(k + 36 + (114 - n) / 2), 50.0f, 0);
            int o = this.font.width(this.ownerText);
            this.font.draw(poseStack, this.ownerText, (float)(k + 36 + (114 - o) / 2), 60.0f, 0);
            this.font.drawWordWrap(poseStack, FINALIZE_WARNING_LABEL, k + 36, 82, 114, 0);
        } else {
            int p = this.font.width(this.pageMsg);
            this.font.draw(poseStack, this.pageMsg, (float)(k - p + 192 - 44), 18.0f, 0);
            DisplayCache displayCache = this.getDisplayCache();
            for (LineInfo lineInfo : displayCache.lines) {
                this.font.draw(poseStack, lineInfo.asComponent, (float)lineInfo.x, (float)lineInfo.y, -16777216);
            }
            this.renderHighlight(poseStack, displayCache.selection);
            this.renderCursor(poseStack, displayCache.cursor, displayCache.cursorAtEnd);
        }
        super.render(poseStack, i, j, f);
    }

    private void renderCursor(PoseStack poseStack, Pos2i pos2i, boolean bl) {
        if (this.frameTick / 6 % 2 == 0) {
            pos2i = this.convertLocalToScreen(pos2i);
            if (!bl) {
                GuiComponent.fill(poseStack, pos2i.x, pos2i.y - 1, pos2i.x + 1, pos2i.y + this.font.lineHeight, -16777216);
            } else {
                this.font.draw(poseStack, "_", (float)pos2i.x, (float)pos2i.y, 0);
            }
        }
    }

    private void renderHighlight(PoseStack poseStack, Rect2i[] rect2is) {
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        for (Rect2i rect2i : rect2is) {
            int i = rect2i.getX();
            int j = rect2i.getY();
            int k = i + rect2i.getWidth();
            int l = j + rect2i.getHeight();
            BookEditScreen.fill(poseStack, i, j, k, l, -16776961);
        }
        RenderSystem.disableColorLogicOp();
    }

    private Pos2i convertScreenToLocal(Pos2i pos2i) {
        return new Pos2i(pos2i.x - (this.width - 192) / 2 - 36, pos2i.y - 32);
    }

    private Pos2i convertLocalToScreen(Pos2i pos2i) {
        return new Pos2i(pos2i.x + (this.width - 192) / 2 + 36, pos2i.y + 32);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (super.mouseClicked(d, e, i)) {
            return true;
        }
        if (i == 0) {
            long l = Util.getMillis();
            DisplayCache displayCache = this.getDisplayCache();
            int j = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)d, (int)e)));
            if (j >= 0) {
                if (j == this.lastIndex && l - this.lastClickTime < 250L) {
                    if (!this.pageEdit.isSelecting()) {
                        this.selectWord(j);
                    } else {
                        this.pageEdit.selectAll();
                    }
                } else {
                    this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
                }
                this.clearDisplayCache();
            }
            this.lastIndex = j;
            this.lastClickTime = l;
        }
        return true;
    }

    private void selectWord(int i) {
        String string = this.getCurrentPageText();
        this.pageEdit.setSelectionRange(StringSplitter.getWordPosition(string, -1, i, false), StringSplitter.getWordPosition(string, 1, i, false));
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (super.mouseDragged(d, e, i, f, g)) {
            return true;
        }
        if (i == 0) {
            DisplayCache displayCache = this.getDisplayCache();
            int j = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)d, (int)e)));
            this.pageEdit.setCursorPos(j, true);
            this.clearDisplayCache();
        }
        return true;
    }

    private DisplayCache getDisplayCache() {
        if (this.displayCache == null) {
            this.displayCache = this.rebuildDisplayCache();
            this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages());
        }
        return this.displayCache;
    }

    private void clearDisplayCache() {
        this.displayCache = null;
    }

    private void clearDisplayCacheAfterPageChange() {
        this.pageEdit.setCursorToEnd();
        this.clearDisplayCache();
    }

    private DisplayCache rebuildDisplayCache() {
        int l;
        Pos2i pos2i;
        boolean bl;
        String string = this.getCurrentPageText();
        if (string.isEmpty()) {
            return DisplayCache.EMPTY;
        }
        int i2 = this.pageEdit.getCursorPos();
        int j2 = this.pageEdit.getSelectionPos();
        IntArrayList intList = new IntArrayList();
        ArrayList list = Lists.newArrayList();
        MutableInt mutableInt = new MutableInt();
        MutableBoolean mutableBoolean = new MutableBoolean();
        StringSplitter stringSplitter = this.font.getSplitter();
        stringSplitter.splitLines(string, 114, Style.EMPTY, true, (style, i, j) -> {
            int k = mutableInt.getAndIncrement();
            String string2 = string.substring(i, j);
            mutableBoolean.setValue(string2.endsWith("\n"));
            String string3 = StringUtils.stripEnd(string2, " \n");
            int l = k * this.font.lineHeight;
            Pos2i pos2i = this.convertLocalToScreen(new Pos2i(0, l));
            intList.add(i);
            list.add(new LineInfo(style, string3, pos2i.x, pos2i.y));
        });
        int[] is = intList.toIntArray();
        boolean bl2 = bl = i2 == string.length();
        if (bl && mutableBoolean.isTrue()) {
            pos2i = new Pos2i(0, list.size() * this.font.lineHeight);
        } else {
            int k = BookEditScreen.findLineFromPos(is, i2);
            l = this.font.width(string.substring(is[k], i2));
            pos2i = new Pos2i(l, k * this.font.lineHeight);
        }
        ArrayList<Rect2i> list2 = Lists.newArrayList();
        if (i2 != j2) {
            int o;
            l = Math.min(i2, j2);
            int m = Math.max(i2, j2);
            int n = BookEditScreen.findLineFromPos(is, l);
            if (n == (o = BookEditScreen.findLineFromPos(is, m))) {
                int p = n * this.font.lineHeight;
                int q = is[n];
                list2.add(this.createPartialLineSelection(string, stringSplitter, l, m, p, q));
            } else {
                int p = n + 1 > is.length ? string.length() : is[n + 1];
                list2.add(this.createPartialLineSelection(string, stringSplitter, l, p, n * this.font.lineHeight, is[n]));
                for (int q = n + 1; q < o; ++q) {
                    int r = q * this.font.lineHeight;
                    String string2 = string.substring(is[q], is[q + 1]);
                    int s = (int)stringSplitter.stringWidth(string2);
                    list2.add(this.createSelection(new Pos2i(0, r), new Pos2i(s, r + this.font.lineHeight)));
                }
                list2.add(this.createPartialLineSelection(string, stringSplitter, is[o], m, o * this.font.lineHeight, is[o]));
            }
        }
        return new DisplayCache(string, pos2i, bl, is, list.toArray(new LineInfo[0]), list2.toArray(new Rect2i[0]));
    }

    static int findLineFromPos(int[] is, int i) {
        int j = Arrays.binarySearch(is, i);
        if (j < 0) {
            return -(j + 2);
        }
        return j;
    }

    private Rect2i createPartialLineSelection(String string, StringSplitter stringSplitter, int i, int j, int k, int l) {
        String string2 = string.substring(l, i);
        String string3 = string.substring(l, j);
        Pos2i pos2i = new Pos2i((int)stringSplitter.stringWidth(string2), k);
        Pos2i pos2i2 = new Pos2i((int)stringSplitter.stringWidth(string3), k + this.font.lineHeight);
        return this.createSelection(pos2i, pos2i2);
    }

    private Rect2i createSelection(Pos2i pos2i, Pos2i pos2i2) {
        Pos2i pos2i3 = this.convertLocalToScreen(pos2i);
        Pos2i pos2i4 = this.convertLocalToScreen(pos2i2);
        int i = Math.min(pos2i3.x, pos2i4.x);
        int j = Math.max(pos2i3.x, pos2i4.x);
        int k = Math.min(pos2i3.y, pos2i4.y);
        int l = Math.max(pos2i3.y, pos2i4.y);
        return new Rect2i(i, k, j - i, l - k);
    }

    @Environment(value=EnvType.CLIENT)
    static class DisplayCache {
        static final DisplayCache EMPTY = new DisplayCache("", new Pos2i(0, 0), true, new int[]{0}, new LineInfo[]{new LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
        private final String fullText;
        final Pos2i cursor;
        final boolean cursorAtEnd;
        private final int[] lineStarts;
        final LineInfo[] lines;
        final Rect2i[] selection;

        public DisplayCache(String string, Pos2i pos2i, boolean bl, int[] is, LineInfo[] lineInfos, Rect2i[] rect2is) {
            this.fullText = string;
            this.cursor = pos2i;
            this.cursorAtEnd = bl;
            this.lineStarts = is;
            this.lines = lineInfos;
            this.selection = rect2is;
        }

        public int getIndexAtPosition(Font font, Pos2i pos2i) {
            int i = pos2i.y / font.lineHeight;
            if (i < 0) {
                return 0;
            }
            if (i >= this.lines.length) {
                return this.fullText.length();
            }
            LineInfo lineInfo = this.lines[i];
            return this.lineStarts[i] + font.getSplitter().plainIndexAtWidth(lineInfo.contents, pos2i.x, lineInfo.style);
        }

        public int changeLine(int i, int j) {
            int o;
            int k = BookEditScreen.findLineFromPos(this.lineStarts, i);
            int l = k + j;
            if (0 <= l && l < this.lineStarts.length) {
                int m = i - this.lineStarts[k];
                int n = this.lines[l].contents.length();
                o = this.lineStarts[l] + Math.min(m, n);
            } else {
                o = i;
            }
            return o;
        }

        public int findLineStart(int i) {
            int j = BookEditScreen.findLineFromPos(this.lineStarts, i);
            return this.lineStarts[j];
        }

        public int findLineEnd(int i) {
            int j = BookEditScreen.findLineFromPos(this.lineStarts, i);
            return this.lineStarts[j] + this.lines[j].contents.length();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class LineInfo {
        final Style style;
        final String contents;
        final Component asComponent;
        final int x;
        final int y;

        public LineInfo(Style style, String string, int i, int j) {
            this.style = style;
            this.contents = string;
            this.x = i;
            this.y = j;
            this.asComponent = Component.literal(string).setStyle(style);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Pos2i {
        public final int x;
        public final int y;

        Pos2i(int i, int j) {
            this.x = i;
            this.y = j;
        }
    }
}

