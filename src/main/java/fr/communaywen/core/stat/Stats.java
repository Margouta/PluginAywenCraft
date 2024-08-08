package fr.communaywen.core.stat;

import fr.communaywen.core.credit.Credit;
import fr.communaywen.core.credit.Feature;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static fr.communaywen.core.stat.StatsType.INT;

@Setter
@Getter
@Feature("Stats")
@Credit("aurel31")
public class Stats {

    @Getter
    public enum StatList {
        PROUT("prout",INT,0);

        private final StatsType type;
        private final String name;
        private final Number defaultValue;

        StatList(String name, StatsType type, Number defaultValue) {

            if (defaultValue.getClass() != type.getType()){
                throw new IllegalArgumentException("The default value is not the same type as the type");
            }

            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

    }

    private final Map<StatList,Number> values;

    public Stats() {
        values = new HashMap<>();
        for (StatList value : StatList.values()) {
            values.put(value,value.defaultValue);
        }
    }

    public Number getValue(StatList stats) {
        return values.get(stats);
    }

    public Stats(Map<StatList,Number> map) {
        values = map;
    }

    public Stats add(StatList stat,Number value) {
        if (stat.getType().getType() != value.getClass()){
            throw new IllegalArgumentException("The value is not the same type as the type of the Stats");
        }
        Number currentValue = values.get(stat);
        switch (value) {
            case Integer i -> values.put(stat, value.intValue() + currentValue.intValue());
            case Long l -> values.put(stat, value.longValue() + currentValue.longValue());
            case Float v -> values.put(stat, value.floatValue() + currentValue.floatValue());
            case Double v -> values.put(stat, value.doubleValue() + currentValue.doubleValue());
            default -> throw new IllegalArgumentException("Unsupported number type");
        }
        return this;
    }

}
