/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatSelectionLogFiller;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatSelectionScreen
extends Screen {
    private static final Component TITLE = Component.translatable("gui.chatSelection.title");
    private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context").withStyle(ChatFormatting.GRAY);
    @Nullable
    private final Screen lastScreen;
    private final ReportingContext reportingContext;
    private Button confirmSelectedButton;
    private MultiLineLabel contextInfoLabel;
    @Nullable
    private ChatSelectionList chatSelectionList;
    final ChatReportBuilder report;
    private final Consumer<ChatReportBuilder> onSelected;
    private ChatSelectionLogFiller chatLogFiller;

    public ChatSelectionScreen(@Nullable Screen screen, ReportingContext reportingContext, ChatReportBuilder chatReportBuilder, Consumer<ChatReportBuilder> consumer) {
        super(TITLE);
        this.lastScreen = screen;
        this.reportingContext = reportingContext;
        this.report = chatReportBuilder.copy();
        this.onSelected = consumer;
    }

    @Override
    protected void init() {
        this.chatLogFiller = new ChatSelectionLogFiller(this.reportingContext, this::canReport);
        this.contextInfoLabel = MultiLineLabel.create(this.font, (FormattedText)CONTEXT_INFO, this.width - 16);
        this.chatSelectionList = new ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * this.font.lineHeight);
        this.chatSelectionList.setRenderBackground(false);
        this.addWidget(this.chatSelectionList);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).bounds(this.width / 2 - 155, this.height - 32, 150, 20).build());
        this.confirmSelectedButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onSelected.accept(this.report);
            this.onClose();
        }).bounds(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
        this.updateConfirmSelectedButton();
        this.extendLog();
        this.chatSelectionList.setScrollAmount(this.chatSelectionList.getMaxScroll());
    }

    private boolean canReport(LoggedChatMessage loggedChatMessage) {
        return loggedChatMessage.canReport(this.report.reportedProfileId());
    }

    private void extendLog() {
        int i = this.chatSelectionList.getMaxVisibleEntries();
        this.chatLogFiller.fillNextPage(i, this.chatSelectionList);
    }

    void onReachedScrollTop() {
        this.extendLog();
    }

    void updateConfirmSelectedButton() {
        this.confirmSelectedButton.active = !this.report.reportedMessages().isEmpty();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.chatSelectionList.render(poseStack, i, j, f);
        ChatSelectionScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        AbuseReportLimits abuseReportLimits = this.reportingContext.sender().reportLimits();
        int k = this.report.reportedMessages().size();
        int l = abuseReportLimits.maxReportedMessageCount();
        MutableComponent component = Component.translatable("gui.chatSelection.selected", k, l);
        ChatSelectionScreen.drawCenteredString(poseStack, this.font, component, this.width / 2, 16 + this.font.lineHeight * 3 / 2, 0xA0A0A0);
        this.contextInfoLabel.renderCentered(poseStack, this.width / 2, this.chatSelectionList.getFooterTop());
        super.render(poseStack, i, j, f);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
    }

    @Environment(value=EnvType.CLIENT)
    public class ChatSelectionList
    extends ObjectSelectionList<Entry>
    implements ChatSelectionLogFiller.Output {
        @Nullable
        private Heading previousHeading;

        public ChatSelectionList(Minecraft minecraft, int i) {
            super(minecraft, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height, 40, ChatSelectionScreen.this.height - 40 - i, 16);
        }

        @Override
        public void setScrollAmount(double d) {
            double e = this.getScrollAmount();
            super.setScrollAmount(d);
            if ((float)this.getMaxScroll() > 1.0E-5f && d <= (double)1.0E-5f && !Mth.equal(d, e)) {
                ChatSelectionScreen.this.onReachedScrollTop();
            }
        }

        @Override
        public void acceptMessage(int i, LoggedChatMessage.Player player) {
            boolean bl = player.canReport(ChatSelectionScreen.this.report.reportedProfileId());
            ChatTrustLevel chatTrustLevel = player.trustLevel();
            GuiMessageTag guiMessageTag = chatTrustLevel.createTag(player.message());
            MessageEntry entry = new MessageEntry(i, player.toContentComponent(), player.toNarrationComponent(), guiMessageTag, bl, true);
            this.addEntryToTop(entry);
            this.updateHeading(player, bl);
        }

        private void updateHeading(LoggedChatMessage.Player player, boolean bl) {
            MessageHeadingEntry entry = new MessageHeadingEntry(player.profile(), player.toHeadingComponent(), bl);
            this.addEntryToTop(entry);
            Heading heading = new Heading(player.profileId(), entry);
            if (this.previousHeading != null && this.previousHeading.canCombine(heading)) {
                this.removeEntryFromTop(this.previousHeading.entry());
            }
            this.previousHeading = heading;
        }

        @Override
        public void acceptDivider(Component component) {
            this.addEntryToTop(new PaddingEntry());
            this.addEntryToTop(new DividerEntry(component));
            this.addEntryToTop(new PaddingEntry());
            this.previousHeading = null;
        }

        @Override
        protected int getScrollbarPosition() {
            return (this.width + this.getRowWidth()) / 2;
        }

        @Override
        public int getRowWidth() {
            return Math.min(350, this.width - 50);
        }

        public int getMaxVisibleEntries() {
            return Mth.positiveCeilDiv(this.y1 - this.y0, this.itemHeight);
        }

        @Override
        protected void renderItem(PoseStack poseStack, int i, int j, float f, int k, int l, int m, int n, int o) {
            Entry entry = (Entry)this.getEntry(k);
            if (this.shouldHighlightEntry(entry)) {
                boolean bl = this.getSelected() == entry;
                int p = this.isFocused() && bl ? -1 : -8355712;
                this.renderSelection(poseStack, m, n, o, p, -16777216);
            }
            entry.render(poseStack, k, m, l, n, o, i, j, this.getHovered() == entry, f);
        }

        private boolean shouldHighlightEntry(Entry entry) {
            if (entry.canSelect()) {
                boolean bl = this.getSelected() == entry;
                boolean bl2 = this.getSelected() == null;
                boolean bl3 = this.getHovered() == entry;
                return bl || bl2 && bl3 && entry.canReport();
            }
            return false;
        }

        @Override
        @Nullable
        protected Entry nextEntry(ScreenDirection screenDirection) {
            return this.nextEntry(screenDirection, Entry::canSelect);
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            Entry entry2 = this.nextEntry(ScreenDirection.UP);
            if (entry2 == null) {
                ChatSelectionScreen.this.onReachedScrollTop();
            }
        }

        @Override
        public boolean keyPressed(int i, int j, int k) {
            Entry entry = (Entry)this.getSelected();
            if (entry != null && entry.keyPressed(i, j, k)) {
                return true;
            }
            return super.keyPressed(i, j, k);
        }

        public int getFooterTop() {
            return this.y1 + ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight;
        }

        @Override
        @Nullable
        protected /* synthetic */ AbstractSelectionList.Entry nextEntry(ScreenDirection screenDirection) {
            return this.nextEntry(screenDirection);
        }

        @Environment(value=EnvType.CLIENT)
        public class MessageEntry
        extends Entry {
            private static final ResourceLocation CHECKMARK_TEXTURE = new ResourceLocation("minecraft", "textures/gui/checkmark.png");
            private static final int CHECKMARK_WIDTH = 9;
            private static final int CHECKMARK_HEIGHT = 8;
            private static final int INDENT_AMOUNT = 11;
            private static final int TAG_MARGIN_LEFT = 4;
            private final int chatId;
            private final FormattedText text;
            private final Component narration;
            @Nullable
            private final List<FormattedCharSequence> hoverText;
            @Nullable
            private final GuiMessageTag.Icon tagIcon;
            @Nullable
            private final List<FormattedCharSequence> tagHoverText;
            private final boolean canReport;
            private final boolean playerMessage;

            public MessageEntry(int i, Component component, @Nullable Component component2, GuiMessageTag guiMessageTag, boolean bl, boolean bl2) {
                this.chatId = i;
                this.tagIcon = Optionull.map(guiMessageTag, GuiMessageTag::icon);
                this.tagHoverText = guiMessageTag != null && guiMessageTag.text() != null ? ChatSelectionScreen.this.font.split(guiMessageTag.text(), ChatSelectionList.this.getRowWidth()) : null;
                this.canReport = bl;
                this.playerMessage = bl2;
                FormattedText formattedText = ChatSelectionScreen.this.font.substrByWidth(component, this.getMaximumTextWidth() - ChatSelectionScreen.this.font.width(CommonComponents.ELLIPSIS));
                if (component != formattedText) {
                    this.text = FormattedText.composite(formattedText, CommonComponents.ELLIPSIS);
                    this.hoverText = ChatSelectionScreen.this.font.split(component, ChatSelectionList.this.getRowWidth());
                } else {
                    this.text = component;
                    this.hoverText = null;
                }
                this.narration = component2;
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                if (this.isSelected() && this.canReport) {
                    this.renderSelectedCheckmark(poseStack, j, k, m);
                }
                int p = k + this.getTextIndent();
                int q = j + 1 + (m - ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight) / 2;
                GuiComponent.drawString(poseStack, ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), p, q, this.canReport ? -1 : -1593835521);
                if (this.hoverText != null && bl) {
                    ChatSelectionScreen.this.setTooltipForNextRenderPass(this.hoverText);
                }
                int r = ChatSelectionScreen.this.font.width(this.text);
                this.renderTag(poseStack, p + r + 4, j, m, n, o);
            }

            private void renderTag(PoseStack poseStack, int i, int j, int k, int l, int m) {
                if (this.tagIcon != null) {
                    int n = j + (k - this.tagIcon.height) / 2;
                    this.tagIcon.draw(poseStack, i, n);
                    if (this.tagHoverText != null && l >= i && l <= i + this.tagIcon.width && m >= n && m <= n + this.tagIcon.height) {
                        ChatSelectionScreen.this.setTooltipForNextRenderPass(this.tagHoverText);
                    }
                }
            }

            private void renderSelectedCheckmark(PoseStack poseStack, int i, int j, int k) {
                int l = j;
                int m = i + (k - 8) / 2;
                RenderSystem.setShaderTexture(0, CHECKMARK_TEXTURE);
                RenderSystem.enableBlend();
                GuiComponent.blit(poseStack, l, m, 0.0f, 0.0f, 9, 8, 9, 8);
                RenderSystem.disableBlend();
            }

            private int getMaximumTextWidth() {
                int i = this.tagIcon != null ? this.tagIcon.width + 4 : 0;
                return ChatSelectionList.this.getRowWidth() - this.getTextIndent() - 4 - i;
            }

            private int getTextIndent() {
                return this.playerMessage ? 11 : 0;
            }

            @Override
            public Component getNarration() {
                return this.isSelected() ? Component.translatable("narrator.select", this.narration) : this.narration;
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    ChatSelectionList.this.setSelected((Entry)null);
                    return this.toggleReport();
                }
                return false;
            }

            @Override
            public boolean keyPressed(int i, int j, int k) {
                if (i == 257 || i == 32 || i == 335) {
                    return this.toggleReport();
                }
                return false;
            }

            @Override
            public boolean isSelected() {
                return ChatSelectionScreen.this.report.isReported(this.chatId);
            }

            @Override
            public boolean canSelect() {
                return true;
            }

            @Override
            public boolean canReport() {
                return this.canReport;
            }

            private boolean toggleReport() {
                if (this.canReport) {
                    ChatSelectionScreen.this.report.toggleReported(this.chatId);
                    ChatSelectionScreen.this.updateConfirmSelectedButton();
                    return true;
                }
                return false;
            }
        }

        @Environment(value=EnvType.CLIENT)
        public class MessageHeadingEntry
        extends Entry {
            private static final int FACE_SIZE = 12;
            private final Component heading;
            private final ResourceLocation skin;
            private final boolean canReport;

            public MessageHeadingEntry(GameProfile gameProfile, Component component, boolean bl) {
                this.heading = component;
                this.canReport = bl;
                this.skin = ChatSelectionList.this.minecraft.getSkinManager().getInsecureSkinLocation(gameProfile);
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                int p = k - 12 - 4;
                int q = j + (m - 12) / 2;
                this.renderFace(poseStack, p, q, this.skin);
                int r = j + 1 + (m - ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight) / 2;
                GuiComponent.drawString(poseStack, ChatSelectionScreen.this.font, this.heading, k, r, this.canReport ? -1 : -1593835521);
            }

            private void renderFace(PoseStack poseStack, int i, int j, ResourceLocation resourceLocation) {
                RenderSystem.setShaderTexture(0, resourceLocation);
                PlayerFaceRenderer.draw(poseStack, i, j, 12);
            }
        }

        @Environment(value=EnvType.CLIENT)
        record Heading(UUID sender, Entry entry) {
            public boolean canCombine(Heading heading) {
                return heading.sender.equals(this.sender);
            }
        }

        @Environment(value=EnvType.CLIENT)
        public abstract class Entry
        extends ObjectSelectionList.Entry<Entry> {
            @Override
            public Component getNarration() {
                return CommonComponents.EMPTY;
            }

            public boolean isSelected() {
                return false;
            }

            public boolean canSelect() {
                return false;
            }

            public boolean canReport() {
                return this.canSelect();
            }
        }

        @Environment(value=EnvType.CLIENT)
        public class PaddingEntry
        extends Entry {
            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            }
        }

        @Environment(value=EnvType.CLIENT)
        public class DividerEntry
        extends Entry {
            private static final int COLOR = -6250336;
            private final Component text;

            public DividerEntry(Component component) {
                this.text = component;
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                int p = j + m / 2;
                int q = k + l - 8;
                int r = ChatSelectionScreen.this.font.width(this.text);
                int s = (k + q - r) / 2;
                int t = p - ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight / 2;
                GuiComponent.drawString(poseStack, ChatSelectionScreen.this.font, this.text, s, t, -6250336);
            }

            @Override
            public Component getNarration() {
                return this.text;
            }
        }
    }
}

