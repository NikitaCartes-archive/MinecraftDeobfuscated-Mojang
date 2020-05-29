/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

@Environment(value=EnvType.CLIENT)
public class MerchantScreen
extends AbstractContainerScreen<MerchantMenu> {
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private int shopItem;
    private final TradeOfferButton[] tradeOfferButtons = new TradeOfferButton[7];
    private int scrollOff;
    private boolean isDragging;

    public MerchantScreen(MerchantMenu merchantMenu, Inventory inventory, Component component) {
        super(merchantMenu, inventory, component);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
    }

    private void postButtonClick() {
        ((MerchantMenu)this.menu).setSelectionHint(this.shopItem);
        ((MerchantMenu)this.menu).tryMoveItems(this.shopItem);
        this.minecraft.getConnection().send(new ServerboundSelectTradePacket(this.shopItem));
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 16 + 2;
        for (int l = 0; l < 7; ++l) {
            this.tradeOfferButtons[l] = this.addButton(new TradeOfferButton(i + 5, k, l, button -> {
                if (button instanceof TradeOfferButton) {
                    this.shopItem = ((TradeOfferButton)button).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }
            }));
            k += 20;
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        int l;
        String string;
        int k = ((MerchantMenu)this.menu).getTraderLevel();
        if (k > 0 && k <= 5 && ((MerchantMenu)this.menu).showProgressBar()) {
            string = "- " + I18n.get("merchant.level." + k, new Object[0]);
            l = this.font.width(this.title);
            int m = this.font.width(string);
            int n = l + m + 3;
            int o = 49 + this.imageWidth / 2 - n / 2;
            this.font.draw(poseStack, this.title, (float)o, 6.0f, 0x404040);
            this.font.draw(poseStack, string, (float)(o + l + 3), 6.0f, 0x404040);
        } else {
            this.font.draw(poseStack, this.title, (float)(49 + this.imageWidth / 2 - this.font.width(this.title) / 2), 6.0f, 0x404040);
        }
        this.font.draw(poseStack, this.inventory.getDisplayName(), (float)this.inventoryLabelX, (float)this.inventoryLabelY, 0x404040);
        string = I18n.get("merchant.trades", new Object[0]);
        l = this.font.width(string);
        this.font.draw(poseStack, string, (float)(5 - l / 2 + 48), 6.0f, 0x404040);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        MerchantScreen.blit(poseStack, k, l, this.getBlitOffset(), 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 512);
        MerchantOffers merchantOffers = ((MerchantMenu)this.menu).getOffers();
        if (!merchantOffers.isEmpty()) {
            int m = this.shopItem;
            if (m < 0 || m >= merchantOffers.size()) {
                return;
            }
            MerchantOffer merchantOffer = (MerchantOffer)merchantOffers.get(m);
            if (merchantOffer.isOutOfStock()) {
                this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                MerchantScreen.blit(poseStack, this.leftPos + 83 + 99, this.topPos + 35, this.getBlitOffset(), 311.0f, 0.0f, 28, 21, 256, 512);
            }
        }
    }

    private void renderProgressBar(PoseStack poseStack, int i, int j, MerchantOffer merchantOffer) {
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        int k = ((MerchantMenu)this.menu).getTraderLevel();
        int l = ((MerchantMenu)this.menu).getTraderXp();
        if (k >= 5) {
            return;
        }
        MerchantScreen.blit(poseStack, i + 136, j + 16, this.getBlitOffset(), 0.0f, 186.0f, 102, 5, 256, 512);
        int m = VillagerData.getMinXpPerLevel(k);
        if (l < m || !VillagerData.canLevelUp(k)) {
            return;
        }
        int n = 100;
        float f = 100.0f / (float)(VillagerData.getMaxXpPerLevel(k) - m);
        int o = Math.min(Mth.floor(f * (float)(l - m)), 100);
        MerchantScreen.blit(poseStack, i + 136, j + 16, this.getBlitOffset(), 0.0f, 191.0f, o + 1, 5, 256, 512);
        int p = ((MerchantMenu)this.menu).getFutureTraderXp();
        if (p > 0) {
            int q = Math.min(Mth.floor((float)p * f), 100 - o);
            MerchantScreen.blit(poseStack, i + 136 + o + 1, j + 16 + 1, this.getBlitOffset(), 2.0f, 182.0f, q, 3, 256, 512);
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
            MerchantScreen.blit(poseStack, i + 94, j + 18 + o, this.getBlitOffset(), 0.0f, 199.0f, 6, 27, 256, 512);
        } else {
            MerchantScreen.blit(poseStack, i + 94, j + 18, this.getBlitOffset(), 6.0f, 199.0f, 6, 27, 256, 512);
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        MerchantOffers merchantOffers = ((MerchantMenu)this.menu).getOffers();
        if (!merchantOffers.isEmpty()) {
            MerchantOffer merchantOffer2;
            int k = (this.width - this.imageWidth) / 2;
            int l = (this.height - this.imageHeight) / 2;
            int m = l + 16 + 1;
            int n = k + 5 + 5;
            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
            this.renderScroller(poseStack, k, l, merchantOffers);
            int o = 0;
            for (MerchantOffer merchantOffer2 : merchantOffers) {
                if (this.canScroll(merchantOffers.size()) && (o < this.scrollOff || o >= 7 + this.scrollOff)) {
                    ++o;
                    continue;
                }
                ItemStack itemStack = merchantOffer2.getBaseCostA();
                ItemStack itemStack2 = merchantOffer2.getCostA();
                ItemStack itemStack3 = merchantOffer2.getCostB();
                ItemStack itemStack4 = merchantOffer2.getResult();
                this.itemRenderer.blitOffset = 100.0f;
                int p = m + 2;
                this.renderAndDecorateCostA(poseStack, itemStack2, itemStack, n, p);
                if (!itemStack3.isEmpty()) {
                    this.itemRenderer.renderAndDecorateFakeItem(itemStack3, k + 5 + 35, p);
                    this.itemRenderer.renderGuiItemDecorations(this.font, itemStack3, k + 5 + 35, p);
                }
                this.renderButtonArrows(poseStack, merchantOffer2, k, p);
                this.itemRenderer.renderAndDecorateFakeItem(itemStack4, k + 5 + 68, p);
                this.itemRenderer.renderGuiItemDecorations(this.font, itemStack4, k + 5 + 68, p);
                this.itemRenderer.blitOffset = 0.0f;
                m += 20;
                ++o;
            }
            int q = this.shopItem;
            merchantOffer2 = (MerchantOffer)merchantOffers.get(q);
            if (((MerchantMenu)this.menu).showProgressBar()) {
                this.renderProgressBar(poseStack, k, l, merchantOffer2);
            }
            if (merchantOffer2.isOutOfStock() && this.isHovering(186, 35, 22, 21, i, j) && ((MerchantMenu)this.menu).canRestock()) {
                this.renderTooltip(poseStack, new TranslatableComponent("merchant.deprecated"), i, j);
            }
            for (TradeOfferButton tradeOfferButton : this.tradeOfferButtons) {
                if (tradeOfferButton.isHovered()) {
                    tradeOfferButton.renderToolTip(poseStack, i, j);
                }
                tradeOfferButton.visible = tradeOfferButton.index < ((MerchantMenu)this.menu).getOffers().size();
            }
            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
        }
        this.renderTooltip(poseStack, i, j);
    }

    private void renderButtonArrows(PoseStack poseStack, MerchantOffer merchantOffer, int i, int j) {
        RenderSystem.enableBlend();
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        if (merchantOffer.isOutOfStock()) {
            MerchantScreen.blit(poseStack, i + 5 + 35 + 20, j + 3, this.getBlitOffset(), 25.0f, 171.0f, 10, 9, 256, 512);
        } else {
            MerchantScreen.blit(poseStack, i + 5 + 35 + 20, j + 3, this.getBlitOffset(), 15.0f, 171.0f, 10, 9, 256, 512);
        }
    }

    private void renderAndDecorateCostA(PoseStack poseStack, ItemStack itemStack, ItemStack itemStack2, int i, int j) {
        this.itemRenderer.renderAndDecorateFakeItem(itemStack, i, j);
        if (itemStack2.getCount() == itemStack.getCount()) {
            this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, i, j);
        } else {
            this.itemRenderer.renderGuiItemDecorations(this.font, itemStack2, i, j, itemStack2.getCount() == 1 ? "1" : null);
            this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, i + 14, j, itemStack.getCount() == 1 ? "1" : null);
            this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
            this.setBlitOffset(this.getBlitOffset() + 300);
            MerchantScreen.blit(poseStack, i + 7, j + 12, this.getBlitOffset(), 0.0f, 176.0f, 9, 2, 256, 512);
            this.setBlitOffset(this.getBlitOffset() - 300);
        }
    }

    private boolean canScroll(int i) {
        return i > 7;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        int i = ((MerchantMenu)this.menu).getOffers().size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.scrollOff = (int)((double)this.scrollOff - f);
            this.scrollOff = Mth.clamp(this.scrollOff, 0, j);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        int j = ((MerchantMenu)this.menu).getOffers().size();
        if (this.isDragging) {
            int k = this.topPos + 18;
            int l = k + 139;
            int m = j - 7;
            float h = ((float)e - (float)k - 13.5f) / ((float)(l - k) - 27.0f);
            h = h * (float)m + 0.5f;
            this.scrollOff = Mth.clamp((int)h, 0, m);
            return true;
        }
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        this.isDragging = false;
        int j = (this.width - this.imageWidth) / 2;
        int k = (this.height - this.imageHeight) / 2;
        if (this.canScroll(((MerchantMenu)this.menu).getOffers().size()) && d > (double)(j + 94) && d < (double)(j + 94 + 6) && e > (double)(k + 18) && e <= (double)(k + 18 + 139 + 1)) {
            this.isDragging = true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Environment(value=EnvType.CLIENT)
    class TradeOfferButton
    extends Button {
        final int index;

        public TradeOfferButton(int i, int j, int k, Button.OnPress onPress) {
            super(i, j, 89, 20, TextComponent.EMPTY, onPress);
            this.index = k;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public void renderToolTip(PoseStack poseStack, int i, int j) {
            if (this.isHovered && ((MerchantMenu)MerchantScreen.this.menu).getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
                if (i < this.x + 20) {
                    ItemStack itemStack = ((MerchantOffer)((MerchantMenu)MerchantScreen.this.menu).getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostA();
                    MerchantScreen.this.renderTooltip(poseStack, itemStack, i, j);
                } else if (i < this.x + 50 && i > this.x + 30) {
                    ItemStack itemStack = ((MerchantOffer)((MerchantMenu)MerchantScreen.this.menu).getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostB();
                    if (!itemStack.isEmpty()) {
                        MerchantScreen.this.renderTooltip(poseStack, itemStack, i, j);
                    }
                } else if (i > this.x + 65) {
                    ItemStack itemStack = ((MerchantOffer)((MerchantMenu)MerchantScreen.this.menu).getOffers().get(this.index + MerchantScreen.this.scrollOff)).getResult();
                    MerchantScreen.this.renderTooltip(poseStack, itemStack, i, j);
                }
            }
        }
    }
}

