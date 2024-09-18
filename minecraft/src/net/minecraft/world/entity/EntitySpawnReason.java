package net.minecraft.world.entity;

public enum EntitySpawnReason {
	NATURAL,
	CHUNK_GENERATION,
	SPAWNER,
	STRUCTURE,
	BREEDING,
	MOB_SUMMONED,
	JOCKEY,
	EVENT,
	CONVERSION,
	REINFORCEMENT,
	TRIGGERED,
	BUCKET,
	SPAWN_ITEM_USE,
	COMMAND,
	DISPENSER,
	PATROL,
	TRIAL_SPAWNER,
	LOAD,
	DIMENSION_TRAVEL;

	public static boolean isSpawner(EntitySpawnReason entitySpawnReason) {
		return entitySpawnReason == SPAWNER || entitySpawnReason == TRIAL_SPAWNER;
	}

	public static boolean ignoresLightRequirements(EntitySpawnReason entitySpawnReason) {
		return entitySpawnReason == TRIAL_SPAWNER;
	}
}
