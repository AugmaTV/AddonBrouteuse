package fr.skydream.addonbrouteuse.mechanic.harvestandplant;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.JobsPlayer;
import fr.skydream.addonbrouteuse.mechanic.harvest.HarvestMechanicManager;
import fr.skydream.addonbrouteuse.mechanic.plant.PlantMechanicManager;
import io.th0rgal.oraxen.compatibilities.CompatibilitiesManager;
import io.th0rgal.oraxen.compatibilities.provided.worldguard.WorldGuardCompatibility;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HarvestAndPlantMechanicManager implements Listener {
    private final MechanicFactory factory;
    private final WorldGuardCompatibility worldGuardCompatibility;

    public HarvestAndPlantMechanicManager(MechanicFactory factory) {
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

        HarvestAndPlantMechanic mechanic = (HarvestAndPlantMechanic) factory.getMechanic(itemID);

        Player player = event.getPlayer();
        JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        Material playerSeed;
        BlockActionInfo bInfo;

        for (Block block : HarvestMechanicManager.getNearbyBlocks(event.getClickedBlock().getLocation(), mechanic.getRadius(),
                mechanic.getHeight())) {
            if (block.getBlockData() instanceof Ageable) {
                if (worldGuardCompatibility != null && !worldGuardCompatibility.cannotBreak(player, block))
                    return;
                Ageable ageable = (Ageable) block.getBlockData();
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    bInfo = new BlockActionInfo(block, ActionType.BREAK);
                    Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), bInfo, block);
                    block.breakNaturally();
                }
            }
            if (block.getType().equals(Material.FARMLAND)) {
                if(PlantMechanicManager.hasSeeds(player)) {
                    Block upperBlock = block.getLocation().add(0, 1, 0).getBlock();
                    if (upperBlock.getType().equals(Material.AIR)) {
                        playerSeed = PlantMechanicManager.getSeeds(player);
                        if(!playerSeed.equals(Material.AIR)) {
                            upperBlock.setType(playerSeed);
                            PlantMechanicManager.removeItem(new ItemStack(PlantMechanicManager.getItemSeed(playerSeed), 1), player);
                            bInfo = new BlockActionInfo(upperBlock, ActionType.PLACE);
                            Jobs.action(jPlayer, bInfo, upperBlock);
                        }
                    }
                }
            }
            if(block.getType().equals(Material.SOUL_SAND)) {
                if(PlantMechanicManager.hasNetherWart(player)) {
                    Block upperBlock = block.getLocation().add(0, 1, 0).getBlock();
                    if (upperBlock.getType().equals(Material.AIR)) {
                        upperBlock.setType(Material.NETHER_WART);
                        PlantMechanicManager.removeItem(new ItemStack(Material.NETHER_WART, 1), player);
                        bInfo = new BlockActionInfo(upperBlock, ActionType.PLACE);
                        Jobs.action(jPlayer, bInfo, upperBlock);
                    }
                }
            }
        }
    }
}
