package net.minecraft.client.gui.screens.resourcepacks.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.resourcepacks.ResourcePackSelectScreen;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;

@Environment(EnvType.CLIENT)
public abstract class ResourcePackList extends ObjectSelectionList<ResourcePackList.ResourcePackEntry> {
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
	private static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("resourcePack.incompatible");
	private static final Component INCOMPATIBLE_CONFIRM_TITLE = new TranslatableComponent("resourcePack.incompatible.confirm.title");
	protected final Minecraft minecraft;
	private final Component title;

	public ResourcePackList(Minecraft minecraft, int i, int j, Component component) {
		super(minecraft, i, j, 32, j - 55 + 4, 36);
		this.minecraft = minecraft;
		this.centerListVertically = false;
		this.setRenderHeader(true, (int)(9.0F * 1.5F));
		this.title = component;
	}

	@Override
	protected void renderHeader(int i, int j, Tesselator tesselator) {
		Component component = new TextComponent("").append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
		this.minecraft
			.font
			.draw(
				component.getColoredString(),
				(float)(i + this.width / 2 - this.minecraft.font.width(component.getColoredString()) / 2),
				(float)Math.min(this.y0 + 3, j),
				16777215
			);
	}

	@Override
	public int getRowWidth() {
		return this.width;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.x1 - 6;
	}

	public void addResourcePackEntry(ResourcePackList.ResourcePackEntry resourcePackEntry) {
		this.addEntry(resourcePackEntry);
		resourcePackEntry.parent = this;
	}

	@Environment(EnvType.CLIENT)
	public static class ResourcePackEntry extends ObjectSelectionList.Entry<ResourcePackList.ResourcePackEntry> {
		private ResourcePackList parent;
		protected final Minecraft minecraft;
		protected final ResourcePackSelectScreen screen;
		private final UnopenedResourcePack resourcePack;

		public ResourcePackEntry(ResourcePackList resourcePackList, ResourcePackSelectScreen resourcePackSelectScreen, UnopenedResourcePack unopenedResourcePack) {
			this.screen = resourcePackSelectScreen;
			this.minecraft = Minecraft.getInstance();
			this.resourcePack = unopenedResourcePack;
			this.parent = resourcePackList;
		}

		public void addToList(SelectedResourcePackList selectedResourcePackList) {
			this.getResourcePack().getDefaultPosition().insert(selectedResourcePackList.children(), this, ResourcePackList.ResourcePackEntry::getResourcePack, true);
			this.updateParentList(selectedResourcePackList);
		}

		public void updateParentList(SelectedResourcePackList selectedResourcePackList) {
			this.parent = selectedResourcePackList;
		}

		protected void bindToIcon() {
			this.resourcePack.bindIcon(this.minecraft.getTextureManager());
		}

		protected PackCompatibility getCompatibility() {
			return this.resourcePack.getCompatibility();
		}

		protected String getDescription() {
			return this.resourcePack.getDescription().getColoredString();
		}

		protected String getName() {
			return this.resourcePack.getTitle().getColoredString();
		}

		public UnopenedResourcePack getResourcePack() {
			return this.resourcePack;
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			PackCompatibility packCompatibility = this.getCompatibility();
			if (!packCompatibility.isCompatible()) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				GuiComponent.fill(k - 1, j - 1, k + l - 9, j + m + 1, -8978432);
			}

			this.bindToIcon();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(k, j, 0.0F, 0.0F, 32, 32, 32, 32);
			String string = this.getName();
			String string2 = this.getDescription();
			if (this.showHoverOverlay() && (this.minecraft.options.touchscreen || bl)) {
				this.minecraft.getTextureManager().bind(ResourcePackList.ICON_OVERLAY_LOCATION);
				GuiComponent.fill(k, j, k + 32, j + 32, -1601138544);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				int p = n - k;
				int q = o - j;
				if (!packCompatibility.isCompatible()) {
					string = ResourcePackList.INCOMPATIBLE_TITLE.getColoredString();
					string2 = packCompatibility.getDescription().getColoredString();
				}

				if (this.canMoveRight()) {
					if (p < 32) {
						GuiComponent.blit(k, j, 0.0F, 32.0F, 32, 32, 256, 256);
					} else {
						GuiComponent.blit(k, j, 0.0F, 0.0F, 32, 32, 256, 256);
					}
				} else {
					if (this.canMoveLeft()) {
						if (p < 16) {
							GuiComponent.blit(k, j, 32.0F, 32.0F, 32, 32, 256, 256);
						} else {
							GuiComponent.blit(k, j, 32.0F, 0.0F, 32, 32, 256, 256);
						}
					}

					if (this.canMoveUp()) {
						if (p < 32 && p > 16 && q < 16) {
							GuiComponent.blit(k, j, 96.0F, 32.0F, 32, 32, 256, 256);
						} else {
							GuiComponent.blit(k, j, 96.0F, 0.0F, 32, 32, 256, 256);
						}
					}

					if (this.canMoveDown()) {
						if (p < 32 && p > 16 && q > 16) {
							GuiComponent.blit(k, j, 64.0F, 32.0F, 32, 32, 256, 256);
						} else {
							GuiComponent.blit(k, j, 64.0F, 0.0F, 32, 32, 256, 256);
						}
					}
				}
			}

			int px = this.minecraft.font.width(string);
			if (px > 157) {
				string = this.minecraft.font.substrByWidth(string, 157 - this.minecraft.font.width("...")) + "...";
			}

			this.minecraft.font.drawShadow(string, (float)(k + 32 + 2), (float)(j + 1), 16777215);
			List<String> list = this.minecraft.font.split(string2, 157);

			for (int r = 0; r < 2 && r < list.size(); r++) {
				this.minecraft.font.drawShadow((String)list.get(r), (float)(k + 32 + 2), (float)(j + 12 + 10 * r), 8421504);
			}
		}

		protected boolean showHoverOverlay() {
			return !this.resourcePack.isFixedPosition() || !this.resourcePack.isRequired();
		}

		protected boolean canMoveRight() {
			return !this.screen.isSelected(this);
		}

		protected boolean canMoveLeft() {
			return this.screen.isSelected(this) && !this.resourcePack.isRequired();
		}

		protected boolean canMoveUp() {
			List<ResourcePackList.ResourcePackEntry> list = this.parent.children();
			int i = list.indexOf(this);
			return i > 0 && !((ResourcePackList.ResourcePackEntry)list.get(i - 1)).resourcePack.isFixedPosition();
		}

		protected boolean canMoveDown() {
			List<ResourcePackList.ResourcePackEntry> list = this.parent.children();
			int i = list.indexOf(this);
			return i >= 0 && i < list.size() - 1 && !((ResourcePackList.ResourcePackEntry)list.get(i + 1)).resourcePack.isFixedPosition();
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			double f = d - (double)this.parent.getRowLeft();
			double g = e - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
			if (this.showHoverOverlay() && f <= 32.0) {
				if (this.canMoveRight()) {
					this.getScreen().setChanged();
					PackCompatibility packCompatibility = this.getCompatibility();
					if (packCompatibility.isCompatible()) {
						this.getScreen().select(this);
					} else {
						Component component = packCompatibility.getConfirmation();
						this.minecraft.setScreen(new ConfirmScreen(bl -> {
							this.minecraft.setScreen(this.getScreen());
							if (bl) {
								this.getScreen().select(this);
							}
						}, ResourcePackList.INCOMPATIBLE_CONFIRM_TITLE, component));
					}

					return true;
				}

				if (f < 16.0 && this.canMoveLeft()) {
					this.getScreen().deselect(this);
					return true;
				}

				if (f > 16.0 && g < 16.0 && this.canMoveUp()) {
					List<ResourcePackList.ResourcePackEntry> list = this.parent.children();
					int j = list.indexOf(this);
					list.remove(j);
					list.add(j - 1, this);
					this.getScreen().setChanged();
					return true;
				}

				if (f > 16.0 && g > 16.0 && this.canMoveDown()) {
					List<ResourcePackList.ResourcePackEntry> list = this.parent.children();
					int j = list.indexOf(this);
					list.remove(j);
					list.add(j + 1, this);
					this.getScreen().setChanged();
					return true;
				}
			}

			return false;
		}

		public ResourcePackSelectScreen getScreen() {
			return this.screen;
		}
	}
}
