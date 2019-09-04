package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
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

	@Nullable
	protected ResourceLocation getTextureLocation(Pufferfish pufferfish) {
		return PUFFER_LOCATION;
	}

	public void render(Pufferfish pufferfish, double d, double e, double f, float g, float h) {
		int i = pufferfish.getPuffState();
		if (i != this.puffStateO) {
			if (i == 0) {
				this.model = this.small;
			} else if (i == 1) {
				this.model = this.mid;
			} else {
				this.model = this.big;
			}
		}

		this.puffStateO = i;
		this.shadowRadius = 0.1F + 0.1F * (float)i;
		super.render(pufferfish, d, e, f, g, h);
	}

	protected void setupRotations(Pufferfish pufferfish, float f, float g, float h) {
		RenderSystem.translatef(0.0F, Mth.cos(f * 0.05F) * 0.08F, 0.0F);
		super.setupRotations(pufferfish, f, g, h);
	}
}
