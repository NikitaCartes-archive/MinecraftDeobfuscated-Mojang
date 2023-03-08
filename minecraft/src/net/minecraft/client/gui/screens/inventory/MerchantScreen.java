package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
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
	private static final int TEXTURE_WIDTH = 512;
	private static final int TEXTURE_HEIGHT = 256;
	private static final int MERCHANT_MENU_PART_X = 99;
	private static final int PROGRESS_BAR_X = 136;
	private static final int PROGRESS_BAR_Y = 16;
	private static final int SELL_ITEM_1_X = 5;
	private static final int SELL_ITEM_2_X = 35;
	private static final int BUY_ITEM_X = 68;
	private static final int LABEL_Y = 6;
	private static final int NUMBER_OF_OFFER_BUTTONS = 7;
	private static final int TRADE_BUTTON_X = 5;
	private static final int TRADE_BUTTON_HEIGHT = 20;
	private static final int TRADE_BUTTON_WIDTH = 88;
	private static final int SCROLLER_HEIGHT = 27;
	private static final int SCROLLER_WIDTH = 6;
	private static final int SCROLL_BAR_HEIGHT = 139;
	private static final int SCROLL_BAR_TOP_POS_Y = 18;
	private static final int SCROLL_BAR_START_X = 94;
	private static final Component TRADES_LABEL = Component.translatable("merchant.trades");
	private static final Component LEVEL_SEPARATOR = Component.literal(" - ");
	private static final Component DEPRECATED_TOOLTIP = Component.translatable("merchant.deprecated");
	private int shopItem;
	private final MerchantScreen.TradeOfferButton[] tradeOfferButtons = new MerchantScreen.TradeOfferButton[7];
	int scrollOff;
	private boolean isDragging;

	public MerchantScreen(MerchantMenu merchantMenu, Inventory inventory, Component component) {
		super(merchantMenu, inventory, component);
		this.imageWidth = 276;
		this.inventoryLabelX = 107;
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
			this.tradeOfferButtons[l] = this.addRenderableWidget(new MerchantScreen.TradeOfferButton(i + 5, k, l, button -> {
				if (button instanceof MerchantScreen.TradeOfferButton) {
					this.shopItem = ((MerchantScreen.TradeOfferButton)button).getIndex() + this.scrollOff;
					this.postButtonClick();
				}
			}));
			k += 20;
		}
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int i, int j) {
		int k = this.menu.getTraderLevel();
		if (k > 0 && k <= 5 && this.menu.showProgressBar()) {
			Component component = this.title.copy().append(LEVEL_SEPARATOR).append(Component.translatable("merchant.level." + k));
			int l = this.font.width(component);
			int m = 49 + this.imageWidth / 2 - l / 2;
			this.font.draw(poseStack, component, (float)m, 6.0F, 4210752);
		} else {
			this.font.draw(poseStack, this.title, (float)(49 + this.imageWidth / 2 - this.font.width(this.title) / 2), 6.0F, 4210752);
		}

		this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
		int n = this.font.width(TRADES_LABEL);
		this.font.draw(poseStack, TRADES_LABEL, (float)(5 - n / 2 + 48), 6.0F, 4210752);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		blit(poseStack, k, l, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
		MerchantOffers merchantOffers = this.menu.getOffers();
		if (!merchantOffers.isEmpty()) {
			int m = this.shopItem;
			if (m < 0 || m >= merchantOffers.size()) {
				return;
			}

			MerchantOffer merchantOffer = (MerchantOffer)merchantOffers.get(m);
			if (merchantOffer.isOutOfStock()) {
				RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
				blit(poseStack, this.leftPos + 83 + 99, this.topPos + 35, 0, 311.0F, 0.0F, 28, 21, 512, 256);
			}
		}
	}

	private void renderProgressBar(PoseStack poseStack, int i, int j, MerchantOffer merchantOffer) {
		RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
		int k = this.menu.getTraderLevel();
		int l = this.menu.getTraderXp();
		if (k < 5) {
			blit(poseStack, i + 136, j + 16, 0, 0.0F, 186.0F, 102, 5, 512, 256);
			int m = VillagerData.getMinXpPerLevel(k);
			if (l >= m && VillagerData.canLevelUp(k)) {
				int n = 100;
				float f = 100.0F / (float)(VillagerData.getMaxXpPerLevel(k) - m);
				int o = Math.min(Mth.floor(f * (float)(l - m)), 100);
				blit(poseStack, i + 136, j + 16, 0, 0.0F, 191.0F, o + 1, 5, 512, 256);
				int p = this.menu.getFutureTraderXp();
				if (p > 0) {
					int q = Math.min(Mth.floor((float)p * f), 100 - o);
					blit(poseStack, i + 136 + o + 1, j + 16 + 1, 0, 2.0F, 182.0F, q, 3, 512, 256);
				}
			}
		}
	}

	private void renderScroller(PoseStack poseStack, int i, int j, MerchantOffers merchantOffers) {
		int k = merchantOffers.size() + 1 - 7;
		if (k > 1) {
			int l = 139 - (27 + (k - 1) * 139 / k);
			int m = 1 + l / k + 139 / k;
			int n = 113;
			int o = Math.min(113, this.scrollOff * m);
			if (this.scrollOff == k - 1) {
				o = 113;
			}

			blit(poseStack, i + 94, j + 18 + o, 0, 0.0F, 199.0F, 6, 27, 512, 256);
		} else {
			blit(poseStack, i + 94, j + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		MerchantOffers merchantOffers = this.menu.getOffers();
		if (!merchantOffers.isEmpty()) {
			int k = (this.width - this.imageWidth) / 2;
			int l = (this.height - this.imageHeight) / 2;
			int m = l + 16 + 1;
			int n = k + 5 + 5;
			RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
			this.renderScroller(poseStack, k, l, merchantOffers);
			int o = 0;

			for (MerchantOffer merchantOffer : merchantOffers) {
				if (!this.canScroll(merchantOffers.size()) || o >= this.scrollOff && o < 7 + this.scrollOff) {
					ItemStack itemStack = merchantOffer.getBaseCostA();
					ItemStack itemStack2 = merchantOffer.getCostA();
					ItemStack itemStack3 = merchantOffer.getCostB();
					ItemStack itemStack4 = merchantOffer.getResult();
					poseStack.pushPose();
					poseStack.translate(0.0F, 0.0F, 100.0F);
					int p = m + 2;
					this.renderAndDecorateCostA(poseStack, itemStack2, itemStack, n, p);
					if (!itemStack3.isEmpty()) {
						this.itemRenderer.renderAndDecorateFakeItem(poseStack, itemStack3, k + 5 + 35, p);
						this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack3, k + 5 + 35, p);
					}

					this.renderButtonArrows(poseStack, merchantOffer, k, p);
					this.itemRenderer.renderAndDecorateFakeItem(poseStack, itemStack4, k + 5 + 68, p);
					this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack4, k + 5 + 68, p);
					poseStack.popPose();
					m += 20;
					o++;
				} else {
					o++;
				}
			}

			int q = this.shopItem;
			MerchantOffer merchantOfferx = (MerchantOffer)merchantOffers.get(q);
			if (this.menu.showProgressBar()) {
				this.renderProgressBar(poseStack, k, l, merchantOfferx);
			}

			if (merchantOfferx.isOutOfStock() && this.isHovering(186, 35, 22, 21, (double)i, (double)j) && this.menu.canRestock()) {
				this.renderTooltip(poseStack, DEPRECATED_TOOLTIP, i, j);
			}

			for (MerchantScreen.TradeOfferButton tradeOfferButton : this.tradeOfferButtons) {
				if (tradeOfferButton.isHoveredOrFocused()) {
					tradeOfferButton.renderToolTip(poseStack, i, j);
				}

				tradeOfferButton.visible = tradeOfferButton.index < this.menu.getOffers().size();
			}

			RenderSystem.enableDepthTest();
		}

		this.renderTooltip(poseStack, i, j);
	}

	private void renderButtonArrows(PoseStack poseStack, MerchantOffer merchantOffer, int i, int j) {
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
		if (merchantOffer.isOutOfStock()) {
			blit(poseStack, i + 5 + 35 + 20, j + 3, 0, 25.0F, 171.0F, 10, 9, 512, 256);
		} else {
			blit(poseStack, i + 5 + 35 + 20, j + 3, 0, 15.0F, 171.0F, 10, 9, 512, 256);
		}
	}

	private void renderAndDecorateCostA(PoseStack poseStack, ItemStack itemStack, ItemStack itemStack2, int i, int j) {
		this.itemRenderer.renderAndDecorateFakeItem(poseStack, itemStack, i, j);
		if (itemStack2.getCount() == itemStack.getCount()) {
			this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack, i, j);
		} else {
			this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack2, i, j, itemStack2.getCount() == 1 ? "1" : null);
			this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack, i + 14, j, itemStack.getCount() == 1 ? "1" : null);
			RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.0F, 300.0F);
			blit(poseStack, i + 7, j + 12, 0, 0.0F, 176.0F, 9, 2, 512, 256);
			poseStack.popPose();
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
			this.scrollOff = Mth.clamp((int)((double)this.scrollOff - f), 0, j);
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
			super(i, j, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
			this.index = k;
			this.visible = false;
		}

		public int getIndex() {
			return this.index;
		}

		public void renderToolTip(PoseStack poseStack, int i, int j) {
			if (this.isHovered && MerchantScreen.this.menu.getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
				if (i < this.getX() + 20) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostA();
					MerchantScreen.this.renderTooltip(poseStack, itemStack, i, j);
				} else if (i < this.getX() + 50 && i > this.getX() + 30) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostB();
					if (!itemStack.isEmpty()) {
						MerchantScreen.this.renderTooltip(poseStack, itemStack, i, j);
					}
				} else if (i > this.getX() + 65) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getResult();
					MerchantScreen.this.renderTooltip(poseStack, itemStack, i, j);
				}
			}
		}
	}
}
