package net.minecraft.world.entity.monster;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class RayTracing extends PathfinderMob {
	private static final String RAYS_NAME = "Ray Tracing";
	private static final List<Component> RAYS_DEATH_LINES = (List<Component>)Stream.of(
			"That was just a warm-up, next time I'll be ready!",
			"I got caught off-guard. I won't make that mistake again",
			"It's not my fault, the lag made me miss my jump!",
			"I was distracted by that beautiful sunset",
			"I was just testing to see how much fall damage I could survive",
			"I was trying to show off my parkour skills, but it didn't go as planned",
			"I thought I had enough food to survive, but apparently not",
			"I underestimated the strength of those zombies",
			"I was practicing my speedrun strats and got a bit carried away",
			"I was trying to get a better view and accidentally fell"
		)
		.map(string -> Component.translatable("chat.type.text", "Ray Tracing", string))
		.collect(Collectors.toList());
	private static final List<Component> RAYS_RANDOM_LINES = (List<Component>)Stream.of(
			"I just found diamonds! Wait, no, it's just coal. Again.",
			"I'm a master builder. I built a dirt house once",
			"Creepers? Never heard of 'em",
			"I could've sworn I put that torch there",
			"If at first you don't succeed, dig straight down",
			"I'm not lost, I'm just exploring",
			"Is it just me, or do those cows have judgmental eyes?",
			"I've never been to the Nether, but I hear it's a nice place to vacation",
			"I heard if you punch trees long enough, they turn into diamonds",
			"Sometimes I feel like the only thing I'm good at is dying in lava",
			"I'm not lost, I'm just temporarily misplaced",
			"I like my Minecraft like I like my coffee: with extra sugar cubes",
			"I heard that if you stare at a pig long enough, it will give you its best pork chop",
			"I'm convinced that creepers were invented by the developers just to mess with us",
			"Why build a fancy castle when you can watch a video of someone else building one?"
		)
		.map(string -> Component.translatable("chat.type.text", "Ray Tracing", string))
		.collect(Collectors.toList());
	private static final List<Component> RAYS_INTRO_LINES = (List<Component>)Stream.of(
			"Did someone say cake? I'm here for the cake!",
			"Greetings, fellow Minecrafters! Let's build some amazing things together",
			"I come bearing gifts... of dirt. Lots and lots of dirt",
			"I hope everyone is ready for some serious block-placing action!",
			"I don't always play Minecraft, but when I do, I prefer to play with awesome people like you",
			"I heard there was a party happening here. Did I miss the memo?",
			"Hey everyone, can I join your Minecraft book club?",
			"I'm not saying I'm the best Minecraft player, but I did once build a castle out of wool",
			"I'm here to mine some blocks and chew bubblegum... and I'm all out of bubblegum",
			"Hello, is this the Minecraft support group? I think I'm addicted"
		)
		.map(string -> Component.translatable("chat.type.text", "Ray Tracing", string))
		.collect(Collectors.toList());
	private static final List<Component> RAYS_OUTRO_LINES = (List<Component>)Stream.of(
			"I have to go take care of my real-life sheep. See you all later!",
			"My mom is calling me for dinner. Gotta run!",
			"Sorry guys, I have a meeting with the Ender Dragon. It's urgent.",
			"I have to go put out a fire... in the real world. Bye!",
			"I'm sorry, I have to go study for my Minecraft finals",
			"My boss just messaged me. Apparently, there's a creeper invasion at work. Gotta go!",
			"I have to go feed my pet slime. They get cranky if I don't feed them on time",
			"I need to take a break. Bye everyone!",
			"I promised my little sibling I would play Minecraft with them. Time to go fulfill that promise!",
			"I'm sorry, but I have to go save the world from the zombie apocalypse. Wish me luck!"
		)
		.map(string -> Component.translatable("chat.type.text", "Ray Tracing", string))
		.collect(Collectors.toList());
	private static final Component FRENCH = Component.translatable("chat.type.text", "Ray Tracing", Component.literal("Omelette du fromage"));
	public boolean firstJoin;
	private long nextLine;

	public RayTracing(EntityType<RayTracing> entityType, Level level) {
		super(entityType, level);
		this.nextLine = level.getGameTime() + (long)level.random.nextInt(80, 600);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Zombie.class, 8.0F, 1.0, 1.0));
		this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Evoker.class, 12.0F, 1.0, 1.0));
		this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Vindicator.class, 8.0F, 1.0, 1.0));
		this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Vex.class, 8.0F, 1.0, 1.0));
		this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Pillager.class, 15.0F, 1.0, 1.0));
		this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Illusioner.class, 12.0F, 1.0, 1.0));
		this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Zoglin.class, 10.0F, 1.0, 1.0));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.0));
		this.goalSelector.addGoal(2, new TemptGoal(this, 1.0, Ingredient.of(Items.DIAMOND), false));
		this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 1.0));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(3, new InteractGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 16.0F));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.23F);
	}

	@Override
	public boolean requiresCustomPersistence() {
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PLAYER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}

	@Override
	public void tick() {
		if (this.nextLine <= this.level.getGameTime()) {
			this.nextLine = this.level.getGameTime() + (long)this.level.random.nextInt(600, 3600);
			if (this.firstJoin) {
				this.sayIntro();
				this.firstJoin = false;
			} else if (!Rules.RAY_TRACING.get()) {
				this.sayOutro();
				this.remove(Entity.RemovalReason.DISCARDED);
				if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
					serverLevel.getServer()
						.getPlayerList()
						.broadcastSystemMessage(Component.translatable("multiplayer.player.left", "Ray Tracing").withStyle(ChatFormatting.YELLOW), false);
				}
			} else {
				this.say((Component)RAYS_RANDOM_LINES.get(this.random.nextInt(RAYS_RANDOM_LINES.size())));
			}
		}

		super.tick();
	}

	@Override
	protected void tickDeath() {
		super.tickDeath();
		if (this.deathTime >= 20 && !this.level.isClientSide() && this.level instanceof ServerLevel serverLevel) {
			serverLevel.addRayTracing();
			this.say((Component)RAYS_DEATH_LINES.get(this.random.nextInt(RAYS_DEATH_LINES.size())));
		}
	}

	@Override
	public void die(DamageSource damageSource) {
		if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
			this.deathMessage(serverLevel.getServer().getPlayerList());
		}

		super.die(damageSource);
	}

	private void deathMessage(PlayerList playerList) {
		boolean bl = this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
		if (bl) {
			playerList.broadcastSystemMessage(this.getCombatTracker().getDeathMessage(), false);
		}
	}

	public void say(Component component) {
		if (!this.level.isClientSide() && this.level instanceof ServerLevel serverLevel) {
			if (Rules.FRENCH_MODE.get()) {
				component = FRENCH;
			}

			serverLevel.getServer().getPlayerList().broadcastSystemMessage(component, false);
		}
	}

	public void sayIntro() {
		this.say((Component)RAYS_INTRO_LINES.get(this.random.nextInt(RAYS_INTRO_LINES.size())));
	}

	public void sayOutro() {
		this.say((Component)RAYS_OUTRO_LINES.get(this.random.nextInt(RAYS_OUTRO_LINES.size())));
	}
}
