package net.minecraft;

import java.util.List;

public record ReportType(String header, List<String> nuggets) {
	public static final ReportType CRASH = new ReportType(
		"Minecraft Crash Report",
		List.of(
			"Who set us up the TNT?",
			"Everything's going to plan. No, really, that was supposed to happen.",
			"Uh... Did I do that?",
			"Oops.",
			"Why did you do that?",
			"I feel sad now :(",
			"My bad.",
			"I'm sorry, Dave.",
			"I let you down. Sorry :(",
			"On the bright side, I bought you a teddy bear!",
			"Daisy, daisy...",
			"Oh - I know what I did wrong!",
			"Hey, that tickles! Hehehe!",
			"I blame Dinnerbone.",
			"You should try our sister game, Minceraft!",
			"Don't be sad. I'll do better next time, I promise!",
			"Don't be sad, have a hug! <3",
			"I just don't know what went wrong :(",
			"Shall we play a game?",
			"Quite honestly, I wouldn't worry myself about that.",
			"I bet Cylons wouldn't have this problem.",
			"Sorry :(",
			"Surprise! Haha. Well, this is awkward.",
			"Would you like a cupcake?",
			"Hi. I'm Minecraft, and I'm a crashaholic.",
			"Ooh. Shiny.",
			"This doesn't make any sense!",
			"Why is it breaking :(",
			"Don't do that.",
			"Ouch. That hurt :(",
			"You're mean.",
			"This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]",
			"There are four lights!",
			"But it works on my machine."
		)
	);
	public static final ReportType PROFILE = new ReportType(
		"Minecraft Profiler Results",
		List.of(
			"I'd Rather Be Surfing",
			"Shiny numbers!",
			"Am I not running fast enough? :(",
			"I'm working as hard as I can!",
			"Will I ever be good enough for you? :(",
			"Speedy. Zoooooom!",
			"Hello world",
			"40% better than a crash report.",
			"Now with extra numbers",
			"Now with less numbers",
			"Now with the same numbers",
			"You should add flames to things, it makes them go faster!",
			"Do you feel the need for... optimization?",
			"*cracks redstone whip*",
			"Maybe if you treated it better then it'll have more motivation to work faster! Poor server."
		)
	);
	public static final ReportType TEST = new ReportType(
		"Minecraft Test Report",
		List.of(
			"Don't mind me",
			"One day I will be a real crash!",
			"Booo! Haha, did I scare you?",
			"Help, I'm trapped in a report factory!",
			"Have I answered your question?",
			"No hugs here, sorry",
			"I Can't Believe It's Not A Crash Report!",
			"Where's the kaboom!?"
		)
	);
	public static final ReportType NETWORK_PROTOCOL_ERROR = new ReportType(
		"Minecraft Network Protocol Error Report",
		List.of(
			"0xBADF00D",
			"+'${`%&NO CARRIER",
			"Please insert The Internet CD #4",
			"Sabotage!",
			"Are you sure you are not moving wrongly?",
			"This time is not my fault, I promise!",
			"All lines are down!",
			"Maybe a shark bit some cable",
			"404",
			"I'm sorry, I don't speak that language",
			"What we've got here is failure to communicate",
			"It's the tubes, they're clogged!",
			"Abort, Retry, Ignore?",
			"Could be worse, I guess",
			"Wait, was the last bit one or zero?",
			"Too many suspicious packets",
			"Don't worry, I'll be fine",
			"Maybe this time it will work!",
			"I heard pigeons are more reliable"
		)
	);
	public static final ReportType CHUNK_IO_ERROR = new ReportType(
		"Minecraft Chunk IO Error Report",
		List.of(
			"I have failed you!",
			"Let's not do it again...",
			"Worst magic trick ever!",
			"Remember to backup your worlds regularly",
			"Pirates stole your chunk!",
			"Ker-chunk!",
			"Ideally, this shouldn't be here",
			"Let's hope it wasn't anything important",
			"Computers were a mistake",
			"Welp",
			"Not my proudest moment",
			"Who needs blocks in a block game, right?",
			"This chunk is no more...it has ceased to be...this is an EX-chunk",
			"loss.mca"
		)
	);

	public String getErrorComment() {
		try {
			return (String)this.nuggets.get((int)(Util.getNanos() % (long)this.nuggets.size()));
		} catch (Throwable var2) {
			return "Witty comment unavailable :(";
		}
	}

	public void appendHeader(StringBuilder stringBuilder, List<String> list) {
		stringBuilder.append("---- ");
		stringBuilder.append(this.header());
		stringBuilder.append(" ----\n");
		stringBuilder.append("// ");
		stringBuilder.append(this.getErrorComment());
		stringBuilder.append('\n');

		for (String string : list) {
			stringBuilder.append("// ");
			stringBuilder.append(string);
			stringBuilder.append('\n');
		}

		stringBuilder.append('\n');
	}
}
