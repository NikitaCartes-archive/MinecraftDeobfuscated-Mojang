/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.HeadedModel;
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
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        float m;
        boolean bl;
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        RenderSystem.pushMatrix();
        if (((Entity)livingEntity).isCrouching()) {
            RenderSystem.translatef(0.0f, 0.2f, 0.0f);
        }
        boolean bl2 = bl = livingEntity instanceof Villager || livingEntity instanceof ZombieVillager;
        if (((LivingEntity)livingEntity).isBaby() && !(livingEntity instanceof Villager)) {
            m = 2.0f;
            float n = 1.4f;
            RenderSystem.translatef(0.0f, 0.5f * l, 0.0f);
            RenderSystem.scalef(0.7f, 0.7f, 0.7f);
            RenderSystem.translatef(0.0f, 16.0f * l, 0.0f);
        }
        ((HeadedModel)this.getParentModel()).translateToHead(0.0625f);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
            m = 1.1875f;
            RenderSystem.scalef(1.1875f, -1.1875f, -1.1875f);
            if (bl) {
                RenderSystem.translatef(0.0f, 0.0625f, 0.0f);
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
            SkullBlockRenderer.instance.renderSkull(-0.5f, 0.0f, -0.5f, null, 180.0f, ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType(), gameProfile, -1, f);
        } else if (!(item instanceof ArmorItem) || ((ArmorItem)item).getSlot() != EquipmentSlot.HEAD) {
            m = 0.625f;
            RenderSystem.translatef(0.0f, -0.25f, 0.0f);
            RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
            RenderSystem.scalef(0.625f, -0.625f, -0.625f);
            if (bl) {
                RenderSystem.translatef(0.0f, 0.1875f, 0.0f);
            }
            Minecraft.getInstance().getItemInHandRenderer().renderItem((LivingEntity)livingEntity, itemStack, ItemTransforms.TransformType.HEAD);
        }
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

