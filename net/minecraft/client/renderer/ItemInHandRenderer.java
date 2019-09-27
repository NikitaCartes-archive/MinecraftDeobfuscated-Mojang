/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Environment(value=EnvType.CLIENT)
public class ItemInHandRenderer {
    private static final ResourceLocation MAP_BACKGROUND_LOCATION = new ResourceLocation("textures/map/map_background.png");
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

    public void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        if (itemStack.isEmpty()) {
            return;
        }
        this.itemRenderer.renderStatic(livingEntity, itemStack, transformType, bl, poseStack, multiBufferSource, livingEntity.level, livingEntity.getLightColor());
    }

    private float calculateMapTilt(float f) {
        float g = 1.0f - f / 45.0f + 0.1f;
        g = Mth.clamp(g, 0.0f, 1.0f);
        g = -Mth.cos(g * (float)Math.PI) * 0.5f + 0.5f;
        return g;
    }

    private void renderMapHand(PoseStack poseStack, MultiBufferSource multiBufferSource, HumanoidArm humanoidArm) {
        this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
        PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(this.minecraft.player);
        poseStack.pushPose();
        float f = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.mulPose(Vector3f.YP.rotation(92.0f, true));
        poseStack.mulPose(Vector3f.XP.rotation(45.0f, true));
        poseStack.mulPose(Vector3f.ZP.rotation(f * -41.0f, true));
        poseStack.translate(f * 0.3f, -1.1f, 0.45f);
        if (humanoidArm == HumanoidArm.RIGHT) {
            playerRenderer.renderRightHand(poseStack, multiBufferSource, this.minecraft.player);
        } else {
            playerRenderer.renderLeftHand(poseStack, multiBufferSource, this.minecraft.player);
        }
        poseStack.popPose();
    }

    private void renderOneHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, HumanoidArm humanoidArm, float g, ItemStack itemStack) {
        float h = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.translate(h * 0.125f, -0.125, 0.0);
        if (!this.minecraft.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotation(h * 10.0f, true));
            this.renderPlayerArm(poseStack, multiBufferSource, f, g, humanoidArm);
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(h * 0.51f, -0.08f + f * -1.2f, -0.75);
        float i = Mth.sqrt(g);
        float j = Mth.sin(i * (float)Math.PI);
        float k = -0.5f * j;
        float l = 0.4f * Mth.sin(i * ((float)Math.PI * 2));
        float m = -0.3f * Mth.sin(g * (float)Math.PI);
        poseStack.translate(h * k, l - 0.3f * j, m);
        poseStack.mulPose(Vector3f.XP.rotation(j * -45.0f, true));
        poseStack.mulPose(Vector3f.YP.rotation(h * j * -30.0f, true));
        this.renderMap(poseStack, multiBufferSource, itemStack);
        poseStack.popPose();
    }

    private void renderTwoHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, float g, float h) {
        float i = Mth.sqrt(h);
        float j = -0.2f * Mth.sin(h * (float)Math.PI);
        float k = -0.4f * Mth.sin(i * (float)Math.PI);
        poseStack.translate(0.0, -j / 2.0f, k);
        float l = this.calculateMapTilt(f);
        poseStack.translate(0.0, 0.04f + g * -1.2f + l * -0.5f, -0.72f);
        poseStack.mulPose(Vector3f.XP.rotation(l * -85.0f, true));
        if (!this.minecraft.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotation(90.0f, true));
            this.renderMapHand(poseStack, multiBufferSource, HumanoidArm.RIGHT);
            this.renderMapHand(poseStack, multiBufferSource, HumanoidArm.LEFT);
            poseStack.popPose();
        }
        float m = Mth.sin(i * (float)Math.PI);
        poseStack.mulPose(Vector3f.XP.rotation(m * 20.0f, true));
        poseStack.scale(2.0f, 2.0f, 2.0f);
        this.renderMap(poseStack, multiBufferSource, this.mainHandItem);
    }

    private void renderMap(PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack) {
        poseStack.mulPose(Vector3f.YP.rotation(180.0f, true));
        poseStack.mulPose(Vector3f.ZP.rotation(180.0f, true));
        poseStack.scale(0.38f, 0.38f, 0.38f);
        this.minecraft.getTextureManager().bind(MAP_BACKGROUND_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        poseStack.translate(-0.5, -0.5, 0.0);
        poseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = poseStack.getPose();
        bufferBuilder.vertex(matrix4f, -7.0f, 135.0f, 0.0f).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, 135.0f, 135.0f, 0.0f).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f, 135.0f, -7.0f, 0.0f).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(matrix4f, -7.0f, -7.0f, 0.0f).uv(0.0f, 0.0f).endVertex();
        tesselator.end();
        MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, this.minecraft.level);
        if (mapItemSavedData != null) {
            this.minecraft.gameRenderer.getMapRenderer().render(poseStack, multiBufferSource, mapItemSavedData, false);
        }
    }

    private void renderPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, float g, HumanoidArm humanoidArm) {
        boolean bl = humanoidArm != HumanoidArm.LEFT;
        float h = bl ? 1.0f : -1.0f;
        float i = Mth.sqrt(g);
        float j = -0.3f * Mth.sin(i * (float)Math.PI);
        float k = 0.4f * Mth.sin(i * ((float)Math.PI * 2));
        float l = -0.4f * Mth.sin(g * (float)Math.PI);
        poseStack.translate(h * (j + 0.64000005f), k + -0.6f + f * -0.6f, l + -0.71999997f);
        poseStack.mulPose(Vector3f.YP.rotation(h * 45.0f, true));
        float m = Mth.sin(g * g * (float)Math.PI);
        float n = Mth.sin(i * (float)Math.PI);
        poseStack.mulPose(Vector3f.YP.rotation(h * n * 70.0f, true));
        poseStack.mulPose(Vector3f.ZP.rotation(h * m * -20.0f, true));
        LocalPlayer abstractClientPlayer = this.minecraft.player;
        this.minecraft.getTextureManager().bind(abstractClientPlayer.getSkinTextureLocation());
        poseStack.translate(h * -1.0f, 3.6f, 3.5);
        poseStack.mulPose(Vector3f.ZP.rotation(h * 120.0f, true));
        poseStack.mulPose(Vector3f.XP.rotation(200.0f, true));
        poseStack.mulPose(Vector3f.YP.rotation(h * -135.0f, true));
        poseStack.translate(h * 5.6f, 0.0, 0.0);
        PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(abstractClientPlayer);
        if (bl) {
            playerRenderer.renderRightHand(poseStack, multiBufferSource, abstractClientPlayer);
        } else {
            playerRenderer.renderLeftHand(poseStack, multiBufferSource, abstractClientPlayer);
        }
    }

    private void applyEatTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack) {
        float i;
        float g = (float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0f;
        float h = g / (float)itemStack.getUseDuration();
        if (h < 0.8f) {
            i = Mth.abs(Mth.cos(g / 4.0f * (float)Math.PI) * 0.1f);
            poseStack.translate(0.0, i, 0.0);
        }
        i = 1.0f - (float)Math.pow(h, 27.0);
        int j = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(i * 0.6f * (float)j, i * -0.5f, i * 0.0f);
        poseStack.mulPose(Vector3f.YP.rotation((float)j * i * 90.0f, true));
        poseStack.mulPose(Vector3f.XP.rotation(i * 10.0f, true));
        poseStack.mulPose(Vector3f.ZP.rotation((float)j * i * 30.0f, true));
    }

    private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {
        int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        float g = Mth.sin(f * f * (float)Math.PI);
        poseStack.mulPose(Vector3f.YP.rotation((float)i * (45.0f + g * -20.0f), true));
        float h = Mth.sin(Mth.sqrt(f) * (float)Math.PI);
        poseStack.mulPose(Vector3f.ZP.rotation((float)i * h * -20.0f, true));
        poseStack.mulPose(Vector3f.XP.rotation(h * -80.0f, true));
        poseStack.mulPose(Vector3f.YP.rotation((float)i * -45.0f, true));
    }

    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {
        int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)i * 0.56f, -0.52f + f * -0.6f, -0.72f);
    }

    public void renderHandsWithItems(float f, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        float l;
        ItemStack itemStack;
        LocalPlayer localPlayer = this.minecraft.player;
        float g = localPlayer.getAttackAnim(f);
        InteractionHand interactionHand = MoreObjects.firstNonNull(localPlayer.swingingArm, InteractionHand.MAIN_HAND);
        float h = Mth.lerp(f, localPlayer.xRotO, localPlayer.xRot);
        boolean bl = true;
        boolean bl2 = true;
        if (localPlayer.isUsingItem()) {
            ItemStack itemStack2;
            InteractionHand interactionHand2;
            itemStack = localPlayer.getUseItem();
            if (itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.CROSSBOW) {
                bl = localPlayer.getUsedItemHand() == InteractionHand.MAIN_HAND;
                boolean bl3 = bl2 = !bl;
            }
            if ((interactionHand2 = localPlayer.getUsedItemHand()) == InteractionHand.MAIN_HAND && (itemStack2 = localPlayer.getOffhandItem()).getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack2)) {
                bl2 = false;
            }
        } else {
            itemStack = localPlayer.getMainHandItem();
            ItemStack itemStack3 = localPlayer.getOffhandItem();
            if (itemStack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack)) {
                boolean bl4 = bl2 = !bl;
            }
            if (itemStack3.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack3)) {
                bl = !itemStack.isEmpty();
                bl2 = !bl;
            }
        }
        float i = Mth.lerp(f, localPlayer.xBobO, localPlayer.xBob);
        float j = Mth.lerp(f, localPlayer.yBobO, localPlayer.yBob);
        poseStack.mulPose(Vector3f.XP.rotation((localPlayer.getViewXRot(f) - i) * 0.1f, true));
        poseStack.mulPose(Vector3f.YP.rotation((localPlayer.getViewYRot(f) - j) * 0.1f, true));
        if (bl) {
            float k = interactionHand == InteractionHand.MAIN_HAND ? g : 0.0f;
            l = 1.0f - Mth.lerp(f, this.oMainHandHeight, this.mainHandHeight);
            this.renderArmWithItem(localPlayer, f, h, InteractionHand.MAIN_HAND, k, this.mainHandItem, l, poseStack, bufferSource);
        }
        if (bl2) {
            float k = interactionHand == InteractionHand.OFF_HAND ? g : 0.0f;
            l = 1.0f - Mth.lerp(f, this.oOffHandHeight, this.offHandHeight);
            this.renderArmWithItem(localPlayer, f, h, InteractionHand.OFF_HAND, k, this.offHandItem, l, poseStack, bufferSource);
        }
        bufferSource.endBatch();
    }

    private void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        boolean bl = interactionHand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidArm = bl ? abstractClientPlayer.getMainArm() : abstractClientPlayer.getMainArm().getOpposite();
        poseStack.pushPose();
        if (itemStack.isEmpty()) {
            if (bl && !abstractClientPlayer.isInvisible()) {
                this.renderPlayerArm(poseStack, multiBufferSource, i, h, humanoidArm);
            }
        } else if (itemStack.getItem() == Items.FILLED_MAP) {
            if (bl && this.offHandItem.isEmpty()) {
                this.renderTwoHandedMap(poseStack, multiBufferSource, g, i, h);
            } else {
                this.renderOneHandedMap(poseStack, multiBufferSource, i, humanoidArm, h, itemStack);
            }
        } else if (itemStack.getItem() == Items.CROSSBOW) {
            int j;
            boolean bl2 = CrossbowItem.isCharged(itemStack);
            boolean bl3 = humanoidArm == HumanoidArm.RIGHT;
            int n = j = bl3 ? 1 : -1;
            if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
                this.applyItemArmTransform(poseStack, humanoidArm, i);
                poseStack.translate((float)j * -0.4785682f, -0.094387f, 0.05731530860066414);
                poseStack.mulPose(Vector3f.XP.rotation(-11.935f, true));
                poseStack.mulPose(Vector3f.YP.rotation((float)j * 65.3f, true));
                poseStack.mulPose(Vector3f.ZP.rotation((float)j * -9.785f, true));
                float k = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0f);
                float l = k / (float)CrossbowItem.getChargeDuration(itemStack);
                if (l > 1.0f) {
                    l = 1.0f;
                }
                if (l > 0.1f) {
                    float m = Mth.sin((k - 0.1f) * 1.3f);
                    float n2 = l - 0.1f;
                    float o = m * n2;
                    poseStack.translate(o * 0.0f, o * 0.004f, o * 0.0f);
                }
                poseStack.translate(l * 0.0f, l * 0.0f, l * 0.04f);
                poseStack.scale(1.0f, 1.0f, 1.0f + l * 0.2f);
                poseStack.mulPose(Vector3f.YN.rotation((float)j * 45.0f, true));
            } else {
                float k = -0.4f * Mth.sin(Mth.sqrt(h) * (float)Math.PI);
                float l = 0.2f * Mth.sin(Mth.sqrt(h) * ((float)Math.PI * 2));
                float m = -0.2f * Mth.sin(h * (float)Math.PI);
                poseStack.translate((float)j * k, l, m);
                this.applyItemArmTransform(poseStack, humanoidArm, i);
                this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
                if (bl2 && h < 0.001f) {
                    poseStack.translate((float)j * -0.641864f, 0.0, 0.0);
                    poseStack.mulPose(Vector3f.YP.rotation((float)j * 10.0f, true));
                }
            }
            this.renderItem(abstractClientPlayer, itemStack, bl3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !bl3, poseStack, multiBufferSource);
        } else {
            boolean bl2;
            boolean bl3 = bl2 = humanoidArm == HumanoidArm.RIGHT;
            if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
                int p = bl2 ? 1 : -1;
                switch (itemStack.getUseAnimation()) {
                    case NONE: {
                        this.applyItemArmTransform(poseStack, humanoidArm, i);
                        break;
                    }
                    case EAT: 
                    case DRINK: {
                        this.applyEatTransform(poseStack, f, humanoidArm, itemStack);
                        this.applyItemArmTransform(poseStack, humanoidArm, i);
                        break;
                    }
                    case BLOCK: {
                        this.applyItemArmTransform(poseStack, humanoidArm, i);
                        break;
                    }
                    case BOW: {
                        this.applyItemArmTransform(poseStack, humanoidArm, i);
                        poseStack.translate((float)p * -0.2785682f, 0.18344387412071228, 0.15731531381607056);
                        poseStack.mulPose(Vector3f.XP.rotation(-13.935f, true));
                        poseStack.mulPose(Vector3f.YP.rotation((float)p * 35.3f, true));
                        poseStack.mulPose(Vector3f.ZP.rotation((float)p * -9.785f, true));
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
                            poseStack.translate(n * 0.0f, n * 0.004f, n * 0.0f);
                        }
                        poseStack.translate(k * 0.0f, k * 0.0f, k * 0.04f);
                        poseStack.scale(1.0f, 1.0f, 1.0f + k * 0.2f);
                        poseStack.mulPose(Vector3f.YN.rotation((float)p * 45.0f, true));
                        break;
                    }
                    case SPEAR: {
                        this.applyItemArmTransform(poseStack, humanoidArm, i);
                        poseStack.translate((float)p * -0.5f, 0.7f, 0.1f);
                        poseStack.mulPose(Vector3f.XP.rotation(-55.0f, true));
                        poseStack.mulPose(Vector3f.YP.rotation((float)p * 35.3f, true));
                        poseStack.mulPose(Vector3f.ZP.rotation((float)p * -9.785f, true));
                        float q = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0f);
                        float k = q / 10.0f;
                        if (k > 1.0f) {
                            k = 1.0f;
                        }
                        if (k > 0.1f) {
                            float l = Mth.sin((q - 0.1f) * 1.3f);
                            float m = k - 0.1f;
                            float n = l * m;
                            poseStack.translate(n * 0.0f, n * 0.004f, n * 0.0f);
                        }
                        poseStack.translate(0.0, 0.0, k * 0.2f);
                        poseStack.scale(1.0f, 1.0f, 1.0f + k * 0.2f);
                        poseStack.mulPose(Vector3f.YN.rotation((float)p * 45.0f, true));
                        break;
                    }
                }
            } else if (abstractClientPlayer.isAutoSpinAttack()) {
                this.applyItemArmTransform(poseStack, humanoidArm, i);
                int p = bl2 ? 1 : -1;
                poseStack.translate((float)p * -0.4f, 0.8f, 0.3f);
                poseStack.mulPose(Vector3f.YP.rotation((float)p * 65.0f, true));
                poseStack.mulPose(Vector3f.ZP.rotation((float)p * -85.0f, true));
            } else {
                float r = -0.4f * Mth.sin(Mth.sqrt(h) * (float)Math.PI);
                float q = 0.2f * Mth.sin(Mth.sqrt(h) * ((float)Math.PI * 2));
                float k = -0.2f * Mth.sin(h * (float)Math.PI);
                int s = bl2 ? 1 : -1;
                poseStack.translate((float)s * r, q, k);
                this.applyItemArmTransform(poseStack, humanoidArm, i);
                this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
            }
            this.renderItem(abstractClientPlayer, itemStack, bl2 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !bl2, poseStack, multiBufferSource);
        }
        poseStack.popPose();
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

