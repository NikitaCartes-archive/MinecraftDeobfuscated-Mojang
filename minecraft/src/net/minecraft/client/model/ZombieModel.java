package net.minecraft.client.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class ZombieModel<T extends Zombie> extends AbstractZombieModel<T> {
	public ZombieModel(Function<ResourceLocation, RenderType> function, float f, boolean bl) {
		this(function, f, 0.0F, 64, bl ? 32 : 64);
	}

	protected ZombieModel(Function<ResourceLocation, RenderType> function, float f, float g, int i, int j) {
		super(function, f, g, i, j);
	}

	public boolean isAggressive(T zombie) {
		return zombie.isAggressive();
	}
}
