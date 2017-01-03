package flavor.pie.synonym;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;
import java.util.Map;

@ConfigSerializable
public class Config {
    public final static TypeToken<Config> type = TypeToken.of(Config.class);
    @Setting public List<AliasEntry> aliases = ImmutableList.of();

    @ConfigSerializable
    public static class AliasEntry {
        @Setting public String cmd;
        @Setting public String replacement;
        @Setting public Map<String, VariableEntry> vars = ImmutableMap.of();
    }

    @ConfigSerializable
    public static class VariableEntry {
        @Setting public String text;
        @Setting public List<VariableOption> options;
    }

    @ConfigSerializable
    public static class VariableOption {
        @Setting public String option;
        @Setting public String value;
        @Setting public MatchType match = MatchType.OPTION;
    }

    public enum MatchType {
        CMD, OPTION
    }
}
