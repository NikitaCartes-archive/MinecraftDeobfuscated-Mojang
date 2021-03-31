package net.minecraft.world.level.block.state.properties;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import java.util.stream.Stream;

public class WoodType {
	private static final Set<WoodType> VALUES = new ObjectArraySet<>();
	public static final WoodType OAK = register(new WoodType("oak"));
	public static final WoodType SPRUCE = register(new WoodType("spruce"));
	public static final WoodType BIRCH = register(new WoodType("birch"));
	public static final WoodType ACACIA = register(new WoodType("acacia"));
	public static final WoodType JUNGLE = register(new WoodType("jungle"));
	public static final WoodType DARK_OAK = register(new WoodType("dark_oak"));
	public static final WoodType CRIMSON = register(new WoodType("crimson"));
	public static final WoodType WARPED = register(new WoodType("warped"));
	private final String name;

	protected WoodType(String string) {
		this.name = string;
	}

	private static WoodType register(WoodType woodType) {
		VALUES.add(woodType);
		return woodType;
	}

	public static Stream<WoodType> values() {
		return VALUES.stream();
	}

	public String name() {
		return this.name;
	}
}
