package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.AFKScoreboard;
import me.dragonwhale7.afkscoreboard.ConfigManager;

import java.util.*;

public final class PrefixRegistry {
    private final Map<String, Prefix> prefixes = new HashMap<>();
    private final Set<String> teamKeys = new HashSet<>();
    private final Map<Integer, List<Prefix>> conditionMap = new HashMap<>();
    private final ConfigManager configManager;
    public PrefixRegistry(ConfigManager configManager) {
        this.configManager = configManager;
        loadPrefixes();
    }

    public void loadPrefixes(){
        prefixes.clear();
        conditionMap.clear();
        for(Prefix prefix : configManager.loadPrefixes()){
            if(prefixes.containsKey(prefix.key())){
                AFKScoreboard.warn("There is already a prefix with the same key!");
                continue;
            }
            prefixes.put(prefix.key(),  prefix);
            teamKeys.add(prefix.teamKey());
            conditionMap.computeIfAbsent(prefix.requireScore(), k->new ArrayList<>()).add(prefix);
        }
    }

    public Prefix getPrefix(String key){
        return prefixes.get(key);
    }

    public Set<String> getAllKeys(){
        return prefixes.keySet();
    }

    public List<Prefix> getPrefixesByCondition(int condition){
        return conditionMap.getOrDefault(condition,List.of());
    }

    public Collection<Prefix> getAllPrefixes(){
        return prefixes.values();
    }

    public List<Integer> getAllConditions(){
        return conditionMap.keySet()
                .stream().sorted().toList();
    }
}
