package fr.skydream.addonbrouteuse.mechanic.harvest;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import io.th0rgal.oraxen.compatibilities.CompatibilitiesManager;
import io.th0rgal.oraxen.compatibilities.provided.worldguard.WorldGuardCompatibility;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HarvestMechanicManager implements Listener {
    private final MechanicFactory factory;
    private final WorldGuardCompatibility worldGuardCompatibility;

    public HarvestMechanicManager(MechanicFactory factory) {
        this.factory = factory;
        if (CompatibilitiesManager.isCompatibilityEnabled("WorldGuard"))
            worldGuardCompatibility = (WorldGuardCompatibility) CompatibilitiesManager
                    .getActiveCompatibility("WorldGuard");
        else
            worldGuardCompatibility = null;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getClickedBlock() == null)
            return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null)
            return;

        String itemID = OraxenItems.getIdByItem(item);

        if (factory.isNotImplementedIn(itemID))
            return;

        HarvestMechanic mechanic = (HarvestMechanic) factory.getMechanic(itemID);

        Player player = event.getPlayer();

        BlockActionInfo bInfo;
        for (Block block : getNearbyBlocks(event.getClickedBlock().getLocation(), mechanic.getRadius(),
                mechanic.getHeight())) {
            if (block.getBlockData() instanceof Ageable) {
                if (worldGuardCompatibility != null && worldGuardCompatibility.cannotBreak(player, block))
                    return;
                Ageable ageable = (Ageable) block.getBlockData();
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    bInfo = new BlockActionInfo(block, ActionType.BREAK);
                    Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), bInfo, block);
                    ItemStack itemOffHand = player.getInventory().getItemInOffHand() != null ? player.getInventory().getItemInOffHand() : null;
                    ItemMeta itemOffHandMeta = itemOffHand != null ? itemOffHand.getItemMeta() : null;
                    if(itemOffHandMeta == null) {
                    }
                    String itemName = itemOffHandMeta != null ? itemOffHandMeta.getLocalizedName() : "null";
                    player.sendMessage(itemName);
                    block.breakNaturally();
                }
            }
        }
    }

    public static List<Block> getNearbyBlocks(Location location, int radius, int height) {
        List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX() - Math.floorDiv(radius, 2); x <= location.getBlockX()
                + Math.floorDiv(radius, 2); x++) {
            for (int y = location.getBlockY() - Math.floorDiv(height, 2); y <= location.getBlockY()
                    + Math.floorDiv(height, 2); y++)
                for (int z = location.getBlockZ() - Math.floorDiv(radius, 2); z <= location.getBlockZ()
                        + Math.floorDiv(radius, 2); z++) {
                    blocks.add(Objects.requireNonNull(location.getWorld()).getBlockAt(x, y, z));
                }
        }
        return blocks;
    }
}
