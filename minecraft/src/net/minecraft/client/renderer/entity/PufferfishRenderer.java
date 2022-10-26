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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

@Environment(EnvType.CLIENT)
public class PufferfishRenderer extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
	private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
	private int puffStateO = 3;
	private final EntityModel<Pufferfish> small;
	private final EntityModel<Pufferfish> mid;
	private final EntityModel<Pufferfish> big = this.getModel();

	public PufferfishRenderer(EntityRendererProvider.Context context) {
		super(context, new PufferfishBigModel<>(context.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2F);
		this.mid = new PufferfishMidModel<>(context.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
		this.small = new PufferfishSmallModel<>(context.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
	}

	public ResourceLocation getTextureLocation(Pufferfish pufferfish) {
		return PUFFER_LOCATION;
	}

	public void render(Pufferfish pufferfish, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		int j = pufferfish.getPuffState();
		if (j != this.puffStateO) {
			if (j == 0) {
				this.model = this.small;
			} else if (j == 1) {
				this.model = this.mid;
			} else {
				this.model = this.big;
			}
		}

		this.puffStateO = j;
		this.shadowRadius = 0.1F + 0.1F * (float)j;
		super.render(pufferfish, f, g, poseStack, multiBufferSource, i);
	}

	protected void setupRotations(Pufferfish pufferfish, PoseStack poseStack, float f, float g, float h) {
		poseStack.translate(0.0F, Mth.cos(f * 0.05F) * 0.08F, 0.0F);
		super.setupRotations(pufferfish, poseStack, f, g, h);
	}
}
