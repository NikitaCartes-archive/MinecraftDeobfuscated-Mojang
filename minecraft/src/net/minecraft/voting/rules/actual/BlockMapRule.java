package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.voting.rules.MapRule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.level.block.Block;

public abstract class BlockMapRule<V> extends MapRule<ResourceKey<Block>, V> {
	private final Map<ResourceKey<Block>, V> entries = new HashMap();

	public BlockMapRule(Codec<V> codec) {
		super(ResourceKey.codec(Registries.BLOCK), codec);
	}

	@Nullable
	public V get(Block block) {
		return (V)this.entries.get(block.builtInRegistryHolder().key());
	}

	protected void set(ResourceKey<Block> resourceKey, V object) {
		this.entries.put(resourceKey, object);
	}

	protected void remove(ResourceKey<Block> resourceKey) {
		this.entries.remove(resourceKey);
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.entries.entrySet().stream().map(entry -> new MapRule.MapRuleChange((ResourceKey)entry.getKey(), entry.getValue()));
	}
}
