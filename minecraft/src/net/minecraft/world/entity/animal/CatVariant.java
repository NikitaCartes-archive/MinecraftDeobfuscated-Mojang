package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public record CatVariant(ResourceLocation texture) {
	public static final CatVariant TABBY = register("tabby", "textures/entity/cat/tabby.png");
	public static final CatVariant BLACK = register("black", "textures/entity/cat/black.png");
	public static final CatVariant RED = register("red", "textures/entity/cat/red.png");
	public static final CatVariant SIAMESE = register("siamese", "textures/entity/cat/siamese.png");
	public static final CatVariant BRITISH = register("british", "textures/entity/cat/british_shorthair.png");
	public static final CatVariant CALICO = register("calico", "textures/entity/cat/calico.png");
	public static final CatVariant PERSIAN = register("persian", "textures/entity/cat/persian.png");
	public static final CatVariant RAGDOLL = register("ragdoll", "textures/entity/cat/ragdoll.png");
	public static final CatVariant WHITE = register("white", "textures/entity/cat/white.png");
	public static final CatVariant JELLIE = register("jellie", "textures/entity/cat/jellie.png");
	public static final CatVariant ALL_BLACK = register("all_black", "textures/entity/cat/all_black.png");

	private static CatVariant register(String string, String string2) {
		return Registry.register(Registry.CAT_VARIANT, string, new CatVariant(new ResourceLocation(string2)));
	}
}
