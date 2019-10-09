package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Environment(EnvType.CLIENT)
public class ItemInHandRenderer {
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

	public void renderItem(
		LivingEntity livingEntity,
		ItemStack itemStack,
		ItemTransforms.TransformType transformType,
		boolean bl,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource
	) {
		if (!itemStack.isEmpty()) {
			this.itemRenderer
				.renderStatic(
					livingEntity, itemStack, transformType, bl, poseStack, multiBufferSource, livingEntity.level, livingEntity.getLightColor(), OverlayTexture.NO_OVERLAY
				);
		}
	}

	private float calculateMapTilt(float f) {
		float g = 1.0F - f / 45.0F + 0.1F;
		g = Mth.clamp(g, 0.0F, 1.0F);
		return -Mth.cos(g * (float) Math.PI) * 0.5F + 0.5F;
	}

	private void renderMapHand(PoseStack poseStack, MultiBufferSource multiBufferSource, HumanoidArm humanoidArm) {
		this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
		PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.minecraft.player);
		poseStack.pushPose();
		float f = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		poseStack.mulPose(Vector3f.YP.rotationDegrees(92.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * -41.0F));
		poseStack.translate((double)(f * 0.3F), -1.1F, 0.45F);
		if (humanoidArm == HumanoidArm.RIGHT) {
			playerRenderer.renderRightHand(poseStack, multiBufferSource, this.minecraft.player);
		} else {
			playerRenderer.renderLeftHand(poseStack, multiBufferSource, this.minecraft.player);
		}

		poseStack.popPose();
	}

	private void renderOneHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, HumanoidArm humanoidArm, float g, ItemStack itemStack) {
		float h = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		poseStack.translate((double)(h * 0.125F), -0.125, 0.0);
		if (!this.minecraft.player.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(h * 10.0F));
			this.renderPlayerArm(poseStack, multiBufferSource, f, g, humanoidArm);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate((double)(h * 0.51F), (double)(-0.08F + f * -1.2F), -0.75);
		float i = Mth.sqrt(g);
		float j = Mth.sin(i * (float) Math.PI);
		float k = -0.5F * j;
		float l = 0.4F * Mth.sin(i * (float) (Math.PI * 2));
		float m = -0.3F * Mth.sin(g * (float) Math.PI);
		poseStack.translate((double)(h * k), (double)(l - 0.3F * j), (double)m);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(j * -45.0F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * j * -30.0F));
		this.renderMap(poseStack, multiBufferSource, itemStack);
		poseStack.popPose();
	}

	private void renderTwoHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, float g, float h) {
		float i = Mth.sqrt(h);
		float j = -0.2F * Mth.sin(h * (float) Math.PI);
		float k = -0.4F * Mth.sin(i * (float) Math.PI);
		poseStack.translate(0.0, (double)(-j / 2.0F), (double)k);
		float l = this.calculateMapTilt(f);
		poseStack.translate(0.0, (double)(0.04F + g * -1.2F + l * -0.5F), -0.72F);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(l * -85.0F));
		if (!this.minecraft.player.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
			this.renderMapHand(poseStack, multiBufferSource, HumanoidArm.RIGHT);
			this.renderMapHand(poseStack, multiBufferSource, HumanoidArm.LEFT);
			poseStack.popPose();
		}

		float m = Mth.sin(i * (float) Math.PI);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(m * 20.0F));
		poseStack.scale(2.0F, 2.0F, 2.0F);
		this.renderMap(poseStack, multiBufferSource, this.mainHandItem);
	}

	private void renderMap(PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack) {
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		poseStack.scale(0.38F, 0.38F, 0.38F);
		poseStack.translate(-0.5, -0.5, 0.0);
		poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.text(MapRenderer.MAP_BACKGROUND_LOCATION));
		Matrix4f matrix4f = poseStack.getPose();
		vertexConsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(15728880).endVertex();
		vertexConsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(15728880).endVertex();
		vertexConsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(15728880).endVertex();
		vertexConsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(15728880).endVertex();
		MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, this.minecraft.level);
		if (mapItemSavedData != null) {
			this.minecraft.gameRenderer.getMapRenderer().render(poseStack, multiBufferSource, mapItemSavedData, false, 15728880);
		}
	}

	private void renderPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, float g, HumanoidArm humanoidArm) {
		boolean bl = humanoidArm != HumanoidArm.LEFT;
		float h = bl ? 1.0F : -1.0F;
		float i = Mth.sqrt(g);
		float j = -0.3F * Mth.sin(i * (float) Math.PI);
		float k = 0.4F * Mth.sin(i * (float) (Math.PI * 2));
		float l = -0.4F * Mth.sin(g * (float) Math.PI);
		poseStack.translate((double)(h * (j + 0.64000005F)), (double)(k + -0.6F + f * -0.6F), (double)(l + -0.71999997F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * 45.0F));
		float m = Mth.sin(g * g * (float) Math.PI);
		float n = Mth.sin(i * (float) Math.PI);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * n * 70.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(h * m * -20.0F));
		AbstractClientPlayer abstractClientPlayer = this.minecraft.player;
		this.minecraft.getTextureManager().bind(abstractClientPlayer.getSkinTextureLocation());
		poseStack.translate((double)(h * -1.0F), 3.6F, 3.5);
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(h * 120.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * -135.0F));
		poseStack.translate((double)(h * 5.6F), 0.0, 0.0);
		PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(abstractClientPlayer);
		if (bl) {
			playerRenderer.renderRightHand(poseStack, multiBufferSource, abstractClientPlayer);
		} else {
			playerRenderer.renderLeftHand(poseStack, multiBufferSource, abstractClientPlayer);
		}
	}

	private void applyEatTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack) {
		float g = (float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F;
		float h = g / (float)itemStack.getUseDuration();
		if (h < 0.8F) {
			float i = Mth.abs(Mth.cos(g / 4.0F * (float) Math.PI) * 0.1F);
			poseStack.translate(0.0, (double)i, 0.0);
		}

		float i = 1.0F - (float)Math.pow((double)h, 27.0);
		int j = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate((double)(i * 0.6F * (float)j), (double)(i * -0.5F), (double)(i * 0.0F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees((float)j * i * 90.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(i * 10.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)j * i * 30.0F));
	}

	private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {
		int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		float g = Mth.sin(f * f * (float) Math.PI);
		poseStack.mulPose(Vector3f.YP.rotationDegrees((float)i * (45.0F + g * -20.0F)));
		float h = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
		poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * h * -20.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(h * -80.0F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees((float)i * -45.0F));
	}

	private void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {
		int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate((double)((float)i * 0.56F), (double)(-0.52F + f * -0.6F), -0.72F);
	}

	public void renderHandsWithItems(float f, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
		LocalPlayer localPlayer = this.minecraft.player;
		float g = localPlayer.getAttackAnim(f);
		InteractionHand interactionHand = MoreObjects.firstNonNull(localPlayer.swingingArm, InteractionHand.MAIN_HAND);
		float h = Mth.lerp(f, localPlayer.xRotO, localPlayer.xRot);
		boolean bl = true;
		boolean bl2 = true;
		if (localPlayer.isUsingItem()) {
			ItemStack itemStack = localPlayer.getUseItem();
			if (itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.CROSSBOW) {
				bl = localPlayer.getUsedItemHand() == InteractionHand.MAIN_HAND;
				bl2 = !bl;
			}

			InteractionHand interactionHand2 = localPlayer.getUsedItemHand();
			if (interactionHand2 == InteractionHand.MAIN_HAND) {
				ItemStack itemStack2 = localPlayer.getOffhandItem();
				if (itemStack2.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack2)) {
					bl2 = false;
				}
			}
		} else {
			ItemStack itemStackx = localPlayer.getMainHandItem();
			ItemStack itemStack3 = localPlayer.getOffhandItem();
			if (itemStackx.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStackx)) {
				bl2 = !bl;
			}

			if (itemStack3.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack3)) {
				bl = !itemStackx.isEmpty();
				bl2 = !bl;
			}
		}

		float i = Mth.lerp(f, localPlayer.xBobO, localPlayer.xBob);
		float j = Mth.lerp(f, localPlayer.yBobO, localPlayer.yBob);
		poseStack.mulPose(Vector3f.XP.rotationDegrees((localPlayer.getViewXRot(f) - i) * 0.1F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees((localPlayer.getViewYRot(f) - j) * 0.1F));
		if (bl) {
			float k = interactionHand == InteractionHand.MAIN_HAND ? g : 0.0F;
			float l = 1.0F - Mth.lerp(f, this.oMainHandHeight, this.mainHandHeight);
			this.renderArmWithItem(localPlayer, f, h, InteractionHand.MAIN_HAND, k, this.mainHandItem, l, poseStack, bufferSource);
		}

		if (bl2) {
			float k = interactionHand == InteractionHand.OFF_HAND ? g : 0.0F;
			float l = 1.0F - Mth.lerp(f, this.oOffHandHeight, this.offHandHeight);
			this.renderArmWithItem(localPlayer, f, h, InteractionHand.OFF_HAND, k, this.offHandItem, l, poseStack, bufferSource);
		}

		bufferSource.endBatch();
	}

	private void renderArmWithItem(
		AbstractClientPlayer abstractClientPlayer,
		float f,
		float g,
		InteractionHand interactionHand,
		float h,
		ItemStack itemStack,
		float i,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource
	) {
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
			boolean bl2 = CrossbowItem.isCharged(itemStack);
			boolean bl3 = humanoidArm == HumanoidArm.RIGHT;
			int j = bl3 ? 1 : -1;
			if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				poseStack.translate((double)((float)j * -0.4785682F), -0.094387F, 0.05731531F);
				poseStack.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
				poseStack.mulPose(Vector3f.YP.rotationDegrees((float)j * 65.3F));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)j * -9.785F));
				float k = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
				float l = k / (float)CrossbowItem.getChargeDuration(itemStack);
				if (l > 1.0F) {
					l = 1.0F;
				}

				if (l > 0.1F) {
					float m = Mth.sin((k - 0.1F) * 1.3F);
					float n = l - 0.1F;
					float o = m * n;
					poseStack.translate((double)(o * 0.0F), (double)(o * 0.004F), (double)(o * 0.0F));
				}

				poseStack.translate((double)(l * 0.0F), (double)(l * 0.0F), (double)(l * 0.04F));
				poseStack.scale(1.0F, 1.0F, 1.0F + l * 0.2F);
				poseStack.mulPose(Vector3f.YN.rotationDegrees((float)j * 45.0F));
			} else {
				float kx = -0.4F * Mth.sin(Mth.sqrt(h) * (float) Math.PI);
				float lx = 0.2F * Mth.sin(Mth.sqrt(h) * (float) (Math.PI * 2));
				float m = -0.2F * Mth.sin(h * (float) Math.PI);
				poseStack.translate((double)((float)j * kx), (double)lx, (double)m);
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
				if (bl2 && h < 0.001F) {
					poseStack.translate((double)((float)j * -0.641864F), 0.0, 0.0);
					poseStack.mulPose(Vector3f.YP.rotationDegrees((float)j * 10.0F));
				}
			}

			this.renderItem(
				abstractClientPlayer,
				itemStack,
				bl3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
				!bl3,
				poseStack,
				multiBufferSource
			);
		} else {
			boolean bl2 = humanoidArm == HumanoidArm.RIGHT;
			if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
				int p = bl2 ? 1 : -1;
				switch (itemStack.getUseAnimation()) {
					case NONE:
						this.applyItemArmTransform(poseStack, humanoidArm, i);
						break;
					case EAT:
					case DRINK:
						this.applyEatTransform(poseStack, f, humanoidArm, itemStack);
						this.applyItemArmTransform(poseStack, humanoidArm, i);
						break;
					case BLOCK:
						this.applyItemArmTransform(poseStack, humanoidArm, i);
						break;
					case BOW:
						this.applyItemArmTransform(poseStack, humanoidArm, i);
						poseStack.translate((double)((float)p * -0.2785682F), 0.18344387F, 0.15731531F);
						poseStack.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
						poseStack.mulPose(Vector3f.YP.rotationDegrees((float)p * 35.3F));
						poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)p * -9.785F));
						float qx = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
						float kxx = qx / 20.0F;
						kxx = (kxx * kxx + kxx * 2.0F) / 3.0F;
						if (kxx > 1.0F) {
							kxx = 1.0F;
						}

						if (kxx > 0.1F) {
							float lx = Mth.sin((qx - 0.1F) * 1.3F);
							float m = kxx - 0.1F;
							float n = lx * m;
							poseStack.translate((double)(n * 0.0F), (double)(n * 0.004F), (double)(n * 0.0F));
						}

						poseStack.translate((double)(kxx * 0.0F), (double)(kxx * 0.0F), (double)(kxx * 0.04F));
						poseStack.scale(1.0F, 1.0F, 1.0F + kxx * 0.2F);
						poseStack.mulPose(Vector3f.YN.rotationDegrees((float)p * 45.0F));
						break;
					case SPEAR:
						this.applyItemArmTransform(poseStack, humanoidArm, i);
						poseStack.translate((double)((float)p * -0.5F), 0.7F, 0.1F);
						poseStack.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
						poseStack.mulPose(Vector3f.YP.rotationDegrees((float)p * 35.3F));
						poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)p * -9.785F));
						float q = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
						float kx = q / 10.0F;
						if (kx > 1.0F) {
							kx = 1.0F;
						}

						if (kx > 0.1F) {
							float lx = Mth.sin((q - 0.1F) * 1.3F);
							float m = kx - 0.1F;
							float n = lx * m;
							poseStack.translate((double)(n * 0.0F), (double)(n * 0.004F), (double)(n * 0.0F));
						}

						poseStack.translate(0.0, 0.0, (double)(kx * 0.2F));
						poseStack.scale(1.0F, 1.0F, 1.0F + kx * 0.2F);
						poseStack.mulPose(Vector3f.YN.rotationDegrees((float)p * 45.0F));
				}
			} else if (abstractClientPlayer.isAutoSpinAttack()) {
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				int p = bl2 ? 1 : -1;
				poseStack.translate((double)((float)p * -0.4F), 0.8F, 0.3F);
				poseStack.mulPose(Vector3f.YP.rotationDegrees((float)p * 65.0F));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)p * -85.0F));
			} else {
				float r = -0.4F * Mth.sin(Mth.sqrt(h) * (float) Math.PI);
				float qxx = 0.2F * Mth.sin(Mth.sqrt(h) * (float) (Math.PI * 2));
				float kxxx = -0.2F * Mth.sin(h * (float) Math.PI);
				int s = bl2 ? 1 : -1;
				poseStack.translate((double)((float)s * r), (double)qxx, (double)kxxx);
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
			}

			this.renderItem(
				abstractClientPlayer,
				itemStack,
				bl2 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
				!bl2,
				poseStack,
				multiBufferSource
			);
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
			this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
			this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
		} else {
			float f = localPlayer.getAttackStrengthScale(1.0F);
			this.mainHandHeight = this.mainHandHeight + Mth.clamp((Objects.equals(this.mainHandItem, itemStack) ? f * f * f : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
			this.offHandHeight = this.offHandHeight + Mth.clamp((float)(Objects.equals(this.offHandItem, itemStack2) ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
		}

		if (this.mainHandHeight < 0.1F) {
			this.mainHandItem = itemStack;
		}

		if (this.offHandHeight < 0.1F) {
			this.offHandItem = itemStack2;
		}
	}

	public void itemUsed(InteractionHand interactionHand) {
		if (interactionHand == InteractionHand.MAIN_HAND) {
			this.mainHandHeight = 0.0F;
		} else {
			this.offHandHeight = 0.0F;
		}
	}
}
