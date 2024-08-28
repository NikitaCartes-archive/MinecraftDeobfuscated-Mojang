package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SnowGolemHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class SnowGolemRenderer extends MobRenderer<SnowGolem, LivingEntityRenderState, SnowGolemModel> {
	private static final ResourceLocation SNOW_GOLEM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/snow_golem.png");

	public SnowGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new SnowGolemModel(context.bakeLayer(ModelLayers.SNOW_GOLEM)), 0.5F);
		this.addLayer(new SnowGolemHeadLayer(this, context.getBlockRenderDispatcher(), context.getItemRenderer()));
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
		return SNOW_GOLEM_LOCATION;
	}

	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	public void extractRenderState(SnowGolem snowGolem, LivingEntityRenderState livingEntityRenderState, float f) {
		super.extractRenderState(snowGolem, livingEntityRenderState, f);
		livingEntityRenderState.headItem = snowGolem.hasPumpkin() ? new ItemStack(Items.CARVED_PUMPKIN) : ItemStack.EMPTY;
		livingEntityRenderState.headItemModel = this.itemRenderer.resolveItemModel(livingEntityRenderState.headItem, snowGolem, ItemDisplayContext.HEAD);
	}
}
