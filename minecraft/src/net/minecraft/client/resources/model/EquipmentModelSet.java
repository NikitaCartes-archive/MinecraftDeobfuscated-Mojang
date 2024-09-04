package net.minecraft.client.resources.model;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.equipment.EquipmentModel;

@Environment(EnvType.CLIENT)
public class EquipmentModelSet extends SimpleJsonResourceReloadListener<EquipmentModel> {
	public static final EquipmentModel MISSING_MODEL = new EquipmentModel(Map.of());
	private Map<ResourceLocation, EquipmentModel> models = Map.of();

	public EquipmentModelSet() {
		super(EquipmentModel.CODEC, "models/equipment");
	}

	protected void apply(Map<ResourceLocation, EquipmentModel> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.models = Map.copyOf(map);
	}

	public EquipmentModel get(ResourceLocation resourceLocation) {
		return (EquipmentModel)this.models.getOrDefault(resourceLocation, MISSING_MODEL);
	}
}
