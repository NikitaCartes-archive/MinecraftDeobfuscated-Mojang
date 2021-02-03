package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
	protected static final StaticTagHelper<GameEvent> HELPER = StaticTags.create(Registry.GAME_EVENT_REGISTRY, "tags/game_events");
	public static final Tag.Named<GameEvent> VIBRATIONS = bind("vibrations");
	public static final Tag.Named<GameEvent> IGNORE_VIBRATIONS_SNEAKING = bind("ignore_vibrations_sneaking");

	private static Tag.Named<GameEvent> bind(String string) {
		return HELPER.bind(string);
	}
}
