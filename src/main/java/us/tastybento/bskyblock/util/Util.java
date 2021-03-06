package us.tastybento.bskyblock.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.util.Vector;

import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.api.user.User;

/**
 * A set of utility methods
 *
 * @author tastybento
 * @author Poslovitch
 */
public class Util {

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";
    private static String serverVersion = null;
    private static BSkyBlock plugin = BSkyBlock.getInstance();

    private Util() {}

    public static void setPlugin(BSkyBlock p) {
        plugin = p;
    }

    /**
     * Returns the server version
     * @return server version
     */
    public static String getServerVersion() {
        if (serverVersion == null) {
            String serverPackageName = plugin.getServer().getClass().getPackage().getName();
            serverVersion = serverPackageName.substring(serverPackageName.lastIndexOf('.') + 1);
        }
        return serverVersion;
    }

    /**
     * Converts a serialized location to a Location. Returns null if string is
     * empty
     *
     * @param s
     *            - serialized location in format "world:x:y:z"
     * @return Location
     */
    public static Location getLocationString(final String s) {
        if (s == null || s.trim().equals("")) {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 6) {
            final World w = Bukkit.getServer().getWorld(parts[0]);
            if (w == null) {
                return null;
            }
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            final float yaw = Float.intBitsToFloat(Integer.parseInt(parts[4]));
            final float pitch = Float.intBitsToFloat(Integer.parseInt(parts[5]));
            return new Location(w, x, y, z, yaw, pitch);
        }
        return null;
    }

    /**
     * Converts a location to a simple string representation
     * If location is null, returns empty string
     *
     * @param l - the location
     * @return String of location
     */
    public static String getStringLocation(final Location l) {
        if (l == null || l.getWorld() == null) {
            return "";
        }
        return l.getWorld().getName() + ":" + format(l.getX()) + ":" + format(l.getY()) + ":" + format(l.getZ()) + ":" + Float.floatToIntBits(l.getYaw()) + ":" + Float.floatToIntBits(l.getPitch());
    }

    private static String format(double num) {
        return String.valueOf(Math.round(num * 100D) / 100D);
    }

    /**
     * Get a list of parameter types for the collection argument in this method
     * @param writeMethod - write method
     * @return a list of parameter types for the collection argument in this method
     */
    public static List<Type> getCollectionParameterTypes(Method writeMethod) {
        List<Type> result = new ArrayList<>();
        // Get the return type
        // This uses a trick to extract what the arguments are of the writeMethod of the field.
        // In this way, we can deduce what type needs to be written at runtime.
        Type[] genericParameterTypes = writeMethod.getGenericParameterTypes();
        // There could be more than one argument, so step through them
        for (Type genericParameterType : genericParameterTypes) {
            // If the argument is a parameter, then do something - this should always be true if the parameter is a collection
            if( genericParameterType instanceof ParameterizedType ) {
                // Get the actual type arguments of the parameter
                Type[] parameters = ((ParameterizedType)genericParameterType).getActualTypeArguments();
                result.addAll(Arrays.asList(parameters));
            }
        }
        return result;
    }

    /**
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     *
     * @param ugly
     *            The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     *
     *         Credits to mikenon on GitHub!
     */
    public static String prettifyText(String ugly) {
        StringBuilder fin = new StringBuilder();
        ugly = ugly.toLowerCase();
        if (ugly.contains("_")) {
            String[] splt = ugly.split("_");
            int i = 0;
            for (String s : splt) {
                i += 1;
                fin.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
                if (i < splt.length) {
                    fin.append(" ");
                }
            }
        } else {
            fin.append(Character.toUpperCase(ugly.charAt(0))).append(ugly.substring(1));
        }
        return fin.toString();
    }

    /**
     * Return a list of online players this player can see, i.e. are not invisible
     * @param user - the User - if null, all player names on the server are shown
     * @return a list of online players this player can see
     */
    public static List<String> getOnlinePlayerList(User user) {
        if (user == null || !user.isPlayer()) {
            // Console and null get to see every player
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        // Otherwise prevent invisible players from seeing
        return Bukkit.getOnlinePlayers().stream().filter(p -> user.getPlayer().canSee(p)).map(Player::getName).collect(Collectors.toList());
    }

    /**
     * Returns all of the items that begin with the given start,
     * ignoring case.  Intended for tabcompletion.
     *
     * @param list - string list
     * @param start - first few chars of a string
     * @return List of items that start with the letters
     */
    public static List<String> tabLimit(final List<String> list, final String start) {
        final List<String> returned = new ArrayList<>();
        for (String s : list) {
            if (s == null) {
                continue;
            }
            if (s.toLowerCase().startsWith(start.toLowerCase())) {
                returned.add(s);
            }
        }

        return returned;
    }

    /**
     * Get the maximum value of a numerical perm setting
     * @param player - the player - the player to check
     * @param perm - the start of the perm, e.g., bskyblock.maxhomes
     * @param permValue - the default value - the result may be higher or lower than this
     * @return max value
     */
    public static int getPermValue(Player player, String perm, int permValue) {
        for (PermissionAttachmentInfo perms : player.getEffectivePermissions()) {
            if (perms.getPermission().startsWith(perm + ".")) {
                // Get the max value should there be more than one
                if (perms.getPermission().contains(perm + ".*")) {
                    return permValue;
                } else {
                    String[] spl = perms.getPermission().split(perm + ".");
                    if (spl.length > 1) {
                        if (!NumberUtils.isDigits(spl[1])) {
                            plugin.logError("Player " + player.getName() + " has permission: " + perms.getPermission() + " <-- the last part MUST be a number! Ignoring...");

                        } else {
                            permValue = Math.max(permValue, Integer.valueOf(spl[1]));
                        }
                    }
                }
            }
            // Do some sanity checking
            if (permValue < 1) {
                permValue = 1;
            }
        }
        return permValue;
    }

    public static String xyz(Vector location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }


    /**
     * Checks is world = world2 irrespective of the world type
     * @param world - world
     * @param world2 - world
     * @return true if the same
     */
    public static boolean sameWorld(World world, World world2) {
        String worldName = world.getName().replaceAll(NETHER, "").replaceAll(THE_END, "");
        String world2Name = world2.getName().replaceAll(NETHER, "").replaceAll(THE_END, "");
        return worldName.equalsIgnoreCase(world2Name);
    }

    /**
     * Convert world to an overworld
     * @param world - world
     * @return over world
     */
    public static World getWorld(World world) {
        return world.getEnvironment().equals(Environment.NORMAL) ? world : Bukkit.getWorld(world.getName().replaceAll(NETHER, "").replaceAll(THE_END, ""));
    }

    /**
     * Converts block face direction to radial degrees. Returns 0 if block face
     * is not radial.
     *
     * @param face
     * @return degrees
     */
    public static float blockFaceToFloat(BlockFace face) {
        switch (face) {
        case EAST:
            return 90F;
        case EAST_NORTH_EAST:
            return 67.5F;
        case EAST_SOUTH_EAST:
            return 0F;
        case NORTH:
            return 0F;
        case NORTH_EAST:
            return 45F;
        case NORTH_NORTH_EAST:
            return 22.5F;
        case NORTH_NORTH_WEST:
            return 337.5F;
        case NORTH_WEST:
            return 315F;
        case SOUTH:
            return 180F;
        case SOUTH_EAST:
            return 135F;
        case SOUTH_SOUTH_EAST:
            return 157.5F;
        case SOUTH_SOUTH_WEST:
            return 202.5F;
        case SOUTH_WEST:
            return 225F;
        case WEST:
            return 270F;
        case WEST_NORTH_WEST:
            return 292.5F;
        case WEST_SOUTH_WEST:
            return 247.5F;
        default:
            return 0F;
        }
    }
}
