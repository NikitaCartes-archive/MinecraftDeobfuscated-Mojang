package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class CompileTaskDynamicQueue {
	private static final int MAX_RECOMPILE_QUOTA = 2;
	private int recompileQuota = 2;
	private final List<SectionRenderDispatcher.RenderSection.CompileTask> tasks = new ObjectArrayList<>();

	public synchronized void add(SectionRenderDispatcher.RenderSection.CompileTask compileTask) {
		this.tasks.add(compileTask);
	}

	@Nullable
	public synchronized SectionRenderDispatcher.RenderSection.CompileTask poll(Vec3 vec3) {
		int i = -1;
		int j = -1;
		double d = Double.MAX_VALUE;
		double e = Double.MAX_VALUE;
		ListIterator<SectionRenderDispatcher.RenderSection.CompileTask> listIterator = this.tasks.listIterator();

		while (listIterator.hasNext()) {
			int k = listIterator.nextIndex();
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = (SectionRenderDispatcher.RenderSection.CompileTask)listIterator.next();
			if (compileTask.isCancelled.get()) {
				listIterator.remove();
			} else {
				double f = compileTask.getOrigin().distToCenterSqr(vec3);
				if (!compileTask.isRecompile() && f < d) {
					d = f;
					i = k;
				}

				if (compileTask.isRecompile() && f < e) {
					e = f;
					j = k;
				}
			}
		}

		boolean bl = j >= 0;
		boolean bl2 = i >= 0;
		if (!bl || bl2 && (this.recompileQuota <= 0 || !(e < d))) {
			this.recompileQuota = 2;
			return this.removeTaskByIndex(i);
		} else {
			this.recompileQuota--;
			return this.removeTaskByIndex(j);
		}
	}

	public int size() {
		return this.tasks.size();
	}

	@Nullable
	private SectionRenderDispatcher.RenderSection.CompileTask removeTaskByIndex(int i) {
		return i >= 0 ? (SectionRenderDispatcher.RenderSection.CompileTask)this.tasks.remove(i) : null;
	}

	public synchronized void clear() {
		for (SectionRenderDispatcher.RenderSection.CompileTask compileTask : this.tasks) {
			compileTask.cancel();
		}

		this.tasks.clear();
	}
}
