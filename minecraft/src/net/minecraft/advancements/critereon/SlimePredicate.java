package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public class SlimePredicate implements EntitySubPredicate {
	private final MinMaxBounds.Ints size;

	private SlimePredicate(MinMaxBounds.Ints ints) {
		this.size = ints;
	}

	public static SlimePredicate sized(MinMaxBounds.Ints ints) {
		return new SlimePredicate(ints);
	}

	public static SlimePredicate fromJson(JsonObject jsonObject) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("size"));
		return new SlimePredicate(ints);
	}

	@Override
	public JsonObject serializeCustomData() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("size", this.size.serializeToJson());
		return jsonObject;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		return entity instanceof Slime slime ? this.size.matches(slime.getSize()) : false;
	}

	@Override
	public EntitySubPredicate.Type type() {
		return EntitySubPredicate.Types.SLIME;
	}
}
