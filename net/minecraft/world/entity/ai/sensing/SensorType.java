/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.sensing.AdultSensor;
import net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor;
import net.minecraft.world.entity.ai.sensing.DummySensor;
import net.minecraft.world.entity.ai.sensing.GolemSensor;
import net.minecraft.world.entity.ai.sensing.HoglinSpecificSensor;
import net.minecraft.world.entity.ai.sensing.HurtBySensor;
import net.minecraft.world.entity.ai.sensing.NearestBedSensor;
import net.minecraft.world.entity.ai.sensing.NearestItemSensor;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.PiglinBruteSpecificSensor;
import net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor;
import net.minecraft.world.entity.ai.sensing.PlayerSensor;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.ai.sensing.VillagerBabiesSensor;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.animal.goat.GoatAi;

public class SensorType<U extends Sensor<?>> {
    public static final SensorType<DummySensor> DUMMY = SensorType.register("dummy", DummySensor::new);
    public static final SensorType<NearestItemSensor> NEAREST_ITEMS = SensorType.register("nearest_items", NearestItemSensor::new);
    public static final SensorType<NearestLivingEntitySensor> NEAREST_LIVING_ENTITIES = SensorType.register("nearest_living_entities", NearestLivingEntitySensor::new);
    public static final SensorType<PlayerSensor> NEAREST_PLAYERS = SensorType.register("nearest_players", PlayerSensor::new);
    public static final SensorType<NearestBedSensor> NEAREST_BED = SensorType.register("nearest_bed", NearestBedSensor::new);
    public static final SensorType<HurtBySensor> HURT_BY = SensorType.register("hurt_by", HurtBySensor::new);
    public static final SensorType<VillagerHostilesSensor> VILLAGER_HOSTILES = SensorType.register("villager_hostiles", VillagerHostilesSensor::new);
    public static final SensorType<VillagerBabiesSensor> VILLAGER_BABIES = SensorType.register("villager_babies", VillagerBabiesSensor::new);
    public static final SensorType<SecondaryPoiSensor> SECONDARY_POIS = SensorType.register("secondary_pois", SecondaryPoiSensor::new);
    public static final SensorType<GolemSensor> GOLEM_DETECTED = SensorType.register("golem_detected", GolemSensor::new);
    public static final SensorType<PiglinSpecificSensor> PIGLIN_SPECIFIC_SENSOR = SensorType.register("piglin_specific_sensor", PiglinSpecificSensor::new);
    public static final SensorType<PiglinBruteSpecificSensor> PIGLIN_BRUTE_SPECIFIC_SENSOR = SensorType.register("piglin_brute_specific_sensor", PiglinBruteSpecificSensor::new);
    public static final SensorType<HoglinSpecificSensor> HOGLIN_SPECIFIC_SENSOR = SensorType.register("hoglin_specific_sensor", HoglinSpecificSensor::new);
    public static final SensorType<AdultSensor> NEAREST_ADULT = SensorType.register("nearest_adult", AdultSensor::new);
    public static final SensorType<AxolotlAttackablesSensor> AXOLOTL_ATTACKABLES = SensorType.register("axolotl_attackables", AxolotlAttackablesSensor::new);
    public static final SensorType<TemptingSensor> AXOLOTL_TEMPTATIONS = SensorType.register("axolotl_temptations", () -> new TemptingSensor(AxolotlAi.getTemptations()));
    public static final SensorType<TemptingSensor> GOAT_TEMPTATIONS = SensorType.register("goat_temptations", () -> new TemptingSensor(GoatAi.getTemptations()));
    private final Supplier<U> factory;

    private SensorType(Supplier<U> supplier) {
        this.factory = supplier;
    }

    public U create() {
        return (U)((Sensor)this.factory.get());
    }

    private static <U extends Sensor<?>> SensorType<U> register(String string, Supplier<U> supplier) {
        return Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(string), new SensorType<U>(supplier));
    }
}

