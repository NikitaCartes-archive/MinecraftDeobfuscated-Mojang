package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class BastionFeature extends JigsawFeature {
	private static final int BASTION_SPAWN_HEIGHT = 33;

	public BastionFeature(Codec<JigsawConfiguration> codec) {
		super(codec, 33, false, false, context -> true);
	}
}
