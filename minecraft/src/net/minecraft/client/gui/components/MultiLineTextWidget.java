package net.minecraft.client.gui.components;

import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SingleKeyCache;

@Environment(EnvType.CLIENT)
public class MultiLineTextWidget extends AbstractStringWidget {
	private OptionalInt maxWidth = OptionalInt.empty();
	private OptionalInt maxRows = OptionalInt.empty();
	private final SingleKeyCache<MultiLineTextWidget.CacheKey, MultiLineLabel> cache;
	private boolean centered = false;

	public MultiLineTextWidget(Component component, Font font) {
		this(0, 0, component, font);
	}

	public MultiLineTextWidget(int i, int j, Component component, Font font) {
		super(i, j, 0, 0, component, font);
		this.cache = Util.singleKeyCache(
			cacheKey -> cacheKey.maxRows.isPresent()
					? MultiLineLabel.create(font, cacheKey.message, cacheKey.maxWidth, cacheKey.maxRows.getAsInt())
					: MultiLineLabel.create(font, cacheKey.message, cacheKey.maxWidth)
		);
		this.active = false;
	}

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
		return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * 9;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		MultiLineLabel multiLineLabel = this.cache.getValue(this.getFreshCacheKey());
		int k = this.getX();
		int l = this.getY();
		int m = 9;
		int n = this.getColor();
		if (this.centered) {
			multiLineLabel.renderCentered(guiGraphics, k + this.getWidth() / 2, l, m, n);
		} else {
			multiLineLabel.renderLeftAligned(guiGraphics, k, l, m, n);
		}
	}

	private MultiLineTextWidget.CacheKey getFreshCacheKey() {
		return new MultiLineTextWidget.CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
	}

	@Environment(EnvType.CLIENT)
	static record CacheKey(Component message, int maxWidth, OptionalInt maxRows) {
	}
}
