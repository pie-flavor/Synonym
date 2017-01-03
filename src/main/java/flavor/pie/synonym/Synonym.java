package flavor.pie.synonym;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(id = "synonym", name = "Synonym", version = "1.0.1", authors = "pie_flavor", description = "A regex-based command alias plugin.")
public class Synonym {
    @Inject
    Game game;
    @Inject @DefaultConfig(sharedRoot = true)
    ConfigurationLoader<CommentedConfigurationNode> loader;
    @Inject @DefaultConfig(sharedRoot = true)
    Path path;
    @Inject
    Logger logger;
    Config config;
    LoadingCache<String, Pattern> patterns = Caffeine.newBuilder().maximumSize(2000).build(Pattern::compile);

    @Listener
    public void preInit(GamePreInitializationEvent e) throws IOException, ObjectMappingException {
        if (!Files.exists(path)) {
            try {
                game.getAssetManager().getAsset(this, "default.conf").get().copyToFile(path);
            } catch (IOException ex) {
                logger.error("Could not copy the default config!");
                mapDefault();
                throw ex;
            }
        }
        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (IOException ex) {
            logger.error("Could not load the config!");
            mapDefault();
            throw ex;
        }
        try {
            config = node.getValue(Config.type);
        } catch (ObjectMappingException ex) {
            logger.error("Could not parse the config!");
            mapDefault();
            throw ex;
        }
    }

    private ConfigurationNode loadDefault() throws IOException {
        Asset asset = game.getAssetManager().getAsset(this, "default.conf").get();
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setURL(asset.getUrl()).build();
        try {
            return loader.load(this.loader.getDefaultOptions());
        } catch (IOException ex) {
            logger.error("Could not load embedded default config!");
            throw ex;
        }
    }

    private void mapDefault() throws IOException, ObjectMappingException {
        ConfigurationNode node = loadDefault();
        try {
            config = node.getValue(Config.type);
        } catch (ObjectMappingException ex) {
            logger.error("Could not parse embedded default config!");
            throw ex;
        }
    }

    @Listener
    public void cmd(SendCommandEvent e) {
        for (Config.AliasEntry entry : config.aliases) {
            Pattern cmdpattern = patterns.get(entry.cmd);
            Matcher cmdmatcher = cmdpattern.matcher(e.getCommand() + " " + e.getArguments());
            if (cmdmatcher.matches()) {
                String newcmd = cmdmatcher.replaceAll(entry.replacement);
                entries: for (Map.Entry<String, Config.VariableEntry> entries : entry.vars.entrySet()) {
                    Config.VariableEntry varentry = entries.getValue();
                    String varname = entries.getKey();
                    cmdmatcher.reset();
                    String varvalue = cmdmatcher.replaceAll(varentry.text);
                    for (Config.VariableOption option : varentry.options) {
                        Pattern optpattern = patterns.get(option.option);
                        Matcher optmatcher = optpattern.matcher(varvalue);
                        if (optmatcher.matches()) {
                            String endvar;
                            if (option.match == Config.MatchType.OPTION) {
                                endvar = optmatcher.replaceAll(option.value);
                            } else {
                                cmdmatcher.reset();
                                endvar = cmdmatcher.replaceAll(option.value);
                            }
                            newcmd = newcmd.replace(varname, endvar);
                            continue entries;
                        }
                    }
                }
                String[] cmd = newcmd.split(" ");
                e.setCommand(cmd[0]);
                e.setArguments(String.join(" ", (CharSequence[]) Arrays.copyOfRange(cmd, 1, cmd.length)));
                return;
            }
        }
    }

}
