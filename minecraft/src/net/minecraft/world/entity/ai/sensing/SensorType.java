package net.minecraft.world.entity.ai.sensing;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.animal.camel.CamelAi;
import net.minecraft.world.entity.animal.frog.FrogAi;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.animal.sniffer.SnifferAi;

public class SensorType<U extends Sensor<?>> {
	public static final SensorType<DummySensor> DUMMY = register("dummy", DummySensor::new);
	public static final SensorType<NearestItemSensor> NEAREST_ITEMS = register("nearest_items", NearestItemSensor::new);
	public static final SensorType<NearestLivingEntitySensor<LivingEntity>> NEAREST_LIVING_ENTITIES = register(
		"nearest_living_entities", NearestLivingEntitySensor::new
	);
	public static final SensorType<PlayerSensor> NEAREST_PLAYERS = register("nearest_players", PlayerSensor::new);
	public static final SensorType<NearestBedSensor> NEAREST_BED = register("nearest_bed", NearestBedSensor::new);
	public static final SensorType<HurtBySensor> HURT_BY = register("hurt_by", HurtBySensor::new);
	public static final SensorType<VillagerHostilesSensor> VILLAGER_HOSTILES = register("villager_hostiles", VillagerHostilesSensor::new);
	public static final SensorType<VillagerBabiesSensor> VILLAGER_BABIES = register("villager_babies", VillagerBabiesSensor::new);
	public static final SensorType<SecondaryPoiSensor> SECONDARY_POIS = register("secondary_pois", SecondaryPoiSensor::new);
	public static final SensorType<GolemSensor> GOLEM_DETECTED = register("golem_detected", GolemSensor::new);
	public static final SensorType<PiglinSpecificSensor> PIGLIN_SPECIFIC_SENSOR = register("piglin_specific_sensor", PiglinSpecificSensor::new);
	public static final SensorType<PiglinBruteSpecificSensor> PIGLIN_BRUTE_SPECIFIC_SENSOR = register(
		"piglin_brute_specific_sensor", PiglinBruteSpecificSensor::new
	);
	public static final SensorType<HoglinSpecificSensor> HOGLIN_SPECIFIC_SENSOR = register("hoglin_specific_sensor", HoglinSpecificSensor::new);
	public static final SensorType<AdultSensor> NEAREST_ADULT = register("nearest_adult", AdultSensor::new);
	public static final SensorType<AxolotlAttackablesSensor> AXOLOTL_ATTACKABLES = register("axolotl_attackables", AxolotlAttackablesSensor::new);
	public static final SensorType<TemptingSensor> AXOLOTL_TEMPTATIONS = register("axolotl_temptations", () -> new TemptingSensor(AxolotlAi.getTemptations()));
	public static final SensorType<TemptingSensor> GOAT_TEMPTATIONS = register("goat_temptations", () -> new TemptingSensor(GoatAi.getTemptations()));
	public static final SensorType<TemptingSensor> FROG_TEMPTATIONS = register("frog_temptations", () -> new TemptingSensor(FrogAi.getTemptations()));
	public static final SensorType<TemptingSensor> CAMEL_TEMPTATIONS = register("camel_temptations", () -> new TemptingSensor(CamelAi.getTemptations()));
	public static final SensorType<FrogAttackablesSensor> FROG_ATTACKABLES = register("frog_attackables", FrogAttackablesSensor::new);
	public static final SensorType<IsInWaterSensor> IS_IN_WATER = register("is_in_water", IsInWaterSensor::new);
	public static final SensorType<WardenEntitySensor> WARDEN_ENTITY_SENSOR = register("warden_entity_sensor", WardenEntitySensor::new);
	public static final SensorType<TemptingSensor> SNIFFER_TEMPTATIONS = register("sniffer_temptations", () -> new TemptingSensor(SnifferAi.getTemptations()));
	public static final SensorType<BreezeAttackEntitySensor> BREEZE_ATTACK_ENTITY_SENSOR = register("breeze_attack_entity_sensor", BreezeAttackEntitySensor::new);
	private final Supplier<U> factory;

	private SensorType(Supplier<U> supplier) {
		this.factory = supplier;
	}

	public U create() {
		return (U)this.factory.get();
	}

	private static <U extends Sensor<?>> SensorType<U> register(String string, Supplier<U> supplier) {
		return Registry.register(BuiltInRegistries.SENSOR_TYPE, new ResourceLocation(string), new SensorType<>(supplier));
	}
}
