package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class HeaderAndFooterLayout implements Layout {
	private static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 36;
	private static final int DEFAULT_CONTENT_MARGIN_TOP = 30;
	private final FrameLayout headerFrame = new FrameLayout();
	private final FrameLayout footerFrame = new FrameLayout();
	private final FrameLayout contentsFrame = new FrameLayout();
	private final Screen screen;
	private int headerHeight;
	private int footerHeight;

	public HeaderAndFooterLayout(Screen screen) {
		this(screen, 36);
	}

	public HeaderAndFooterLayout(Screen screen, int i) {
		this(screen, i, i);
	}

	public HeaderAndFooterLayout(Screen screen, int i, int j) {
		this.screen = screen;
		this.headerHeight = i;
		this.footerHeight = j;
		this.headerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
		this.footerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
		this.contentsFrame.defaultChildLayoutSetting().align(0.5F, 0.0F).paddingTop(30);
	}

	@Override
	public void setX(int i) {
	}

	@Override
	public void setY(int i) {
	}

	@Override
	public int getX() {
		return 0;
	}

	@Override
	public int getY() {
		return 0;
	}

	@Override
	public int getWidth() {
		return this.screen.width;
	}

	@Override
	public int getHeight() {
		return this.screen.height;
	}

	public int getFooterHeight() {
		return this.footerHeight;
	}

	public void setFooterHeight(int i) {
		this.footerHeight = i;
	}

	public void setHeaderHeight(int i) {
		this.headerHeight = i;
	}

	public int getHeaderHeight() {
		return this.headerHeight;
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		this.headerFrame.visitChildren(consumer);
		this.contentsFrame.visitChildren(consumer);
		this.footerFrame.visitChildren(consumer);
	}

	@Override
	public void arrangeElements() {
		int i = this.getHeaderHeight();
		int j = this.getFooterHeight();
		this.headerFrame.setMinWidth(this.screen.width);
		this.headerFrame.setMinHeight(i);
		this.headerFrame.setPosition(0, 0);
		this.headerFrame.arrangeElements();
		this.footerFrame.setMinWidth(this.screen.width);
		this.footerFrame.setMinHeight(j);
		this.footerFrame.arrangeElements();
		this.footerFrame.setY(this.screen.height - j);
		this.contentsFrame.setMinWidth(this.screen.width);
		this.contentsFrame.setMinHeight(this.screen.height - i - j);
		this.contentsFrame.setPosition(0, i);
		this.contentsFrame.arrangeElements();
	}

	public <T extends LayoutElement> T addToHeader(T layoutElement) {
		return this.headerFrame.addChild(layoutElement);
	}

	public <T extends LayoutElement> T addToHeader(T layoutElement, LayoutSettings layoutSettings) {
		return this.headerFrame.addChild(layoutElement, layoutSettings);
	}

	public <T extends LayoutElement> T addToFooter(T layoutElement) {
		return this.footerFrame.addChild(layoutElement);
	}

	public <T extends LayoutElement> T addToFooter(T layoutElement, LayoutSettings layoutSettings) {
		return this.footerFrame.addChild(layoutElement, layoutSettings);
	}

	public <T extends LayoutElement> T addToContents(T layoutElement) {
		return this.contentsFrame.addChild(layoutElement);
	}

	public <T extends LayoutElement> T addToContents(T layoutElement, LayoutSettings layoutSettings) {
		return this.contentsFrame.addChild(layoutElement, layoutSettings);
	}

	public LayoutSettings newHeaderLayoutSettings() {
		return this.headerFrame.newChildLayoutSettings();
	}

	public LayoutSettings newContentLayoutSettings() {
		return this.contentsFrame.newChildLayoutSettings();
	}

	public LayoutSettings newFooterLayoutSettings() {
		return this.footerFrame.newChildLayoutSettings();
	}
}
