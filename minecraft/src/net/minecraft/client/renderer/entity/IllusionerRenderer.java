package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class IllusionerRenderer extends IllagerRenderer<Illusioner> {
	private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

	public IllusionerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
		this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this) {
			public void render(Illusioner illusioner, float f, float g, float h, float i, float j, float k, float l) {
				if (illusioner.isCastingSpell() || illusioner.isAggressive()) {
					super.render(illusioner, f, g, h, i, j, k, l);
				}
			}
		});
		this.model.getHat().visible = true;
	}

	protected ResourceLocation getTextureLocation(Illusioner illusioner) {
		return ILLUSIONER;
	}

	public void render(Illusioner illusioner, double d, double e, double f, float g, float h) {
		if (illusioner.isInvisible()) {
			Vec3[] vec3s = illusioner.getIllusionOffsets(h);
			float i = this.getBob(illusioner, h);

			for (int j = 0; j < vec3s.length; j++) {
				super.render(
					illusioner,
					d + vec3s[j].x + (double)Mth.cos((float)j + i * 0.5F) * 0.025,
					e + vec3s[j].y + (double)Mth.cos((float)j + i * 0.75F) * 0.0125,
					f + vec3s[j].z + (double)Mth.cos((float)j + i * 0.7F) * 0.025,
					g,
					h
				);
			}
		} else {
			super.render(illusioner, d, e, f, g, h);
		}
	}

	protected boolean isVisible(Illusioner illusioner) {
		return true;
	}
}
