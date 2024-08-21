package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

@Environment(EnvType.CLIENT)
public class ViewArea {
	protected final LevelRenderer levelRenderer;
	protected final Level level;
	protected int sectionGridSizeY;
	protected int sectionGridSizeX;
	protected int sectionGridSizeZ;
	private int viewDistance;
	private SectionPos cameraSectionPos;
	public SectionRenderDispatcher.RenderSection[] sections;

	public ViewArea(SectionRenderDispatcher sectionRenderDispatcher, Level level, int i, LevelRenderer levelRenderer) {
		this.levelRenderer = levelRenderer;
		this.level = level;
		this.setViewDistance(i);
		this.createSections(sectionRenderDispatcher);
		this.cameraSectionPos = SectionPos.of(this.viewDistance + 1, 0, this.viewDistance + 1);
	}

	protected void createSections(SectionRenderDispatcher sectionRenderDispatcher) {
		if (!Minecraft.getInstance().isSameThread()) {
			throw new IllegalStateException("createSections called from wrong thread: " + Thread.currentThread().getName());
		} else {
			int i = this.sectionGridSizeX * this.sectionGridSizeY * this.sectionGridSizeZ;
			this.sections = new SectionRenderDispatcher.RenderSection[i];

			for (int j = 0; j < this.sectionGridSizeX; j++) {
				for (int k = 0; k < this.sectionGridSizeY; k++) {
					for (int l = 0; l < this.sectionGridSizeZ; l++) {
						int m = this.getSectionIndex(j, k, l);
						this.sections[m] = sectionRenderDispatcher.new RenderSection(m, SectionPos.asLong(j, k + this.level.getMinSectionY(), l));
					}
				}
			}
		}
	}

	public void releaseAllBuffers() {
		for (SectionRenderDispatcher.RenderSection renderSection : this.sections) {
			renderSection.releaseBuffers();
		}
	}

	private int getSectionIndex(int i, int j, int k) {
		return (k * this.sectionGridSizeY + j) * this.sectionGridSizeX + i;
	}

	protected void setViewDistance(int i) {
		int j = i * 2 + 1;
		this.sectionGridSizeX = j;
		this.sectionGridSizeY = this.level.getSectionsCount();
		this.sectionGridSizeZ = j;
		this.viewDistance = i;
	}

	public int getViewDistance() {
		return this.viewDistance;
	}

	public LevelHeightAccessor getLevelHeightAccessor() {
		return this.level;
	}

	public void repositionCamera(SectionPos sectionPos) {
		for (int i = 0; i < this.sectionGridSizeX; i++) {
			int j = sectionPos.x() - this.viewDistance;
			int k = j + Math.floorMod(i - j, this.sectionGridSizeX);

			for (int l = 0; l < this.sectionGridSizeZ; l++) {
				int m = sectionPos.z() - this.viewDistance;
				int n = m + Math.floorMod(l - m, this.sectionGridSizeZ);

				for (int o = 0; o < this.sectionGridSizeY; o++) {
					int p = this.level.getMinSectionY() + o;
					SectionRenderDispatcher.RenderSection renderSection = this.sections[this.getSectionIndex(i, o, l)];
					long q = renderSection.getSectionNode();
					if (q != SectionPos.asLong(k, p, n)) {
						renderSection.setSectionNode(SectionPos.asLong(k, p, n));
					}
				}
			}
		}

		this.cameraSectionPos = sectionPos;
		this.levelRenderer.getSectionOcclusionGraph().invalidate();
	}

	public SectionPos getCameraSectionPos() {
		return this.cameraSectionPos;
	}

	public void setDirty(int i, int j, int k, boolean bl) {
		SectionRenderDispatcher.RenderSection renderSection = this.getRenderSection(i, j, k);
		if (renderSection != null) {
			renderSection.setDirty(bl);
		}
	}

	@Nullable
	protected SectionRenderDispatcher.RenderSection getRenderSectionAt(BlockPos blockPos) {
		return this.getRenderSection(SectionPos.asLong(blockPos));
	}

	@Nullable
	protected SectionRenderDispatcher.RenderSection getRenderSection(long l) {
		int i = SectionPos.x(l);
		int j = SectionPos.y(l);
		int k = SectionPos.z(l);
		return this.getRenderSection(i, j, k);
	}

	@Nullable
	private SectionRenderDispatcher.RenderSection getRenderSection(int i, int j, int k) {
		if (!this.containsSection(i, j, k)) {
			return null;
		} else {
			int l = j - this.level.getMinSectionY();
			int m = Math.floorMod(i, this.sectionGridSizeX);
			int n = Math.floorMod(k, this.sectionGridSizeZ);
			return this.sections[this.getSectionIndex(m, l, n)];
		}
	}

	private boolean containsSection(int i, int j, int k) {
		if (j >= this.level.getMinSectionY() && j <= this.level.getMaxSectionY()) {
			return i < this.cameraSectionPos.x() - this.viewDistance || i > this.cameraSectionPos.x() + this.viewDistance
				? false
				: k >= this.cameraSectionPos.z() - this.viewDistance && k <= this.cameraSectionPos.z() + this.viewDistance;
		} else {
			return false;
		}
	}
}
