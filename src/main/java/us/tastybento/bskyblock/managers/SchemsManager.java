package us.tastybento.bskyblock.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;

import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.api.addons.Addon;
import us.tastybento.bskyblock.database.objects.Island;
import us.tastybento.bskyblock.island.builders.Clipboard;

public class SchemsManager {

    private BSkyBlock plugin;
    private Map<World, Clipboard> islandSchems;

    /**
     * @param plugin
     */
    public SchemsManager(BSkyBlock plugin) {
        this.plugin = plugin;
        islandSchems = new HashMap<>();
    }

    private void copySchems(File schems, World world, String name) {
        if (!schems.exists() && !schems.mkdirs()) {
            plugin.logError("Could not make schems folder!");
            return;
        }

        Optional<Addon> addon = plugin.getIWM().getAddon(world);
        if (addon.isPresent()) {
            addon.get().saveResource("schems/" + name + ".schem", false);
        } else {
            plugin.saveResource("schems/" + name + ".schem", false);
        }
    }

    public Clipboard get(World world) {
        return islandSchems.get(world);
    }

    /**
     * Load schems for world. Will try and load nether and end schems too if settings are set.
     * @param world - world
     */
    public void loadIslands(World world) {
        if (plugin.getSchemsManager().loadSchem(world, "island")) {
            plugin.log("Loaded island for " + plugin.getIWM().getFriendlyName(world));
        } else {
            plugin.logError("Could not load island for " + plugin.getIWM().getFriendlyName(world));
        }
        if (plugin.getIWM().isNetherGenerate(world) && plugin.getIWM().isNetherIslands(world)) {

            if (plugin.getSchemsManager().loadSchem(plugin.getIWM().getNetherWorld(world), "nether-island")) {
                plugin.log("Loaded nether island for " + plugin.getIWM().getFriendlyName(world));
            } else {
                plugin.logError("Could not load nether island for " + plugin.getIWM().getFriendlyName(world));
            }
        }
        if (plugin.getIWM().isEndGenerate(world) && plugin.getIWM().isEndIslands(world)) {
            if (plugin.getSchemsManager().loadSchem(plugin.getIWM().getEndWorld(world), "end-island")) {
                plugin.log("Loaded end island for " + plugin.getIWM().getFriendlyName(world));
            } else {
                plugin.logError("Could not load end island for " + plugin.getIWM().getFriendlyName(world));
            }
        }


    }

    private boolean loadSchem(World world, String name) {
        File schems = new File(plugin.getIWM().getDataFolder(world), "schems");
        copySchems(schems, world, name);
        try {
            Clipboard cb = new Clipboard(plugin, schems);
            cb.load(name);
            islandSchems.put(world, cb);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.logError("Could not load " + name + " schem");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Paste the schem for world to the island center location and run task afterwards
     * @param world - world to paste to
     * @param island - the island who owns this schem
     * @param task - task to run after pasting is completed
     */
    public void paste(World world, Island island, Runnable task) {
        if (islandSchems.containsKey(world)) {
            islandSchems.get(world).paste(world, island, task);
        }
    }

    /**
     * Paste the schem to world for island
     * @param world
     * @param island
     */
    public void paste(World world, Island island) {
        paste(world, island, null);

    }

}
