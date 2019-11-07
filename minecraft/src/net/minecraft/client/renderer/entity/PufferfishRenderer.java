package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

@Environment(EnvType.CLIENT)
public class PufferfishRenderer extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
	private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
	private int puffStateO;
	private final PufferfishSmallModel<Pufferfish> small = new PufferfishSmallModel<>();
	private final PufferfishMidModel<Pufferfish> mid = new PufferfishMidModel<>();
	private final PufferfishBigModel<Pufferfish> big = new PufferfishBigModel<>();

	public PufferfishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new PufferfishBigModel<>(), 0.2F);
		this.puffStateO = 3;
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
		poseStack.translate(0.0, (double)(Mth.cos(f * 0.05F) * 0.08F), 0.0);
		super.setupRotations(pufferfish, poseStack, f, g, h);
	}
}
