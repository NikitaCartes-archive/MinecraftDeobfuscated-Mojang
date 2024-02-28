package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class HeaderAndFooterLayout implements Layout {
	public static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 33;
	private static final int CONTENT_MARGIN_TOP = 30;
	private final FrameLayout headerFrame = new FrameLayout();
	private final FrameLayout footerFrame = new FrameLayout();
	private final FrameLayout contentsFrame = new FrameLayout();
	private final Screen screen;
	private int headerHeight;
	private int footerHeight;

	public HeaderAndFooterLayout(Screen screen) {
		this(screen, 33);
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

	public int getContentHeight() {
		return this.screen.height - this.getHeaderHeight() - this.getFooterHeight();
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
		this.contentsFrame.arrangeElements();
		int k = i + 30;
		int l = this.screen.height - j - this.contentsFrame.getHeight();
		this.contentsFrame.setPosition(0, Math.min(k, l));
	}

	public <T extends LayoutElement> T addToHeader(T layoutElement) {
		return this.headerFrame.addChild(layoutElement);
	}

	public <T extends LayoutElement> T addToHeader(T layoutElement, Consumer<LayoutSettings> consumer) {
		return this.headerFrame.addChild(layoutElement, consumer);
	}

	public void addTitleHeader(Component component, Font font) {
		this.headerFrame.addChild(new StringWidget(component, font));
	}

	public <T extends LayoutElement> T addToFooter(T layoutElement) {
		return this.footerFrame.addChild(layoutElement);
	}

	public <T extends LayoutElement> T addToFooter(T layoutElement, Consumer<LayoutSettings> consumer) {
		return this.footerFrame.addChild(layoutElement, consumer);
	}

	public <T extends LayoutElement> T addToContents(T layoutElement) {
		return this.contentsFrame.addChild(layoutElement);
	}

	public <T extends LayoutElement> T addToContents(T layoutElement, Consumer<LayoutSettings> consumer) {
		return this.contentsFrame.addChild(layoutElement, consumer);
	}
}
