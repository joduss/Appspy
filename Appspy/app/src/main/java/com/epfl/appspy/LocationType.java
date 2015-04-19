package com.epfl.appspy;

/**
 * Created by Jonathan Duss on 19.04.15.
 */
public enum LocationType {
    GPS(2), NETWORK(1), NONE(0);
    private final int value;
    public static final double NO_VALUE = 0;

    private LocationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    static LocationType fromInt(int val){
        switch (val){
            case(0): return NONE;
            case(1): return NETWORK;
            case(2): return GPS;
        }
        return null;
    }
};
