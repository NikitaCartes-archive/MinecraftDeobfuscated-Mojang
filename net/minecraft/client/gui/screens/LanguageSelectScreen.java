/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class LanguageSelectScreen
extends OptionsSubScreen {
    private static final Component WARNING_LABEL = Component.literal("(").append(Component.translatable("options.languageWarning")).append(")").withStyle(ChatFormatting.GRAY);
    private LanguageSelectionList packSelectionList;
    final LanguageManager languageManager;

    public LanguageSelectScreen(Screen screen, Options options, LanguageManager languageManager) {
        super(screen, options, Component.translatable("options.language"));
        this.languageManager = languageManager;
    }

    @Override
    protected void init() {
        this.packSelectionList = new LanguageSelectionList(this.minecraft);
        this.addWidget(this.packSelectionList);
        this.addRenderableWidget(this.options.forceUnicodeFont().createButton(this.options, this.width / 2 - 155, this.height - 38, 150));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            LanguageSelectionList.Entry entry = (LanguageSelectionList.Entry)this.packSelectionList.getSelected();
            if (entry != null && !entry.language.getCode().equals(this.languageManager.getSelected().getCode())) {
                this.languageManager.setSelected(entry.language);
                this.options.languageCode = entry.language.getCode();
                this.minecraft.reloadResourcePacks();
                this.options.save();
            }
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 155 + 160, this.height - 38, 150, 20).build());
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.packSelectionList.render(poseStack, i, j, f);
        LanguageSelectScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        LanguageSelectScreen.drawCenteredString(poseStack, this.font, WARNING_LABEL, this.width / 2, this.height - 56, 0x808080);
        super.render(poseStack, i, j, f);
    }

    @Environment(value=EnvType.CLIENT)
    class LanguageSelectionList
    extends ObjectSelectionList<Entry> {
        public LanguageSelectionList(Minecraft minecraft) {
            super(minecraft, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);
            for (LanguageInfo languageInfo : LanguageSelectScreen.this.languageManager.getLanguages()) {
                Entry entry = new Entry(languageInfo);
                this.addEntry(entry);
                if (!LanguageSelectScreen.this.languageManager.getSelected().getCode().equals(languageInfo.getCode())) continue;
                this.setSelected(entry);
            }
            if (this.getSelected() != null) {
                this.centerScrollOn((Entry)this.getSelected());
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Override
        protected void renderBackground(PoseStack poseStack) {
            LanguageSelectScreen.this.renderBackground(poseStack);
        }

        @Override
        protected boolean isFocused() {
            return LanguageSelectScreen.this.getFocused() == this;
        }

        @Environment(value=EnvType.CLIENT)
        public class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final LanguageInfo language;

            public Entry(LanguageInfo languageInfo) {
                this.language = languageInfo;
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                String string = this.language.toString();
                LanguageSelectScreen.this.font.drawShadow(poseStack, string, LanguageSelectionList.this.width / 2 - LanguageSelectScreen.this.font.width(string) / 2, j + 1, 0xFFFFFF, true);
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    this.select();
                    return true;
                }
                return false;
            }

            private void select() {
                LanguageSelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.language);
            }
        }
    }
}

