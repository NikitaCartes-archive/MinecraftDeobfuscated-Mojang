package net.minecraft.data.tags;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class UpdateOneTwentyOneDamageTypes {
	public static void bootstrap(BootstapContext<DamageType> bootstapContext) {
		bootstapContext.register(DamageTypes.WIND_CHARGE, new DamageType("mob", 0.1F));
	}
}
