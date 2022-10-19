/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlags;

@Environment(value=EnvType.CLIENT)
public class ConfirmExperimentalFeaturesScreen
extends Screen {
    private static final Component TITLE = Component.translatable("selectWorld.experimental.title");
    private static final Component MESSAGE = Component.translatable("selectWorld.experimental.message");
    private static final Component DETAILS_BUTTON = Component.translatable("selectWorld.experimental.details");
    private static final int MARGIN = 20;
    private final BooleanConsumer callback;
    final Collection<Pack> enabledPacks;
    private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;

    public ConfirmExperimentalFeaturesScreen(Collection<Pack> collection, BooleanConsumer booleanConsumer) {
        super(TITLE);
        this.enabledPacks = collection;
        this.callback = booleanConsumer;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
    }

    private int messageHeight() {
        return this.multilineMessage.getLineCount() * this.font.lineHeight;
    }

    private int titleTop() {
        int i = (this.height - this.messageHeight()) / 2;
        return Mth.clamp(i - 20 - this.font.lineHeight, 10, 80);
    }

    @Override
    protected void init() {
        super.init();
        this.multilineMessage = MultiLineLabel.create(this.font, (FormattedText)MESSAGE, this.width - 50);
        int i = Mth.clamp(this.titleTop() + 20 + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24);
        this.addRenderableWidget(new Button(this.width / 2 - 50 - 105, i, 100, 20, CommonComponents.GUI_PROCEED, button -> this.callback.accept(true)));
        this.addRenderableWidget(new Button(this.width / 2 - 50, i, 100, 20, DETAILS_BUTTON, button -> this.minecraft.setScreen(new DetailsScreen())));
        this.addRenderableWidget(new Button(this.width / 2 - 50 + 105, i, 100, 20, CommonComponents.GUI_BACK, button -> this.callback.accept(false)));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        ConfirmExperimentalFeaturesScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.titleTop(), 0xFFFFFF);
        this.multilineMessage.renderCentered(poseStack, this.width / 2, this.titleTop() + 20);
        super.render(poseStack, i, j, f);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Environment(value=EnvType.CLIENT)
    class DetailsScreen
    extends Screen {
        private PackList packList;

        DetailsScreen() {
            super(Component.translatable("selectWorld.experimental.details.title"));
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(ConfirmExperimentalFeaturesScreen.this);
        }

        @Override
        protected void init() {
            super.init();
            this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, button -> this.onClose()));
            this.packList = new PackList(this.minecraft, ConfirmExperimentalFeaturesScreen.this.enabledPacks);
            this.addWidget(this.packList);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            this.renderBackground(poseStack);
            this.packList.render(poseStack, i, j, f);
            DetailsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 0xFFFFFF);
            super.render(poseStack, i, j, f);
        }

        @Environment(value=EnvType.CLIENT)
        class PackList
        extends ObjectSelectionList<PackListEntry> {
            public PackList(Minecraft minecraft, Collection<Pack> collection) {
                super(minecraft, DetailsScreen.this.width, DetailsScreen.this.height, 32, DetailsScreen.this.height - 64, (minecraft.font.lineHeight + 2) * 3);
                for (Pack pack : collection) {
                    String string = FeatureFlags.printMissingFlags(FeatureFlags.VANILLA_SET, pack.getRequestedFeatures());
                    if (string.isEmpty()) continue;
                    MutableComponent component = ComponentUtils.mergeStyles(pack.getTitle().copy(), Style.EMPTY.withBold(true));
                    MutableComponent component2 = Component.translatable("selectWorld.experimental.details.entry", string);
                    this.addEntry(new PackListEntry(component, component2, MultiLineLabel.create(DetailsScreen.this.font, (FormattedText)component2, this.getRowWidth())));
                }
            }

            @Override
            public int getRowWidth() {
                return this.width * 3 / 4;
            }

            @Override
            public boolean isFocused() {
                return DetailsScreen.this.getFocused() == this;
            }
        }

        @Environment(value=EnvType.CLIENT)
        class PackListEntry
        extends ObjectSelectionList.Entry<PackListEntry> {
            private final Component packId;
            private final Component message;
            private final MultiLineLabel splitMessage;

            PackListEntry(Component component, Component component2, MultiLineLabel multiLineLabel) {
                this.packId = component;
                this.message = component2;
                this.splitMessage = multiLineLabel;
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                GuiComponent.drawString(poseStack, ((DetailsScreen)DetailsScreen.this).minecraft.font, this.packId, k, j, 0xFFFFFF);
                this.splitMessage.renderLeftAligned(poseStack, k, j + 12, ((DetailsScreen)DetailsScreen.this).font.lineHeight, 0xFFFFFF);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.packId, this.message));
            }
        }
    }
}

