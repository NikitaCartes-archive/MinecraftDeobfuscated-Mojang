package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class LighthingBoltPredicate implements EntitySubPredicate {
	private static final String BLOCKS_SET_ON_FIRE_KEY = "blocks_set_on_fire";
	private static final String ENTITY_STRUCK_KEY = "entity_struck";
	private final MinMaxBounds.Ints blocksSetOnFire;
	private final EntityPredicate entityStruck;

	private LighthingBoltPredicate(MinMaxBounds.Ints ints, EntityPredicate entityPredicate) {
		this.blocksSetOnFire = ints;
		this.entityStruck = entityPredicate;
	}

	public static LighthingBoltPredicate blockSetOnFire(MinMaxBounds.Ints ints) {
		return new LighthingBoltPredicate(ints, EntityPredicate.ANY);
	}

	public static LighthingBoltPredicate fromJson(JsonObject jsonObject) {
		return new LighthingBoltPredicate(MinMaxBounds.Ints.fromJson(jsonObject.get("blocks_set_on_fire")), EntityPredicate.fromJson(jsonObject.get("entity_struck")));
	}

	@Override
	public JsonObject serializeCustomData() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("blocks_set_on_fire", this.blocksSetOnFire.serializeToJson());
		jsonObject.add("entity_struck", this.entityStruck.serializeToJson());
		return jsonObject;
	}

	@Override
	public EntitySubPredicate.Type type() {
		return EntitySubPredicate.Types.LIGHTNING;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		return !(entity instanceof LightningBolt lightningBolt)
			? false
			: this.blocksSetOnFire.matches(lightningBolt.getBlocksSetOnFire())
				&& (this.entityStruck == EntityPredicate.ANY || lightningBolt.getHitEntities().anyMatch(entityx -> this.entityStruck.matches(serverLevel, vec3, entityx)));
	}
}
