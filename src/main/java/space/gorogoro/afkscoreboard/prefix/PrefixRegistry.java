package space.gorogoro.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.AFKScoreboard;

import java.util.*;

public final class PrefixRegistry {
    private final Map<String, Prefix> prefixes = new HashMap<>();
    private final Set<String> teamKeys = new HashSet<>();
    private final Map<String,Map<Integer,List<Prefix>>> requireScoreMap = new HashMap<>();
    private final PrefixConfigManager prefixConfigManager;

    public PrefixRegistry(PrefixConfigManager prefixConfigManager) {
        this.prefixConfigManager = prefixConfigManager;
        loadPrefixes();
    }

    public void loadPrefixes(){
        Set<Prefix> prefixSet = prefixConfigManager.loadPrefixes();
        if(prefixSet == null){
            AFKScoreboard.warn("An error occurred while loading prefix.yml!");
            return;
        }
        prefixes.clear();
        teamKeys.clear();
        requireScoreMap.clear();
        for(Prefix prefix : prefixSet){
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
            for(String zoneName:prefix.requireZones()){
                requireScoreMap.computeIfAbsent(zoneName, k -> new HashMap<>())
                        .computeIfAbsent(prefix.requireScore(), k -> new ArrayList<>())
                        .add(prefix);
            }
        }
    }

    public Prefix getPrefix(String key){
        return prefixes.get(key);
    }

    public Set<String> getTeamKeys(){
        return Set.copyOf(teamKeys);
    }

    public List<Prefix> getPrefixesByRequirement(String zoneName, int condition){
        Map<Integer,List<Prefix>> map = requireScoreMap.get(zoneName);
        if(map == null){return List.of();}
        return map.getOrDefault(condition,List.of());
    }

    public Collection<Prefix> getAllPrefixes(){
        return List.copyOf(prefixes.values());
    }

    public Set<Integer> getAllConditions(String zoneName){
        return requireScoreMap.getOrDefault(zoneName,Map.of()).keySet();
    }

    public Set<String> getAllRequireZones(){
        return requireScoreMap.keySet();
    }
}
