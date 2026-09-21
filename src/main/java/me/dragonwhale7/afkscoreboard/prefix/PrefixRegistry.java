package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.AFKScoreboard;
import me.dragonwhale7.afkscoreboard.ConfigManager;
import net.kyori.adventure.text.format.NamedTextColor;

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
        teamKeys.clear();
        conditionMap.clear();
        for(Prefix prefix : configManager.loadPrefixes()){
            AFKScoreboard.warn("key:"+prefix.key()+prefix.prefixText()+prefix.requireScore());
            if(prefixes.containsKey(prefix.key())){
                AFKScoreboard.warn("There is already a prefix with the same key!");
                continue;
            }
            prefixes.put(prefix.key(),  prefix);
            if(!teamKeys.contains(prefix.teamKey())){
                teamKeys.add(prefix.teamKey());
            }else {
                prefixes.remove(prefix.key());
                AFKScoreboard.warn("There is already a team with the same key!");
                continue;
            }
            conditionMap.computeIfAbsent(prefix.requireScore(), k->new ArrayList<>()).add(prefix);
        }
    }

    public Prefix getPrefix(String key){
        return prefixes.get(key);
    }

    public Set<String> getAllKeys(){
        return Set.copyOf(prefixes.keySet());
    }
    public Set<String> getTeamKeys(){
        return Set.copyOf(teamKeys);
    }

    public List<Prefix> getPrefixesByCondition(int condition){
        return List.copyOf(conditionMap.getOrDefault(condition,List.of()));
    }

    public Collection<Prefix> getAllPrefixes(){
        return List.copyOf(prefixes.values());
    }

    public List<Integer> getAllConditions(){
        return conditionMap.keySet()
                .stream().sorted().toList();
    }
}
