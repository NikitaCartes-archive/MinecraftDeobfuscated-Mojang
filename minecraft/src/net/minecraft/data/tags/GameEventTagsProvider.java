package net.minecraft.data.tags;

import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTagsProvider extends TagsProvider<GameEvent> {
	public GameEventTagsProvider(DataGenerator dataGenerator) {
		super(dataGenerator, Registry.GAME_EVENT);
	}

	@Override
	protected void addTags() {
		this.tag(GameEventTags.VIBRATIONS)
			.add(
				GameEvent.BLOCK_ATTACH,
				GameEvent.BLOCK_CHANGE,
				GameEvent.BLOCK_CLOSE,
				GameEvent.BLOCK_DESTROY,
				GameEvent.BLOCK_DETACH,
				GameEvent.BLOCK_OPEN,
				GameEvent.BLOCK_PLACE,
				GameEvent.BLOCK_PRESS,
				GameEvent.BLOCK_SWITCH,
				GameEvent.BLOCK_UNPRESS,
				GameEvent.BLOCK_UNSWITCH,
				GameEvent.CONTAINER_CLOSE,
				GameEvent.CONTAINER_OPEN,
				GameEvent.DISPENSE_FAIL,
				GameEvent.DRINKING_FINISH,
				GameEvent.EAT,
				GameEvent.ELYTRA_FREE_FALL,
				GameEvent.ENTITY_DAMAGED,
				GameEvent.ENTITY_KILLED,
				GameEvent.ENTITY_PLACE,
				GameEvent.EQUIP,
				GameEvent.EXPLODE,
				GameEvent.FISHING_ROD_CAST,
				GameEvent.FISHING_ROD_REEL_IN,
				GameEvent.FLAP,
				GameEvent.FLUID_PICKUP,
				GameEvent.FLUID_PLACE,
				GameEvent.HIT_GROUND,
				GameEvent.MOB_INTERACT,
				GameEvent.LIGHTNING_STRIKE,
				GameEvent.MINECART_MOVING,
				GameEvent.PISTON_CONTRACT,
				GameEvent.PISTON_EXTEND,
				GameEvent.PRIME_FUSE,
				GameEvent.PROJECTILE_LAND,
				GameEvent.PROJECTILE_SHOOT,
				GameEvent.RAVAGER_ROAR,
				GameEvent.RING_BELL,
				GameEvent.SHEAR,
				GameEvent.SHULKER_CLOSE,
				GameEvent.SHULKER_OPEN,
				GameEvent.SPLASH,
				GameEvent.STEP,
				GameEvent.SWIM,
				GameEvent.WOLF_SHAKING
			);
		this.tag(GameEventTags.WARDEN_EVENTS_CAN_LISTEN).addTag(GameEventTags.VIBRATIONS).add(GameEvent.SCULK_SENSOR_TENDRILS_CLICKING).add(GameEvent.SHRIEK);
		this.tag(GameEventTags.IGNORE_VIBRATIONS_SNEAKING).add(GameEvent.HIT_GROUND, GameEvent.PROJECTILE_SHOOT, GameEvent.STEP, GameEvent.SWIM);
	}

	@Override
	protected Path getPath(ResourceLocation resourceLocation) {
		return this.generator.getOutputFolder().resolve("data/" + resourceLocation.getNamespace() + "/tags/game_events/" + resourceLocation.getPath() + ".json");
	}

	@Override
	public String getName() {
		return "Game Event Tags";
	}
}
