package net.minecraft.data.tags;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTagsProvider extends TagsProvider<GameEvent> {
	@VisibleForTesting
	static final GameEvent[] VIBRATIONS_EXCEPT_FLAP = new GameEvent[]{
		GameEvent.BLOCK_ATTACH,
		GameEvent.BLOCK_CHANGE,
		GameEvent.BLOCK_CLOSE,
		GameEvent.BLOCK_DESTROY,
		GameEvent.BLOCK_DETACH,
		GameEvent.BLOCK_OPEN,
		GameEvent.BLOCK_PLACE,
		GameEvent.BLOCK_ACTIVATE,
		GameEvent.BLOCK_DEACTIVATE,
		GameEvent.CONTAINER_CLOSE,
		GameEvent.CONTAINER_OPEN,
		GameEvent.DISPENSE_FAIL,
		GameEvent.DRINK,
		GameEvent.EAT,
		GameEvent.ELYTRA_GLIDE,
		GameEvent.ENTITY_DAMAGE,
		GameEvent.ENTITY_DIE,
		GameEvent.ENTITY_INTERACT,
		GameEvent.ENTITY_PLACE,
		GameEvent.ENTITY_ROAR,
		GameEvent.ENTITY_SHAKE,
		GameEvent.EQUIP,
		GameEvent.EXPLODE,
		GameEvent.FLUID_PICKUP,
		GameEvent.FLUID_PLACE,
		GameEvent.HIT_GROUND,
		GameEvent.INSTRUMENT_PLAY,
		GameEvent.ITEM_INTERACT_FINISH,
		GameEvent.LIGHTNING_STRIKE,
		GameEvent.NOTE_BLOCK_PLAY,
		GameEvent.PISTON_CONTRACT,
		GameEvent.PISTON_EXTEND,
		GameEvent.PRIME_FUSE,
		GameEvent.PROJECTILE_LAND,
		GameEvent.PROJECTILE_SHOOT,
		GameEvent.SHEAR,
		GameEvent.SPLASH,
		GameEvent.STEP,
		GameEvent.SWIM,
		GameEvent.TELEPORT
	};

	public GameEventTagsProvider(PackOutput packOutput) {
		super(packOutput, Registry.GAME_EVENT);
	}

	@Override
	protected void addTags() {
		this.tag(GameEventTags.VIBRATIONS).add(VIBRATIONS_EXCEPT_FLAP).add(GameEvent.FLAP);
		this.tag(GameEventTags.SHRIEKER_CAN_LISTEN).add(GameEvent.SCULK_SENSOR_TENDRILS_CLICKING);
		this.tag(GameEventTags.WARDEN_CAN_LISTEN).add(VIBRATIONS_EXCEPT_FLAP).add(GameEvent.SHRIEK).addTag(GameEventTags.SHRIEKER_CAN_LISTEN);
		this.tag(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)
			.add(GameEvent.HIT_GROUND, GameEvent.PROJECTILE_SHOOT, GameEvent.STEP, GameEvent.SWIM, GameEvent.ITEM_INTERACT_START, GameEvent.ITEM_INTERACT_FINISH);
		this.tag(GameEventTags.ALLAY_CAN_LISTEN).add(GameEvent.NOTE_BLOCK_PLAY);
	}
}
