package fr.skydream.addonbrouteuse.mechanic.harvest;

import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class HarvestMechanic extends Mechanic {
    private final int radius;
    private final int height;

    public HarvestMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.radius = section.getInt("radius");
        this.height = section.getInt("height");
    }

    public int getRadius() {
        return this.radius;
    }

    public int getHeight() {
        return height;
    }
}
