package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PufferfishRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

@Environment(EnvType.CLIENT)
public class PufferfishRenderer extends MobRenderer<Pufferfish, PufferfishRenderState, EntityModel<EntityRenderState>> {
	private static final ResourceLocation PUFFER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/pufferfish.png");
	private final EntityModel<EntityRenderState> small;
	private final EntityModel<EntityRenderState> mid;
	private final EntityModel<EntityRenderState> big = this.getModel();

	public PufferfishRenderer(EntityRendererProvider.Context context) {
		super(context, new PufferfishBigModel(context.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2F);
		this.mid = new PufferfishMidModel(context.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
		this.small = new PufferfishSmallModel(context.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
	}

	public ResourceLocation getTextureLocation(PufferfishRenderState pufferfishRenderState) {
		return PUFFER_LOCATION;
	}

	public PufferfishRenderState createRenderState() {
		return new PufferfishRenderState();
	}

	public void render(PufferfishRenderState pufferfishRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.model = switch (pufferfishRenderState.puffState) {
			case 0 -> this.small;
			case 1 -> this.mid;
			default -> this.big;
		};
		this.shadowRadius = 0.1F + 0.1F * (float)pufferfishRenderState.puffState;
		super.render(pufferfishRenderState, poseStack, multiBufferSource, i);
	}

	public void extractRenderState(Pufferfish pufferfish, PufferfishRenderState pufferfishRenderState, float f) {
		super.extractRenderState(pufferfish, pufferfishRenderState, f);
		pufferfishRenderState.puffState = pufferfish.getPuffState();
	}

	protected void setupRotations(PufferfishRenderState pufferfishRenderState, PoseStack poseStack, float f, float g) {
		poseStack.translate(0.0F, Mth.cos(pufferfishRenderState.ageInTicks * 0.05F) * 0.08F, 0.0F);
		super.setupRotations(pufferfishRenderState, poseStack, f, g);
	}
}
