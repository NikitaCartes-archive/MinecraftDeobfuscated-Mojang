package net.minecraft.voting.rules.actual;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.voting.rules.ResourceKeyReplacementRule;

public class SoundEventReplacementRule extends ResourceKeyReplacementRule<SoundEvent> {
	private final Map<SoundEvent, SoundEvent> cache = new HashMap();

	public SoundEventReplacementRule() {
		super(Registries.SOUND_EVENT);
	}

	public SoundEvent replace(SoundEvent soundEvent) {
		return (SoundEvent)this.cache.computeIfAbsent(soundEvent, soundEvent2 -> {
			ResourceKey<SoundEvent> resourceKey = (ResourceKey<SoundEvent>)BuiltInRegistries.SOUND_EVENT.getResourceKey(soundEvent2).orElse(null);
			if (resourceKey != null) {
				ResourceKey<SoundEvent> resourceKey2 = (ResourceKey<SoundEvent>)this.entries.get(resourceKey);
				if (resourceKey2 != null) {
					SoundEvent soundEvent3 = BuiltInRegistries.SOUND_EVENT.get(resourceKey2);
					if (soundEvent3 != null) {
						return soundEvent3;
					}
				}
			}

			return soundEvent;
		});
	}

	@Override
	protected void set(ResourceKey<SoundEvent> resourceKey, ResourceKey<SoundEvent> resourceKey2) {
		super.set(resourceKey, resourceKey2);
		this.cache.clear();
	}

	@Override
	protected void remove(ResourceKey<SoundEvent> resourceKey) {
		super.remove(resourceKey);
		this.cache.clear();
	}

	protected Component description(ResourceKey<SoundEvent> resourceKey, ResourceKey<SoundEvent> resourceKey2) {
		return Component.translatable(
			"rule.replace_sound", Component.literal(resourceKey.location().toShortLanguageKey()), Component.literal(resourceKey2.location().toShortLanguageKey())
		);
	}
}
