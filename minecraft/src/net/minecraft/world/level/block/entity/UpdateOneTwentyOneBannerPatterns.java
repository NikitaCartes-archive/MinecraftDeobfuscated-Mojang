package net.minecraft.world.level.block.entity;

import net.minecraft.data.worldgen.BootstrapContext;

public interface UpdateOneTwentyOneBannerPatterns {
	static void bootstrap(BootstrapContext<BannerPattern> bootstrapContext) {
		BannerPatterns.register(bootstrapContext, BannerPatterns.FLOW);
		BannerPatterns.register(bootstrapContext, BannerPatterns.GUSTER);
	}
}
