package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;

@Environment(EnvType.CLIENT)
public class VexRenderer extends MobRenderer<Vex, VexRenderState, VexModel> {
	private static final ResourceLocation VEX_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex.png");
	private static final ResourceLocation VEX_CHARGING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex_charging.png");

	public VexRenderer(EntityRendererProvider.Context context) {
		super(context, new VexModel(context.bakeLayer(ModelLayers.VEX)), 0.3F);
		this.addLayer(new ItemInHandLayer<>(this, context.getItemRenderer()));
	}

	protected int getBlockLightLevel(Vex vex, BlockPos blockPos) {
		return 15;
	}

	public ResourceLocation getTextureLocation(VexRenderState vexRenderState) {
		return vexRenderState.isCharging ? VEX_CHARGING_LOCATION : VEX_LOCATION;
	}

	public VexRenderState createRenderState() {
		return new VexRenderState();
	}

	public void extractRenderState(Vex vex, VexRenderState vexRenderState, float f) {
		super.extractRenderState(vex, vexRenderState, f);
		vexRenderState.isCharging = vex.isCharging();
	}
}
