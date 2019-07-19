package net.minecraft.world.level.block;

public class MelonBlock extends StemGrownBlock {
	protected MelonBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public StemBlock getStem() {
		return (StemBlock)Blocks.MELON_STEM;
	}

	@Override
	public AttachedStemBlock getAttachedStem() {
		return (AttachedStemBlock)Blocks.ATTACHED_MELON_STEM;
	}
}
