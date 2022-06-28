package org.rsminion.core.multipliers;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MultiplierData {

    //Multiplier, Count
    private final Map<Number, Integer> multipliers = new HashMap<>();

    private @Getter Number multiplier = null;

    public void setMode() {
        Map.Entry<Number, Integer> curr = null;
        for(Map.Entry<Number, Integer> m : multipliers.entrySet()) {
            if(curr == null || (m.getValue() > curr.getValue()))
                curr = m;
        }
        this.multiplier = curr != null ? curr.getKey() : null;
    }

    public MultiplierData insert(Number multiplier) {
        if(!multipliers.containsKey(multiplier))
            multipliers.put(multiplier, 1);
        else multipliers.compute(multiplier, (m,c) -> c += 1);
        return this;
    }

    @Override
    public String toString() {
        if(multiplier != null) {
            if(multiplier instanceof Long)
                return multiplier.longValue() + "L";
            else if(multiplier instanceof Double)
                return multiplier.doubleValue() + "D";
            else if(multiplier instanceof Float)
                return multiplier.floatValue() + "F";
            else
                return String.valueOf(multiplier.intValue());
        }
        return "0";
    }

}
