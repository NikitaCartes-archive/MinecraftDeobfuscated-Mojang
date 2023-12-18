package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record GameEvent(int notificationRadius) {
	public static final Holder.Reference<GameEvent> BLOCK_ACTIVATE = register("block_activate");
	public static final Holder.Reference<GameEvent> BLOCK_ATTACH = register("block_attach");
	public static final Holder.Reference<GameEvent> BLOCK_CHANGE = register("block_change");
	public static final Holder.Reference<GameEvent> BLOCK_CLOSE = register("block_close");
	public static final Holder.Reference<GameEvent> BLOCK_DEACTIVATE = register("block_deactivate");
	public static final Holder.Reference<GameEvent> BLOCK_DESTROY = register("block_destroy");
	public static final Holder.Reference<GameEvent> BLOCK_DETACH = register("block_detach");
	public static final Holder.Reference<GameEvent> BLOCK_OPEN = register("block_open");
	public static final Holder.Reference<GameEvent> BLOCK_PLACE = register("block_place");
	public static final Holder.Reference<GameEvent> CONTAINER_CLOSE = register("container_close");
	public static final Holder.Reference<GameEvent> CONTAINER_OPEN = register("container_open");
	public static final Holder.Reference<GameEvent> DRINK = register("drink");
	public static final Holder.Reference<GameEvent> EAT = register("eat");
	public static final Holder.Reference<GameEvent> ELYTRA_GLIDE = register("elytra_glide");
	public static final Holder.Reference<GameEvent> ENTITY_DAMAGE = register("entity_damage");
	public static final Holder.Reference<GameEvent> ENTITY_DIE = register("entity_die");
	public static final Holder.Reference<GameEvent> ENTITY_DISMOUNT = register("entity_dismount");
	public static final Holder.Reference<GameEvent> ENTITY_INTERACT = register("entity_interact");
	public static final Holder.Reference<GameEvent> ENTITY_MOUNT = register("entity_mount");
	public static final Holder.Reference<GameEvent> ENTITY_PLACE = register("entity_place");
	public static final Holder.Reference<GameEvent> ENTITY_ACTION = register("entity_action");
	public static final Holder.Reference<GameEvent> EQUIP = register("equip");
	public static final Holder.Reference<GameEvent> EXPLODE = register("explode");
	public static final Holder.Reference<GameEvent> FLAP = register("flap");
	public static final Holder.Reference<GameEvent> FLUID_PICKUP = register("fluid_pickup");
	public static final Holder.Reference<GameEvent> FLUID_PLACE = register("fluid_place");
	public static final Holder.Reference<GameEvent> HIT_GROUND = register("hit_ground");
	public static final Holder.Reference<GameEvent> INSTRUMENT_PLAY = register("instrument_play");
	public static final Holder.Reference<GameEvent> ITEM_INTERACT_FINISH = register("item_interact_finish");
	public static final Holder.Reference<GameEvent> ITEM_INTERACT_START = register("item_interact_start");
	public static final Holder.Reference<GameEvent> JUKEBOX_PLAY = register("jukebox_play", 10);
	public static final Holder.Reference<GameEvent> JUKEBOX_STOP_PLAY = register("jukebox_stop_play", 10);
	public static final Holder.Reference<GameEvent> LIGHTNING_STRIKE = register("lightning_strike");
	public static final Holder.Reference<GameEvent> NOTE_BLOCK_PLAY = register("note_block_play");
	public static final Holder.Reference<GameEvent> PRIME_FUSE = register("prime_fuse");
	public static final Holder.Reference<GameEvent> PROJECTILE_LAND = register("projectile_land");
	public static final Holder.Reference<GameEvent> PROJECTILE_SHOOT = register("projectile_shoot");
	public static final Holder.Reference<GameEvent> SCULK_SENSOR_TENDRILS_CLICKING = register("sculk_sensor_tendrils_clicking");
	public static final Holder.Reference<GameEvent> SHEAR = register("shear");
	public static final Holder.Reference<GameEvent> SHRIEK = register("shriek", 32);
	public static final Holder.Reference<GameEvent> SPLASH = register("splash");
	public static final Holder.Reference<GameEvent> STEP = register("step");
	public static final Holder.Reference<GameEvent> SWIM = register("swim");
	public static final Holder.Reference<GameEvent> TELEPORT = register("teleport");
	public static final Holder.Reference<GameEvent> UNEQUIP = register("unequip");
	public static final Holder.Reference<GameEvent> RESONATE_1 = register("resonate_1");
	public static final Holder.Reference<GameEvent> RESONATE_2 = register("resonate_2");
	public static final Holder.Reference<GameEvent> RESONATE_3 = register("resonate_3");
	public static final Holder.Reference<GameEvent> RESONATE_4 = register("resonate_4");
	public static final Holder.Reference<GameEvent> RESONATE_5 = register("resonate_5");
	public static final Holder.Reference<GameEvent> RESONATE_6 = register("resonate_6");
	public static final Holder.Reference<GameEvent> RESONATE_7 = register("resonate_7");
	public static final Holder.Reference<GameEvent> RESONATE_8 = register("resonate_8");
	public static final Holder.Reference<GameEvent> RESONATE_9 = register("resonate_9");
	public static final Holder.Reference<GameEvent> RESONATE_10 = register("resonate_10");
	public static final Holder.Reference<GameEvent> RESONATE_11 = register("resonate_11");
	public static final Holder.Reference<GameEvent> RESONATE_12 = register("resonate_12");
	public static final Holder.Reference<GameEvent> RESONATE_13 = register("resonate_13");
	public static final Holder.Reference<GameEvent> RESONATE_14 = register("resonate_14");
	public static final Holder.Reference<GameEvent> RESONATE_15 = register("resonate_15");
	public static final int DEFAULT_NOTIFICATION_RADIUS = 16;

	public static Holder<GameEvent> bootstrap(Registry<GameEvent> registry) {
		return BLOCK_ACTIVATE;
	}

	private static Holder.Reference<GameEvent> register(String string) {
		return register(string, 16);
	}

	private static Holder.Reference<GameEvent> register(String string, int i) {
		return Registry.registerForHolder(BuiltInRegistries.GAME_EVENT, new ResourceLocation(string), new GameEvent(i));
	}

	public static record Context(@Nullable Entity sourceEntity, @Nullable BlockState affectedState) {
		public static GameEvent.Context of(@Nullable Entity entity) {
			return new GameEvent.Context(entity, null);
		}

		public static GameEvent.Context of(@Nullable BlockState blockState) {
			return new GameEvent.Context(null, blockState);
		}

		public static GameEvent.Context of(@Nullable Entity entity, @Nullable BlockState blockState) {
			return new GameEvent.Context(entity, blockState);
		}
	}

	public static final class ListenerInfo implements Comparable<GameEvent.ListenerInfo> {
		private final Holder<GameEvent> gameEvent;
		private final Vec3 source;
		private final GameEvent.Context context;
		private final GameEventListener recipient;
		private final double distanceToRecipient;

		public ListenerInfo(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context, GameEventListener gameEventListener, Vec3 vec32) {
			this.gameEvent = holder;
			this.source = vec3;
			this.context = context;
			this.recipient = gameEventListener;
			this.distanceToRecipient = vec3.distanceToSqr(vec32);
		}

		public int compareTo(GameEvent.ListenerInfo listenerInfo) {
			return Double.compare(this.distanceToRecipient, listenerInfo.distanceToRecipient);
		}

		public Holder<GameEvent> gameEvent() {
			return this.gameEvent;
		}

		public Vec3 source() {
			return this.source;
		}

		public GameEvent.Context context() {
			return this.context;
		}

		public GameEventListener recipient() {
			return this.recipient;
		}
	}
}
