package org.rsminion.core.multipliers;

import org.rsminion.core.matching.data.RSHook;

import java.util.HashMap;
import java.util.Map;

public class MultiplierCache {

    //Field Key(Hash), MultiplierData
    private static Map<Integer, MultiplierData> multipliers = new HashMap<>();

    public static void insert(int key, Number multiplier) {
        if(multipliers.computeIfPresent(key, (k,md) -> md.insert(multiplier)) == null)
            multipliers.put(key, new MultiplierData().insert(multiplier));
    }

    public static void filterMultipliers() {
        for(MultiplierData md : multipliers.values())
            md.setMode();
    }

    public static String get(String owner, String name) {
        MultiplierData data;
        return (data = multipliers.get(toKey(owner, name))) != null ? data.toString() : "0";
    }

    public static String get(RSHook hook) {
        return get(hook.getObfOwner(), hook.getObfName());
    }

    public static int toKey(String owner, String name) {
        return (owner+"/"+name).hashCode();
    }

}
