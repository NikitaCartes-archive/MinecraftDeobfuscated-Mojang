package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

@Environment(EnvType.CLIENT)
public class ItemInHandRenderer {
	private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
	private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
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
		MultiBufferSource multiBufferSource,
		int i
	) {
		if (!itemStack.isEmpty()) {
			this.itemRenderer.renderStatic(livingEntity, itemStack, transformType, bl, poseStack, multiBufferSource, livingEntity.level, i, OverlayTexture.NO_OVERLAY);
		}
	}

	private float calculateMapTilt(float f) {
		float g = 1.0F - f / 45.0F + 0.1F;
		g = Mth.clamp(g, 0.0F, 1.0F);
		return -Mth.cos(g * (float) Math.PI) * 0.5F + 0.5F;
	}

	private void renderMapHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, HumanoidArm humanoidArm) {
		this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
		PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.minecraft.player);
		poseStack.pushPose();
		float f = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		poseStack.mulPose(Vector3f.YP.rotationDegrees(92.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * -41.0F));
		poseStack.translate((double)(f * 0.3F), -1.1F, 0.45F);
		if (humanoidArm == HumanoidArm.RIGHT) {
			playerRenderer.renderRightHand(poseStack, multiBufferSource, i, this.minecraft.player);
		} else {
			playerRenderer.renderLeftHand(poseStack, multiBufferSource, i, this.minecraft.player);
		}

		poseStack.popPose();
	}

	private void renderOneHandedMap(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, HumanoidArm humanoidArm, float g, ItemStack itemStack
	) {
		float h = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		poseStack.translate((double)(h * 0.125F), -0.125, 0.0);
		if (!this.minecraft.player.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(h * 10.0F));
			this.renderPlayerArm(poseStack, multiBufferSource, i, f, g, humanoidArm);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate((double)(h * 0.51F), (double)(-0.08F + f * -1.2F), -0.75);
		float j = Mth.sqrt(g);
		float k = Mth.sin(j * (float) Math.PI);
		float l = -0.5F * k;
		float m = 0.4F * Mth.sin(j * (float) (Math.PI * 2));
		float n = -0.3F * Mth.sin(g * (float) Math.PI);
		poseStack.translate((double)(h * l), (double)(m - 0.3F * k), (double)n);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(k * -45.0F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * k * -30.0F));
		this.renderMap(poseStack, multiBufferSource, i, itemStack);
		poseStack.popPose();
	}

	private void renderTwoHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, float h) {
		float j = Mth.sqrt(h);
		float k = -0.2F * Mth.sin(h * (float) Math.PI);
		float l = -0.4F * Mth.sin(j * (float) Math.PI);
		poseStack.translate(0.0, (double)(-k / 2.0F), (double)l);
		float m = this.calculateMapTilt(f);
		poseStack.translate(0.0, (double)(0.04F + g * -1.2F + m * -0.5F), -0.72F);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(m * -85.0F));
		if (!this.minecraft.player.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
			this.renderMapHand(poseStack, multiBufferSource, i, HumanoidArm.RIGHT);
			this.renderMapHand(poseStack, multiBufferSource, i, HumanoidArm.LEFT);
			poseStack.popPose();
		}

		float n = Mth.sin(j * (float) Math.PI);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(n * 20.0F));
		poseStack.scale(2.0F, 2.0F, 2.0F);
		this.renderMap(poseStack, multiBufferSource, i, this.mainHandItem);
	}

	private void renderMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ItemStack itemStack) {
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		poseStack.scale(0.38F, 0.38F, 0.38F);
		poseStack.translate(-0.5, -0.5, 0.0);
		poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
		MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, this.minecraft.level);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(mapItemSavedData == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
		Matrix4f matrix4f = poseStack.last().pose();
		vertexConsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(i).endVertex();
		vertexConsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(i).endVertex();
		vertexConsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(i).endVertex();
		vertexConsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(i).endVertex();
		if (mapItemSavedData != null) {
			this.minecraft.gameRenderer.getMapRenderer().render(poseStack, multiBufferSource, mapItemSavedData, false, i);
		}
	}

	private void renderPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm) {
		boolean bl = humanoidArm != HumanoidArm.LEFT;
		float h = bl ? 1.0F : -1.0F;
		float j = Mth.sqrt(g);
		float k = -0.3F * Mth.sin(j * (float) Math.PI);
		float l = 0.4F * Mth.sin(j * (float) (Math.PI * 2));
		float m = -0.4F * Mth.sin(g * (float) Math.PI);
		poseStack.translate((double)(h * (k + 0.64000005F)), (double)(l + -0.6F + f * -0.6F), (double)(m + -0.71999997F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * 45.0F));
		float n = Mth.sin(g * g * (float) Math.PI);
		float o = Mth.sin(j * (float) Math.PI);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * o * 70.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(h * n * -20.0F));
		AbstractClientPlayer abstractClientPlayer = this.minecraft.player;
		this.minecraft.getTextureManager().bind(abstractClientPlayer.getSkinTextureLocation());
		poseStack.translate((double)(h * -1.0F), 3.6F, 3.5);
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(h * 120.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(h * -135.0F));
		poseStack.translate((double)(h * 5.6F), 0.0, 0.0);
		PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(abstractClientPlayer);
		if (bl) {
			playerRenderer.renderRightHand(poseStack, multiBufferSource, i, abstractClientPlayer);
		} else {
			playerRenderer.renderLeftHand(poseStack, multiBufferSource, i, abstractClientPlayer);
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

	public void renderHandsWithItems(float f, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer localPlayer, int i) {
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

		float j = Mth.lerp(f, localPlayer.xBobO, localPlayer.xBob);
		float k = Mth.lerp(f, localPlayer.yBobO, localPlayer.yBob);
		poseStack.mulPose(Vector3f.XP.rotationDegrees((localPlayer.getViewXRot(f) - j) * 0.1F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees((localPlayer.getViewYRot(f) - k) * 0.1F));
		if (bl) {
			float l = interactionHand == InteractionHand.MAIN_HAND ? g : 0.0F;
			float m = 1.0F - Mth.lerp(f, this.oMainHandHeight, this.mainHandHeight);
			this.renderArmWithItem(localPlayer, f, h, InteractionHand.MAIN_HAND, l, this.mainHandItem, m, poseStack, bufferSource, i);
		}

		if (bl2) {
			float l = interactionHand == InteractionHand.OFF_HAND ? g : 0.0F;
			float m = 1.0F - Mth.lerp(f, this.oOffHandHeight, this.offHandHeight);
			this.renderArmWithItem(localPlayer, f, h, InteractionHand.OFF_HAND, l, this.offHandItem, m, poseStack, bufferSource, i);
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
		MultiBufferSource multiBufferSource,
		int j
	) {
		boolean bl = interactionHand == InteractionHand.MAIN_HAND;
		HumanoidArm humanoidArm = bl ? abstractClientPlayer.getMainArm() : abstractClientPlayer.getMainArm().getOpposite();
		poseStack.pushPose();
		if (itemStack.isEmpty()) {
			if (bl && !abstractClientPlayer.isInvisible()) {
				this.renderPlayerArm(poseStack, multiBufferSource, j, i, h, humanoidArm);
			}
		} else if (itemStack.getItem() == Items.FILLED_MAP) {
			if (bl && this.offHandItem.isEmpty()) {
				this.renderTwoHandedMap(poseStack, multiBufferSource, j, g, i, h);
			} else {
				this.renderOneHandedMap(poseStack, multiBufferSource, j, i, humanoidArm, h, itemStack);
			}
		} else if (itemStack.getItem() == Items.CROSSBOW) {
			boolean bl2 = CrossbowItem.isCharged(itemStack);
			boolean bl3 = humanoidArm == HumanoidArm.RIGHT;
			int k = bl3 ? 1 : -1;
			if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				poseStack.translate((double)((float)k * -0.4785682F), -0.094387F, 0.05731531F);
				poseStack.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
				poseStack.mulPose(Vector3f.YP.rotationDegrees((float)k * 65.3F));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)k * -9.785F));
				float l = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
				float m = l / (float)CrossbowItem.getChargeDuration(itemStack);
				if (m > 1.0F) {
					m = 1.0F;
				}

				if (m > 0.1F) {
					float n = Mth.sin((l - 0.1F) * 1.3F);
					float o = m - 0.1F;
					float p = n * o;
					poseStack.translate((double)(p * 0.0F), (double)(p * 0.004F), (double)(p * 0.0F));
				}

				poseStack.translate((double)(m * 0.0F), (double)(m * 0.0F), (double)(m * 0.04F));
				poseStack.scale(1.0F, 1.0F, 1.0F + m * 0.2F);
				poseStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
			} else {
				float lx = -0.4F * Mth.sin(Mth.sqrt(h) * (float) Math.PI);
				float mx = 0.2F * Mth.sin(Mth.sqrt(h) * (float) (Math.PI * 2));
				float n = -0.2F * Mth.sin(h * (float) Math.PI);
				poseStack.translate((double)((float)k * lx), (double)mx, (double)n);
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
				if (bl2 && h < 0.001F) {
					poseStack.translate((double)((float)k * -0.641864F), 0.0, 0.0);
					poseStack.mulPose(Vector3f.YP.rotationDegrees((float)k * 10.0F));
				}
			}

			this.renderItem(
				abstractClientPlayer,
				itemStack,
				bl3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
				!bl3,
				poseStack,
				multiBufferSource,
				j
			);
		} else {
			boolean bl2 = humanoidArm == HumanoidArm.RIGHT;
			if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
				int q = bl2 ? 1 : -1;
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
						poseStack.translate((double)((float)q * -0.2785682F), 0.18344387F, 0.15731531F);
						poseStack.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
						poseStack.mulPose(Vector3f.YP.rotationDegrees((float)q * 35.3F));
						poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)q * -9.785F));
						float rx = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
						float lxx = rx / 20.0F;
						lxx = (lxx * lxx + lxx * 2.0F) / 3.0F;
						if (lxx > 1.0F) {
							lxx = 1.0F;
						}

						if (lxx > 0.1F) {
							float mx = Mth.sin((rx - 0.1F) * 1.3F);
							float n = lxx - 0.1F;
							float o = mx * n;
							poseStack.translate((double)(o * 0.0F), (double)(o * 0.004F), (double)(o * 0.0F));
						}

						poseStack.translate((double)(lxx * 0.0F), (double)(lxx * 0.0F), (double)(lxx * 0.04F));
						poseStack.scale(1.0F, 1.0F, 1.0F + lxx * 0.2F);
						poseStack.mulPose(Vector3f.YN.rotationDegrees((float)q * 45.0F));
						break;
					case SPEAR:
						this.applyItemArmTransform(poseStack, humanoidArm, i);
						poseStack.translate((double)((float)q * -0.5F), 0.7F, 0.1F);
						poseStack.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
						poseStack.mulPose(Vector3f.YP.rotationDegrees((float)q * 35.3F));
						poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)q * -9.785F));
						float r = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
						float lx = r / 10.0F;
						if (lx > 1.0F) {
							lx = 1.0F;
						}

						if (lx > 0.1F) {
							float mx = Mth.sin((r - 0.1F) * 1.3F);
							float n = lx - 0.1F;
							float o = mx * n;
							poseStack.translate((double)(o * 0.0F), (double)(o * 0.004F), (double)(o * 0.0F));
						}

						poseStack.translate(0.0, 0.0, (double)(lx * 0.2F));
						poseStack.scale(1.0F, 1.0F, 1.0F + lx * 0.2F);
						poseStack.mulPose(Vector3f.YN.rotationDegrees((float)q * 45.0F));
				}
			} else if (abstractClientPlayer.isAutoSpinAttack()) {
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				int q = bl2 ? 1 : -1;
				poseStack.translate((double)((float)q * -0.4F), 0.8F, 0.3F);
				poseStack.mulPose(Vector3f.YP.rotationDegrees((float)q * 65.0F));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)q * -85.0F));
			} else {
				float s = -0.4F * Mth.sin(Mth.sqrt(h) * (float) Math.PI);
				float rxx = 0.2F * Mth.sin(Mth.sqrt(h) * (float) (Math.PI * 2));
				float lxxx = -0.2F * Mth.sin(h * (float) Math.PI);
				int t = bl2 ? 1 : -1;
				poseStack.translate((double)((float)t * s), (double)rxx, (double)lxxx);
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
			}

			this.renderItem(
				abstractClientPlayer,
				itemStack,
				bl2 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
				!bl2,
				poseStack,
				multiBufferSource,
				j
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
		if (ItemStack.matches(this.mainHandItem, itemStack)) {
			this.mainHandItem = itemStack;
		}

		if (ItemStack.matches(this.offHandItem, itemStack2)) {
			this.offHandItem = itemStack2;
		}

		if (localPlayer.isHandsBusy()) {
			this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
			this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
		} else {
			float f = localPlayer.getAttackStrengthScale(1.0F);
			this.mainHandHeight = this.mainHandHeight + Mth.clamp((this.mainHandItem == itemStack ? f * f * f : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
			this.offHandHeight = this.offHandHeight + Mth.clamp((float)(this.offHandItem == itemStack2 ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
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
