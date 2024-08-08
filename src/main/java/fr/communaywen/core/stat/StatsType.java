package fr.communaywen.core.stat;


import lombok.Getter;

@Getter
public enum StatsType {

    INT(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    FLOAT(Float.class);

    private final Class<?> type;

    StatsType(Class<?> type) {
        this.type = type;
    }

}
