package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

@Environment(EnvType.CLIENT)
public class CustomHeadLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
	private final float scaleX;
	private final float scaleY;
	private final float scaleZ;
	private final Map<SkullBlock.Type, SkullModelBase> skullModels;
	private final boolean barrels;

	public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet) {
		this(renderLayerParent, entityModelSet, false);
	}

	public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet, float f, float g, float h) {
		this(renderLayerParent, entityModelSet, f, g, h, false);
	}

	public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet, boolean bl) {
		this(renderLayerParent, entityModelSet, 1.0F, 1.0F, 1.0F, bl);
	}

	public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet, float f, float g, float h, boolean bl) {
		super(renderLayerParent);
		this.barrels = bl;
		this.scaleX = f;
		this.scaleY = g;
		this.scaleZ = h;
		this.skullModels = SkullBlockRenderer.createSkullRenderers(entityModelSet);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		if (!itemStack.isEmpty() && (!this.barrels || !itemStack.is(Items.BARREL))) {
			Item item = itemStack.getItem();
			poseStack.pushPose();
			poseStack.scale(this.scaleX, this.scaleY, this.scaleZ);
			boolean bl = livingEntity instanceof Villager || livingEntity instanceof ZombieVillager;
			if (livingEntity.isBaby() && !(livingEntity instanceof Villager)) {
				float m = 2.0F;
				float n = 1.4F;
				poseStack.translate(0.0, 0.03125, 0.0);
				poseStack.scale(0.7F, 0.7F, 0.7F);
				poseStack.translate(0.0, 1.0, 0.0);
			}

			this.getParentModel().getHead().translateAndRotate(poseStack);
			if (this.getParentModel() instanceof QuadrupedModel) {
				poseStack.translate(0.0, 0.25, -0.25125);
				if (livingEntity instanceof Panda) {
					poseStack.scale(1.333F, 1.333F, 1.333F);
				}
			}

			if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
				float m = 1.1875F;
				poseStack.scale(1.1875F, -1.1875F, -1.1875F);
				if (bl) {
					poseStack.translate(0.0, 0.0625, 0.0);
				}

				GameProfile gameProfile = null;
				if (itemStack.hasTag()) {
					CompoundTag compoundTag = itemStack.getTag();
					if (compoundTag.contains("SkullOwner", 10)) {
						gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
					}
				}

				poseStack.translate(-0.5, 0.0, -0.5);
				SkullBlock.Type type = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType();
				SkullModelBase skullModelBase = (SkullModelBase)this.skullModels.get(type);
				RenderType renderType = SkullBlockRenderer.getRenderType(type, gameProfile);
				SkullBlockRenderer.renderSkull(null, 180.0F, f, poseStack, multiBufferSource, i, skullModelBase, renderType);
			} else if (!(item instanceof ArmorItem) || ((ArmorItem)item).getSlot() != EquipmentSlot.HEAD) {
				translateToHead(poseStack, bl);
				Minecraft.getInstance()
					.getItemInHandRenderer()
					.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, i);
			}

			poseStack.popPose();
		}
	}

	public static void translateToHead(PoseStack poseStack, boolean bl) {
		float f = 0.625F;
		poseStack.translate(0.0, -0.25, 0.0);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		poseStack.scale(0.625F, -0.625F, -0.625F);
		if (bl) {
			poseStack.translate(0.0, 0.1875, 0.0);
		}
	}
}
