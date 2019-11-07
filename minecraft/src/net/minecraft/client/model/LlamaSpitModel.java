package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class LlamaSpitModel<T extends Entity> extends ListModel<T> {
	private final ModelPart main = new ModelPart(this);

	public LlamaSpitModel() {
		this(0.0F);
	}

	public LlamaSpitModel(float f) {
		int i = 2;
		this.main.texOffs(0, 0).addBox(-4.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
		this.main.texOffs(0, 0).addBox(0.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
		this.main.texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 2.0F, 2.0F, 2.0F, f);
		this.main.texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
		this.main.texOffs(0, 0).addBox(2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
		this.main.texOffs(0, 0).addBox(0.0F, 2.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
		this.main.texOffs(0, 0).addBox(0.0F, 0.0F, 2.0F, 2.0F, 2.0F, 2.0F, f);
		this.main.setPos(0.0F, 0.0F, 0.0F);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.main);
	}
}
