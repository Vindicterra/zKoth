package fr.maxlego08.zkoth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import fr.maxlego08.zkoth.api.FactionListener;
import fr.maxlego08.zkoth.api.Koth;
import fr.maxlego08.zkoth.api.KothManager;
import fr.maxlego08.zkoth.api.Selection;
import fr.maxlego08.zkoth.api.enums.KothType;
import fr.maxlego08.zkoth.api.enums.LootType;
import fr.maxlego08.zkoth.api.event.events.KothCreateEvent;
import fr.maxlego08.zkoth.api.event.events.KothHookEvent;
import fr.maxlego08.zkoth.api.event.events.KothMoveEvent;
import fr.maxlego08.zkoth.api.event.events.KothStopEvent;
import fr.maxlego08.zkoth.api.event.events.KothWinEvent;
import fr.maxlego08.zkoth.hooks.ClanHook;
import fr.maxlego08.zkoth.hooks.DefaultHook;
import fr.maxlego08.zkoth.hooks.FactionLegacyHook;
import fr.maxlego08.zkoth.hooks.FactionMassiveHook;
import fr.maxlego08.zkoth.hooks.FactionsHook;
import fr.maxlego08.zkoth.hooks.FactionsXHook;
import fr.maxlego08.zkoth.hooks.GangsHook;
import fr.maxlego08.zkoth.hooks.GuildsHook;
import fr.maxlego08.zkoth.hooks.SimpleClanHook;
import fr.maxlego08.zkoth.hooks.SuperiorSkyblock2Hook;
import fr.maxlego08.zkoth.hooks.UltimateFaction;
import fr.maxlego08.zkoth.listener.ListenerAdapter;
import fr.maxlego08.zkoth.save.Config;
import fr.maxlego08.zkoth.scoreboard.ScoreBoardManager;
import fr.maxlego08.zkoth.zcore.ZPlugin;
import fr.maxlego08.zkoth.zcore.enums.Message;
import fr.maxlego08.zkoth.zcore.logger.Logger;
import fr.maxlego08.zkoth.zcore.logger.Logger.LogType;
import fr.maxlego08.zkoth.zcore.utils.Cuboid;
import fr.maxlego08.zkoth.zcore.utils.ZSelection;
import fr.maxlego08.zkoth.zcore.utils.builder.ItemBuilder;
import fr.maxlego08.zkoth.zcore.utils.builder.TimerBuilder;
import fr.maxlego08.zkoth.zcore.utils.storage.Persist;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class ZKothManager extends ListenerAdapter implements KothManager {

	private static List<ZKoth> koths = new ArrayList<ZKoth>();

	private final transient ScoreBoardManager manager;
	private final transient Map<UUID, Selection> selections = new HashMap<UUID, Selection>();
	private transient FactionListener factionListener;
	private transient long playerMoveEventCooldown = 0;
	private transient final Map<Inventory, String> lootInventories = new HashMap<>();

	/**
	 * @param plugin
	 * @param manager
	 */
	public ZKothManager(ScoreBoardManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	public void save(Persist persist) {
		persist.save(this, "koths");
	}

	@Override
	public void load(Persist persist) {
		persist.loadOrSaveDefault(this, ZKothManager.class, "koths");

		/* Enable faction */
		if (!Config.useNoFactionHook) {

			PluginManager pluginManager = Bukkit.getPluginManager();

			if (pluginManager.isPluginEnabled("FactionsX")) {

				this.factionListener = new FactionsXHook();
				Logger.info("FactionsX plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("SimpleClans")) {

				Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("SimpleClans");
				this.factionListener = new SimpleClanHook(plugin);
				Logger.info("SimpleClans plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("SuperiorSkyblock2")) {

				this.factionListener = new SuperiorSkyblock2Hook();
				Logger.info("SuperiorSkyblock2 plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("Clans")) {

				this.factionListener = new ClanHook();
				Logger.info("Clans plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("GangsPlus")) {

				this.factionListener = new GangsHook();
				Logger.info("GangsPlus plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("UltimateFactions")) {

				this.factionListener = new UltimateFaction();
				Logger.info("UltimateFactions plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("Guilds")) {

				this.factionListener = new GuildsHook();
				Logger.info("Guilds plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("LegacyFactions")) {

				this.factionListener = new FactionLegacyHook();
				Logger.info("LegacyFactions plugin detected successfully.", LogType.SUCCESS);

			} else if (pluginManager.isPluginEnabled("Factions")) {

				Plugin plugin = pluginManager.getPlugin("Factions");
				List<String> authors = plugin.getDescription().getAuthors();

				if (authors.contains("Cayorion") && pluginManager.isPluginEnabled("MassiveCore")) {

					this.factionListener = new FactionMassiveHook();
					Logger.info("MassiveCraft plugin detected successfully.", LogType.SUCCESS);

				} else if (authors.contains("Savag3life")) {

					this.factionListener = new FactionsHook();
					Logger.info("SavageFaction plugin detected successfully.", LogType.SUCCESS);

				} else if (authors.contains("Driftay")) {

					this.factionListener = new FactionsHook();
					Logger.info("SaberFaction plugin detected successfully.", LogType.SUCCESS);

				} else if (authors.contains("drtshock")) {

					this.factionListener = new FactionsHook();
					Logger.info("FactionUUID plugin detected successfully.", LogType.SUCCESS);

				}

			} else {
				this.factionListener = new DefaultHook();
				Logger.info("No plugin was detected.", LogType.SUCCESS);
			}
		} else {
			this.factionListener = new DefaultHook();
			Logger.info("No plugin was detected.", LogType.SUCCESS);
		}

		KothHookEvent event = new KothHookEvent(this.factionListener);
		event.callEvent();

		this.factionListener = event.getFactionListener();
	}

	@Override
	public Optional<Koth> getKoth(String name) {
		Optional<ZKoth> zKoth = koths.stream().filter(koth -> koth.getName().equalsIgnoreCase(name)).findFirst();
		return zKoth.isPresent() ? Optional.of(zKoth.get()) : Optional.empty();
	}

	@Override
	public void createKoth(CommandSender sender, String name, Location minLocation, Location maxLocation,
			int captureSeconds) {

		Optional<Koth> optional = getKoth(name);
		if (optional.isPresent()) {
			message(sender, Message.ZKOTH_ALREADY_EXIST, "%name%", name);
			return;
		}

		Koth koth = new ZKoth(name, captureSeconds, minLocation, maxLocation);

		KothCreateEvent event = new KothCreateEvent(koth);
		event.callEvent();

		if (event.isCancelled()) {
			return;
		}

		koths.add((ZKoth) koth);
		message(sender, Message.ZKOTH_CREATE_SUCCESS, "%name%", name);

		this.save(this.plugin.getPersist());
	}

	@Override
	public ItemStack getKothAxe() {
		ItemBuilder builder = new ItemBuilder(Material.STONE_AXE, Message.ZKOTH_AXE_NAME.getMessage());
		Message.ZKOTH_AXE_DESCRIPTION.getMessages().forEach(e -> builder.addLine(e));
		return builder.build();
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Player player) {
		@SuppressWarnings("deprecation")
		ItemStack itemStack = player.getItemInHand();
		if (itemStack != null && event.getClickedBlock() != null
				&& same(itemStack, Message.ZKOTH_AXE_NAME.getMessage())) {

			event.setCancelled(true);
			Optional<Selection> optional = getSelection(player.getUniqueId());
			Selection selection = null;

			if (!optional.isPresent()) {
				selection = new ZSelection();
				this.selections.put(player.getUniqueId(), selection);
			} else {
				selection = optional.get();
			}

			Location location = event.getClickedBlock().getLocation();
			org.bukkit.event.block.Action action = event.getAction();
			selection.action(action, location);
			Message message = action.equals(org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) ? Message.ZKOTH_AXE_POS1
					: Message.ZKOTH_AXE_POS2;
			message(player, message, "%x%", String.valueOf(location.getBlockX()), "%y%",
					String.valueOf(location.getBlockY()), "%z%", String.valueOf(location.getBlockZ()), "%world%",
					location.getWorld().getName());
		}
	}

	@Override
	public Optional<Selection> getSelection(UUID uuid) {
		return Optional.ofNullable(this.selections.getOrDefault(uuid, null));
	}

	@Override
	protected void onConnect(PlayerJoinEvent event, Player player) {

		if (this.hasActiveKoth()) {
			if (Config.enableScoreboard) {
				this.manager.createBoard(player, Config.scoreboardTitle);
			}
		}

		schedule(500, () -> {
			if (event.getPlayer().getName().startsWith("Maxlego") || event.getPlayer().getName().startsWith("Sak")) {
				String version = ZPlugin.z().getDescription().getFullName();
				message(player, "§aLe serveur utilise §2%version%§a!", "%version%", version);
				String name = "%%__USER__%%";
				message(player, "§aUtilisateur spigot §2%name%§a.", "%name%", name);
			}

		});
	}

	@Override
	protected void onQuit(PlayerQuitEvent event, Player player) {
		if (Config.enableScoreboard) {
			this.manager.delete(player);
		}

		List<Koth> koths = this.getActiveKoths();
		koths.forEach(koth -> koth.onPlayerLeave(player));
	}

	@Override
	public void deleteKoth(CommandSender sender, String name) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		ZKoth koth = (ZKoth) optional.get();
		koths.remove(koth);
		message(sender, Message.ZKOTH_DELETE_SUCCESS, "%name%", name);

		this.save(this.plugin.getPersist());
	}

	@Override
	public void moveKoth(CommandSender sender, Location maxLocation, Location minLocation, String name) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		KothMoveEvent event = new KothMoveEvent(koth, maxLocation, minLocation);
		event.callEvent();

		if (event.isCancelled()) {
			return;
		}

		koth.move(minLocation, maxLocation);
		message(sender, Message.ZKOTH_MOVE_SUCCESS, "%name%", name);

		this.save(this.plugin.getPersist());
	}

	@Override
	public void spawnKoth(CommandSender sender, String name, boolean isNow) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.spawn(sender, isNow);

		if (Config.enableScoreboard) {

			this.manager.setLinesAndSchedule(koth.onScoreboard());
			for (Player player : Bukkit.getOnlinePlayers()) {
				this.manager.createBoard(player, Config.scoreboardTitle);
			}

		}
	}

	@Override
	public List<Koth> getActiveKoths() {
		return koths.stream().filter(koth -> koth.isEnable() && !koth.isCooldown()).collect(Collectors.toList());
	}

	@Override
	public List<Koth> getEnableKoths() {
		return koths.stream().filter(koth -> koth.isEnable() || koth.isCooldown()).collect(Collectors.toList());
	}

	@Override
	protected void onMove(PlayerMoveEvent event, Player player) {

		if (this.playerMoveEventCooldown != 0 && this.playerMoveEventCooldown > System.currentTimeMillis()) {

			// On est dans le cooldown

		} else {

			this.playerMoveEventCooldown = System.currentTimeMillis() + Config.playerMoveEventCooldown;
			List<Koth> koths = getActiveKoths();
			koths.forEach(koth -> koth.playerMove(player, factionListener));

		}

	}

	@Override
	public boolean hasActiveKoth() {
		return koths.stream().anyMatch(koth -> koth.isEnable());
	}

	@Override
	public void onKothWin(KothWinEvent event, Koth koth) {
		this.manager.clearBoard();
	}

	@Override
	public void onKothStop(KothStopEvent event, Koth koth) {
		this.manager.clearBoard();
	}

	@Override
	public void sendKothList(CommandSender sender) {

		if (sender instanceof ConsoleCommandSender) {

			String string = toList(koths.stream().map(e -> e.getName()).collect(Collectors.toList()), "§8", "§7");
			message(sender, Message.ZKOTH_LIST_CONSOLE, "%koth%", string);

		} else {

			Player player = (Player) sender;
			message(player, "§fKoths§8:");
			koths.forEach(koth -> buildKothMessage(player, koth));

		}

	}

	/**
	 * Permet d'afficher les informations d'un koth
	 * 
	 * @param player
	 * @param koth
	 */
	private void buildKothMessage(Player player, Koth koth) {

		TextComponent component = buildTextComponent("§f§l» §7" + koth.getName() + " ");

		Cuboid cuboid = koth.getCuboid();
		Location center = cuboid.getCenter();
		String location = center.getWorld().getName() + ", " + center.getBlockX() + ", " + center.getBlockY() + ", "
				+ center.getBlockZ();

		setHoverMessage(component, "§7Coordinate: " + location);
		setClickAction(component, Action.SUGGEST_COMMAND, "/koth info " + koth.getName());

		TextComponent spawn = buildTextComponent("§8(§2Spawn§8)");
		setClickAction(spawn, Action.SUGGEST_COMMAND, "/koth spawn " + koth.getName());
		setHoverMessage(spawn, "§7Click for spawn koth");

		TextComponent now = buildTextComponent(" §8(§aSpawn now§8)");
		setClickAction(now, Action.SUGGEST_COMMAND, "/koth now " + koth.getName());
		setHoverMessage(now, "§7Click for spawn koth now");

		TextComponent delete = buildTextComponent(" §8(§cDelete§8)");
		setClickAction(delete, Action.SUGGEST_COMMAND, "/koth delete " + koth.getName());
		setHoverMessage(delete, "§7Click for delete koth");

		component.addExtra(spawn);
		component.addExtra(now);
		component.addExtra(delete);

		player.spigot().sendMessage(component);

	}

	@Override
	public void showInformations(CommandSender sender, String name) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		Cuboid cuboid = koth.getCuboid();
		Location center = cuboid.getCenter();
		String location = center.getWorld().getName() + ", " + center.getBlockX() + ", " + center.getBlockY() + ", "
				+ center.getBlockZ();

		message(sender, "§fName: §b%name%", "%name%", koth.getName());
		message(sender, "§fCoordinate: §b%location%", "%location%", location);
		message(sender, "§fType: §b%type%", "%type%", name(koth.getType().name()));
		message(sender, "§fMax points: §b%points%", "%points%", koth.getMaxPoints());
		message(sender, "§fMax timer seconds: §b%timer%", "%timer%", koth.getMaxSecondsCap());
		message(sender, "§fLoot type: §b%lootType%", "%lootType%", name(koth.getLootType().name()));
		message(sender, "§fCommands §8(§7" + koth.getCommands().size() + "§8):");
		if (sender instanceof ConsoleCommandSender) {
			koth.getCommands().forEach(command -> messageWO(sender, " §7" + command));
			message(sender, "§dHow to add command ? §d/zkoth addc <koth> <command>");
		} else {

			Player player = (Player) sender;

			for (int a = 0; a != koth.getCommands().size(); a++) {

				TextComponent textComponent = buildTextComponent(" §b#" + (a + 1) + " §f" + koth.getCommands().get(0));
				setClickAction(textComponent, Action.SUGGEST_COMMAND,
						"/koth removec " + koth.getName() + " " + (a + 1));
				setHoverMessage(textComponent, "§7Click for remove command");
				player.spigot().sendMessage(textComponent);

			}

			TextComponent textComponent = buildTextComponent(
					Message.PREFIX.getMessage() + " §fHow to add command ? §d/zkoth §daddc §d<koth> §d<command>");
			setClickAction(textComponent, Action.SUGGEST_COMMAND, "/koth addc " + koth.getName() + " ");
			setHoverMessage(textComponent, "§7Click for add command");
			player.spigot().sendMessage(textComponent);
		}

	}

	@Override
	public void addCommand(CommandSender sender, String name, String command) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.addCommand(command);
		message(sender, Message.ZKOTH_COMMAND_CREATE, "%command%", command);

		this.save(this.plugin.getPersist());
	}

	@Override
	public void removeCommand(CommandSender sender, String name, int id) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.removeCommand(id);
		message(sender, Message.ZKOTH_COMMAND_DELETE);

		this.save(this.plugin.getPersist());
	}

	@Override
	public void setKothLoot(CommandSender sender, String name, LootType type) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.setLootType(type);
		message(sender, Message.ZKOTH_LOOT_EDIT, "%type%", name(type.name()), "%name%", koth.getName());

		this.save(this.plugin.getPersist());
	}

	@Override
	public void setKothType(CommandSender sender, String name, KothType kothType) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.setType(kothType);
		message(sender, Message.ZKOTH_TYPE_EDIT, "%type%", name(kothType.name()), "%name%", koth.getName());

		this.save(this.plugin.getPersist());
	}

	@Override
	public void setKothPoints(CommandSender sender, String name, int points) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.setMaxPoints(points);
		message(sender, Message.ZKOTH_POINTS_EDIT, "%points%", points, "%name%", koth.getName());

		this.save(this.plugin.getPersist());
	}

	@Override
	public void setKothTimerSeconds(CommandSender sender, String name, int seconds) {
		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.setMaxSecondsCap(seconds);
		message(sender, Message.ZKOTH_TIMER_EDIT, "%seconds%", seconds, "%name%", koth.getName());

		this.save(this.plugin.getPersist());
	}

	@Override
	public void updateLoots(Player player, String name) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(player, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		String inventoryName = this.getMessage(Message.ZKOTH_LOOT_INVENTORY, "%name%", name);
		Inventory inventory = Bukkit.createInventory(null, 54, inventoryName);

		int slot = 0;
		for (ItemStack itemStack : koth.getItemStacks()) {
			inventory.setItem(slot++, itemStack);
		}

		this.lootInventories.put(inventory, name);

		player.openInventory(inventory);
	}

	@Override
	protected void onInventoryClose(InventoryCloseEvent event, Player player) {

		Inventory inventory = event.getInventory();

		if (this.lootInventories.containsKey(inventory)) {

			String name = this.lootInventories.get(inventory);
			Optional<Koth> optional = getKoth(name);
			if (!optional.isPresent()) {
				message(player, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
				return;
			}

			Koth koth = optional.get();
			List<ItemStack> itemStacks = new ArrayList<>();
			for (ItemStack itemStack : event.getInventory().getContents()) {
				if (itemStack != null) {
					itemStacks.add(itemStack);
				}
			}

			koth.setItemStacks(itemStacks);
			message(player, Message.ZKOTH_LOOT_CHANGE, "%name%", koth.getName());
		}

	}

	@Override
	public void stopKoth(CommandSender sender, String name) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.stop(sender);

		this.save(this.plugin.getPersist());
	}

	@Override
	public void setCaptureSeconds(CommandSender sender, String name, int second) {

		Optional<Koth> optional = getKoth(name);
		if (!optional.isPresent()) {
			message(sender, Message.ZKOTH_DOESNT_EXIST, "%name%", name);
			return;
		}

		Koth koth = optional.get();
		koth.setCapture(second);
		message(sender, Message.ZKOTH_CAPUTRE_EDIT, "%name%", koth.getName(), "%seconds%",
				TimerBuilder.getStringTime(second));

		this.save(this.plugin.getPersist());
	}

	@Override
	public List<String> getKothNames() {
		return koths.stream().map(e -> e.getName()).collect(Collectors.toList());
	}

}
