/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BookViewScreen
extends Screen {
    public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
    public static final int PAGE_TEXT_X_OFFSET = 36;
    public static final int PAGE_TEXT_Y_OFFSET = 30;
    public static final BookAccess EMPTY_ACCESS = new BookAccess(){

        @Override
        public int getPageCount() {
            return 0;
        }

        @Override
        public FormattedText getPageRaw(int i) {
            return FormattedText.EMPTY;
        }
    };
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
    protected static final int TEXT_WIDTH = 114;
    protected static final int TEXT_HEIGHT = 128;
    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    private BookAccess bookAccess;
    private int currentPage;
    private List<FormattedCharSequence> cachedPageComponents = Collections.emptyList();
    private int cachedPage = -1;
    private Component pageMsg = CommonComponents.EMPTY;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;

    public BookViewScreen(BookAccess bookAccess) {
        this(bookAccess, true);
    }

    public BookViewScreen() {
        this(EMPTY_ACCESS, false);
    }

    private BookViewScreen(BookAccess bookAccess, boolean bl) {
        super(NarratorChatListener.NO_TITLE);
        this.bookAccess = bookAccess;
        this.playTurnSound = bl;
    }

    public void setBookAccess(BookAccess bookAccess) {
        this.bookAccess = bookAccess;
        this.currentPage = Mth.clamp(this.currentPage, 0, bookAccess.getPageCount());
        this.updateButtonVisibility();
        this.cachedPage = -1;
    }

    public boolean setPage(int i) {
        int j = Mth.clamp(i, 0, this.bookAccess.getPageCount() - 1);
        if (j != this.currentPage) {
            this.currentPage = j;
            this.updateButtonVisibility();
            this.cachedPage = -1;
            return true;
        }
        return false;
    }

    protected boolean forcePage(int i) {
        return this.setPage(i);
    }

    @Override
    protected void init() {
        this.createMenuControls();
        this.createPageControlButtons();
    }

    protected void createMenuControls() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, 196, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(null)));
    }

    protected void createPageControlButtons() {
        int i = (this.width - 192) / 2;
        int j = 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, button -> this.pageForward(), this.playTurnSound));
        this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, button -> this.pageBack(), this.playTurnSound));
        this.updateButtonVisibility();
    }

    private int getNumPages() {
        return this.bookAccess.getPageCount();
    }

    protected void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }
        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        }
        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        switch (i) {
            case 266: {
                this.backButton.onPress();
                return true;
            }
            case 267: {
                this.forwardButton.onPress();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BOOK_LOCATION);
        int k = (this.width - 192) / 2;
        int l = 2;
        this.blit(poseStack, k, 2, 0, 0, 192, 192);
        if (this.cachedPage != this.currentPage) {
            FormattedText formattedText = this.bookAccess.getPage(this.currentPage);
            this.cachedPageComponents = this.font.split(formattedText, 114);
            this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
        }
        this.cachedPage = this.currentPage;
        int m = this.font.width(this.pageMsg);
        this.font.draw(poseStack, this.pageMsg, (float)(k - m + 192 - 44), 18.0f, 0);
        int n = Math.min(128 / this.font.lineHeight, this.cachedPageComponents.size());
        for (int o = 0; o < n; ++o) {
            FormattedCharSequence formattedCharSequence = this.cachedPageComponents.get(o);
            this.font.draw(poseStack, formattedCharSequence, (float)(k + 36), (float)(32 + o * this.font.lineHeight), 0);
        }
        Style style = this.getClickedComponentStyleAt(i, j);
        if (style != null) {
            this.renderComponentHoverEffect(poseStack, style, i, j);
        }
        super.render(poseStack, i, j, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        Style style;
        if (i == 0 && (style = this.getClickedComponentStyleAt(d, e)) != null && this.handleComponentClicked(style)) {
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean handleComponentClicked(Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return false;
        }
        if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String string = clickEvent.getValue();
            try {
                int i = Integer.parseInt(string) - 1;
                return this.forcePage(i);
            } catch (Exception exception) {
                return false;
            }
        }
        boolean bl = super.handleComponentClicked(style);
        if (bl && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.closeScreen();
        }
        return bl;
    }

    protected void closeScreen() {
        this.minecraft.setScreen(null);
    }

    @Nullable
    public Style getClickedComponentStyleAt(double d, double e) {
        if (this.cachedPageComponents.isEmpty()) {
            return null;
        }
        int i = Mth.floor(d - (double)((this.width - 192) / 2) - 36.0);
        int j = Mth.floor(e - 2.0 - 30.0);
        if (i < 0 || j < 0) {
            return null;
        }
        int k = Math.min(128 / this.font.lineHeight, this.cachedPageComponents.size());
        if (i <= 114 && j < this.minecraft.font.lineHeight * k + k) {
            int l = j / this.minecraft.font.lineHeight;
            if (l >= 0 && l < this.cachedPageComponents.size()) {
                FormattedCharSequence formattedCharSequence = this.cachedPageComponents.get(l);
                return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedCharSequence, i);
            }
            return null;
        }
        return null;
    }

    static List<String> loadPages(CompoundTag compoundTag) {
        ImmutableList.Builder builder = ImmutableList.builder();
        BookViewScreen.loadPages(compoundTag, builder::add);
        return builder.build();
    }

    public static void loadPages(CompoundTag compoundTag, Consumer<String> consumer) {
        IntFunction<String> intFunction;
        ListTag listTag = compoundTag.getList("pages", 8).copy();
        if (Minecraft.getInstance().isTextFilteringEnabled() && compoundTag.contains("filtered_pages", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("filtered_pages");
            intFunction = i -> {
                String string = String.valueOf(i);
                return compoundTag2.contains(string) ? compoundTag2.getString(string) : listTag.getString(i);
            };
        } else {
            intFunction = listTag::getString;
        }
        for (int i2 = 0; i2 < listTag.size(); ++i2) {
            consumer.accept(intFunction.apply(i2));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface BookAccess {
        public int getPageCount();

        public FormattedText getPageRaw(int var1);

        default public FormattedText getPage(int i) {
            if (i >= 0 && i < this.getPageCount()) {
                return this.getPageRaw(i);
            }
            return FormattedText.EMPTY;
        }

        public static BookAccess fromItem(ItemStack itemStack) {
            if (itemStack.is(Items.WRITTEN_BOOK)) {
                return new WrittenBookAccess(itemStack);
            }
            if (itemStack.is(Items.WRITABLE_BOOK)) {
                return new WritableBookAccess(itemStack);
            }
            return EMPTY_ACCESS;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WritableBookAccess
    implements BookAccess {
        private final List<String> pages;

        public WritableBookAccess(ItemStack itemStack) {
            this.pages = WritableBookAccess.readPages(itemStack);
        }

        private static List<String> readPages(ItemStack itemStack) {
            CompoundTag compoundTag = itemStack.getTag();
            return compoundTag != null ? BookViewScreen.loadPages(compoundTag) : ImmutableList.of();
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public FormattedText getPageRaw(int i) {
            return FormattedText.of(this.pages.get(i));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WrittenBookAccess
    implements BookAccess {
        private final List<String> pages;

        public WrittenBookAccess(ItemStack itemStack) {
            this.pages = WrittenBookAccess.readPages(itemStack);
        }

        private static List<String> readPages(ItemStack itemStack) {
            CompoundTag compoundTag = itemStack.getTag();
            if (compoundTag != null && WrittenBookItem.makeSureTagIsValid(compoundTag)) {
                return BookViewScreen.loadPages(compoundTag);
            }
            return ImmutableList.of(Component.Serializer.toJson(Component.translatable("book.invalid.tag").withStyle(ChatFormatting.DARK_RED)));
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public FormattedText getPageRaw(int i) {
            String string = this.pages.get(i);
            try {
                MutableComponent formattedText = Component.Serializer.fromJson(string);
                if (formattedText != null) {
                    return formattedText;
                }
            } catch (Exception exception) {
                // empty catch block
            }
            return FormattedText.of(string);
        }
    }
}

