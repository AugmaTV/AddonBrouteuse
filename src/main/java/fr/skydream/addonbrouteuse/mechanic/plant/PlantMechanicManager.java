package fr.skydream.addonbrouteuse.mechanic.plant;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.JobsPlayer;
import io.th0rgal.oraxen.compatibilities.CompatibilitiesManager;
import io.th0rgal.oraxen.compatibilities.provided.worldguard.WorldGuardCompatibility;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlantMechanicManager implements Listener {
    private final MechanicFactory factory;
    private final WorldGuardCompatibility worldGuardCompatibility;

    public PlantMechanicManager(MechanicFactory factory) {
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

        PlantMechanic mechanic = (PlantMechanic) factory.getMechanic(itemID);

        Player player = event.getPlayer();

        BlockActionInfo bInfo;
        Material playerSeed;
        JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(player);

        for (Block block : getNearbyBlocks(event.getClickedBlock().getLocation(), mechanic.getRadius(), mechanic.getHeight())) {
            if (worldGuardCompatibility != null && !worldGuardCompatibility.cannotBreak(player, block))
                return;
            if (block.getType() == Material.FARMLAND) {
                if(hasSeeds(player)) {
                    Block upperBlock = block.getLocation().add(0, 1, 0).getBlock();
                    if (upperBlock.getType().equals(Material.AIR)) {
                        playerSeed = getSeeds(player);
                        if(!playerSeed.equals(Material.AIR)) {
                            upperBlock.setType(playerSeed);
                            removeItem(new ItemStack(getItemSeed(playerSeed), 1), player);
                            bInfo = new BlockActionInfo(upperBlock, ActionType.PLACE);
                            Jobs.action(jPlayer, bInfo, upperBlock);
                        }
                    }
                }
            }
            if(block.getType().equals(Material.SOUL_SAND)) {
                if(hasNetherWart(player)) {
                    Block upperBlock = block.getLocation().add(0, 1, 0).getBlock();
                    if (upperBlock.getType().equals(Material.AIR)) {
                        upperBlock.setType(Material.NETHER_WART);
                        removeItem(new ItemStack(Material.NETHER_WART, 1), player);
                        bInfo = new BlockActionInfo(upperBlock, ActionType.PLACE);
                        Jobs.action(jPlayer, bInfo, upperBlock);
                    }
                }
            }
        }
    }

    private static List<Block> getNearbyBlocks(Location location, int radius, int height) {
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

    public static boolean hasSeeds(HumanEntity player) {
        return player.getInventory().contains(Material.WHEAT_SEEDS) || player.getInventory().contains(Material.BEETROOT_SEEDS) || player.getInventory().contains(Material.POTATO) ||player.getInventory().contains(Material.CARROT);
    }

    public static boolean hasNetherWart(HumanEntity player) {
        return player.getInventory().contains(Material.NETHER_WART);
    }

    public static Material getSeeds(Player player) {
        if(player.getInventory().contains(Material.WHEAT_SEEDS)) {
            return Material.WHEAT;
        } else if(player.getInventory().contains(Material.BEETROOT_SEEDS)) {
            return Material.BEETROOTS;
        } else if(player.getInventory().contains(Material.CARROT)) {
            return Material.CARROTS;
        } else if(player.getInventory().contains(Material.POTATO)) {
            return Material.POTATOES;
        } else {
            return Material.AIR;
        }

    }

    public static void removeItem(ItemStack item, HumanEntity player) {
        player.getInventory().removeItem(item);
    }

    public static Material getItemSeed(Material mat) {
        switch(mat) {
            case BEETROOTS:
                return Material.BEETROOT_SEEDS;
            case WHEAT:
                return Material.WHEAT_SEEDS;
            case POTATOES:
                return Material.POTATO;
            case CARROTS:
                return Material.CARROT;
            default:
                return Material.AIR;
        }
    }
}
