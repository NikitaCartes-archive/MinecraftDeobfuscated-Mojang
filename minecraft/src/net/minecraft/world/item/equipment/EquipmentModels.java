package net.minecraft.world.item.equipment;

import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public interface EquipmentModels {
	ResourceLocation LEATHER = ResourceLocation.withDefaultNamespace("leather");
	ResourceLocation CHAINMAIL = ResourceLocation.withDefaultNamespace("chainmail");
	ResourceLocation IRON = ResourceLocation.withDefaultNamespace("iron");
	ResourceLocation GOLD = ResourceLocation.withDefaultNamespace("gold");
	ResourceLocation DIAMOND = ResourceLocation.withDefaultNamespace("diamond");
	ResourceLocation TURTLE_SCUTE = ResourceLocation.withDefaultNamespace("turtle_scute");
	ResourceLocation NETHERITE = ResourceLocation.withDefaultNamespace("netherite");
	ResourceLocation ARMADILLO_SCUTE = ResourceLocation.withDefaultNamespace("armadillo_scute");
	ResourceLocation ELYTRA = ResourceLocation.withDefaultNamespace("elytra");
	Map<DyeColor, ResourceLocation> CARPETS = Util.makeEnumMap(
		DyeColor.class, dyeColor -> ResourceLocation.withDefaultNamespace(dyeColor.getSerializedName() + "_carpet")
	);
	ResourceLocation TRADER_LLAMA = ResourceLocation.withDefaultNamespace("trader_llama");

	static void bootstrap(BiConsumer<ResourceLocation, EquipmentModel> biConsumer) {
		biConsumer.accept(
			LEATHER,
			EquipmentModel.builder()
				.addHumanoidLayers(ResourceLocation.withDefaultNamespace("leather"), true)
				.addHumanoidLayers(ResourceLocation.withDefaultNamespace("leather_overlay"), false)
				.addLayers(EquipmentModel.LayerType.HORSE_BODY, EquipmentModel.Layer.leatherDyeable(ResourceLocation.withDefaultNamespace("leather"), true))
				.build()
		);
		biConsumer.accept(CHAINMAIL, onlyHumanoid("chainmail"));
		biConsumer.accept(IRON, humanoidAndHorse("iron"));
		biConsumer.accept(GOLD, humanoidAndHorse("gold"));
		biConsumer.accept(DIAMOND, humanoidAndHorse("diamond"));
		biConsumer.accept(TURTLE_SCUTE, EquipmentModel.builder().addMainHumanoidLayer(ResourceLocation.withDefaultNamespace("turtle_scute"), false).build());
		biConsumer.accept(NETHERITE, onlyHumanoid("netherite"));
		biConsumer.accept(
			ARMADILLO_SCUTE,
			EquipmentModel.builder()
				.addLayers(EquipmentModel.LayerType.WOLF_BODY, EquipmentModel.Layer.onlyIfDyed(ResourceLocation.withDefaultNamespace("armadillo_scute"), false))
				.addLayers(EquipmentModel.LayerType.WOLF_BODY, EquipmentModel.Layer.onlyIfDyed(ResourceLocation.withDefaultNamespace("armadillo_scute_overlay"), true))
				.build()
		);
		biConsumer.accept(
			ELYTRA,
			EquipmentModel.builder()
				.addLayers(EquipmentModel.LayerType.WINGS, new EquipmentModel.Layer(ResourceLocation.withDefaultNamespace("elytra"), Optional.empty(), true))
				.build()
		);

		for (Entry<DyeColor, ResourceLocation> entry : CARPETS.entrySet()) {
			DyeColor dyeColor = (DyeColor)entry.getKey();
			ResourceLocation resourceLocation = (ResourceLocation)entry.getValue();
			biConsumer.accept(
				resourceLocation,
				EquipmentModel.builder()
					.addLayers(EquipmentModel.LayerType.LLAMA_BODY, new EquipmentModel.Layer(ResourceLocation.withDefaultNamespace(dyeColor.getSerializedName())))
					.build()
			);
		}

		biConsumer.accept(
			TRADER_LLAMA,
			EquipmentModel.builder()
				.addLayers(EquipmentModel.LayerType.LLAMA_BODY, new EquipmentModel.Layer(ResourceLocation.withDefaultNamespace("trader_llama")))
				.build()
		);
	}

	private static EquipmentModel onlyHumanoid(String string) {
		return EquipmentModel.builder().addHumanoidLayers(ResourceLocation.withDefaultNamespace(string)).build();
	}

	private static EquipmentModel humanoidAndHorse(String string) {
		return EquipmentModel.builder()
			.addHumanoidLayers(ResourceLocation.withDefaultNamespace(string))
			.addLayers(EquipmentModel.LayerType.HORSE_BODY, EquipmentModel.Layer.leatherDyeable(ResourceLocation.withDefaultNamespace(string), false))
			.build();
	}
}
