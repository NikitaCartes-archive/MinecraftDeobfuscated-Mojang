package net.minecraft.world.level.block;

public abstract class StemGrownBlock extends Block {
	public StemGrownBlock(Block.Properties properties) {
		super(properties);
	}

	public abstract StemBlock getStem();

	public abstract AttachedStemBlock getAttachedStem();
}
