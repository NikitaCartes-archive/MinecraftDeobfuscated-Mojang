package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;

public class NopProcessor extends StructureProcessor {
	public static final Codec<NopProcessor> CODEC = Codec.unit((Supplier<NopProcessor>)(() -> NopProcessor.INSTANCE));
	public static final NopProcessor INSTANCE = new NopProcessor();

	private NopProcessor() {
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.NOP;
	}
}
