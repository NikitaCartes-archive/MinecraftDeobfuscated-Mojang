package net.minecraft.world.level.storage.loot;

import java.util.Random;
import net.minecraft.resources.ResourceLocation;

public interface RandomIntGenerator {
	ResourceLocation CONSTANT = new ResourceLocation("constant");
	ResourceLocation UNIFORM = new ResourceLocation("uniform");
	ResourceLocation BINOMIAL = new ResourceLocation("binomial");

	int getInt(Random random);

	ResourceLocation getType();
}
