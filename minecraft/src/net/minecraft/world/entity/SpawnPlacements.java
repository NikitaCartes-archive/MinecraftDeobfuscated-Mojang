package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnPlacements {
	private static final Map<EntityType<?>, SpawnPlacements.Data> DATA_BY_TYPE = Maps.<EntityType<?>, SpawnPlacements.Data>newHashMap();

	private static <T extends Mob> void register(
		EntityType<T> entityType, SpawnPlacementType spawnPlacementType, Heightmap.Types types, SpawnPlacements.SpawnPredicate<T> spawnPredicate
	) {
		SpawnPlacements.Data data = (SpawnPlacements.Data)DATA_BY_TYPE.put(entityType, new SpawnPlacements.Data(types, spawnPlacementType, spawnPredicate));
		if (data != null) {
			throw new IllegalStateException("Duplicate registration for type " + BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
		}
	}

	public static SpawnPlacementType getPlacementType(EntityType<?> entityType) {
		SpawnPlacements.Data data = (SpawnPlacements.Data)DATA_BY_TYPE.get(entityType);
		return data == null ? SpawnPlacementTypes.NO_RESTRICTIONS : data.placement;
	}

	public static boolean isSpawnPositionOk(EntityType<?> entityType, LevelReader levelReader, BlockPos blockPos) {
		return getPlacementType(entityType).isSpawnPositionOk(levelReader, blockPos, entityType);
	}

	public static Heightmap.Types getHeightmapType(@Nullable EntityType<?> entityType) {
		SpawnPlacements.Data data = (SpawnPlacements.Data)DATA_BY_TYPE.get(entityType);
		return data == null ? Heightmap.Types.MOTION_BLOCKING_NO_LEAVES : data.heightMap;
	}

	public static <T extends Entity> boolean checkSpawnRules(
		EntityType<T> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		SpawnPlacements.Data data = (SpawnPlacements.Data)DATA_BY_TYPE.get(entityType);
		return data == null || data.predicate.test(entityType, serverLevelAccessor, mobSpawnType, blockPos, randomSource);
	}

	static {
		register(EntityType.AXOLOTL, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Axolotl::checkAxolotlSpawnRules);
		register(EntityType.COD, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
		register(EntityType.DOLPHIN, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
		register(EntityType.DROWNED, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Drowned::checkDrownedSpawnRules);
		register(EntityType.GUARDIAN, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Guardian::checkGuardianSpawnRules);
		register(EntityType.PUFFERFISH, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
		register(EntityType.SALMON, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
		register(EntityType.SQUID, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
		register(EntityType.TROPICAL_FISH, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TropicalFish::checkTropicalFishSpawnRules);
		register(EntityType.ARMADILLO, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Armadillo::checkArmadilloSpawnRules);
		register(EntityType.BAT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Bat::checkBatSpawnRules);
		register(EntityType.BATATO, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Bat::checkBatSpawnRules);
		register(EntityType.BLAZE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules);
		register(EntityType.BOGGED, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.CAVE_SPIDER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.CHICKEN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.COW, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.CREEPER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.DONKEY, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.ENDERMAN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.ENDERMITE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Endermite::checkEndermiteSpawnRules);
		register(EntityType.ENDER_DRAGON, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.FROG, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Frog::checkFrogSpawnRules);
		register(EntityType.GHAST, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Ghast::checkGhastSpawnRules);
		register(EntityType.GIANT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.GLOW_SQUID, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GlowSquid::checkGlowSquidSpawnRules);
		register(EntityType.GOAT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Goat::checkGoatSpawnRules);
		register(EntityType.HORSE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.HUSK, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Husk::checkHuskSpawnRules);
		register(EntityType.IRON_GOLEM, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.LLAMA, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.MAGMA_CUBE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MagmaCube::checkMagmaCubeSpawnRules);
		register(EntityType.MOOSHROOM, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MushroomCow::checkMushroomSpawnRules);
		register(EntityType.MULE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.OCELOT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Ocelot::checkOcelotSpawnRules);
		register(EntityType.PARROT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Parrot::checkParrotSpawnRules);
		register(EntityType.PIG, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.HOGLIN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Hoglin::checkHoglinSpawnRules);
		register(EntityType.PIGLIN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Piglin::checkPiglinSpawnRules);
		register(EntityType.PILLAGER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PatrollingMonster::checkPatrollingMonsterSpawnRules);
		register(EntityType.POLAR_BEAR, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PolarBear::checkPolarBearSpawnRules);
		register(EntityType.POISONOUS_POTATO_ZOMBIE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.RABBIT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Rabbit::checkRabbitSpawnRules);
		register(EntityType.SHEEP, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.SILVERFISH, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Silverfish::checkSilverfishSpawnRules);
		register(EntityType.SKELETON, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.SKELETON_HORSE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SkeletonHorse::checkSkeletonHorseSpawnRules);
		register(EntityType.SLIME, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Slime::checkSlimeSpawnRules);
		register(EntityType.MEGA_SPUD, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.SNOW_GOLEM, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.SPIDER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.STRAY, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Stray::checkStraySpawnRules);
		register(EntityType.STRIDER, SpawnPlacementTypes.IN_LAVA, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Strider::checkStriderSpawnRules);
		register(EntityType.TURTLE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Turtle::checkTurtleSpawnRules);
		register(EntityType.VILLAGER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.WITCH, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Witch::checkWitchSpawnRules);
		register(EntityType.WITHER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.WITHER_SKELETON, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.WOLF, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Wolf::checkWolfSpawnRules);
		register(EntityType.ZOMBIE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.ZOMBIE_HORSE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ZombieHorse::checkZombieHorseSpawnRules);
		register(
			EntityType.ZOMBIFIED_PIGLIN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ZombifiedPiglin::checkZombifiedPiglinSpawnRules
		);
		register(EntityType.ZOMBIE_VILLAGER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.CAT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.ELDER_GUARDIAN, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Guardian::checkGuardianSpawnRules);
		register(EntityType.TOXIFIN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.PLAGUEWHALE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.EVOKER, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.FOX, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Fox::checkFoxSpawnRules);
		register(EntityType.ILLUSIONER, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.PANDA, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.PHANTOM, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.RAVAGER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.SHULKER, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.TRADER_LLAMA, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		register(EntityType.VEX, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.VINDICATOR, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		register(EntityType.WANDERING_TRADER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		register(EntityType.WARDEN, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
	}

	static record Data(Heightmap.Types heightMap, SpawnPlacementType placement, SpawnPlacements.SpawnPredicate<?> predicate) {
	}

	@FunctionalInterface
	public interface SpawnPredicate<T extends Entity> {
		boolean test(EntityType<T> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource);
	}
}
