package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GeodeBlockSettings {
	public final BlockStateProvider fillingProvider;
	public final BlockStateProvider innerLayerProvider;
	public final BlockStateProvider alternateInnerLayerProvider;
	public final BlockStateProvider middleLayerProvider;
	public final BlockStateProvider outerLayerProvider;
	public final List<BlockState> innerPlacements;
	public final TagKey<Block> cannotReplace;
	public final TagKey<Block> invalidBlocks;
	public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("filling_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.fillingProvider),
					BlockStateProvider.CODEC.fieldOf("inner_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.innerLayerProvider),
					BlockStateProvider.CODEC.fieldOf("alternate_inner_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.alternateInnerLayerProvider),
					BlockStateProvider.CODEC.fieldOf("middle_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.middleLayerProvider),
					BlockStateProvider.CODEC.fieldOf("outer_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.outerLayerProvider),
					BlockState.CODEC.listOf().fieldOf("inner_placements").forGetter(geodeBlockSettings -> geodeBlockSettings.innerPlacements),
					TagKey.hashedCodec(Registries.BLOCK).fieldOf("cannot_replace").forGetter(geodeBlockSettings -> geodeBlockSettings.cannotReplace),
					TagKey.hashedCodec(Registries.BLOCK).fieldOf("invalid_blocks").forGetter(geodeBlockSettings -> geodeBlockSettings.invalidBlocks)
				)
				.apply(instance, GeodeBlockSettings::new)
	);

	public GeodeBlockSettings(
		BlockStateProvider blockStateProvider,
		BlockStateProvider blockStateProvider2,
		BlockStateProvider blockStateProvider3,
		BlockStateProvider blockStateProvider4,
		BlockStateProvider blockStateProvider5,
		List<BlockState> list,
		TagKey<Block> tagKey,
		TagKey<Block> tagKey2
	) {
		this.fillingProvider = blockStateProvider;
		this.innerLayerProvider = blockStateProvider2;
		this.alternateInnerLayerProvider = blockStateProvider3;
		this.middleLayerProvider = blockStateProvider4;
		this.outerLayerProvider = blockStateProvider5;
		this.innerPlacements = list;
		this.cannotReplace = tagKey;
		this.invalidBlocks = tagKey2;
	}
}
