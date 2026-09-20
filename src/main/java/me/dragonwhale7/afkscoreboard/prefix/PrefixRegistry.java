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

    public List<Prefix> getPrefixesByCondition(int condition){
        return conditionMap.get(condition);
    }

    public Collection<Prefix> getAllPrefixes(){
        return prefixes.values();
    }

    public Set<Integer> getAllConditions(){
        return conditionMap.keySet();
    }
}
