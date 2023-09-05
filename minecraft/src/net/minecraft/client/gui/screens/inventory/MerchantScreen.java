package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
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
	private static final ResourceLocation OUT_OF_STOCK_SPRITE = new ResourceLocation("container/villager/out_of_stock");
	private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = new ResourceLocation("container/villager/experience_bar_background");
	private static final ResourceLocation EXPERIENCE_BAR_CURRENT_SPRITE = new ResourceLocation("container/villager/experience_bar_current");
	private static final ResourceLocation EXPERIENCE_BAR_RESULT_SPRITE = new ResourceLocation("container/villager/experience_bar_result");
	private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("container/villager/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_SPRITE = new ResourceLocation("container/villager/scroller_disabled");
	private static final ResourceLocation TRADE_ARROW_OUT_OF_STOCK_SPRITE = new ResourceLocation("container/villager/trade_arrow_out_of_stock");
	private static final ResourceLocation TRADE_ARROW_SPRITE = new ResourceLocation("container/villager/trade_arrow");
	private static final ResourceLocation DISCOUNT_STRIKETHRUOGH_SPRITE = new ResourceLocation("container/villager/discount_strikethrough");
	private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager.png");
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
	protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
		int k = this.menu.getTraderLevel();
		if (k > 0 && k <= 5 && this.menu.showProgressBar()) {
			Component component = Component.translatable("merchant.title", this.title, Component.translatable("merchant.level." + k));
			int l = this.font.width(component);
			int m = 49 + this.imageWidth / 2 - l / 2;
			guiGraphics.drawString(this.font, component, m, 6, 4210752, false);
		} else {
			guiGraphics.drawString(this.font, this.title, 49 + this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
		}

		guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
		int n = this.font.width(TRADES_LABEL);
		guiGraphics.drawString(this.font, TRADES_LABEL, 5 - n / 2 + 48, 6, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(VILLAGER_LOCATION, k, l, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
		MerchantOffers merchantOffers = this.menu.getOffers();
		if (!merchantOffers.isEmpty()) {
			int m = this.shopItem;
			if (m < 0 || m >= merchantOffers.size()) {
				return;
			}

			MerchantOffer merchantOffer = (MerchantOffer)merchantOffers.get(m);
			if (merchantOffer.isOutOfStock()) {
				guiGraphics.blitSprite(OUT_OF_STOCK_SPRITE, this.leftPos + 83 + 99, this.topPos + 35, 0, 28, 21);
			}
		}
	}

	private void renderProgressBar(GuiGraphics guiGraphics, int i, int j, MerchantOffer merchantOffer) {
		int k = this.menu.getTraderLevel();
		int l = this.menu.getTraderXp();
		if (k < 5) {
			guiGraphics.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, i + 136, j + 16, 0, 102, 5);
			int m = VillagerData.getMinXpPerLevel(k);
			if (l >= m && VillagerData.canLevelUp(k)) {
				int n = 102;
				float f = 102.0F / (float)(VillagerData.getMaxXpPerLevel(k) - m);
				int o = Math.min(Mth.floor(f * (float)(l - m)), 102);
				guiGraphics.blitSprite(EXPERIENCE_BAR_CURRENT_SPRITE, 102, 5, 0, 0, i + 136, j + 16, 0, o, 5);
				int p = this.menu.getFutureTraderXp();
				if (p > 0) {
					int q = Math.min(Mth.floor((float)p * f), 102 - o);
					guiGraphics.blitSprite(EXPERIENCE_BAR_RESULT_SPRITE, 102, 5, o, 0, i + 136 + o, j + 16, 0, q, 5);
				}
			}
		}
	}

	private void renderScroller(GuiGraphics guiGraphics, int i, int j, MerchantOffers merchantOffers) {
		int k = merchantOffers.size() + 1 - 7;
		if (k > 1) {
			int l = 139 - (27 + (k - 1) * 139 / k);
			int m = 1 + l / k + 139 / k;
			int n = 113;
			int o = Math.min(113, this.scrollOff * m);
			if (this.scrollOff == k - 1) {
				o = 113;
			}

			guiGraphics.blitSprite(SCROLLER_SPRITE, i + 94, j + 18 + o, 0, 6, 27);
		} else {
			guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, i + 94, j + 18, 0, 6, 27);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		MerchantOffers merchantOffers = this.menu.getOffers();
		if (!merchantOffers.isEmpty()) {
			int k = (this.width - this.imageWidth) / 2;
			int l = (this.height - this.imageHeight) / 2;
			int m = l + 16 + 1;
			int n = k + 5 + 5;
			this.renderScroller(guiGraphics, k, l, merchantOffers);
			int o = 0;

			for (MerchantOffer merchantOffer : merchantOffers) {
				if (!this.canScroll(merchantOffers.size()) || o >= this.scrollOff && o < 7 + this.scrollOff) {
					ItemStack itemStack = merchantOffer.getBaseCostA();
					ItemStack itemStack2 = merchantOffer.getCostA();
					ItemStack itemStack3 = merchantOffer.getCostB();
					ItemStack itemStack4 = merchantOffer.getResult();
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
					int p = m + 2;
					this.renderAndDecorateCostA(guiGraphics, itemStack2, itemStack, n, p);
					if (!itemStack3.isEmpty()) {
						guiGraphics.renderFakeItem(itemStack3, k + 5 + 35, p);
						guiGraphics.renderItemDecorations(this.font, itemStack3, k + 5 + 35, p);
					}

					this.renderButtonArrows(guiGraphics, merchantOffer, k, p);
					guiGraphics.renderFakeItem(itemStack4, k + 5 + 68, p);
					guiGraphics.renderItemDecorations(this.font, itemStack4, k + 5 + 68, p);
					guiGraphics.pose().popPose();
					m += 20;
					o++;
				} else {
					o++;
				}
			}

			int q = this.shopItem;
			MerchantOffer merchantOfferx = (MerchantOffer)merchantOffers.get(q);
			if (this.menu.showProgressBar()) {
				this.renderProgressBar(guiGraphics, k, l, merchantOfferx);
			}

			if (merchantOfferx.isOutOfStock() && this.isHovering(186, 35, 22, 21, (double)i, (double)j) && this.menu.canRestock()) {
				guiGraphics.renderTooltip(this.font, DEPRECATED_TOOLTIP, i, j);
			}

			for (MerchantScreen.TradeOfferButton tradeOfferButton : this.tradeOfferButtons) {
				if (tradeOfferButton.isHoveredOrFocused()) {
					tradeOfferButton.renderToolTip(guiGraphics, i, j);
				}

				tradeOfferButton.visible = tradeOfferButton.index < this.menu.getOffers().size();
			}

			RenderSystem.enableDepthTest();
		}

		this.renderTooltip(guiGraphics, i, j);
	}

	private void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer merchantOffer, int i, int j) {
		RenderSystem.enableBlend();
		if (merchantOffer.isOutOfStock()) {
			guiGraphics.blitSprite(TRADE_ARROW_OUT_OF_STOCK_SPRITE, i + 5 + 35 + 20, j + 3, 0, 10, 9);
		} else {
			guiGraphics.blitSprite(TRADE_ARROW_SPRITE, i + 5 + 35 + 20, j + 3, 0, 10, 9);
		}
	}

	private void renderAndDecorateCostA(GuiGraphics guiGraphics, ItemStack itemStack, ItemStack itemStack2, int i, int j) {
		guiGraphics.renderFakeItem(itemStack, i, j);
		if (itemStack2.getCount() == itemStack.getCount()) {
			guiGraphics.renderItemDecorations(this.font, itemStack, i, j);
		} else {
			guiGraphics.renderItemDecorations(this.font, itemStack2, i, j, itemStack2.getCount() == 1 ? "1" : null);
			guiGraphics.renderItemDecorations(this.font, itemStack, i + 14, j, itemStack.getCount() == 1 ? "1" : null);
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
			guiGraphics.blitSprite(DISCOUNT_STRIKETHRUOGH_SPRITE, i + 7, j + 12, 0, 9, 2);
			guiGraphics.pose().popPose();
		}
	}

	private boolean canScroll(int i) {
		return i > 7;
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		int i = this.menu.getOffers().size();
		if (this.canScroll(i)) {
			int j = i - 7;
			this.scrollOff = Mth.clamp((int)((double)this.scrollOff - g), 0, j);
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

		public void renderToolTip(GuiGraphics guiGraphics, int i, int j) {
			if (this.isHovered && MerchantScreen.this.menu.getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
				if (i < this.getX() + 20) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostA();
					guiGraphics.renderTooltip(MerchantScreen.this.font, itemStack, i, j);
				} else if (i < this.getX() + 50 && i > this.getX() + 30) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostB();
					if (!itemStack.isEmpty()) {
						guiGraphics.renderTooltip(MerchantScreen.this.font, itemStack, i, j);
					}
				} else if (i > this.getX() + 65) {
					ItemStack itemStack = ((MerchantOffer)MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff)).getResult();
					guiGraphics.renderTooltip(MerchantScreen.this.font, itemStack, i, j);
				}
			}
		}
	}
}
