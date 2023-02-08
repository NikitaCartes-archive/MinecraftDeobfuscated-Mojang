/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

@Environment(value=EnvType.CLIENT)
public class CustomHeadLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemInHandRenderer itemInHandRenderer;

    public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet, ItemInHandRenderer itemInHandRenderer) {
        this(renderLayerParent, entityModelSet, 1.0f, 1.0f, 1.0f, itemInHandRenderer);
    }

    public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet, float f, float g, float h, ItemInHandRenderer itemInHandRenderer) {
        super(renderLayerParent);
        this.scaleX = f;
        this.scaleY = g;
        this.scaleZ = h;
        this.skullModels = SkullBlockRenderer.createSkullRenderers(entityModelSet);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        ArmorItem armorItem;
        float n;
        boolean bl;
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        poseStack.pushPose();
        poseStack.scale(this.scaleX, this.scaleY, this.scaleZ);
        boolean bl2 = bl = livingEntity instanceof Villager || livingEntity instanceof ZombieVillager;
        if (((LivingEntity)livingEntity).isBaby() && !(livingEntity instanceof Villager)) {
            float m = 2.0f;
            n = 1.4f;
            poseStack.translate(0.0f, 0.03125f, 0.0f);
            poseStack.scale(0.7f, 0.7f, 0.7f);
            poseStack.translate(0.0f, 1.0f, 0.0f);
        }
        ((HeadedModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
            CompoundTag compoundTag;
            n = 1.1875f;
            poseStack.scale(1.1875f, -1.1875f, -1.1875f);
            if (bl) {
                poseStack.translate(0.0f, 0.0625f, 0.0f);
            }
            GameProfile gameProfile = null;
            if (itemStack.hasTag() && (compoundTag = itemStack.getTag()).contains("SkullOwner", 10)) {
                gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
            }
            poseStack.translate(-0.5, 0.0, -0.5);
            SkullBlock.Type type = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType();
            SkullModelBase skullModelBase = this.skullModels.get(type);
            RenderType renderType = SkullBlockRenderer.getRenderType(type, gameProfile);
            SkullBlockRenderer.renderSkull(null, 180.0f, f, poseStack, multiBufferSource, i, skullModelBase, renderType);
        } else if (!(item instanceof ArmorItem) || (armorItem = (ArmorItem)item).getEquipmentSlot() != EquipmentSlot.HEAD) {
            CustomHeadLayer.translateToHead(poseStack, bl);
            this.itemInHandRenderer.renderItem((LivingEntity)livingEntity, itemStack, ItemDisplayContext.HEAD, false, poseStack, multiBufferSource, i);
        }
        poseStack.popPose();
    }

    public static void translateToHead(PoseStack poseStack, boolean bl) {
        float f = 0.625f;
        poseStack.translate(0.0f, -0.25f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
        if (bl) {
            poseStack.translate(0.0f, 0.1875f, 0.0f);
        }
    }
}

