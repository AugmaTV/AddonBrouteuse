package fr.skydream.addonbrouteuse;

import fr.skydream.addonbrouteuse.mechanic.harvest.HarvestMechanicFactory;
import fr.skydream.addonbrouteuse.mechanic.harvestandplant.HarvestAndPlantMechanicFactory;
import fr.skydream.addonbrouteuse.mechanic.plant.PlantMechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AddonBrouteuse extends JavaPlugin {

    @Override
    public void onEnable() {
        MechanicsManager.registerMechanicFactory("harvest", HarvestMechanicFactory.class);
        MechanicsManager.registerMechanicFactory("plant", PlantMechanicFactory.class);
        MechanicsManager.registerMechanicFactory("harvestandplant", HarvestAndPlantMechanicFactory.class);
    }

    @Override
    public void onDisable() {
    }
}
