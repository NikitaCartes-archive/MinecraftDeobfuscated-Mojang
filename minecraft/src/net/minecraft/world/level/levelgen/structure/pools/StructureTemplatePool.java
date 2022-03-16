package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class StructureTemplatePool {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SIZE_UNSET = Integer.MIN_VALUE;
	public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("name").forGetter(StructureTemplatePool::getName),
					ResourceLocation.CODEC.fieldOf("fallback").forGetter(StructureTemplatePool::getFallback),
					Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.intRange(1, 150).fieldOf("weight"))
						.codec()
						.listOf()
						.fieldOf("elements")
						.forGetter(structureTemplatePool -> structureTemplatePool.rawTemplates)
				)
				.apply(instance, StructureTemplatePool::new)
	);
	public static final Codec<Holder<StructureTemplatePool>> CODEC = RegistryFileCodec.create(Registry.TEMPLATE_POOL_REGISTRY, DIRECT_CODEC);
	private final ResourceLocation name;
	private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
	private final List<StructurePoolElement> templates;
	private final ResourceLocation fallback;
	private int maxSize = Integer.MIN_VALUE;

	public StructureTemplatePool(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, List<Pair<StructurePoolElement, Integer>> list) {
		this.name = resourceLocation;
		this.rawTemplates = list;
		this.templates = Lists.<StructurePoolElement>newArrayList();

		for (Pair<StructurePoolElement, Integer> pair : list) {
			StructurePoolElement structurePoolElement = pair.getFirst();

			for (int i = 0; i < pair.getSecond(); i++) {
				this.templates.add(structurePoolElement);
			}
		}

		this.fallback = resourceLocation2;
	}

	public StructureTemplatePool(
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> list,
		StructureTemplatePool.Projection projection
	) {
		this.name = resourceLocation;
		this.rawTemplates = Lists.<Pair<StructurePoolElement, Integer>>newArrayList();
		this.templates = Lists.<StructurePoolElement>newArrayList();

		for (Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> pair : list) {
			StructurePoolElement structurePoolElement = (StructurePoolElement)pair.getFirst().apply(projection);
			this.rawTemplates.add(Pair.of(structurePoolElement, pair.getSecond()));

			for (int i = 0; i < pair.getSecond(); i++) {
				this.templates.add(structurePoolElement);
			}
		}

		this.fallback = resourceLocation2;
	}

	public int getMaxSize(StructureTemplateManager structureTemplateManager) {
		if (this.maxSize == Integer.MIN_VALUE) {
			this.maxSize = this.templates
				.stream()
				.filter(structurePoolElement -> structurePoolElement != EmptyPoolElement.INSTANCE)
				.mapToInt(structurePoolElement -> structurePoolElement.getBoundingBox(structureTemplateManager, BlockPos.ZERO, Rotation.NONE).getYSpan())
				.max()
				.orElse(0);
		}

		return this.maxSize;
	}

	public ResourceLocation getFallback() {
		return this.fallback;
	}

	public StructurePoolElement getRandomTemplate(Random random) {
		return (StructurePoolElement)this.templates.get(random.nextInt(this.templates.size()));
	}

	public List<StructurePoolElement> getShuffledTemplates(Random random) {
		return ImmutableList.copyOf(ObjectArrays.shuffle((StructurePoolElement[])this.templates.toArray(new StructurePoolElement[0]), random));
	}

	public ResourceLocation getName() {
		return this.name;
	}

	public int size() {
		return this.templates.size();
	}

	public static enum Projection implements StringRepresentable {
		TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
		RIGID("rigid", ImmutableList.of());

		public static final Codec<StructureTemplatePool.Projection> CODEC = StringRepresentable.fromEnum(
			StructureTemplatePool.Projection::values, StructureTemplatePool.Projection::byName
		);
		private static final Map<String, StructureTemplatePool.Projection> BY_NAME = (Map<String, StructureTemplatePool.Projection>)Arrays.stream(values())
			.collect(Collectors.toMap(StructureTemplatePool.Projection::getName, projection -> projection));
		private final String name;
		private final ImmutableList<StructureProcessor> processors;

		private Projection(String string2, ImmutableList<StructureProcessor> immutableList) {
			this.name = string2;
			this.processors = immutableList;
		}

		public String getName() {
			return this.name;
		}

		public static StructureTemplatePool.Projection byName(String string) {
			return (StructureTemplatePool.Projection)BY_NAME.get(string);
		}

		public ImmutableList<StructureProcessor> getProcessors() {
			return this.processors;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
