package net.minecraft.client.profiling;

import com.mojang.blaze3d.systems.TimerQuery;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.profiling.ProfilerSamplerAdapter;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;

@Environment(EnvType.CLIENT)
public class ClientMetricsSamplersProvider implements MetricsSamplerProvider {
	private final LevelRenderer levelRenderer;
	private final Set<MetricSampler> samplers = new ObjectOpenHashSet<>();
	private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();

	public ClientMetricsSamplersProvider(LongSupplier longSupplier, LevelRenderer levelRenderer) {
		this.levelRenderer = levelRenderer;
		this.samplers.add(ServerMetricsSamplersProvider.tickTimeSampler(longSupplier));
		this.registerStaticSamplers();
	}

	private void registerStaticSamplers() {
		this.samplers.addAll(ServerMetricsSamplersProvider.runtimeIndependentSamplers());
		this.samplers.add(MetricSampler.create("totalChunks", MetricCategory.CHUNK_RENDERING, this.levelRenderer, LevelRenderer::getTotalSections));
		this.samplers.add(MetricSampler.create("renderedChunks", MetricCategory.CHUNK_RENDERING, this.levelRenderer, LevelRenderer::countRenderedSections));
		this.samplers.add(MetricSampler.create("lastViewDistance", MetricCategory.CHUNK_RENDERING, this.levelRenderer, LevelRenderer::getLastViewDistance));
		SectionRenderDispatcher sectionRenderDispatcher = this.levelRenderer.getSectionRenderDispatcher();
		this.samplers
			.add(MetricSampler.create("toUpload", MetricCategory.CHUNK_RENDERING_DISPATCHING, sectionRenderDispatcher, SectionRenderDispatcher::getToUpload));
		this.samplers
			.add(
				MetricSampler.create("freeBufferCount", MetricCategory.CHUNK_RENDERING_DISPATCHING, sectionRenderDispatcher, SectionRenderDispatcher::getFreeBufferCount)
			);
		this.samplers
			.add(MetricSampler.create("toBatchCount", MetricCategory.CHUNK_RENDERING_DISPATCHING, sectionRenderDispatcher, SectionRenderDispatcher::getToBatchCount));
		if (TimerQuery.getInstance().isPresent()) {
			this.samplers.add(MetricSampler.create("gpuUtilization", MetricCategory.GPU, Minecraft.getInstance(), Minecraft::getGpuUtilization));
		}
	}

	@Override
	public Set<MetricSampler> samplers(Supplier<ProfileCollector> supplier) {
		this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler(supplier));
		return this.samplers;
	}
}
