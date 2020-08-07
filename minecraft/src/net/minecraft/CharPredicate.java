package net.minecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface CharPredicate {
	boolean test(char c);
}
