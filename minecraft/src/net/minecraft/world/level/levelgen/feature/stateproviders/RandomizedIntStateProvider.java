package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class RandomizedIntStateProvider extends BlockStateProvider {
	public static final Codec<RandomizedIntStateProvider> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("source").forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.source),
					Codec.STRING.fieldOf("property").forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.propertyName),
					UniformInt.CODEC.fieldOf("values").forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.values)
				)
				.apply(instance, RandomizedIntStateProvider::new)
	);
	private final BlockStateProvider source;
	private final String propertyName;
	@Nullable
	private IntegerProperty property;
	private final UniformInt values;

	public RandomizedIntStateProvider(BlockStateProvider blockStateProvider, IntegerProperty integerProperty, UniformInt uniformInt) {
		this.source = blockStateProvider;
		this.property = integerProperty;
		this.propertyName = integerProperty.getName();
		this.values = uniformInt;
		Collection<Integer> collection = integerProperty.getPossibleValues();

		for (int i = uniformInt.getBaseValue(); i < uniformInt.getMaxValue(); i++) {
			if (!collection.contains(i)) {
				throw new IllegalArgumentException("Property value out of range: " + integerProperty.getName() + ": " + i);
			}
		}
	}

	public RandomizedIntStateProvider(BlockStateProvider blockStateProvider, String string, UniformInt uniformInt) {
		this.source = blockStateProvider;
		this.propertyName = string;
		this.values = uniformInt;
	}

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.RANDOMIZED_INT_STATE_PROVIDER;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		BlockState blockState = this.source.getState(random, blockPos);
		if (this.property == null || !blockState.hasProperty(this.property)) {
			this.property = findProperty(blockState, this.propertyName);
		}

		return blockState.setValue(this.property, Integer.valueOf(this.values.sample(random)));
	}

	private static IntegerProperty findProperty(BlockState blockState, String string) {
		Collection<Property<?>> collection = blockState.getProperties();
		Optional<IntegerProperty> optional = collection.stream()
			.filter(property -> property.getName().equals(string))
			.filter(property -> property instanceof IntegerProperty)
			.map(property -> (IntegerProperty)property)
			.findAny();
		return (IntegerProperty)optional.orElseThrow(() -> new IllegalArgumentException("Illegal property: " + string));
	}
}
