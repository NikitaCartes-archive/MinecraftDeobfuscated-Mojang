package net.minecraft.world.level.material;

public final class Material {
	public static final Material PLANT = new Material(MaterialColor.PLANT, false);
	public static final Material DEPRECATED_NONSOLID = new Material(MaterialColor.NONE, false);
	public static final Material DEPRECATED = new Material(MaterialColor.NONE, true);
	private final MaterialColor color;
	private final boolean solidBlocking;

	public Material(MaterialColor materialColor, boolean bl) {
		this.color = materialColor;
		this.solidBlocking = bl;
	}

	public boolean isSolidBlocking() {
		return this.solidBlocking;
	}

	public MaterialColor getColor() {
		return this.color;
	}
}
