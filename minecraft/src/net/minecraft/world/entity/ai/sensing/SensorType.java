package net.minecraft.world.entity.ai.sensing;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class SensorType<U extends Sensor<?>> {
	public static final SensorType<DummySensor> DUMMY = register("dummy", DummySensor::new);
	public static final SensorType<NearestItemSensor> NEAREST_ITEMS = register("nearest_items", NearestItemSensor::new);
	public static final SensorType<NearestLivingEntitySensor> NEAREST_LIVING_ENTITIES = register("nearest_living_entities", NearestLivingEntitySensor::new);
	public static final SensorType<PlayerSensor> NEAREST_PLAYERS = register("nearest_players", PlayerSensor::new);
	public static final SensorType<InteractableDoorsSensor> INTERACTABLE_DOORS = register("interactable_doors", InteractableDoorsSensor::new);
	public static final SensorType<NearestBedSensor> NEAREST_BED = register("nearest_bed", NearestBedSensor::new);
	public static final SensorType<HurtBySensor> HURT_BY = register("hurt_by", HurtBySensor::new);
	public static final SensorType<VillagerHostilesSensor> VILLAGER_HOSTILES = register("villager_hostiles", VillagerHostilesSensor::new);
	public static final SensorType<VillagerBabiesSensor> VILLAGER_BABIES = register("villager_babies", VillagerBabiesSensor::new);
	public static final SensorType<SecondaryPoiSensor> SECONDARY_POIS = register("secondary_pois", SecondaryPoiSensor::new);
	public static final SensorType<GolemSensor> GOLEM_LAST_SEEN = register("golem_last_seen", GolemSensor::new);
	public static final SensorType<PiglinSpecificSensor> PIGLIN_SPECIFIC_SENSOR = register("piglin_specific_sensor", PiglinSpecificSensor::new);
	public static final SensorType<HoglinSpecificSensor> HOGLIN_SPECIFIC_SENSOR = register("hoglin_specific_sensor", HoglinSpecificSensor::new);
	public static final SensorType<AdultSensor> NEAREST_ADULT = register("nearest_adult", AdultSensor::new);
	private final Supplier<U> factory;

	private SensorType(Supplier<U> supplier) {
		this.factory = supplier;
	}

	public U create() {
		return (U)this.factory.get();
	}

	private static <U extends Sensor<?>> SensorType<U> register(String string, Supplier<U> supplier) {
		return Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(string), new SensorType<>(supplier));
	}
}
