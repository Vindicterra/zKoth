package fr.maxlego08.zkoth.api;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import fr.maxlego08.zkoth.zcore.utils.storage.Saveable;

public interface KothManager extends Saveable {

	/**
	 * 
	 * @param name
	 * @return
	 */
	public Optional<Koth> getKoth(String name);

	/**
	 * Create a KOTH
	 * 
	 * @param sender
	 * @param name
	 * @param minLocation
	 * @param maxLocation
	 * @param captureSeconds
	 */
	public void createKoth(CommandSender sender, String name, Location minLocation, Location maxLocation,
			int captureSeconds);

	/**
	 * 
	 * @param sender
	 * @param name
	 */
	public void deleteKoth(CommandSender sender, String name);

	/**
	 * Get koth Axe
	 * 
	 * @return {@link ItemStack}
	 */
	public ItemStack getKothAxe();

	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public Optional<Selection> getSelection(UUID uuid);

}
