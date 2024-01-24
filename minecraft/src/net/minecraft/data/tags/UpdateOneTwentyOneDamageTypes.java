package net.minecraft.data.tags;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class UpdateOneTwentyOneDamageTypes {
	public static void bootstrap(BootstrapContext<DamageType> bootstrapContext) {
		bootstrapContext.register(DamageTypes.WIND_CHARGE, new DamageType("mob", 0.1F));
	}
}
