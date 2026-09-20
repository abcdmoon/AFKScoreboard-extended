package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.ConfigManager;

import java.util.*;

public final class PrefixRegistry {
    private final Map<String, Prefix> prefixes = new HashMap<>();
    private final Map<Integer, List<Prefix>> conditionMap = new HashMap<>();
    private final ConfigManager configManager;
    public PrefixRegistry(ConfigManager configManager) {
        this.configManager = configManager;
        for(Prefix prefix : configManager.loadPrefixes()){
            prefixes.put(prefix.key(),  prefix);
            conditionMap.computeIfAbsent(prefix.requireScore(), k->new ArrayList<>()).add(prefix);
        }
    }

    public Prefix getPrefix(String key){
        return prefixes.get(key);
    }

    public List<Prefix> getPrefixesByCondition(int condition){
        return conditionMap.getOrDefault(condition, new ArrayList<>());
    }

    public Collection<Prefix> getAllPrefixes(){
        return prefixes.values();
    }

    public List<Integer> getAllConditions(){
        return conditionMap.keySet()
                .stream().sorted().toList();
    }
}
