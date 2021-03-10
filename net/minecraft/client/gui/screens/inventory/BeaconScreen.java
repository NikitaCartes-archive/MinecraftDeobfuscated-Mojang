/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private static final Component PRIMARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.primary");
    private static final Component SECONDARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.secondary");
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
            public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
            }

            @Override
            public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
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
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        BeaconScreen.drawCenteredString(poseStack, this.font, PRIMARY_EFFECT_LABEL, 62, 10, 0xE0E0E0);
        BeaconScreen.drawCenteredString(poseStack, this.font, SECONDARY_EFFECT_LABEL, 169, 10, 0xE0E0E0);
        for (AbstractWidget abstractWidget : this.buttons) {
            if (!abstractWidget.isHovered()) continue;
            abstractWidget.renderToolTip(poseStack, i - this.leftPos, j - this.topPos);
            break;
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BEACON_LOCATION);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        this.itemRenderer.blitOffset = 100.0f;
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.NETHERITE_INGOT), k + 20, l + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.EMERALD), k + 41, l + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.DIAMOND), k + 41 + 22, l + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.GOLD_INGOT), k + 42 + 44, l + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.IRON_INGOT), k + 42 + 66, l + 109);
        this.itemRenderer.blitOffset = 0.0f;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        this.renderTooltip(poseStack, i, j);
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
        public void renderToolTip(PoseStack poseStack, int i, int j) {
            BeaconScreen.this.renderTooltip(poseStack, CommonComponents.GUI_CANCEL, i, j);
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
        public void renderToolTip(PoseStack poseStack, int i, int j) {
            BeaconScreen.this.renderTooltip(poseStack, CommonComponents.GUI_DONE, i, j);
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
        protected void renderIcon(PoseStack poseStack) {
            this.blit(poseStack, this.x + 2, this.y + 2, this.iconX, this.iconY, 18, 18);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconPowerButton
    extends BeaconScreenButton {
        private final MobEffect effect;
        private final TextureAtlasSprite sprite;
        private final boolean isPrimary;
        private final Component tooltip;

        public BeaconPowerButton(int i, int j, MobEffect mobEffect, boolean bl) {
            super(i, j);
            this.effect = mobEffect;
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(mobEffect);
            this.isPrimary = bl;
            this.tooltip = this.createTooltip(mobEffect, bl);
        }

        private Component createTooltip(MobEffect mobEffect, boolean bl) {
            TranslatableComponent mutableComponent = new TranslatableComponent(mobEffect.getDescriptionId());
            if (!bl && mobEffect != MobEffects.REGENERATION) {
                mutableComponent.append(" II");
            }
            return mutableComponent;
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
        public void renderToolTip(PoseStack poseStack, int i, int j) {
            BeaconScreen.this.renderTooltip(poseStack, this.tooltip, i, j);
        }

        @Override
        protected void renderIcon(PoseStack poseStack) {
            RenderSystem.setShaderTexture(0, this.sprite.atlas().location());
            BeaconPowerButton.blit(poseStack, this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.sprite);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class BeaconScreenButton
    extends AbstractButton {
        private boolean selected;

        protected BeaconScreenButton(int i, int j) {
            super(i, j, 22, 22, TextComponent.EMPTY);
        }

        @Override
        public void renderButton(PoseStack poseStack, int i, int j, float f) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, BEACON_LOCATION);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            int k = 219;
            int l = 0;
            if (!this.active) {
                l += this.width * 2;
            } else if (this.selected) {
                l += this.width * 1;
            } else if (this.isHovered()) {
                l += this.width * 3;
            }
            this.blit(poseStack, this.x, this.y, l, 219, this.width, this.height);
            this.renderIcon(poseStack);
        }

        protected abstract void renderIcon(PoseStack var1);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean bl) {
            this.selected = bl;
        }
    }
}

