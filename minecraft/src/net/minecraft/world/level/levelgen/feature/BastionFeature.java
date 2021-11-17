package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class BastionFeature extends JigsawFeature {
	private static final int BASTION_SPAWN_HEIGHT = 33;

	public BastionFeature(Codec<JigsawConfiguration> codec) {
		super(codec, 33, false, false, BastionFeature::checkLocation);
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
		return worldgenRandom.nextInt(5) >= 2;
	}
}
