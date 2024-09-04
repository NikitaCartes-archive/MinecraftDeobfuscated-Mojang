package net.minecraft.data.models;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.EquipmentModels;

public class EquipmentModelProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;

	public EquipmentModelProvider(PackOutput packOutput) {
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/equipment");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		Map<ResourceLocation, EquipmentModel> map = new HashMap();
		EquipmentModels.bootstrap((resourceLocation, equipmentModel) -> {
			if (map.putIfAbsent(resourceLocation, equipmentModel) != null) {
				throw new IllegalStateException("Tried to register equipment model twice for id: " + resourceLocation);
			}
		});
		return DataProvider.saveAll(cachedOutput, EquipmentModel.CODEC, this.pathProvider, map);
	}

	@Override
	public String getName() {
		return "Equipment Model Definitions";
	}
}
