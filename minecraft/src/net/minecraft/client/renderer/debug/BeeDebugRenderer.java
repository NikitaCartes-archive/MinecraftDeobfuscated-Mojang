package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
	private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
	private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
	private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
	private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
	private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
	private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
	private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
	private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
	private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
	private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
	private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
	private static final boolean SHOW_HIVE_MEMBERS = true;
	private static final boolean SHOW_BLACKLISTS = true;
	private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
	private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
	private static final int MAX_TARGETING_DIST = 8;
	private static final int HIVE_TIMEOUT = 20;
	private static final float TEXT_SCALE = 0.02F;
	private static final int ORANGE = -23296;
	private static final int GRAY = -3355444;
	private static final int PINK = -98404;
	private final Minecraft minecraft;
	private final Map<BlockPos, BeeDebugRenderer.HiveDebugInfo> hives = new HashMap();
	private final Map<UUID, BeeDebugPayload.BeeInfo> beeInfosPerEntity = new HashMap();
	@Nullable
	private UUID lastLookedAtUuid;

	public BeeDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void clear() {
		this.hives.clear();
		this.beeInfosPerEntity.clear();
		this.lastLookedAtUuid = null;
	}

	public void addOrUpdateHiveInfo(HiveDebugPayload.HiveInfo hiveInfo, long l) {
		this.hives.put(hiveInfo.pos(), new BeeDebugRenderer.HiveDebugInfo(hiveInfo, l));
	}

	public void addOrUpdateBeeInfo(BeeDebugPayload.BeeInfo beeInfo) {
		this.beeInfosPerEntity.put(beeInfo.uuid(), beeInfo);
	}

	public void removeBeeInfo(int i) {
		this.beeInfosPerEntity.values().removeIf(beeInfo -> beeInfo.id() == i);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		this.clearRemovedHives();
		this.clearRemovedBees();
		this.doRender(poseStack, multiBufferSource);
		if (!this.minecraft.player.isSpectator()) {
			this.updateLastLookedAtUuid();
		}
	}

	private void clearRemovedBees() {
		this.beeInfosPerEntity.entrySet().removeIf(entry -> this.minecraft.level.getEntity(((BeeDebugPayload.BeeInfo)entry.getValue()).id()) == null);
	}

	private void clearRemovedHives() {
		long l = this.minecraft.level.getGameTime() - 20L;
		this.hives.entrySet().removeIf(entry -> ((BeeDebugRenderer.HiveDebugInfo)entry.getValue()).lastSeen() < l);
	}

	private void doRender(PoseStack poseStack, MultiBufferSource multiBufferSource) {
		BlockPos blockPos = this.getCamera().getBlockPosition();
		this.beeInfosPerEntity.values().forEach(beeInfo -> {
			if (this.isPlayerCloseEnoughToMob(beeInfo)) {
				this.renderBeeInfo(poseStack, multiBufferSource, beeInfo);
			}
		});
		this.renderFlowerInfos(poseStack, multiBufferSource);

		for (BlockPos blockPos2 : this.hives.keySet()) {
			if (blockPos.closerThan(blockPos2, 30.0)) {
				highlightHive(poseStack, multiBufferSource, blockPos2);
			}
		}

		Map<BlockPos, Set<UUID>> map = this.createHiveBlacklistMap();
		this.hives.values().forEach(hiveDebugInfo -> {
			if (blockPos.closerThan(hiveDebugInfo.info.pos(), 30.0)) {
				Set<UUID> set = (Set<UUID>)map.get(hiveDebugInfo.info.pos());
				this.renderHiveInfo(poseStack, multiBufferSource, hiveDebugInfo.info, (Collection<UUID>)(set == null ? Sets.<UUID>newHashSet() : set));
			}
		});
		this.getGhostHives().forEach((blockPos2x, list) -> {
			if (blockPos.closerThan(blockPos2x, 30.0)) {
				this.renderGhostHive(poseStack, multiBufferSource, blockPos2x, list);
			}
		});
	}

	private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
		Map<BlockPos, Set<UUID>> map = Maps.<BlockPos, Set<UUID>>newHashMap();
		this.beeInfosPerEntity
			.values()
			.forEach(beeInfo -> beeInfo.blacklistedHives().forEach(blockPos -> ((Set)map.computeIfAbsent(blockPos, blockPosx -> Sets.newHashSet())).add(beeInfo.uuid())));
		return map;
	}

	private void renderFlowerInfos(PoseStack poseStack, MultiBufferSource multiBufferSource) {
		Map<BlockPos, Set<UUID>> map = Maps.<BlockPos, Set<UUID>>newHashMap();
		this.beeInfosPerEntity.values().forEach(beeInfo -> {
			if (beeInfo.flowerPos() != null) {
				((Set)map.computeIfAbsent(beeInfo.flowerPos(), blockPos -> new HashSet())).add(beeInfo.uuid());
			}
		});
		map.forEach((blockPos, set) -> {
			Set<String> set2 = (Set<String>)set.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
			int i = 1;
			renderTextOverPos(poseStack, multiBufferSource, set2.toString(), blockPos, i++, -256);
			renderTextOverPos(poseStack, multiBufferSource, "Flower", blockPos, i++, -1);
			float f = 0.05F;
			DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
		});
	}

	private static String getBeeUuidsAsString(Collection<UUID> collection) {
		if (collection.isEmpty()) {
			return "-";
		} else {
			return collection.size() > 3
				? collection.size() + " bees"
				: ((Set)collection.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet())).toString();
		}
	}

	private static void highlightHive(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos) {
		float f = 0.05F;
		DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
	}

	private void renderGhostHive(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, List<String> list) {
		float f = 0.05F;
		DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
		renderTextOverPos(poseStack, multiBufferSource, list + "", blockPos, 0, -256);
		renderTextOverPos(poseStack, multiBufferSource, "Ghost Hive", blockPos, 1, -65536);
	}

	private void renderHiveInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, HiveDebugPayload.HiveInfo hiveInfo, Collection<UUID> collection) {
		int i = 0;
		if (!collection.isEmpty()) {
			renderTextOverHive(poseStack, multiBufferSource, "Blacklisted by " + getBeeUuidsAsString(collection), hiveInfo, i++, -65536);
		}

		renderTextOverHive(poseStack, multiBufferSource, "Out: " + getBeeUuidsAsString(this.getHiveMembers(hiveInfo.pos())), hiveInfo, i++, -3355444);
		if (hiveInfo.occupantCount() == 0) {
			renderTextOverHive(poseStack, multiBufferSource, "In: -", hiveInfo, i++, -256);
		} else if (hiveInfo.occupantCount() == 1) {
			renderTextOverHive(poseStack, multiBufferSource, "In: 1 bee", hiveInfo, i++, -256);
		} else {
			renderTextOverHive(poseStack, multiBufferSource, "In: " + hiveInfo.occupantCount() + " bees", hiveInfo, i++, -256);
		}

		renderTextOverHive(poseStack, multiBufferSource, "Honey: " + hiveInfo.honeyLevel(), hiveInfo, i++, -23296);
		renderTextOverHive(poseStack, multiBufferSource, hiveInfo.hiveType() + (hiveInfo.sedated() ? " (sedated)" : ""), hiveInfo, i++, -1);
	}

	private void renderPath(PoseStack poseStack, MultiBufferSource multiBufferSource, BeeDebugPayload.BeeInfo beeInfo) {
		if (beeInfo.path() != null) {
			PathfindingRenderer.renderPath(
				poseStack,
				multiBufferSource,
				beeInfo.path(),
				0.5F,
				false,
				false,
				this.getCamera().getPosition().x(),
				this.getCamera().getPosition().y(),
				this.getCamera().getPosition().z()
			);
		}
	}

	private void renderBeeInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, BeeDebugPayload.BeeInfo beeInfo) {
		boolean bl = this.isBeeSelected(beeInfo);
		int i = 0;
		renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), i++, beeInfo.toString(), -1, 0.03F);
		if (beeInfo.hivePos() == null) {
			renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), i++, "No hive", -98404, 0.02F);
		} else {
			renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), i++, "Hive: " + this.getPosDescription(beeInfo, beeInfo.hivePos()), -256, 0.02F);
		}

		if (beeInfo.flowerPos() == null) {
			renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), i++, "No flower", -98404, 0.02F);
		} else {
			renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), i++, "Flower: " + this.getPosDescription(beeInfo, beeInfo.flowerPos()), -256, 0.02F);
		}

		for (String string : beeInfo.goals()) {
			renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), i++, string, -16711936, 0.02F);
		}

		if (bl) {
			this.renderPath(poseStack, multiBufferSource, beeInfo);
		}

		if (beeInfo.travelTicks() > 0) {
			int j = beeInfo.travelTicks() < 2400 ? -3355444 : -23296;
			renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), i++, "Travelling: " + beeInfo.travelTicks() + " ticks", j, 0.02F);
		}
	}

	private static void renderTextOverHive(
		PoseStack poseStack, MultiBufferSource multiBufferSource, String string, HiveDebugPayload.HiveInfo hiveInfo, int i, int j
	) {
		renderTextOverPos(poseStack, multiBufferSource, string, hiveInfo.pos(), i, j);
	}

	private static void renderTextOverPos(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, BlockPos blockPos, int i, int j) {
		double d = 1.3;
		double e = 0.2;
		double f = (double)blockPos.getX() + 0.5;
		double g = (double)blockPos.getY() + 1.3 + (double)i * 0.2;
		double h = (double)blockPos.getZ() + 0.5;
		DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, f, g, h, j, 0.02F, true, 0.0F, true);
	}

	private static void renderTextOverMob(PoseStack poseStack, MultiBufferSource multiBufferSource, Position position, int i, String string, int j, float f) {
		double d = 2.4;
		double e = 0.25;
		BlockPos blockPos = BlockPos.containing(position);
		double g = (double)blockPos.getX() + 0.5;
		double h = position.y() + 2.4 + (double)i * 0.25;
		double k = (double)blockPos.getZ() + 0.5;
		float l = 0.5F;
		DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, g, h, k, j, f, false, 0.5F, true);
	}

	private Camera getCamera() {
		return this.minecraft.gameRenderer.getMainCamera();
	}

	private Set<String> getHiveMemberNames(HiveDebugPayload.HiveInfo hiveInfo) {
		return (Set<String>)this.getHiveMembers(hiveInfo.pos()).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
	}

	private String getPosDescription(BeeDebugPayload.BeeInfo beeInfo, BlockPos blockPos) {
		double d = Math.sqrt(blockPos.distToCenterSqr(beeInfo.pos()));
		double e = (double)Math.round(d * 10.0) / 10.0;
		return blockPos.toShortString() + " (dist " + e + ")";
	}

	private boolean isBeeSelected(BeeDebugPayload.BeeInfo beeInfo) {
		return Objects.equals(this.lastLookedAtUuid, beeInfo.uuid());
	}

	private boolean isPlayerCloseEnoughToMob(BeeDebugPayload.BeeInfo beeInfo) {
		Player player = this.minecraft.player;
		BlockPos blockPos = BlockPos.containing(player.getX(), beeInfo.pos().y(), player.getZ());
		BlockPos blockPos2 = BlockPos.containing(beeInfo.pos());
		return blockPos.closerThan(blockPos2, 30.0);
	}

	private Collection<UUID> getHiveMembers(BlockPos blockPos) {
		return (Collection<UUID>)this.beeInfosPerEntity
			.values()
			.stream()
			.filter(beeInfo -> beeInfo.hasHive(blockPos))
			.map(BeeDebugPayload.BeeInfo::uuid)
			.collect(Collectors.toSet());
	}

	private Map<BlockPos, List<String>> getGhostHives() {
		Map<BlockPos, List<String>> map = Maps.<BlockPos, List<String>>newHashMap();

		for (BeeDebugPayload.BeeInfo beeInfo : this.beeInfosPerEntity.values()) {
			if (beeInfo.hivePos() != null && !this.hives.containsKey(beeInfo.hivePos())) {
				((List)map.computeIfAbsent(beeInfo.hivePos(), blockPos -> Lists.newArrayList())).add(beeInfo.generateName());
			}
		}

		return map;
	}

	private void updateLastLookedAtUuid() {
		DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> this.lastLookedAtUuid = entity.getUUID());
	}

	@Environment(EnvType.CLIENT)
	static record HiveDebugInfo(HiveDebugPayload.HiveInfo info, long lastSeen) {
	}
}
