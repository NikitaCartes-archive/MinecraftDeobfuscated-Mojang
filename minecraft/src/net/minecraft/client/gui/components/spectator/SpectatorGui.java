package net.minecraft.client.gui.components.spectator;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class SpectatorGui implements SpectatorMenuListener {
	private static final ResourceLocation HOTBAR_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar");
	private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
	private static final long FADE_OUT_DELAY = 5000L;
	private static final long FADE_OUT_TIME = 2000L;
	private final Minecraft minecraft;
	private long lastSelectionTime;
	@Nullable
	private SpectatorMenu menu;

	public SpectatorGui(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void onHotbarSelected(int i) {
		this.lastSelectionTime = Util.getMillis();
		if (this.menu != null) {
			this.menu.selectSlot(i);
		} else {
			this.menu = new SpectatorMenu(this);
		}
	}

	private float getHotbarAlpha() {
		long l = this.lastSelectionTime - Util.getMillis() + 5000L;
		return Mth.clamp((float)l / 2000.0F, 0.0F, 1.0F);
	}

	public void renderHotbar(GuiGraphics guiGraphics) {
		if (this.menu != null) {
			float f = this.getHotbarAlpha();
			if (f <= 0.0F) {
				this.menu.exit();
			} else {
				int i = guiGraphics.guiWidth() / 2;
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
				int j = Mth.floor((float)guiGraphics.guiHeight() - 22.0F * f);
				SpectatorPage spectatorPage = this.menu.getCurrentPage();
				this.renderPage(guiGraphics, f, i, j, spectatorPage);
				guiGraphics.pose().popPose();
			}
		}
	}

	protected void renderPage(GuiGraphics guiGraphics, float f, int i, int j, SpectatorPage spectatorPage) {
		int k = ARGB.white(f);
		guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SPRITE, i - 91, j, 182, 22, k);
		if (spectatorPage.getSelectedSlot() >= 0) {
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SELECTION_SPRITE, i - 91 - 1 + spectatorPage.getSelectedSlot() * 20, j - 1, 24, 23, k);
		}

		for (int l = 0; l < 9; l++) {
			this.renderSlot(guiGraphics, l, guiGraphics.guiWidth() / 2 - 90 + l * 20 + 2, (float)(j + 3), f, spectatorPage.getItem(l));
		}
	}

	private void renderSlot(GuiGraphics guiGraphics, int i, int j, float f, float g, SpectatorMenuItem spectatorMenuItem) {
		if (spectatorMenuItem != SpectatorMenu.EMPTY_SLOT) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate((float)j, f, 0.0F);
			float h = spectatorMenuItem.isEnabled() ? 1.0F : 0.25F;
			spectatorMenuItem.renderIcon(guiGraphics, h, g);
			guiGraphics.pose().popPose();
			int k = (int)(g * 255.0F);
			if (k > 3 && spectatorMenuItem.isEnabled()) {
				Component component = this.minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
				guiGraphics.drawString(this.minecraft.font, component, j + 19 - 2 - this.minecraft.font.width(component), (int)f + 6 + 3, 16777215 + (k << 24));
			}
		}
	}

	public void renderTooltip(GuiGraphics guiGraphics) {
		int i = (int)(this.getHotbarAlpha() * 255.0F);
		if (i > 3 && this.menu != null) {
			SpectatorMenuItem spectatorMenuItem = this.menu.getSelectedItem();
			Component component = spectatorMenuItem == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : spectatorMenuItem.getName();
			if (component != null) {
				int j = this.minecraft.font.width(component);
				int k = (guiGraphics.guiWidth() - j) / 2;
				int l = guiGraphics.guiHeight() - 35;
				guiGraphics.drawStringWithBackdrop(this.minecraft.font, component, k, l, j, ARGB.color(i, -1));
			}
		}
	}

	@Override
	public void onSpectatorMenuClosed(SpectatorMenu spectatorMenu) {
		this.menu = null;
		this.lastSelectionTime = 0L;
	}

	public boolean isMenuActive() {
		return this.menu != null;
	}

	public void onMouseScrolled(int i) {
		int j = this.menu.getSelectedSlot() + i;

		while (j >= 0 && j <= 8 && (this.menu.getItem(j) == SpectatorMenu.EMPTY_SLOT || !this.menu.getItem(j).isEnabled())) {
			j += i;
		}

		if (j >= 0 && j <= 8) {
			this.menu.selectSlot(j);
			this.lastSelectionTime = Util.getMillis();
		}
	}

	public void onMouseMiddleClick() {
		this.lastSelectionTime = Util.getMillis();
		if (this.isMenuActive()) {
			int i = this.menu.getSelectedSlot();
			if (i != -1) {
				this.menu.selectSlot(i);
			}
		} else {
			this.menu = new SpectatorMenu(this);
		}
	}
}
