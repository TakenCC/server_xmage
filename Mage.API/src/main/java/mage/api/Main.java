package mage.api;

import mage.api.config.RestApplication;
import mage.cards.ExpansionSet;
import mage.cards.Sets;
import mage.cards.decks.DeckValidatorFactory;
import mage.cards.repository.CardScanner;
import mage.cards.repository.PluginClassloaderRegistery;
import mage.cards.repository.RepositoryUtil;
import mage.game.match.MatchType;
import mage.game.tournament.TournamentType;
import mage.server.AuthorizedUserRepository;
import mage.server.draft.CubeFactory;
import mage.server.game.GameFactory;
import mage.server.game.PlayerFactory;
import mage.server.managers.ConfigSettings;
import mage.server.managers.ManagerFactory;
import mage.server.record.UserStatsRepository;
import mage.server.tournament.TournamentFactory;
import mage.server.util.ConfigFactory;
import mage.server.util.ConfigWrapper;
import mage.server.util.PluginClassLoader;
import mage.server.util.ServerMessagesUtil;
import mage.server.util.config.GamePlugin;
import mage.server.util.config.Plugin;
import mage.utils.MageVersion;
import mage.utils.SystemUtil;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);
    private static final MageVersion version = new MageVersion(Main.class);

    private static final String configPathProp = "xmage.config.path";
    private static final String portProp = "xmage.api.port";
    private static final String hostProp = "xmage.api.host";

    private static final File pluginFolder = new File("plugins");
    private static final File extensionFolder = new File("extensions");
    private static final String defaultConfigPath = Paths.get("config", "config.xml").toString();

    public static final PluginClassLoader classLoader = new PluginClassLoader();
    private static HttpServer server;
    private static ManagerFactory managerFactory;
    private static ConfigWrapper config;

    public static void main(String[] args) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        logger.info("Starting MAGE REST API SERVER version: " + version);
        logger.info("Java version: " + System.getProperty("java.version"));
        logger.info("Logging level: " + logger.getEffectiveLevel());
        logger.info("Default charset: " + Charset.defaultCharset());

        final String configPath;
        if (System.getProperty(configPathProp) != null) {
            configPath = System.getProperty(configPathProp);
        } else {
            configPath = defaultConfigPath;
        }

        logger.info(String.format("Reading configuration from path=%s", configPath));
        config = new ConfigWrapper(ConfigFactory.loadFromFile(configPath));

        // Initialize authentication
        if (config.isAuthenticationActivated()) {
            logger.info("Check authorized user DB version ...");
            if (!AuthorizedUserRepository.getInstance().checkAlterAndMigrateAuthorizedUser()) {
                logger.fatal("Failed to start server.");
                return;
            }
            logger.info("Done.");
        }

        // Initialize database
        RepositoryUtil.bootstrapLocalDb();
        logger.info("Done.");

        // Load extensions
        logger.info("Loading extension packages...");
        if (!extensionFolder.exists()) {
            if (!extensionFolder.mkdirs()) {
                logger.error("Could not create extensions directory.");
            }
        }
        File[] extensionDirectories = extensionFolder.listFiles();
        List<mage.server.ExtensionPackage> extensions = new ArrayList<>();
        if (extensionDirectories != null) {
            for (File f : extensionDirectories) {
                if (f.isDirectory()) {
                    try {
                        logger.info(" - Loading extension from " + f);
                        extensions.add(mage.server.ExtensionPackageLoader.loadExtension(f));
                    } catch (IOException e) {
                        logger.error("Could not load extension in " + f + '!', e);
                    }
                }
            }
        }
        logger.info("Done.");

        if (!extensions.isEmpty()) {
            logger.info("Registering custom sets...");
            for (mage.server.ExtensionPackage pkg : extensions) {
                for (ExpansionSet set : pkg.getSets()) {
                    logger.info("- Loading " + set.getName() + " (" + set.getCode() + ')');
                    Sets.getInstance().addSet(set);
                }
                PluginClassloaderRegistery.registerPluginClassloader(pkg.getClassLoader());
            }
            logger.info("Done.");
        }

        // Load cards
        logger.info("Loading cards...");
        CardScanner.scan();
        logger.info("Done.");

        // Update user stats
        logger.info("Updating user stats DB...");
        UserStatsRepository.instance.updateUserStats();
        logger.info("Done.");
        deleteSavedGames();

        // Create manager factory
        managerFactory = new mage.server.MainManagerFactory(config);

        // Load game types
        int gameTypes = 0;
        for (GamePlugin plugin : config.getGameTypes()) {
            gameTypes++;
            GameFactory.instance.addGameType(plugin.getName(), loadGameType(plugin), loadPlugin(plugin));
        }

        // Load tournament types
        int tourneyTypes = 0;
        for (GamePlugin plugin : config.getTournamentTypes()) {
            tourneyTypes++;
            TournamentFactory.instance.addTournamentType(plugin.getName(), loadTournamentType(plugin), loadPlugin(plugin));
        }

        // Load player types
        int playerTypes = 0;
        for (Plugin plugin : config.getPlayerTypes()) {
            playerTypes++;
            PlayerFactory.instance.addPlayerType(plugin.getName(), loadPlugin(plugin));
        }

        // Load cube types
        int cubeTypes = 0;
        for (Plugin plugin : config.getDraftCubes()) {
            cubeTypes++;
            CubeFactory.instance.addDraftCube(plugin.getName(), loadPlugin(plugin));
        }

        // Load deck types
        int deckTypes = 0;
        for (Plugin plugin : config.getDeckTypes()) {
            deckTypes++;
            DeckValidatorFactory.instance.addDeckType(plugin.getName(), loadPlugin(plugin));
        }

        // Load extension plugins
        for (mage.server.ExtensionPackage pkg : extensions) {
            for (Map.Entry<String, Class> entry : pkg.getDraftCubes().entrySet()) {
                logger.info("Loading extension: [" + entry.getKey() + "] " + entry.getValue().toString());
                cubeTypes++;
                CubeFactory.instance.addDraftCube(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Class> entry : pkg.getDeckTypes().entrySet()) {
                logger.info("Loading extension: [" + entry.getKey() + "] " + entry.getValue().toString());
                deckTypes++;
                DeckValidatorFactory.instance.addDeckType(entry.getKey(), entry.getValue());
            }
        }

        logger.info("Loaded game types: " + gameTypes
                + ", tourneys: " + tourneyTypes
                + ", players: " + playerTypes
                + ", cubes: " + cubeTypes
                + ", decks: " + deckTypes);

        // Initialize statistics
        ServerMessagesUtil.instance.setStartDate(System.currentTimeMillis());

        // Start HTTP server
        String host = System.getProperty(hostProp, "localhost");
        int port = Integer.parseInt(System.getProperty(portProp, "8080"));
        URI baseUri = UriBuilder.fromUri("http://" + host + "/").port(port).build();

        RestApplication resourceConfig = new RestApplication();
        
        // Configure dependency injection
        resourceConfig.register(new mage.api.config.DependencyBinder(managerFactory));

        try {
            server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
            logger.info("Started MAGE REST API server - listening on http://" + host + ":" + port);
            logger.info("REST API available at http://" + host + ":" + port + "/api");
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down MAGE REST API server...");
                if (server != null) {
                    server.shutdownNow();
                }
            }));

            // Keep server running
            Thread.currentThread().join();
        } catch (Exception ex) {
            logger.fatal("Failed to start REST API server", ex);
        }
    }

    private static Class<?> loadPlugin(Plugin plugin) {
        try {
            logger.debug("Loading plugin: " + plugin.getClassName());
            if (plugin.getName() == null || plugin.getName().isEmpty()
                    || plugin.getJar() == null || plugin.getJar().isEmpty()
                    || plugin.getClassName() == null || plugin.getClassName().isEmpty()
            ) {
                logger.error(String.format("Can't load plugin, found miss fields in config.xml: %s, %s, %s",
                        plugin.getName(),
                        plugin.getJar(),
                        plugin.getClassName()
                ));
                return null;
            }
            classLoader.addURL(new File(pluginFolder, plugin.getJar()).toURI().toURL());
            return Class.forName(plugin.getClassName(), true, classLoader);
        } catch (Exception ex) {
            logger.warn(new StringBuilder("Plugin not Found: ").append(plugin.getClassName()).append(" - ").append(plugin.getJar()), ex);
        }
        return null;
    }

    private static MatchType loadGameType(GamePlugin plugin) {
        try {
            classLoader.addURL(new File(pluginFolder, plugin.getJar()).toURI().toURL());
            logger.debug("Loading game type: " + plugin.getClassName());
            return (MatchType) Class.forName(plugin.getTypeName(), true, classLoader).getConstructor().newInstance();
        } catch (Exception ex) {
            logger.warn("Game type not found:" + plugin.getJar(), ex);
        }
        return null;
    }

    private static TournamentType loadTournamentType(GamePlugin plugin) {
        try {
            classLoader.addURL(new File(pluginFolder, plugin.getJar()).toURI().toURL());
            return (TournamentType) Class.forName(plugin.getTypeName(), true, classLoader).getConstructor().newInstance();
        } catch (Exception ex) {
            logger.warn("Tournament type not found:" + plugin.getName() + " / " + plugin.getJar(), ex);
        }
        return null;
    }

    private static void deleteSavedGames() {
        File directory = new File("saved/");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File[] files = directory.listFiles(
                (dir, name) -> name.endsWith(".game")
        );
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static ManagerFactory getManagerFactory() {
        return managerFactory;
    }

    public static MageVersion getVersion() {
        return version;
    }
}

