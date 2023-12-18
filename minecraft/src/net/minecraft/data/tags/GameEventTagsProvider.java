package net.minecraft.data.tags;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class GameEventTagsProvider extends TagsProvider<GameEvent> {
	@VisibleForTesting
	static final List<ResourceKey<GameEvent>> VIBRATIONS_EXCEPT_FLAP = List.of(
		GameEvent.BLOCK_ATTACH.key(),
		GameEvent.BLOCK_CHANGE.key(),
		GameEvent.BLOCK_CLOSE.key(),
		GameEvent.BLOCK_DESTROY.key(),
		GameEvent.BLOCK_DETACH.key(),
		GameEvent.BLOCK_OPEN.key(),
		GameEvent.BLOCK_PLACE.key(),
		GameEvent.BLOCK_ACTIVATE.key(),
		GameEvent.BLOCK_DEACTIVATE.key(),
		GameEvent.CONTAINER_CLOSE.key(),
		GameEvent.CONTAINER_OPEN.key(),
		GameEvent.DRINK.key(),
		GameEvent.EAT.key(),
		GameEvent.ELYTRA_GLIDE.key(),
		GameEvent.ENTITY_DAMAGE.key(),
		GameEvent.ENTITY_DIE.key(),
		GameEvent.ENTITY_DISMOUNT.key(),
		GameEvent.ENTITY_INTERACT.key(),
		GameEvent.ENTITY_MOUNT.key(),
		GameEvent.ENTITY_PLACE.key(),
		GameEvent.ENTITY_ACTION.key(),
		GameEvent.EQUIP.key(),
		GameEvent.EXPLODE.key(),
		GameEvent.FLUID_PICKUP.key(),
		GameEvent.FLUID_PLACE.key(),
		GameEvent.HIT_GROUND.key(),
		GameEvent.INSTRUMENT_PLAY.key(),
		GameEvent.ITEM_INTERACT_FINISH.key(),
		GameEvent.LIGHTNING_STRIKE.key(),
		GameEvent.NOTE_BLOCK_PLAY.key(),
		GameEvent.PRIME_FUSE.key(),
		GameEvent.PROJECTILE_LAND.key(),
		GameEvent.PROJECTILE_SHOOT.key(),
		GameEvent.SHEAR.key(),
		GameEvent.SPLASH.key(),
		GameEvent.STEP.key(),
		GameEvent.SWIM.key(),
		GameEvent.TELEPORT.key(),
		GameEvent.UNEQUIP.key()
	);

	public GameEventTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.GAME_EVENT, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(GameEventTags.VIBRATIONS).addAll(VIBRATIONS_EXCEPT_FLAP).addAll(VibrationSystem.RESONANCE_EVENTS).add(GameEvent.FLAP.key());
		this.tag(GameEventTags.SHRIEKER_CAN_LISTEN).add(GameEvent.SCULK_SENSOR_TENDRILS_CLICKING.key());
		this.tag(GameEventTags.WARDEN_CAN_LISTEN)
			.addAll(VIBRATIONS_EXCEPT_FLAP)
			.addAll(VibrationSystem.RESONANCE_EVENTS)
			.add(GameEvent.SHRIEK.key())
			.addTag(GameEventTags.SHRIEKER_CAN_LISTEN);
		this.tag(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)
			.add(
				GameEvent.HIT_GROUND.key(),
				GameEvent.PROJECTILE_SHOOT.key(),
				GameEvent.STEP.key(),
				GameEvent.SWIM.key(),
				GameEvent.ITEM_INTERACT_START.key(),
				GameEvent.ITEM_INTERACT_FINISH.key()
			);
		this.tag(GameEventTags.ALLAY_CAN_LISTEN).add(GameEvent.NOTE_BLOCK_PLAY.key());
	}
}
