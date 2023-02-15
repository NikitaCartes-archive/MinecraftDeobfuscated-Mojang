package net.minecraft.world.level.block.state.properties;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public record WoodType(
	String name, BlockSetType setType, SoundType soundType, SoundType hangingSignSoundType, SoundEvent fenceGateClose, SoundEvent fenceGateOpen
) {
	private static final Set<WoodType> VALUES = new ObjectArraySet<>();
	public static final WoodType OAK = register(new WoodType("oak", BlockSetType.OAK));
	public static final WoodType SPRUCE = register(new WoodType("spruce", BlockSetType.SPRUCE));
	public static final WoodType BIRCH = register(new WoodType("birch", BlockSetType.BIRCH));
	public static final WoodType ACACIA = register(new WoodType("acacia", BlockSetType.ACACIA));
	public static final WoodType CHERRY = register(
		new WoodType(
			"cherry",
			BlockSetType.CHERRY,
			SoundType.CHERRY_WOOD,
			SoundType.CHERRY_WOOD_HANGING_SIGN,
			SoundEvents.CHERRY_WOOD_FENCE_GATE_CLOSE,
			SoundEvents.CHERRY_WOOD_FENCE_GATE_OPEN
		)
	);
	public static final WoodType JUNGLE = register(new WoodType("jungle", BlockSetType.JUNGLE));
	public static final WoodType DARK_OAK = register(new WoodType("dark_oak", BlockSetType.DARK_OAK));
	public static final WoodType CRIMSON = register(
		new WoodType(
			"crimson",
			BlockSetType.CRIMSON,
			SoundType.NETHER_WOOD,
			SoundType.NETHER_WOOD_HANGING_SIGN,
			SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE,
			SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN
		)
	);
	public static final WoodType WARPED = register(
		new WoodType(
			"warped",
			BlockSetType.WARPED,
			SoundType.NETHER_WOOD,
			SoundType.NETHER_WOOD_HANGING_SIGN,
			SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE,
			SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN
		)
	);
	public static final WoodType MANGROVE = register(new WoodType("mangrove", BlockSetType.MANGROVE));
	public static final WoodType BAMBOO = register(
		new WoodType(
			"bamboo",
			BlockSetType.BAMBOO,
			SoundType.BAMBOO_WOOD,
			SoundType.BAMBOO_WOOD_HANGING_SIGN,
			SoundEvents.BAMBOO_WOOD_FENCE_GATE_CLOSE,
			SoundEvents.BAMBOO_WOOD_FENCE_GATE_OPEN
		)
	);

	public WoodType(String string, BlockSetType blockSetType) {
		this(string, blockSetType, SoundType.WOOD, SoundType.HANGING_SIGN, SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN);
	}

	private static WoodType register(WoodType woodType) {
		VALUES.add(woodType);
		return woodType;
	}

	public static Stream<WoodType> values() {
		return VALUES.stream();
	}
}
