package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

@Environment(EnvType.CLIENT)
public class MerchantScreen extends AbstractContainerScreen<MerchantMenu> {
	private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
	private int shopItem;
	private final MerchantScreen.TradeOfferButton[] tradeOfferButtons = new MerchantScreen.TradeOfferButton[7];
	private int scrollOff;
	private boolean isDragging;

	public MerchantScreen(MerchantMenu merchantMenu, Inventory inventory, Component component) {
		super(merchantMenu, inventory, component);
		this.imageWidth = 276;
	}

	private void postButtonClick() {
		this.menu.setSelectionHint(this.shopItem);
		this.menu.tryMoveItems(this.shopItem);
		this.minecraft.getConnection().send(new ServerboundSelectTradePacket(this.shopItem));
	}

	@Override
	protected void init() {
		super.init();
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		int k = j + 16 + 2;

		for (int l = 0; l < 7; l++) {
			this.tradeOfferButtons[l] = this.addButton(new MerchantScreen.TradeOfferButton(i + 5, k, l, button -> {
				if (button instanceof MerchantScreen.TradeOfferButton) {
					this.shopItem = ((MerchantScreen.TradeOfferButton)button).getIndex() + this.scrollOff;
					this.postButtonClick();
				}
			}));
			k += 20;
		}
	}

	@Override
	protected void renderLabels(int i, int j) {
		int k = this.menu.getTraderLevel();
		int l = this.imageHeight - 94;
		if (k > 0 && k <= 5 && this.menu.showProgressBar()) {
			String string = this.title.getColoredString();
			String string2 = "- " + I18n.get("merchant.level." + k);
			int m = this.font.width(string);
			int n = this.font.width(string2);
			int o = m + n + 3;
			int p = 49 + this.imageWidth / 2 - o / 2;
			this.font.draw(string, (float)p, 6.0F, 4210752);
			this.font.draw(this.inventory.getDisplayName().getColoredString(), 107.0F, (float)l, 4210752);
			this.font.draw(string2, (float)(p + m + 3), 6.0F, 4210752);
		} else {
			String string = this.title.getColoredString();
			this.font.draw(string, (float)(49 + this.imageWidth / 2 - this.font.width(string) / 2), 6.0F, 4210752);
			this.font.draw(this.inventory.getDisplayName().getColoredString(), 107.0F, (float)l, 4210752);
		}

		String string = I18n.get("merchant.trades");
		int q = this.font.width(string);
		this.font.draw(string, (float)(5 - q / 2 + 48), 6.0F, 4210752);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		blit(k, l, this.getBlitOffset(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 512);
		MerchantOffers merchantOffers = this.menu.getOffers();
		if (!merchantOffers.isEmpty()) {
			int m = this.shopItem;
			if (m < 0 || m >= merchantOffers.size()) {
				return;
			}

			MerchantOffer merchantOffer = (MerchantOffer)merchantOffers.get(m);
			if (merchantOffer.isOutOfStock()) {
				this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.disableLighting();
				blit(this.leftPos + 83 + 99, this.topPos + 35, this.getBlitOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
			}
		}
	}

	private void renderProgressBar(int i, int j, MerchantOffer merchantOffer) {
		this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
		int k = this.menu.getTraderLevel();
		int l = this.menu.getTraderXp();
		if (k < 5) {
			blit(i + 136, j + 16, this.getBlitOffset(), 0.0F, 186.0F, 102, 5, 256, 512);
			int m = VillagerData.getMinXpPerLevel(k);
			if (l >= m && VillagerData.canLevelUp(k)) {
				int n = 100;
				float f = (float)(100 / (VillagerData.getMaxXpPerLevel(k) - m));
				int o = Mth.floor(f * (float)(l - m));
				blit(i + 136, j + 16, this.getBlitOffset(), 0.0F, 191.0F, o + 1, 5, 256, 512);
				int p = this.menu.getFutureTraderXp();
				if (p > 0) {
					int q = Math.min(Mth.floor((float)p * f), 100 - o);
					blit(i + 136 + o + 1, j + 16 + 1, this.getBlitOffset(), 2.0F, 182.0F, q, 3, 256, 512);
				}
			}
		}
	}

	private void renderScroller(int i, int j, MerchantOffers merchantOffers) {
		Lighting.turnOff();
		int k = merchantOffers.size() + 1 - 7;
		if (k > 1) {
			int l = 139 - (27 + (k - 1) * 139 / k);
			int m = 1 + l / k + 139 / k;
			int n = 113;
			int o = Math.min(113, this.scrollOff * m);
			if (this.scrollOff == k - 1) {
				o = 113;
			}

			blit(i + 94, j + 18 + o, this.getBlitOffset(), 0.0F, 199.0F, 6, 27, 256, 512);
		} else {
			blit(i + 94, j + 18, this.getBlitOffset(), 6.0F, 199.0F, 6, 27, 256, 512);
		}

		Lighting.turnOnGui();
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		super.render(i, j, f);
		MerchantOffers merchantOffers = this.menu.getOffers();
		if (!merchantOffers.isEmpty()) {
			int k = (this.width - this.imageWidth) / 2;
			int l = (this.height - this.imageHeight) / 2;
			int m = l + 16 + 1;
			int n = k + 5 + 5;
			RenderSystem.pushMatrix();
			Lighting.turnOnGui();
			RenderSystem.enableRescaleNormal();
			RenderSystem.enableColorMaterial();
			RenderSystem.enableLighting();
			this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
			this.renderScroller(k, l, merchantOffers);
			int o = 0;

			for (MerchantOffer merchantOffer : merchantOffers) {
				if (!this.canScroll(merchantOffers.size()) || o >= this.scrollOff && o < 7 + this.scrollOff) {
					ItemStack itemStack = merchantOffer.getBaseCostA();
					ItemStack itemStack2 = merchantOffer.getCostA();
					ItemStack itemStack3 = merchantOffer.getCostB();
					ItemStack itemStack4 = merchantOffer.getResult();
					this.itemRenderer.blitOffset = 100.0F;
					int p = m + 2;
					this.renderAndDecorateCostA(itemStack2, itemStack, n, p);
					if (!itemStack3.isEmpty()) {
						this.itemRenderer.renderAndDecorateItem(itemStack3, k + 5 + 35, p);
						this.itemRenderer.renderGuiItemDecorations(this.font, itemStack3, k + 5 + 35, p);
					}

					this.renderButtonArrows(merchantOffer, k, p);
					this.itemRenderer.renderAndDecorateItem(itemStack4, k + 5 + 68, p);
					this.itemRenderer.renderGuiItemDecorations(this.font, itemStack4, k + 5 + 68, p);
					this.itemRenderer.blitOffset = 0.0F;
					m += 20;
					o++;
				} else {
					o++;
				}
			}

			int q = this.shopItem;
			MerchantOffer merchantOfferx = (MerchantOffer)merchantOffers.get(q);
			RenderSystem.disableLighting();
			if (this.menu.showProgressBar()) {
				this.renderProgressBar(k, l, merchantOfferx);
			}

			if (merchantOfferx.isOutOfStock() && this.isHovering(186, 35, 22, 21, (double)i, (double)j) && this.menu.canRestock()) {
				this.renderTooltip(I18n.get("merchant.deprecated"), i, j);
			}

			for (MerchantScreen.TradeOfferButton tradeOfferButton : this.tradeOfferButtons) {
				if (tradeOfferButton.isHovered()) {
					tradeOfferButton.renderToolTip(i, j);
				}

				tradeOfferButton.visible = tradeOfferButton.index < this.menu.getOffers().size();
			}

			RenderSystem.popMatrix();
			RenderSystem.enableLighting();
			RenderSystem.enableDepthTest();
			Lighting.turnOn();
		}

		this.renderTooltip(i, j);
	}

	private void renderButtonArrows(MerchantOffer merchantOffer, int i, int j) {
		Lighting.turnOff();
		RenderSystem.enableBlend();
		this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
		if (merchantOffer.isOutOfStock()) {
			blit(i + 5 + 35 + 20, j + 3, this.getBlitOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
		} else {
			blit(i + 5 + 35 + 20, j + 3, this.getBlitOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
		}

		Lighting.turnOnGui();
	}

	private void renderAndDecorateCostA(ItemStack itemStack, ItemStack itemStack2, int i, int j) {
		this.itemRenderer.renderAndDecorateItem(itemStack, i, j);
		if (itemStack2.getCount() == itemStack.getCount()) {
			this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, i, j);
		} else {
			this.itemRenderer.renderGuiItemDecorations(this.font, itemStack2, i, j, itemStack2.getCount() == 1 ? "1" : null);
			this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, i + 14, j, itemStack.getCount() == 1 ? "1" : null);
			this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
			this.setBlitOffset(this.getBlitOffset() + 300);
			Lighting.turnOff();
			blit(i + 7, j + 12, this.getBlitOffset(), 0.0F, 176.0F, 9, 2, 256, 512);
			Lighting.turnOnGui();
			this.setBlitOffset(this.getBlitOffset() - 300);
		}
	}

	private boolean canScroll(int i) {
		return i > 7;
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		int i = this.menu.getOffers().size();
		if (this.canScroll(i)) {
			int j = i - 7;
			this.scrollOff = (int)((double)this.scrollOff - f);
			this.scrollOff = Mth.clamp(this.scrollOff, 0, j);
		}

		return true;
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		int j = this.menu.getOffers().size();
		if (this.isDragging) {
			int k = this.topPos + 18;
			int l = k + 139;
			int m = j - 7;
			float h = ((float)e - (float)k - 13.5F) / ((float)(l - k) - 27.0F);
			h = h * (float)m + 0.5F;
			this.scrollOff = Mth.clamp((int)h, 0, m);
			return true;
		} else {
			return super.mouseDragged(d, e, i, f, g);
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		this.isDragging = false;
		int j = (this.width - this.imageWidth) / 2;
		int k = (this.height - this.imageHeight) / 2;
		if (this.canScroll(this.menu.getOffers().size())
			&& d > (double)(j + 94)
			&& d < (double)(j + 94 + 6)
			&& e > (double)(k + 18)
			&& e <= (double)(k + 18 + 139 + 1)) {
			this.isDragging = true;
		}

		return super.mouseClicked(d, e, i);
	}

	@Environment(EnvType.CLIENT)
	class TradeOfferButton extends Button {
		final int index;

		public TradeOfferButton(int i, int j, int k, Button.OnPress onPress) {
			super(i, j, 89, 20, "", onPress);
			this.index = k;
			this.visible = false;
		}

		public int getIndex() {
			return this.index;
		}

		@Override
		public void renderToolTip(int i, int j) {
			if (this.isHovered && MerchantScreen.this.menu.getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
				if (i < this.x + 20) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostA();
					MerchantScreen.this.renderTooltip(itemStack, i, j);
				} else if (i < this.x + 50 && i > this.x + 30) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostB();
					if (!itemStack.isEmpty()) {
						MerchantScreen.this.renderTooltip(itemStack, i, j);
					}
				} else if (i > this.x + 65) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getResult();
					MerchantScreen.this.renderTooltip(itemStack, i, j);
				}
			}
		}
	}
}
