/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

@Environment(value=EnvType.CLIENT)
public class CustomHeadLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        float n;
        boolean bl;
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        poseStack.pushPose();
        if (((Entity)livingEntity).isCrouching()) {
            poseStack.translate(0.0, 0.2f, 0.0);
        }
        boolean bl2 = bl = livingEntity instanceof Villager || livingEntity instanceof ZombieVillager;
        if (((LivingEntity)livingEntity).isBaby() && !(livingEntity instanceof Villager)) {
            n = 2.0f;
            float o = 1.4f;
            poseStack.translate(0.0, 0.5f * m, 0.0);
            poseStack.scale(0.7f, 0.7f, 0.7f);
            poseStack.translate(0.0, 16.0f * m, 0.0);
        }
        ((HeadedModel)this.getParentModel()).getHead().translateAndRotate(poseStack, 0.0625f);
        if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
            n = 1.1875f;
            poseStack.scale(1.1875f, -1.1875f, -1.1875f);
            if (bl) {
                poseStack.translate(0.0, 0.0625, 0.0);
            }
            GameProfile gameProfile = null;
            if (itemStack.hasTag()) {
                String string;
                CompoundTag compoundTag = itemStack.getTag();
                if (compoundTag.contains("SkullOwner", 10)) {
                    gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
                } else if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(string = compoundTag.getString("SkullOwner"))) {
                    gameProfile = SkullBlockEntity.updateGameprofile(new GameProfile(null, string));
                    compoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfile));
                }
            }
            poseStack.translate(-0.5, 0.0, -0.5);
            SkullBlockRenderer.renderSkull(null, 180.0f, ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType(), gameProfile, f, poseStack, multiBufferSource, i);
        } else if (!(item instanceof ArmorItem) || ((ArmorItem)item).getSlot() != EquipmentSlot.HEAD) {
            n = 0.625f;
            poseStack.translate(0.0, -0.25, 0.0);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
            poseStack.scale(0.625f, -0.625f, -0.625f);
            if (bl) {
                poseStack.translate(0.0, 0.1875, 0.0);
            }
            Minecraft.getInstance().getItemInHandRenderer().renderItem((LivingEntity)livingEntity, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource);
        }
        poseStack.popPose();
    }
}

