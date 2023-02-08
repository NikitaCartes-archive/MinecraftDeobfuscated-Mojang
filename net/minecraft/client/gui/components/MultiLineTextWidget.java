/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.SingleKeyCache;

@Environment(value=EnvType.CLIENT)
public class MultiLineTextWidget
extends AbstractStringWidget {
    private OptionalInt maxWidth = OptionalInt.empty();
    private OptionalInt maxRows = OptionalInt.empty();
    private final SingleKeyCache<CacheKey, MultiLineLabel> cache = Util.singleKeyCache(cacheKey -> {
        if (cacheKey.maxRows.isPresent()) {
            return MultiLineLabel.create(font, (FormattedText)cacheKey.message, cacheKey.maxWidth, cacheKey.maxRows.getAsInt());
        }
        return MultiLineLabel.create(font, (FormattedText)cacheKey.message, cacheKey.maxWidth);
    });
    private boolean centered = false;

    public MultiLineTextWidget(Component component, Font font) {
        this(0, 0, component, font);
    }

    public MultiLineTextWidget(int i, int j, Component component, Font font) {
        super(i, j, 0, 0, component, font);
        this.active = false;
    }

    @Override
    public MultiLineTextWidget setColor(int i) {
        super.setColor(i);
        return this;
    }

    public MultiLineTextWidget setMaxWidth(int i) {
        this.maxWidth = OptionalInt.of(i);
        return this;
    }

    public MultiLineTextWidget setMaxRows(int i) {
        this.maxRows = OptionalInt.of(i);
        return this;
    }

    public MultiLineTextWidget setCentered(boolean bl) {
        this.centered = bl;
        return this;
    }

    @Override
    public int getWidth() {
        return this.cache.getValue(this.getFreshCacheKey()).getWidth();
    }

    @Override
    public int getHeight() {
        return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * this.getFont().lineHeight;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        MultiLineLabel multiLineLabel = this.cache.getValue(this.getFreshCacheKey());
        int k = this.getX();
        int l = this.getY();
        int m = this.getFont().lineHeight;
        int n = this.getColor();
        if (this.centered) {
            multiLineLabel.renderCentered(poseStack, k + this.getWidth() / 2, l, m, n);
        } else {
            multiLineLabel.renderLeftAligned(poseStack, k, l, m, n);
        }
    }

    private CacheKey getFreshCacheKey() {
        return new CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
    }

    @Override
    public /* synthetic */ AbstractStringWidget setColor(int i) {
        return this.setColor(i);
    }

    @Environment(value=EnvType.CLIENT)
    record CacheKey(Component message, int maxWidth, OptionalInt maxRows) {
    }
}

