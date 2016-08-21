package nl.rutgerkok.pokkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import nl.rutgerkok.pokkit.pluginservice.MainScoreboardService;
import nl.rutgerkok.pokkit.pluginservice.PermissionsYml;
import nl.rutgerkok.pokkit.pluginservice.PlayerBlockEvents;
import nl.rutgerkok.pokkit.pluginservice.PlayerChatEvents;
import nl.rutgerkok.pokkit.pluginservice.PlayerConnectEvents;
import nl.rutgerkok.pokkit.pluginservice.PlayerInteractEvents;
import nl.rutgerkok.pokkit.pluginservice.PluginService;
import nl.rutgerkok.pokkit.pluginservice.PokkitService;

import org.bukkit.Bukkit;

import com.google.common.collect.ImmutableList;

import cn.nukkit.plugin.PluginBase;

/**
 * Startup class.
 *
 */
public final class Pokkit {

    public static String NAME = "Pokkit";
    static String VERSION = "?";
    static String BUKKIT_VERSION = "?";

    /**
     * Use {@code throw Pokkit.unsupported()} to indicate that a Bukkit API
     * method has not been implemented yet.
     *
     * @return Never returns normally. The only reason that this type is present
     *         is so that you can use {@code throw Pokkit.unsupported()}, which
     *         prevents compile errors.
     * @throws UnsupportedOperationException
     *             Always.
     */
    public static final RuntimeException unsupported() {
        throw new UnsupportedOperationException("This method is not supported yet by " + NAME + " " + VERSION);
    }

    private final List<PokkitService> services = ImmutableList.of(new MainScoreboardService(), new PermissionsYml(),
            new PluginService(), new PlayerBlockEvents(), new PlayerConnectEvents(), new PlayerChatEvents(),
            new PlayerInteractEvents());

    public Pokkit() {
        // Created using reflection
    }

    private void loadVersionNumbers(Logger logger) {
        Properties properties = new Properties();

        // Retrieving our version is easy
        VERSION = Pokkit.class.getPackage().getImplementationVersion();

        // Retrieving Spigot version is more difficult
        try (InputStream stream = Pokkit.class
                .getResourceAsStream("/META-INF/maven/org.spigotmc/spigot-api/pom.properties")) {
            properties.load(stream);
            BUKKIT_VERSION = properties.getProperty("version");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading Spigot version", e);
        }
    }

    public void onDisable(PluginBase plugin) {
        services.forEach(service -> service.onDisable(plugin));
    }

    public void onEnable(PluginBase plugin) {
        services.forEach(service -> service.onEnable(plugin));
    }

    public void onLoad(PluginBase plugin) {

        File pluginFolder = new File(plugin.getDataFolder(), "bukkitPlugins");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        Logger logger = new PokkitLogger(plugin.getLogger());
        LogManager.getLogManager().addLogger(logger);

        loadVersionNumbers(logger);

        PokkitServer server = new PokkitServer(plugin.getServer(), logger, pluginFolder);
        Bukkit.setServer(server);

        // Load the services
        services.forEach(service -> service.onLoad(plugin));
    }
}
