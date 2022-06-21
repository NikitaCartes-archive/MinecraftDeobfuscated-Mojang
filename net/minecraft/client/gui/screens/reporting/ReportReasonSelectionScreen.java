/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.reporting;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ReportReasonSelectionScreen
extends Screen {
    private static final String STANDARDS_LINK = "https://aka.ms/mccommunitystandards";
    private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
    private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
    private static final Component COMMUNITY_STANDARDS_LABEL = Component.translatable("gui.chatReport.standards", Component.translatable("gui.chatReport.standards_name").withStyle(ChatFormatting.UNDERLINE)).withStyle(ChatFormatting.GRAY);
    private static final int FOOTER_HEIGHT = 85;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int CONTENT_WIDTH = 320;
    private static final int PADDING = 4;
    @Nullable
    private final Screen lastScreen;
    @Nullable
    private ReasonSelectionList reasonSelectionList;
    @Nullable
    private final ReportReason selectedReasonOnInit;
    private final Consumer<ReportReason> onSelectedReason;

    public ReportReasonSelectionScreen(@Nullable Screen screen, @Nullable ReportReason reportReason, Consumer<ReportReason> consumer) {
        super(REASON_TITLE);
        this.lastScreen = screen;
        this.selectedReasonOnInit = reportReason;
        this.onSelectedReason = consumer;
    }

    @Override
    protected void init() {
        int i = this.font.width(COMMUNITY_STANDARDS_LABEL);
        int j = (this.width - i) / 2;
        this.addRenderableWidget(new PlainTextButton(j, 16 + this.font.lineHeight * 3 / 2, i, this.font.lineHeight, COMMUNITY_STANDARDS_LABEL, button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(STANDARDS_LINK);
            }
            this.minecraft.setScreen(this);
        }, STANDARDS_LINK, true)), this.font));
        this.reasonSelectionList = new ReasonSelectionList(this.minecraft);
        this.reasonSelectionList.setRenderBackground(false);
        this.addWidget(this.reasonSelectionList);
        ReasonSelectionList.Entry entry = Util.mapNullable(this.selectedReasonOnInit, this.reasonSelectionList::findEntry);
        this.reasonSelectionList.setSelected(entry);
        this.addRenderableWidget(new Button(this.buttonLeft(), this.buttonTop(), 150, 20, CommonComponents.GUI_DONE, button -> {
            ReasonSelectionList.Entry entry = (ReasonSelectionList.Entry)this.reasonSelectionList.getSelected();
            if (entry != null) {
                this.onSelectedReason.accept(entry.getReason());
            }
            this.minecraft.setScreen(this.lastScreen);
        }));
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.reasonSelectionList.render(poseStack, i, j, f);
        ReportReasonSelectionScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        super.render(poseStack, i, j, f);
        ReportReasonSelectionScreen.fill(poseStack, this.contentLeft(), this.descriptionTop(), this.contentRight(), this.descriptionBottom(), 0x7F000000);
        ReportReasonSelectionScreen.drawString(poseStack, this.font, REASON_DESCRIPTION, this.contentLeft() + 4, this.descriptionTop() + 4, -8421505);
        ReasonSelectionList.Entry entry = (ReasonSelectionList.Entry)this.reasonSelectionList.getSelected();
        if (entry != null) {
            int k = this.contentLeft() + 4 + 16;
            int l = this.contentRight() - 4;
            int m = this.descriptionTop() + 4 + this.font.lineHeight + 2;
            int n = this.descriptionBottom() - 4;
            int o = l - k;
            int p = n - m;
            int q = this.font.wordWrapHeight(entry.reason.description(), o);
            this.font.drawWordWrap(entry.reason.description(), k, m + (p - q) / 2, o, -1);
        }
    }

    private int buttonLeft() {
        return this.contentRight() - 150;
    }

    private int buttonTop() {
        return this.height - 20 - 4;
    }

    private int contentLeft() {
        return (this.width - 320) / 2;
    }

    private int contentRight() {
        return (this.width + 320) / 2;
    }

    private int descriptionTop() {
        return this.height - 85 + 4;
    }

    private int descriptionBottom() {
        return this.buttonTop() - 4;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Environment(value=EnvType.CLIENT)
    public class ReasonSelectionList
    extends ObjectSelectionList<Entry> {
        public ReasonSelectionList(Minecraft minecraft) {
            super(minecraft, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.height, 40, ReportReasonSelectionScreen.this.height - 85, 18);
            for (ReportReason reportReason : ReportReason.values()) {
                this.addEntry(new Entry(reportReason));
            }
        }

        @Nullable
        public Entry findEntry(ReportReason reportReason) {
            return this.children().stream().filter(entry -> entry.reason == reportReason).findFirst().orElse(null);
        }

        @Override
        public int getRowWidth() {
            return 320;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowRight() - 2;
        }

        @Environment(value=EnvType.CLIENT)
        public class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final ReportReason reason;

            public Entry(ReportReason reportReason) {
                this.reason = reportReason;
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                int p = k + 1;
                int q = j + (m - ((ReportReasonSelectionScreen)ReportReasonSelectionScreen.this).font.lineHeight) / 2 + 1;
                GuiComponent.drawString(poseStack, ReportReasonSelectionScreen.this.font, this.reason.title(), p, q, -1);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("gui.abuseReport.reason.narration", this.reason.title(), this.reason.description());
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    ReasonSelectionList.this.setSelected(this);
                    return true;
                }
                return false;
            }

            public ReportReason getReason() {
                return this.reason;
            }
        }
    }
}

