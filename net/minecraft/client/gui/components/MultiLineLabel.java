/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

@Environment(value=EnvType.CLIENT)
public interface MultiLineLabel {
    public static final MultiLineLabel EMPTY = new MultiLineLabel(){

        @Override
        public int renderCentered(PoseStack poseStack, int i, int j) {
            return j;
        }

        @Override
        public int renderCentered(PoseStack poseStack, int i, int j, int k, int l) {
            return j;
        }

        @Override
        public int renderLeftAligned(PoseStack poseStack, int i, int j, int k, int l) {
            return j;
        }

        @Override
        public int renderLeftAlignedNoShadow(PoseStack poseStack, int i, int j, int k, int l) {
            return j;
        }

        @Override
        public void renderBackgroundCentered(PoseStack poseStack, int i, int j, int k, int l, int m) {
        }

        @Override
        public int getLineCount() {
            return 0;
        }

        @Override
        public int getWidth() {
            return 0;
        }
    };

    public static MultiLineLabel create(Font font, FormattedText formattedText, int i) {
        return MultiLineLabel.createFixed(font, font.split(formattedText, i).stream().map(formattedCharSequence -> new TextWithWidth((FormattedCharSequence)formattedCharSequence, font.width((FormattedCharSequence)formattedCharSequence))).collect(ImmutableList.toImmutableList()));
    }

    public static MultiLineLabel create(Font font, FormattedText formattedText, int i, int j) {
        return MultiLineLabel.createFixed(font, font.split(formattedText, i).stream().limit(j).map(formattedCharSequence -> new TextWithWidth((FormattedCharSequence)formattedCharSequence, font.width((FormattedCharSequence)formattedCharSequence))).collect(ImmutableList.toImmutableList()));
    }

    public static MultiLineLabel create(Font font, Component ... components) {
        return MultiLineLabel.createFixed(font, Arrays.stream(components).map(Component::getVisualOrderText).map(formattedCharSequence -> new TextWithWidth((FormattedCharSequence)formattedCharSequence, font.width((FormattedCharSequence)formattedCharSequence))).collect(ImmutableList.toImmutableList()));
    }

    public static MultiLineLabel create(Font font, List<Component> list) {
        return MultiLineLabel.createFixed(font, list.stream().map(Component::getVisualOrderText).map(formattedCharSequence -> new TextWithWidth((FormattedCharSequence)formattedCharSequence, font.width((FormattedCharSequence)formattedCharSequence))).collect(ImmutableList.toImmutableList()));
    }

    public static MultiLineLabel createFixed(final Font font, final List<TextWithWidth> list) {
        if (list.isEmpty()) {
            return EMPTY;
        }
        return new MultiLineLabel(){
            private final int width;
            {
                this.width = list.stream().mapToInt(textWithWidth -> textWithWidth.width).max().orElse(0);
            }

            @Override
            public int renderCentered(PoseStack poseStack, int i, int j) {
                return this.renderCentered(poseStack, i, j, font.lineHeight, 0xFFFFFF);
            }

            @Override
            public int renderCentered(PoseStack poseStack, int i, int j, int k, int l) {
                int m = j;
                for (TextWithWidth textWithWidth : list) {
                    font.drawShadow(poseStack, textWithWidth.text, (float)(i - textWithWidth.width / 2), (float)m, l);
                    m += k;
                }
                return m;
            }

            @Override
            public int renderLeftAligned(PoseStack poseStack, int i, int j, int k, int l) {
                int m = j;
                for (TextWithWidth textWithWidth : list) {
                    font.drawShadow(poseStack, textWithWidth.text, (float)i, (float)m, l);
                    m += k;
                }
                return m;
            }

            @Override
            public int renderLeftAlignedNoShadow(PoseStack poseStack, int i, int j, int k, int l) {
                int m = j;
                for (TextWithWidth textWithWidth : list) {
                    font.draw(poseStack, textWithWidth.text, (float)i, (float)m, l);
                    m += k;
                }
                return m;
            }

            @Override
            public void renderBackgroundCentered(PoseStack poseStack, int i, int j, int k, int l, int m) {
                int n = list.stream().mapToInt(textWithWidth -> textWithWidth.width).max().orElse(0);
                if (n > 0) {
                    GuiComponent.fill(poseStack, i - n / 2 - l, j - l, i + n / 2 + l, j + list.size() * k + l, m);
                }
            }

            @Override
            public int getLineCount() {
                return list.size();
            }

            @Override
            public int getWidth() {
                return this.width;
            }
        };
    }

    public int renderCentered(PoseStack var1, int var2, int var3);

    public int renderCentered(PoseStack var1, int var2, int var3, int var4, int var5);

    public int renderLeftAligned(PoseStack var1, int var2, int var3, int var4, int var5);

    public int renderLeftAlignedNoShadow(PoseStack var1, int var2, int var3, int var4, int var5);

    public void renderBackgroundCentered(PoseStack var1, int var2, int var3, int var4, int var5, int var6);

    public int getLineCount();

    public int getWidth();

    @Environment(value=EnvType.CLIENT)
    public static class TextWithWidth {
        final FormattedCharSequence text;
        final int width;

        TextWithWidth(FormattedCharSequence formattedCharSequence, int i) {
            this.text = formattedCharSequence;
            this.width = i;
        }
    }
}

