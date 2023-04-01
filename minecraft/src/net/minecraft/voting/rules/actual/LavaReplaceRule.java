package net.minecraft.voting.rules.actual;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class LavaReplaceRule extends BlockReplaceSingleRule {
	private final String descriptionId;

	public LavaReplaceRule(String string, Block block) {
		super(block);
		this.descriptionId = string;
	}

	@Override
	protected Component valueDescription(ResourceKey<Block> resourceKey) {
		Component component = Component.translatable(this.defaultBlock.getDescriptionId());
		Component component2 = Component.translatable(Util.makeDescriptionId("block", resourceKey.location()));
		return Component.translatable(this.descriptionId, component2, component);
	}
}
