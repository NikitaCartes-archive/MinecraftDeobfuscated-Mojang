package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class AbstractArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
	protected static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	protected final A innerModel;
	protected final A outerModel;
	private float alpha = 1.0F;
	private float red = 1.0F;
	private float green = 1.0F;
	private float blue = 1.0F;
	private boolean colorized;
	private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.<String, ResourceLocation>newHashMap();

	protected AbstractArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2) {
		super(renderLayerParent);
		this.innerModel = humanoidModel;
		this.outerModel = humanoidModel2;
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.CHEST);
		this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.LEGS);
		this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.FEET);
		this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.HEAD);
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}

	private void renderArmorPiece(T livingEntity, float f, float g, float h, float i, float j, float k, float l, EquipmentSlot equipmentSlot) {
		ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
		if (itemStack.getItem() instanceof ArmorItem) {
			ArmorItem armorItem = (ArmorItem)itemStack.getItem();
			if (armorItem.getSlot() == equipmentSlot) {
				A humanoidModel = this.getArmorModel(equipmentSlot);
				this.getParentModel().copyPropertiesTo(humanoidModel);
				humanoidModel.prepareMobModel(livingEntity, f, g, h);
				this.setPartVisibility(humanoidModel, equipmentSlot);
				boolean bl = this.usesInnerModel(equipmentSlot);
				this.bindTexture(this.getArmorLocation(armorItem, bl));
				if (armorItem instanceof DyeableArmorItem) {
					int m = ((DyeableArmorItem)armorItem).getColor(itemStack);
					float n = (float)(m >> 16 & 0xFF) / 255.0F;
					float o = (float)(m >> 8 & 0xFF) / 255.0F;
					float p = (float)(m & 0xFF) / 255.0F;
					RenderSystem.color4f(this.red * n, this.green * o, this.blue * p, this.alpha);
					humanoidModel.render(livingEntity, f, g, i, j, k, l);
					this.bindTexture(this.getArmorLocation(armorItem, bl, "overlay"));
				}

				RenderSystem.color4f(this.red, this.green, this.blue, this.alpha);
				humanoidModel.render(livingEntity, f, g, i, j, k, l);
				if (!this.colorized && itemStack.isEnchanted()) {
					renderFoil(this::bindTexture, livingEntity, humanoidModel, f, g, h, i, j, k, l);
				}
			}
		}
	}

	public A getArmorModel(EquipmentSlot equipmentSlot) {
		return this.usesInnerModel(equipmentSlot) ? this.innerModel : this.outerModel;
	}

	private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.LEGS;
	}

	public static <T extends Entity> void renderFoil(
		Consumer<ResourceLocation> consumer, T entity, EntityModel<T> entityModel, float f, float g, float h, float i, float j, float k, float l
	) {
		float m = (float)entity.tickCount + h;
		consumer.accept(ENCHANT_GLINT_LOCATION);
		GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
		gameRenderer.resetFogColor(true);
		RenderSystem.enableBlend();
		RenderSystem.depthFunc(514);
		RenderSystem.depthMask(false);
		float n = 0.5F;
		RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);

		for (int o = 0; o < 2; o++) {
			RenderSystem.disableLighting();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
			float p = 0.76F;
			RenderSystem.color4f(0.38F, 0.19F, 0.608F, 1.0F);
			RenderSystem.matrixMode(5890);
			RenderSystem.loadIdentity();
			float q = 0.33333334F;
			RenderSystem.scalef(0.33333334F, 0.33333334F, 0.33333334F);
			RenderSystem.rotatef(30.0F - (float)o * 60.0F, 0.0F, 0.0F, 1.0F);
			RenderSystem.translatef(0.0F, m * (0.001F + (float)o * 0.003F) * 20.0F, 0.0F);
			RenderSystem.matrixMode(5888);
			entityModel.render(entity, f, g, i, j, k, l);
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		}

		RenderSystem.matrixMode(5890);
		RenderSystem.loadIdentity();
		RenderSystem.matrixMode(5888);
		RenderSystem.enableLighting();
		RenderSystem.depthMask(true);
		RenderSystem.depthFunc(515);
		RenderSystem.disableBlend();
		gameRenderer.resetFogColor(false);
	}

	private ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl) {
		return this.getArmorLocation(armorItem, bl, null);
	}

	private ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl, @Nullable String string) {
		String string2 = "textures/models/armor/" + armorItem.getMaterial().getName() + "_layer_" + (bl ? 2 : 1) + (string == null ? "" : "_" + string) + ".png";
		return (ResourceLocation)ARMOR_LOCATION_CACHE.computeIfAbsent(string2, ResourceLocation::new);
	}

	protected abstract void setPartVisibility(A humanoidModel, EquipmentSlot equipmentSlot);

	protected abstract void hideAllArmor(A humanoidModel);
}
