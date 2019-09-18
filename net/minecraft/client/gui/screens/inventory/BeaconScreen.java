/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

@Environment(value=EnvType.CLIENT)
public class BeaconScreen
extends AbstractContainerScreen<BeaconMenu> {
    private static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
    private BeaconConfirmButton confirmButton;
    private boolean initPowerButtons;
    private MobEffect primary;
    private MobEffect secondary;

    public BeaconScreen(final BeaconMenu beaconMenu, Inventory inventory, Component component) {
        super(beaconMenu, inventory, component);
        this.imageWidth = 230;
        this.imageHeight = 219;
        beaconMenu.addSlotListener(new ContainerListener(){

            @Override
            public void refreshContainer(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList) {
            }

            @Override
            public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
            }

            @Override
            public void setContainerData(AbstractContainerMenu abstractContainerMenu, int i, int j) {
                BeaconScreen.this.primary = beaconMenu.getPrimaryEffect();
                BeaconScreen.this.secondary = beaconMenu.getSecondaryEffect();
                BeaconScreen.this.initPowerButtons = true;
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        this.confirmButton = this.addButton(new BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
        this.addButton(new BeaconCancelButton(this.leftPos + 190, this.topPos + 107));
        this.initPowerButtons = true;
        this.confirmButton.active = false;
    }

    @Override
    public void tick() {
        super.tick();
        int i = ((BeaconMenu)this.menu).getLevels();
        if (this.initPowerButtons && i >= 0) {
            BeaconPowerButton beaconPowerButton;
            MobEffect mobEffect;
            int m;
            int l;
            int k;
            int j;
            this.initPowerButtons = false;
            for (j = 0; j <= 2; ++j) {
                k = BeaconBlockEntity.BEACON_EFFECTS[j].length;
                l = k * 22 + (k - 1) * 2;
                for (m = 0; m < k; ++m) {
                    mobEffect = BeaconBlockEntity.BEACON_EFFECTS[j][m];
                    beaconPowerButton = new BeaconPowerButton(this.leftPos + 76 + m * 24 - l / 2, this.topPos + 22 + j * 25, mobEffect, true);
                    this.addButton(beaconPowerButton);
                    if (j >= i) {
                        beaconPowerButton.active = false;
                        continue;
                    }
                    if (mobEffect != this.primary) continue;
                    beaconPowerButton.setSelected(true);
                }
            }
            j = 3;
            k = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
            l = k * 22 + (k - 1) * 2;
            for (m = 0; m < k - 1; ++m) {
                mobEffect = BeaconBlockEntity.BEACON_EFFECTS[3][m];
                beaconPowerButton = new BeaconPowerButton(this.leftPos + 167 + m * 24 - l / 2, this.topPos + 47, mobEffect, false);
                this.addButton(beaconPowerButton);
                if (3 >= i) {
                    beaconPowerButton.active = false;
                    continue;
                }
                if (mobEffect != this.secondary) continue;
                beaconPowerButton.setSelected(true);
            }
            if (this.primary != null) {
                BeaconPowerButton beaconPowerButton2 = new BeaconPowerButton(this.leftPos + 167 + (k - 1) * 24 - l / 2, this.topPos + 47, this.primary, false);
                this.addButton(beaconPowerButton2);
                if (3 >= i) {
                    beaconPowerButton2.active = false;
                } else if (this.primary == this.secondary) {
                    beaconPowerButton2.setSelected(true);
                }
            }
        }
        this.confirmButton.active = ((BeaconMenu)this.menu).hasPayment() && this.primary != null;
    }

    @Override
    protected void renderLabels(int i, int j) {
        Lighting.turnOff();
        this.drawCenteredString(this.font, I18n.get("block.minecraft.beacon.primary", new Object[0]), 62, 10, 0xE0E0E0);
        this.drawCenteredString(this.font, I18n.get("block.minecraft.beacon.secondary", new Object[0]), 169, 10, 0xE0E0E0);
        for (AbstractWidget abstractWidget : this.buttons) {
            if (!abstractWidget.isHovered()) continue;
            abstractWidget.renderToolTip(i - this.leftPos, j - this.topPos);
            break;
        }
        Lighting.turnOnGui();
    }

    @Override
    protected void renderBg(float f, int i, int j) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(BEACON_LOCATION);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
        this.itemRenderer.blitOffset = 100.0f;
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.EMERALD), k + 42, l + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.DIAMOND), k + 42 + 22, l + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.GOLD_INGOT), k + 42 + 44, l + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.IRON_INGOT), k + 42 + 66, l + 109);
        this.itemRenderer.blitOffset = 0.0f;
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        super.render(i, j, f);
        this.renderTooltip(i, j);
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconCancelButton
    extends BeaconSpriteScreenButton {
        public BeaconCancelButton(int i, int j) {
            super(i, j, 112, 220);
        }

        @Override
        public void onPress() {
            ((BeaconScreen)BeaconScreen.this).minecraft.player.connection.send(new ServerboundContainerClosePacket(((BeaconScreen)BeaconScreen.this).minecraft.player.containerMenu.containerId));
            BeaconScreen.this.minecraft.setScreen(null);
        }

        @Override
        public void renderToolTip(int i, int j) {
            BeaconScreen.this.renderTooltip(I18n.get("gui.cancel", new Object[0]), i, j);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconConfirmButton
    extends BeaconSpriteScreenButton {
        public BeaconConfirmButton(int i, int j) {
            super(i, j, 90, 220);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft.getConnection().send(new ServerboundSetBeaconPacket(MobEffect.getId(BeaconScreen.this.primary), MobEffect.getId(BeaconScreen.this.secondary)));
            ((BeaconScreen)BeaconScreen.this).minecraft.player.connection.send(new ServerboundContainerClosePacket(((BeaconScreen)BeaconScreen.this).minecraft.player.containerMenu.containerId));
            BeaconScreen.this.minecraft.setScreen(null);
        }

        @Override
        public void renderToolTip(int i, int j) {
            BeaconScreen.this.renderTooltip(I18n.get("gui.done", new Object[0]), i, j);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class BeaconSpriteScreenButton
    extends BeaconScreenButton {
        private final int iconX;
        private final int iconY;

        protected BeaconSpriteScreenButton(int i, int j, int k, int l) {
            super(i, j);
            this.iconX = k;
            this.iconY = l;
        }

        @Override
        protected void renderIcon() {
            this.blit(this.x + 2, this.y + 2, this.iconX, this.iconY, 18, 18);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconPowerButton
    extends BeaconScreenButton {
        private final MobEffect effect;
        private final TextureAtlasSprite sprite;
        private final boolean isPrimary;

        public BeaconPowerButton(int i, int j, MobEffect mobEffect, boolean bl) {
            super(i, j);
            this.effect = mobEffect;
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(mobEffect);
            this.isPrimary = bl;
        }

        @Override
        public void onPress() {
            if (this.isSelected()) {
                return;
            }
            if (this.isPrimary) {
                BeaconScreen.this.primary = this.effect;
            } else {
                BeaconScreen.this.secondary = this.effect;
            }
            BeaconScreen.this.buttons.clear();
            BeaconScreen.this.children.clear();
            BeaconScreen.this.init();
            BeaconScreen.this.tick();
        }

        @Override
        public void renderToolTip(int i, int j) {
            String string = I18n.get(this.effect.getDescriptionId(), new Object[0]);
            if (!this.isPrimary && this.effect != MobEffects.REGENERATION) {
                string = string + " II";
            }
            BeaconScreen.this.renderTooltip(string, i, j);
        }

        @Override
        protected void renderIcon() {
            Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_MOB_EFFECTS);
            BeaconPowerButton.blit(this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.sprite);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class BeaconScreenButton
    extends AbstractButton {
        private boolean selected;

        protected BeaconScreenButton(int i, int j) {
            super(i, j, 22, 22, "");
        }

        @Override
        public void renderButton(int i, int j, float f) {
            Minecraft.getInstance().getTextureManager().bind(BEACON_LOCATION);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            int k = 219;
            int l = 0;
            if (!this.active) {
                l += this.width * 2;
            } else if (this.selected) {
                l += this.width * 1;
            } else if (this.isHovered()) {
                l += this.width * 3;
            }
            this.blit(this.x, this.y, l, 219, this.width, this.height);
            this.renderIcon();
        }

        protected abstract void renderIcon();

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean bl) {
            this.selected = bl;
        }
    }
}

