/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BeaconScreen
extends AbstractContainerScreen<BeaconMenu> {
    static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
    private static final Component PRIMARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.primary");
    private static final Component SECONDARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.secondary");
    private final List<BeaconButton> beaconButtons = Lists.newArrayList();
    @Nullable
    MobEffect primary;
    @Nullable
    MobEffect secondary;

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
            }
        });
    }

    private <T extends AbstractWidget> void addBeaconButton(T abstractWidget) {
        this.addRenderableWidget(abstractWidget);
        this.beaconButtons.add((BeaconButton)((Object)abstractWidget));
    }

    @Override
    protected void init() {
        BeaconPowerButton beaconPowerButton;
        MobEffect mobEffect;
        int l;
        int k;
        int j;
        int i;
        super.init();
        this.beaconButtons.clear();
        this.addBeaconButton(new BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
        this.addBeaconButton(new BeaconCancelButton(this.leftPos + 190, this.topPos + 107));
        for (i = 0; i <= 2; ++i) {
            j = BeaconBlockEntity.BEACON_EFFECTS[i].length;
            k = j * 22 + (j - 1) * 2;
            for (l = 0; l < j; ++l) {
                mobEffect = BeaconBlockEntity.BEACON_EFFECTS[i][l];
                beaconPowerButton = new BeaconPowerButton(this.leftPos + 76 + l * 24 - k / 2, this.topPos + 22 + i * 25, mobEffect, true, i);
                beaconPowerButton.active = false;
                this.addBeaconButton(beaconPowerButton);
            }
        }
        i = 3;
        j = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
        k = j * 22 + (j - 1) * 2;
        for (l = 0; l < j - 1; ++l) {
            mobEffect = BeaconBlockEntity.BEACON_EFFECTS[3][l];
            beaconPowerButton = new BeaconPowerButton(this.leftPos + 167 + l * 24 - k / 2, this.topPos + 47, mobEffect, false, 3);
            beaconPowerButton.active = false;
            this.addBeaconButton(beaconPowerButton);
        }
        BeaconUpgradePowerButton beaconPowerButton2 = new BeaconUpgradePowerButton(this.leftPos + 167 + (j - 1) * 24 - k / 2, this.topPos + 47, BeaconBlockEntity.BEACON_EFFECTS[0][0]);
        beaconPowerButton2.visible = false;
        this.addBeaconButton(beaconPowerButton2);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    void updateButtons() {
        int i = ((BeaconMenu)this.menu).getLevels();
        this.beaconButtons.forEach(beaconButton -> beaconButton.updateStatus(i));
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        BeaconScreen.drawCenteredString(poseStack, this.font, PRIMARY_EFFECT_LABEL, 62, 10, 0xE0E0E0);
        BeaconScreen.drawCenteredString(poseStack, this.font, SECONDARY_EFFECT_LABEL, 169, 10, 0xE0E0E0);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
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
    static interface BeaconButton {
        public void updateStatus(int var1);
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconConfirmButton
    extends BeaconSpriteScreenButton {
        public BeaconConfirmButton(int i, int j) {
            super(i, j, 90, 220, CommonComponents.GUI_DONE);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft.getConnection().send(new ServerboundSetBeaconPacket(Optional.ofNullable(BeaconScreen.this.primary), Optional.ofNullable(BeaconScreen.this.secondary)));
            ((BeaconScreen)BeaconScreen.this).minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int i) {
            this.active = ((BeaconMenu)BeaconScreen.this.menu).hasPayment() && BeaconScreen.this.primary != null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconCancelButton
    extends BeaconSpriteScreenButton {
        public BeaconCancelButton(int i, int j) {
            super(i, j, 112, 220, CommonComponents.GUI_CANCEL);
        }

        @Override
        public void onPress() {
            ((BeaconScreen)BeaconScreen.this).minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int i) {
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconPowerButton
    extends BeaconScreenButton {
        private final boolean isPrimary;
        protected final int tier;
        private MobEffect effect;
        private TextureAtlasSprite sprite;

        public BeaconPowerButton(int i, int j, MobEffect mobEffect, boolean bl, int k) {
            super(i, j);
            this.isPrimary = bl;
            this.tier = k;
            this.setEffect(mobEffect);
        }

        protected void setEffect(MobEffect mobEffect) {
            this.effect = mobEffect;
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(mobEffect);
            this.setTooltip(Tooltip.create(this.createEffectDescription(mobEffect), null));
        }

        protected MutableComponent createEffectDescription(MobEffect mobEffect) {
            return Component.translatable(mobEffect.getDescriptionId());
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
            BeaconScreen.this.updateButtons();
        }

        @Override
        protected void renderIcon(PoseStack poseStack) {
            RenderSystem.setShaderTexture(0, this.sprite.atlasLocation());
            BeaconPowerButton.blit(poseStack, this.getX() + 2, this.getY() + 2, this.getBlitOffset(), 18, 18, this.sprite);
        }

        @Override
        public void updateStatus(int i) {
            this.active = this.tier < i;
            this.setSelected(this.effect == (this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            return this.createEffectDescription(this.effect);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BeaconUpgradePowerButton
    extends BeaconPowerButton {
        public BeaconUpgradePowerButton(int i, int j, MobEffect mobEffect) {
            super(i, j, mobEffect, false, 3);
        }

        @Override
        protected MutableComponent createEffectDescription(MobEffect mobEffect) {
            return Component.translatable(mobEffect.getDescriptionId()).append(" II");
        }

        @Override
        public void updateStatus(int i) {
            if (BeaconScreen.this.primary != null) {
                this.visible = true;
                this.setEffect(BeaconScreen.this.primary);
                super.updateStatus(i);
            } else {
                this.visible = false;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class BeaconSpriteScreenButton
    extends BeaconScreenButton {
        private final int iconX;
        private final int iconY;

        protected BeaconSpriteScreenButton(int i, int j, int k, int l, Component component) {
            super(i, j, component);
            this.iconX = k;
            this.iconY = l;
        }

        @Override
        protected void renderIcon(PoseStack poseStack) {
            this.blit(poseStack, this.getX() + 2, this.getY() + 2, this.iconX, this.iconY, 18, 18);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class BeaconScreenButton
    extends AbstractButton
    implements BeaconButton {
        private boolean selected;

        protected BeaconScreenButton(int i, int j) {
            super(i, j, 22, 22, CommonComponents.EMPTY);
        }

        protected BeaconScreenButton(int i, int j, Component component) {
            super(i, j, 22, 22, component);
        }

        @Override
        public void renderButton(PoseStack poseStack, int i, int j, float f) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, BEACON_LOCATION);
            int k = 219;
            int l = 0;
            if (!this.active) {
                l += this.width * 2;
            } else if (this.selected) {
                l += this.width * 1;
            } else if (this.isHoveredOrFocused()) {
                l += this.width * 3;
            }
            this.blit(poseStack, this.getX(), this.getY(), l, 219, this.width, this.height);
            this.renderIcon(poseStack);
        }

        protected abstract void renderIcon(PoseStack var1);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean bl) {
            this.selected = bl;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}

