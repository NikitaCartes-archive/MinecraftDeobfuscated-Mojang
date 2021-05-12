package net.minecraft.client.gui.spectator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class SpectatorMenu {
	private static final SpectatorMenuItem CLOSE_ITEM = new SpectatorMenu.CloseSpectatorItem();
	private static final SpectatorMenuItem SCROLL_LEFT = new SpectatorMenu.ScrollMenuItem(-1, true);
	private static final SpectatorMenuItem SCROLL_RIGHT_ENABLED = new SpectatorMenu.ScrollMenuItem(1, true);
	private static final SpectatorMenuItem SCROLL_RIGHT_DISABLED = new SpectatorMenu.ScrollMenuItem(1, false);
	private static final int MAX_PER_PAGE = 8;
	static final Component CLOSE_MENU_TEXT = new TranslatableComponent("spectatorMenu.close");
	static final Component PREVIOUS_PAGE_TEXT = new TranslatableComponent("spectatorMenu.previous_page");
	static final Component NEXT_PAGE_TEXT = new TranslatableComponent("spectatorMenu.next_page");
	public static final SpectatorMenuItem EMPTY_SLOT = new SpectatorMenuItem() {
		@Override
		public void selectItem(SpectatorMenu spectatorMenu) {
		}

		@Override
		public Component getName() {
			return TextComponent.EMPTY;
		}

		@Override
		public void renderIcon(PoseStack poseStack, float f, int i) {
		}

		@Override
		public boolean isEnabled() {
			return false;
		}
	};
	private final SpectatorMenuListener listener;
	private SpectatorMenuCategory category;
	private int selectedSlot = -1;
	int page;

	public SpectatorMenu(SpectatorMenuListener spectatorMenuListener) {
		this.category = new RootSpectatorMenuCategory();
		this.listener = spectatorMenuListener;
	}

	public SpectatorMenuItem getItem(int i) {
		int j = i + this.page * 6;
		if (this.page > 0 && i == 0) {
			return SCROLL_LEFT;
		} else if (i == 7) {
			return j < this.category.getItems().size() ? SCROLL_RIGHT_ENABLED : SCROLL_RIGHT_DISABLED;
		} else if (i == 8) {
			return CLOSE_ITEM;
		} else {
			return j >= 0 && j < this.category.getItems().size() ? MoreObjects.firstNonNull((SpectatorMenuItem)this.category.getItems().get(j), EMPTY_SLOT) : EMPTY_SLOT;
		}
	}

	public List<SpectatorMenuItem> getItems() {
		List<SpectatorMenuItem> list = Lists.<SpectatorMenuItem>newArrayList();

		for (int i = 0; i <= 8; i++) {
			list.add(this.getItem(i));
		}

		return list;
	}

	public SpectatorMenuItem getSelectedItem() {
		return this.getItem(this.selectedSlot);
	}

	public SpectatorMenuCategory getSelectedCategory() {
		return this.category;
	}

	public void selectSlot(int i) {
		SpectatorMenuItem spectatorMenuItem = this.getItem(i);
		if (spectatorMenuItem != EMPTY_SLOT) {
			if (this.selectedSlot == i && spectatorMenuItem.isEnabled()) {
				spectatorMenuItem.selectItem(this);
			} else {
				this.selectedSlot = i;
			}
		}
	}

	public void exit() {
		this.listener.onSpectatorMenuClosed(this);
	}

	public int getSelectedSlot() {
		return this.selectedSlot;
	}

	public void selectCategory(SpectatorMenuCategory spectatorMenuCategory) {
		this.category = spectatorMenuCategory;
		this.selectedSlot = -1;
		this.page = 0;
	}

	public SpectatorPage getCurrentPage() {
		return new SpectatorPage(this.getItems(), this.selectedSlot);
	}

	@Environment(EnvType.CLIENT)
	static class CloseSpectatorItem implements SpectatorMenuItem {
		@Override
		public void selectItem(SpectatorMenu spectatorMenu) {
			spectatorMenu.exit();
		}

		@Override
		public Component getName() {
			return SpectatorMenu.CLOSE_MENU_TEXT;
		}

		@Override
		public void renderIcon(PoseStack poseStack, float f, int i) {
			RenderSystem.setShaderTexture(0, SpectatorGui.SPECTATOR_LOCATION);
			GuiComponent.blit(poseStack, 0, 0, 128.0F, 0.0F, 16, 16, 256, 256);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}

	@Environment(EnvType.CLIENT)
	static class ScrollMenuItem implements SpectatorMenuItem {
		private final int direction;
		private final boolean enabled;

		public ScrollMenuItem(int i, boolean bl) {
			this.direction = i;
			this.enabled = bl;
		}

		@Override
		public void selectItem(SpectatorMenu spectatorMenu) {
			spectatorMenu.page = spectatorMenu.page + this.direction;
		}

		@Override
		public Component getName() {
			return this.direction < 0 ? SpectatorMenu.PREVIOUS_PAGE_TEXT : SpectatorMenu.NEXT_PAGE_TEXT;
		}

		@Override
		public void renderIcon(PoseStack poseStack, float f, int i) {
			RenderSystem.setShaderTexture(0, SpectatorGui.SPECTATOR_LOCATION);
			if (this.direction < 0) {
				GuiComponent.blit(poseStack, 0, 0, 144.0F, 0.0F, 16, 16, 256, 256);
			} else {
				GuiComponent.blit(poseStack, 0, 0, 160.0F, 0.0F, 16, 16, 256, 256);
			}
		}

		@Override
		public boolean isEnabled() {
			return this.enabled;
		}
	}
}
