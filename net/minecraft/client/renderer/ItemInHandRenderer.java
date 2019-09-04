/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Environment(value=EnvType.CLIENT)
public class ItemInHandRenderer {
    private static final ResourceLocation MAP_BACKGROUND_LOCATION = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");
    private final Minecraft minecraft;
    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private float mainHandHeight;
    private float oMainHandHeight;
    private float offHandHeight;
    private float oOffHandHeight;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;

    public ItemInHandRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
        this.itemRenderer = minecraft.getItemRenderer();
    }

    public void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType) {
        this.renderItem(livingEntity, itemStack, transformType, false);
    }

    public void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl) {
        boolean bl2;
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        Block block = Block.byItem(item);
        RenderSystem.pushMatrix();
        boolean bl3 = bl2 = this.itemRenderer.isGui3d(itemStack) && block.getRenderLayer() == BlockLayer.TRANSLUCENT;
        if (bl2) {
            RenderSystem.depthMask(false);
        }
        this.itemRenderer.renderWithMobState(itemStack, livingEntity, transformType, bl);
        if (bl2) {
            RenderSystem.depthMask(true);
        }
        RenderSystem.popMatrix();
    }

    private void enableLight(float f, float g) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(g, 0.0f, 1.0f, 0.0f);
        Lighting.turnOn();
        RenderSystem.popMatrix();
    }

    private void setLightValue() {
        LocalPlayer abstractClientPlayer = this.minecraft.player;
        int i = this.minecraft.level.getLightColor(new BlockPos(abstractClientPlayer.x, abstractClientPlayer.y + (double)abstractClientPlayer.getEyeHeight(), abstractClientPlayer.z));
        float f = i & 0xFFFF;
        float g = i >> 16;
        RenderSystem.glMultiTexCoord2f(33985, f, g);
    }

    private void setPlayerBob(float f) {
        LocalPlayer localPlayer = this.minecraft.player;
        float g = Mth.lerp(f, localPlayer.xBobO, localPlayer.xBob);
        float h = Mth.lerp(f, localPlayer.yBobO, localPlayer.yBob);
        RenderSystem.rotatef((localPlayer.getViewXRot(f) - g) * 0.1f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef((localPlayer.getViewYRot(f) - h) * 0.1f, 0.0f, 1.0f, 0.0f);
    }

    private float calculateMapTilt(float f) {
        float g = 1.0f - f / 45.0f + 0.1f;
        g = Mth.clamp(g, 0.0f, 1.0f);
        g = -Mth.cos(g * (float)Math.PI) * 0.5f + 0.5f;
        return g;
    }

    private void renderMapHands() {
        if (this.minecraft.player.isInvisible()) {
            return;
        }
        RenderSystem.disableCull();
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(90.0f, 0.0f, 1.0f, 0.0f);
        this.renderMapHand(HumanoidArm.RIGHT);
        this.renderMapHand(HumanoidArm.LEFT);
        RenderSystem.popMatrix();
        RenderSystem.enableCull();
    }

    private void renderMapHand(HumanoidArm humanoidArm) {
        this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
        Object entityRenderer = this.entityRenderDispatcher.getRenderer(this.minecraft.player);
        PlayerRenderer playerRenderer = (PlayerRenderer)entityRenderer;
        RenderSystem.pushMatrix();
        float f = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        RenderSystem.rotatef(92.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(45.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(f * -41.0f, 0.0f, 0.0f, 1.0f);
        RenderSystem.translatef(f * 0.3f, -1.1f, 0.45f);
        if (humanoidArm == HumanoidArm.RIGHT) {
            playerRenderer.renderRightHand(this.minecraft.player);
        } else {
            playerRenderer.renderLeftHand(this.minecraft.player);
        }
        RenderSystem.popMatrix();
    }

    private void renderOneHandedMap(float f, HumanoidArm humanoidArm, float g, ItemStack itemStack) {
        float h = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        RenderSystem.translatef(h * 0.125f, -0.125f, 0.0f);
        if (!this.minecraft.player.isInvisible()) {
            RenderSystem.pushMatrix();
            RenderSystem.rotatef(h * 10.0f, 0.0f, 0.0f, 1.0f);
            this.renderPlayerArm(f, g, humanoidArm);
            RenderSystem.popMatrix();
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef(h * 0.51f, -0.08f + f * -1.2f, -0.75f);
        float i = Mth.sqrt(g);
        float j = Mth.sin(i * (float)Math.PI);
        float k = -0.5f * j;
        float l = 0.4f * Mth.sin(i * ((float)Math.PI * 2));
        float m = -0.3f * Mth.sin(g * (float)Math.PI);
        RenderSystem.translatef(h * k, l - 0.3f * j, m);
        RenderSystem.rotatef(j * -45.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(h * j * -30.0f, 0.0f, 1.0f, 0.0f);
        this.renderMap(itemStack);
        RenderSystem.popMatrix();
    }

    private void renderTwoHandedMap(float f, float g, float h) {
        float i = Mth.sqrt(h);
        float j = -0.2f * Mth.sin(h * (float)Math.PI);
        float k = -0.4f * Mth.sin(i * (float)Math.PI);
        RenderSystem.translatef(0.0f, -j / 2.0f, k);
        float l = this.calculateMapTilt(f);
        RenderSystem.translatef(0.0f, 0.04f + g * -1.2f + l * -0.5f, -0.72f);
        RenderSystem.rotatef(l * -85.0f, 1.0f, 0.0f, 0.0f);
        this.renderMapHands();
        float m = Mth.sin(i * (float)Math.PI);
        RenderSystem.rotatef(m * 20.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.scalef(2.0f, 2.0f, 2.0f);
        this.renderMap(this.mainHandItem);
    }

    private void renderMap(ItemStack itemStack) {
        RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
        RenderSystem.scalef(0.38f, 0.38f, 0.38f);
        RenderSystem.disableLighting();
        this.minecraft.getTextureManager().bind(MAP_BACKGROUND_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.translatef(-0.5f, -0.5f, 0.0f);
        RenderSystem.scalef(0.0078125f, 0.0078125f, 0.0078125f);
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(-7.0, 135.0, 0.0).uv(0.0, 1.0).endVertex();
        bufferBuilder.vertex(135.0, 135.0, 0.0).uv(1.0, 1.0).endVertex();
        bufferBuilder.vertex(135.0, -7.0, 0.0).uv(1.0, 0.0).endVertex();
        bufferBuilder.vertex(-7.0, -7.0, 0.0).uv(0.0, 0.0).endVertex();
        tesselator.end();
        MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, this.minecraft.level);
        if (mapItemSavedData != null) {
            this.minecraft.gameRenderer.getMapRenderer().render(mapItemSavedData, false);
        }
        RenderSystem.enableLighting();
    }

    private void renderPlayerArm(float f, float g, HumanoidArm humanoidArm) {
        boolean bl = humanoidArm != HumanoidArm.LEFT;
        float h = bl ? 1.0f : -1.0f;
        float i = Mth.sqrt(g);
        float j = -0.3f * Mth.sin(i * (float)Math.PI);
        float k = 0.4f * Mth.sin(i * ((float)Math.PI * 2));
        float l = -0.4f * Mth.sin(g * (float)Math.PI);
        RenderSystem.translatef(h * (j + 0.64000005f), k + -0.6f + f * -0.6f, l + -0.71999997f);
        RenderSystem.rotatef(h * 45.0f, 0.0f, 1.0f, 0.0f);
        float m = Mth.sin(g * g * (float)Math.PI);
        float n = Mth.sin(i * (float)Math.PI);
        RenderSystem.rotatef(h * n * 70.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(h * m * -20.0f, 0.0f, 0.0f, 1.0f);
        LocalPlayer abstractClientPlayer = this.minecraft.player;
        this.minecraft.getTextureManager().bind(abstractClientPlayer.getSkinTextureLocation());
        RenderSystem.translatef(h * -1.0f, 3.6f, 3.5f);
        RenderSystem.rotatef(h * 120.0f, 0.0f, 0.0f, 1.0f);
        RenderSystem.rotatef(200.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(h * -135.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.translatef(h * 5.6f, 0.0f, 0.0f);
        PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(abstractClientPlayer);
        RenderSystem.disableCull();
        if (bl) {
            playerRenderer.renderRightHand(abstractClientPlayer);
        } else {
            playerRenderer.renderLeftHand(abstractClientPlayer);
        }
        RenderSystem.enableCull();
    }

    private void applyEatTransform(float f, HumanoidArm humanoidArm, ItemStack itemStack) {
        float i;
        float g = (float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0f;
        float h = g / (float)itemStack.getUseDuration();
        if (h < 0.8f) {
            i = Mth.abs(Mth.cos(g / 4.0f * (float)Math.PI) * 0.1f);
            RenderSystem.translatef(0.0f, i, 0.0f);
        }
        i = 1.0f - (float)Math.pow(h, 27.0);
        int j = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        RenderSystem.translatef(i * 0.6f * (float)j, i * -0.5f, i * 0.0f);
        RenderSystem.rotatef((float)j * i * 90.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(i * 10.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef((float)j * i * 30.0f, 0.0f, 0.0f, 1.0f);
    }

    private void applyItemArmAttackTransform(HumanoidArm humanoidArm, float f) {
        int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        float g = Mth.sin(f * f * (float)Math.PI);
        RenderSystem.rotatef((float)i * (45.0f + g * -20.0f), 0.0f, 1.0f, 0.0f);
        float h = Mth.sin(Mth.sqrt(f) * (float)Math.PI);
        RenderSystem.rotatef((float)i * h * -20.0f, 0.0f, 0.0f, 1.0f);
        RenderSystem.rotatef(h * -80.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef((float)i * -45.0f, 0.0f, 1.0f, 0.0f);
    }

    private void applyItemArmTransform(HumanoidArm humanoidArm, float f) {
        int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        RenderSystem.translatef((float)i * 0.56f, -0.52f + f * -0.6f, -0.72f);
    }

    public void render(float f) {
        ItemStack itemStack;
        LocalPlayer abstractClientPlayer = this.minecraft.player;
        float g = abstractClientPlayer.getAttackAnim(f);
        InteractionHand interactionHand = MoreObjects.firstNonNull(abstractClientPlayer.swingingArm, InteractionHand.MAIN_HAND);
        float h = Mth.lerp(f, abstractClientPlayer.xRotO, abstractClientPlayer.xRot);
        float i = Mth.lerp(f, abstractClientPlayer.yRotO, abstractClientPlayer.yRot);
        boolean bl = true;
        boolean bl2 = true;
        if (((LivingEntity)abstractClientPlayer).isUsingItem()) {
            ItemStack itemStack2;
            InteractionHand interactionHand2;
            itemStack = abstractClientPlayer.getUseItem();
            if (itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.CROSSBOW) {
                bl = ((LivingEntity)abstractClientPlayer).getUsedItemHand() == InteractionHand.MAIN_HAND;
                boolean bl3 = bl2 = !bl;
            }
            if ((interactionHand2 = ((LivingEntity)abstractClientPlayer).getUsedItemHand()) == InteractionHand.MAIN_HAND && (itemStack2 = abstractClientPlayer.getOffhandItem()).getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack2)) {
                bl2 = false;
            }
        } else {
            itemStack = abstractClientPlayer.getMainHandItem();
            ItemStack itemStack3 = abstractClientPlayer.getOffhandItem();
            if (itemStack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack)) {
                boolean bl4 = bl2 = !bl;
            }
            if (itemStack3.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack3)) {
                bl = !itemStack.isEmpty();
                bl2 = !bl;
            }
        }
        this.enableLight(h, i);
        this.setLightValue();
        this.setPlayerBob(f);
        RenderSystem.enableRescaleNormal();
        if (bl) {
            float j = interactionHand == InteractionHand.MAIN_HAND ? g : 0.0f;
            float k = 1.0f - Mth.lerp(f, this.oMainHandHeight, this.mainHandHeight);
            this.renderArmWithItem(abstractClientPlayer, f, h, InteractionHand.MAIN_HAND, j, this.mainHandItem, k);
        }
        if (bl2) {
            float j = interactionHand == InteractionHand.OFF_HAND ? g : 0.0f;
            float k = 1.0f - Mth.lerp(f, this.oOffHandHeight, this.offHandHeight);
            this.renderArmWithItem(abstractClientPlayer, f, h, InteractionHand.OFF_HAND, j, this.offHandItem, k);
        }
        RenderSystem.disableRescaleNormal();
        Lighting.turnOff();
    }

    public void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i) {
        boolean bl = interactionHand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidArm = bl ? abstractClientPlayer.getMainArm() : abstractClientPlayer.getMainArm().getOpposite();
        RenderSystem.pushMatrix();
        if (itemStack.isEmpty()) {
            if (bl && !abstractClientPlayer.isInvisible()) {
                this.renderPlayerArm(i, h, humanoidArm);
            }
        } else if (itemStack.getItem() == Items.FILLED_MAP) {
            if (bl && this.offHandItem.isEmpty()) {
                this.renderTwoHandedMap(g, i, h);
            } else {
                this.renderOneHandedMap(i, humanoidArm, h, itemStack);
            }
        } else if (itemStack.getItem() == Items.CROSSBOW) {
            int j;
            boolean bl2 = CrossbowItem.isCharged(itemStack);
            boolean bl3 = humanoidArm == HumanoidArm.RIGHT;
            int n = j = bl3 ? 1 : -1;
            if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
                this.applyItemArmTransform(humanoidArm, i);
                RenderSystem.translatef((float)j * -0.4785682f, -0.094387f, 0.05731531f);
                RenderSystem.rotatef(-11.935f, 1.0f, 0.0f, 0.0f);
                RenderSystem.rotatef((float)j * 65.3f, 0.0f, 1.0f, 0.0f);
                RenderSystem.rotatef((float)j * -9.785f, 0.0f, 0.0f, 1.0f);
                float k = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0f);
                float l = k / (float)CrossbowItem.getChargeDuration(itemStack);
                if (l > 1.0f) {
                    l = 1.0f;
                }
                if (l > 0.1f) {
                    float m = Mth.sin((k - 0.1f) * 1.3f);
                    float n2 = l - 0.1f;
                    float o = m * n2;
                    RenderSystem.translatef(o * 0.0f, o * 0.004f, o * 0.0f);
                }
                RenderSystem.translatef(l * 0.0f, l * 0.0f, l * 0.04f);
                RenderSystem.scalef(1.0f, 1.0f, 1.0f + l * 0.2f);
                RenderSystem.rotatef((float)j * 45.0f, 0.0f, -1.0f, 0.0f);
            } else {
                float k = -0.4f * Mth.sin(Mth.sqrt(h) * (float)Math.PI);
                float l = 0.2f * Mth.sin(Mth.sqrt(h) * ((float)Math.PI * 2));
                float m = -0.2f * Mth.sin(h * (float)Math.PI);
                RenderSystem.translatef((float)j * k, l, m);
                this.applyItemArmTransform(humanoidArm, i);
                this.applyItemArmAttackTransform(humanoidArm, h);
                if (bl2 && h < 0.001f) {
                    RenderSystem.translatef((float)j * -0.641864f, 0.0f, 0.0f);
                    RenderSystem.rotatef((float)j * 10.0f, 0.0f, 1.0f, 0.0f);
                }
            }
            this.renderItem(abstractClientPlayer, itemStack, bl3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !bl3);
        } else {
            boolean bl2;
            boolean bl3 = bl2 = humanoidArm == HumanoidArm.RIGHT;
            if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
                int p = bl2 ? 1 : -1;
                switch (itemStack.getUseAnimation()) {
                    case NONE: {
                        this.applyItemArmTransform(humanoidArm, i);
                        break;
                    }
                    case EAT: 
                    case DRINK: {
                        this.applyEatTransform(f, humanoidArm, itemStack);
                        this.applyItemArmTransform(humanoidArm, i);
                        break;
                    }
                    case BLOCK: {
                        this.applyItemArmTransform(humanoidArm, i);
                        break;
                    }
                    case BOW: {
                        this.applyItemArmTransform(humanoidArm, i);
                        RenderSystem.translatef((float)p * -0.2785682f, 0.18344387f, 0.15731531f);
                        RenderSystem.rotatef(-13.935f, 1.0f, 0.0f, 0.0f);
                        RenderSystem.rotatef((float)p * 35.3f, 0.0f, 1.0f, 0.0f);
                        RenderSystem.rotatef((float)p * -9.785f, 0.0f, 0.0f, 1.0f);
                        float q = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0f);
                        float k = q / 20.0f;
                        k = (k * k + k * 2.0f) / 3.0f;
                        if (k > 1.0f) {
                            k = 1.0f;
                        }
                        if (k > 0.1f) {
                            float l = Mth.sin((q - 0.1f) * 1.3f);
                            float m = k - 0.1f;
                            float n = l * m;
                            RenderSystem.translatef(n * 0.0f, n * 0.004f, n * 0.0f);
                        }
                        RenderSystem.translatef(k * 0.0f, k * 0.0f, k * 0.04f);
                        RenderSystem.scalef(1.0f, 1.0f, 1.0f + k * 0.2f);
                        RenderSystem.rotatef((float)p * 45.0f, 0.0f, -1.0f, 0.0f);
                        break;
                    }
                    case SPEAR: {
                        this.applyItemArmTransform(humanoidArm, i);
                        RenderSystem.translatef((float)p * -0.5f, 0.7f, 0.1f);
                        RenderSystem.rotatef(-55.0f, 1.0f, 0.0f, 0.0f);
                        RenderSystem.rotatef((float)p * 35.3f, 0.0f, 1.0f, 0.0f);
                        RenderSystem.rotatef((float)p * -9.785f, 0.0f, 0.0f, 1.0f);
                        float q = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0f);
                        float k = q / 10.0f;
                        if (k > 1.0f) {
                            k = 1.0f;
                        }
                        if (k > 0.1f) {
                            float l = Mth.sin((q - 0.1f) * 1.3f);
                            float m = k - 0.1f;
                            float n = l * m;
                            RenderSystem.translatef(n * 0.0f, n * 0.004f, n * 0.0f);
                        }
                        RenderSystem.translatef(0.0f, 0.0f, k * 0.2f);
                        RenderSystem.scalef(1.0f, 1.0f, 1.0f + k * 0.2f);
                        RenderSystem.rotatef((float)p * 45.0f, 0.0f, -1.0f, 0.0f);
                        break;
                    }
                }
            } else if (abstractClientPlayer.isAutoSpinAttack()) {
                this.applyItemArmTransform(humanoidArm, i);
                int p = bl2 ? 1 : -1;
                RenderSystem.translatef((float)p * -0.4f, 0.8f, 0.3f);
                RenderSystem.rotatef((float)p * 65.0f, 0.0f, 1.0f, 0.0f);
                RenderSystem.rotatef((float)p * -85.0f, 0.0f, 0.0f, 1.0f);
            } else {
                float r = -0.4f * Mth.sin(Mth.sqrt(h) * (float)Math.PI);
                float q = 0.2f * Mth.sin(Mth.sqrt(h) * ((float)Math.PI * 2));
                float k = -0.2f * Mth.sin(h * (float)Math.PI);
                int s = bl2 ? 1 : -1;
                RenderSystem.translatef((float)s * r, q, k);
                this.applyItemArmTransform(humanoidArm, i);
                this.applyItemArmAttackTransform(humanoidArm, h);
            }
            this.renderItem(abstractClientPlayer, itemStack, bl2 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !bl2);
        }
        RenderSystem.popMatrix();
    }

    public void renderScreenEffect(float f) {
        RenderSystem.disableAlphaTest();
        if (this.minecraft.player.isInWall()) {
            BlockState blockState = this.minecraft.level.getBlockState(new BlockPos(this.minecraft.player));
            LocalPlayer player = this.minecraft.player;
            for (int i = 0; i < 8; ++i) {
                double d = player.x + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
                double e = player.y + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f);
                double g = player.z + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
                BlockPos blockPos = new BlockPos(d, e + (double)player.getEyeHeight(), g);
                BlockState blockState2 = this.minecraft.level.getBlockState(blockPos);
                if (!blockState2.isViewBlocking(this.minecraft.level, blockPos)) continue;
                blockState = blockState2;
            }
            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                this.renderTex(this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState));
            }
        }
        if (!this.minecraft.player.isSpectator()) {
            if (this.minecraft.player.isUnderLiquid(FluidTags.WATER)) {
                this.renderWater(f);
            }
            if (this.minecraft.player.isOnFire()) {
                this.renderFire();
            }
        }
        RenderSystem.enableAlphaTest();
    }

    private void renderTex(TextureAtlasSprite textureAtlasSprite) {
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float f = 0.1f;
        RenderSystem.color4f(0.1f, 0.1f, 0.1f, 0.5f);
        RenderSystem.pushMatrix();
        float g = -1.0f;
        float h = 1.0f;
        float i = -1.0f;
        float j = 1.0f;
        float k = -0.5f;
        float l = textureAtlasSprite.getU0();
        float m = textureAtlasSprite.getU1();
        float n = textureAtlasSprite.getV0();
        float o = textureAtlasSprite.getV1();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(-1.0, -1.0, -0.5).uv(m, o).endVertex();
        bufferBuilder.vertex(1.0, -1.0, -0.5).uv(l, o).endVertex();
        bufferBuilder.vertex(1.0, 1.0, -0.5).uv(l, n).endVertex();
        bufferBuilder.vertex(-1.0, 1.0, -0.5).uv(m, n).endVertex();
        tesselator.end();
        RenderSystem.popMatrix();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderWater(float f) {
        this.minecraft.getTextureManager().bind(UNDERWATER_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float g = this.minecraft.player.getBrightness();
        RenderSystem.color4f(g, g, g, 0.1f);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.pushMatrix();
        float h = 4.0f;
        float i = -1.0f;
        float j = 1.0f;
        float k = -1.0f;
        float l = 1.0f;
        float m = -0.5f;
        float n = -this.minecraft.player.yRot / 64.0f;
        float o = this.minecraft.player.xRot / 64.0f;
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(-1.0, -1.0, -0.5).uv(4.0f + n, 4.0f + o).endVertex();
        bufferBuilder.vertex(1.0, -1.0, -0.5).uv(0.0f + n, 4.0f + o).endVertex();
        bufferBuilder.vertex(1.0, 1.0, -0.5).uv(0.0f + n, 0.0f + o).endVertex();
        bufferBuilder.vertex(-1.0, 1.0, -0.5).uv(4.0f + n, 0.0f + o).endVertex();
        tesselator.end();
        RenderSystem.popMatrix();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private void renderFire() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 0.9f);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        float f = 1.0f;
        for (int i = 0; i < 2; ++i) {
            RenderSystem.pushMatrix();
            TextureAtlasSprite textureAtlasSprite = this.minecraft.getTextureAtlas().getSprite(ModelBakery.FIRE_1);
            this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
            float g = textureAtlasSprite.getU0();
            float h = textureAtlasSprite.getU1();
            float j = textureAtlasSprite.getV0();
            float k = textureAtlasSprite.getV1();
            float l = -0.5f;
            float m = 0.5f;
            float n = -0.5f;
            float o = 0.5f;
            float p = -0.5f;
            RenderSystem.translatef((float)(-(i * 2 - 1)) * 0.24f, -0.3f, 0.0f);
            RenderSystem.rotatef((float)(i * 2 - 1) * 10.0f, 0.0f, 1.0f, 0.0f);
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(-0.5, -0.5, -0.5).uv(h, k).endVertex();
            bufferBuilder.vertex(0.5, -0.5, -0.5).uv(g, k).endVertex();
            bufferBuilder.vertex(0.5, 0.5, -0.5).uv(g, j).endVertex();
            bufferBuilder.vertex(-0.5, 0.5, -0.5).uv(h, j).endVertex();
            tesselator.end();
            RenderSystem.popMatrix();
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }

    public void tick() {
        this.oMainHandHeight = this.mainHandHeight;
        this.oOffHandHeight = this.offHandHeight;
        LocalPlayer localPlayer = this.minecraft.player;
        ItemStack itemStack = localPlayer.getMainHandItem();
        ItemStack itemStack2 = localPlayer.getOffhandItem();
        if (localPlayer.isHandsBusy()) {
            this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4f, 0.0f, 1.0f);
            this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4f, 0.0f, 1.0f);
        } else {
            float f = localPlayer.getAttackStrengthScale(1.0f);
            this.mainHandHeight += Mth.clamp((Objects.equals(this.mainHandItem, itemStack) ? f * f * f : 0.0f) - this.mainHandHeight, -0.4f, 0.4f);
            this.offHandHeight += Mth.clamp((float)(Objects.equals(this.offHandItem, itemStack2) ? 1 : 0) - this.offHandHeight, -0.4f, 0.4f);
        }
        if (this.mainHandHeight < 0.1f) {
            this.mainHandItem = itemStack;
        }
        if (this.offHandHeight < 0.1f) {
            this.offHandItem = itemStack2;
        }
    }

    public void itemUsed(InteractionHand interactionHand) {
        if (interactionHand == InteractionHand.MAIN_HAND) {
            this.mainHandHeight = 0.0f;
        } else {
            this.offHandHeight = 0.0f;
        }
    }
}

