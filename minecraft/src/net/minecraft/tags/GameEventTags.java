package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
	public static final TagKey<GameEvent> VIBRATIONS = create("vibrations");
	public static final TagKey<GameEvent> WARDEN_CAN_LISTEN = create("warden_can_listen");
	public static final TagKey<GameEvent> SHRIEKER_CAN_LISTEN = create("shrieker_can_listen");
	public static final TagKey<GameEvent> IGNORE_VIBRATIONS_SNEAKING = create("ignore_vibrations_sneaking");
	public static final TagKey<GameEvent> ALLAY_CAN_LISTEN = create("allay_can_listen");

	private static TagKey<GameEvent> create(String string) {
		return TagKey.create(Registries.GAME_EVENT, ResourceLocation.withDefaultNamespace(string));
	}
}
