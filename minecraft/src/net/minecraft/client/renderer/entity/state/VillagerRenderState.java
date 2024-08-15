package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

@Environment(EnvType.CLIENT)
public class VillagerRenderState extends LivingEntityRenderState implements VillagerDataHolderRenderState {
	public boolean isUnhappy;
	public VillagerData villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1);

	@Override
	public VillagerData getVillagerData() {
		return this.villagerData;
	}
}
