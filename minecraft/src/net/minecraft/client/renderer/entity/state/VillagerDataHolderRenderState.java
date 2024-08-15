package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.npc.VillagerData;

@Environment(EnvType.CLIENT)
public interface VillagerDataHolderRenderState {
	VillagerData getVillagerData();
}
