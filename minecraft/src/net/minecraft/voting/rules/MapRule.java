package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

public abstract class MapRule<K, V> implements Rule {
	private final Codec<MapRule<K, V>.MapRuleChange> codec;

	protected MapRule(Codec<K> codec, Codec<V> codec2) {
		this.codec = RecordCodecBuilder.create(
			instance -> instance.group(
						codec.fieldOf("key").forGetter(mapRuleChange -> mapRuleChange.key), codec2.fieldOf("value").forGetter(mapRuleChange -> mapRuleChange.value)
					)
					.apply(instance, (object, object2) -> new MapRule.MapRuleChange(object, object2))
		);
	}

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	protected abstract Component description(K object, V object2);

	protected abstract void set(K object, V object2);

	protected abstract void remove(K object);

	protected class MapRuleChange implements RuleChange.Simple {
		final K key;
		final V value;
		private final Component description;

		public MapRuleChange(K object, V object2) {
			this.key = object;
			this.value = object2;
			this.description = MapRule.this.description(object, object2);
		}

		@Override
		public Rule rule() {
			return MapRule.this;
		}

		@Override
		public void update(RuleAction ruleAction) {
			switch (ruleAction) {
				case APPROVE:
					MapRule.this.set(this.key, this.value);
					break;
				case REPEAL:
					MapRule.this.remove(this.key);
			}
		}

		@Override
		public Component description() {
			return this.description;
		}
	}
}
