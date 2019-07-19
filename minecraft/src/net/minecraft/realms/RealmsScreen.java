package net.minecraft.realms;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class RealmsScreen extends RealmsGuiEventListener implements RealmsConfirmResultListener {
	public static final int SKIN_HEAD_U = 8;
	public static final int SKIN_HEAD_V = 8;
	public static final int SKIN_HEAD_WIDTH = 8;
	public static final int SKIN_HEAD_HEIGHT = 8;
	public static final int SKIN_HAT_U = 40;
	public static final int SKIN_HAT_V = 8;
	public static final int SKIN_HAT_WIDTH = 8;
	public static final int SKIN_HAT_HEIGHT = 8;
	public static final int SKIN_TEX_WIDTH = 64;
	public static final int SKIN_TEX_HEIGHT = 64;
	private Minecraft minecraft;
	public int width;
	public int height;
	private final RealmsScreenProxy proxy = new RealmsScreenProxy(this);

	public RealmsScreenProxy getProxy() {
		return this.proxy;
	}

	public void init() {
	}

	public void init(Minecraft minecraft, int i, int j) {
		this.minecraft = minecraft;
	}

	public void drawCenteredString(String string, int i, int j, int k) {
		this.proxy.drawCenteredString(string, i, j, k);
	}

	public int draw(String string, int i, int j, int k, boolean bl) {
		return this.proxy.draw(string, i, j, k, bl);
	}

	public void drawString(String string, int i, int j, int k) {
		this.drawString(string, i, j, k, true);
	}

	public void drawString(String string, int i, int j, int k, boolean bl) {
		this.proxy.drawString(string, i, j, k, false);
	}

	public void blit(int i, int j, int k, int l, int m, int n) {
		this.proxy.blit(i, j, k, l, m, n);
	}

	public static void blit(int i, int j, float f, float g, int k, int l, int m, int n, int o, int p) {
		GuiComponent.blit(i, j, m, n, f, g, k, l, o, p);
	}

	public static void blit(int i, int j, float f, float g, int k, int l, int m, int n) {
		GuiComponent.blit(i, j, f, g, k, l, m, n);
	}

	public void fillGradient(int i, int j, int k, int l, int m, int n) {
		this.proxy.fillGradient(i, j, k, l, m, n);
	}

	public void renderBackground() {
		this.proxy.renderBackground();
	}

	public boolean isPauseScreen() {
		return this.proxy.isPauseScreen();
	}

	public void renderBackground(int i) {
		this.proxy.renderBackground(i);
	}

	public void render(int i, int j, float f) {
		for (int k = 0; k < this.proxy.buttons().size(); k++) {
			((AbstractRealmsButton)this.proxy.buttons().get(k)).render(i, j, f);
		}
	}

	public void renderTooltip(ItemStack itemStack, int i, int j) {
		this.proxy.renderTooltip(itemStack, i, j);
	}

	public void renderTooltip(String string, int i, int j) {
		this.proxy.renderTooltip(string, i, j);
	}

	public void renderTooltip(List<String> list, int i, int j) {
		this.proxy.renderTooltip(list, i, j);
	}

	public static void bind(String string) {
		Realms.bind(string);
	}

	public void tick() {
		this.tickButtons();
	}

	protected void tickButtons() {
		for (AbstractRealmsButton<?> abstractRealmsButton : this.buttons()) {
			abstractRealmsButton.tick();
		}
	}

	public int width() {
		return this.proxy.width;
	}

	public int height() {
		return this.proxy.height;
	}

	public int fontLineHeight() {
		return this.proxy.fontLineHeight();
	}

	public int fontWidth(String string) {
		return this.proxy.fontWidth(string);
	}

	public void fontDrawShadow(String string, int i, int j, int k) {
		this.proxy.fontDrawShadow(string, i, j, k);
	}

	public List<String> fontSplit(String string, int i) {
		return this.proxy.fontSplit(string, i);
	}

	public void childrenClear() {
		this.proxy.childrenClear();
	}

	public void addWidget(RealmsGuiEventListener realmsGuiEventListener) {
		this.proxy.addWidget(realmsGuiEventListener);
	}

	public void removeWidget(RealmsGuiEventListener realmsGuiEventListener) {
		this.proxy.removeWidget(realmsGuiEventListener);
	}

	public boolean hasWidget(RealmsGuiEventListener realmsGuiEventListener) {
		return this.proxy.hasWidget(realmsGuiEventListener);
	}

	public void buttonsAdd(AbstractRealmsButton<?> abstractRealmsButton) {
		this.proxy.buttonsAdd(abstractRealmsButton);
	}

	public List<AbstractRealmsButton<?>> buttons() {
		return this.proxy.buttons();
	}

	protected void buttonsClear() {
		this.proxy.buttonsClear();
	}

	protected void focusOn(RealmsGuiEventListener realmsGuiEventListener) {
		this.proxy.magicalSpecialHackyFocus(realmsGuiEventListener.getProxy());
	}

	public RealmsEditBox newEditBox(int i, int j, int k, int l, int m) {
		return this.newEditBox(i, j, k, l, m, "");
	}

	public RealmsEditBox newEditBox(int i, int j, int k, int l, int m, String string) {
		return new RealmsEditBox(i, j, k, l, m, string);
	}

	@Override
	public void confirmResult(boolean bl, int i) {
	}

	public static String getLocalizedString(String string) {
		return Realms.getLocalizedString(string);
	}

	public static String getLocalizedString(String string, Object... objects) {
		return Realms.getLocalizedString(string, objects);
	}

	public List<String> getLocalizedStringWithLineWidth(String string, int i) {
		return this.minecraft.font.split(I18n.get(string), i);
	}

	public RealmsAnvilLevelStorageSource getLevelStorageSource() {
		return new RealmsAnvilLevelStorageSource(Minecraft.getInstance().getLevelSource());
	}

	public void removed() {
	}

	protected void removeButton(RealmsButton realmsButton) {
		this.proxy.removeButton(realmsButton);
	}

	protected void setKeyboardHandlerSendRepeatsToGui(boolean bl) {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(bl);
	}

	protected boolean isKeyDown(int i) {
		return InputConstants.isKeyDown(Minecraft.getInstance().window.getWindow(), i);
	}

	protected void narrateLabels() {
		this.getProxy().narrateLabels();
	}

	public boolean isFocused(RealmsGuiEventListener realmsGuiEventListener) {
		return this.getProxy().getFocused() == realmsGuiEventListener.getProxy();
	}
}
