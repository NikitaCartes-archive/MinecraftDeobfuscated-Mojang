package net.minecraft.voting.rules.actual;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.voting.rules.ResourceKeySingleRule;
import net.minecraft.world.level.block.Block;

public abstract class BlockReplaceSingleRule extends ResourceKeySingleRule<Block> {
	protected final Block defaultBlock;

	public BlockReplaceSingleRule(Block block) {
		super(Registries.BLOCK, block.builtInRegistryHolder().key());
		this.defaultBlock = block;
	}

	public Block get() {
		return this.currentValue() == this.defaultValue()
			? this.defaultBlock
			: (Block)Objects.requireNonNullElse(BuiltInRegistries.BLOCK.get(this.currentValue()), this.defaultBlock);
	}
}
