package fr.maxlego08.zkoth.command.commands;

import fr.maxlego08.zkoth.ZKothPlugin;
import fr.maxlego08.zkoth.command.VCommand;
import fr.maxlego08.zkoth.zcore.utils.commands.CommandType;

public class CommandKothVersion extends VCommand {

	public CommandKothVersion() {
		this.addSubCommand("version");
		this.addSubCommand("v");
		this.addSubCommand("ver");
	}

	@Override
	protected CommandType perform(ZKothPlugin main) {

		message(sender, "�aVersion du plugin�7: �2" + main.getDescription().getVersion());
		message(sender, "�aAuteur�7: �2Maxlego08");
		message(sender, "�aDiscord�7: �2http://discord.groupez.dev/");
		message(sender, "�aBuy it for �d6��7: �2https://groupez.dev/resources/zKoth.9");
		message(sender, "�aGift code of �d5%�8: �fZKOTH-2021");
		message(sender, "�aSponsor�7: �chttps://serveur-minecraft-vote.fr/?ref=345");

		return CommandType.SUCCESS;
	}

}
