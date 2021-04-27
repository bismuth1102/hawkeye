package com.skillion.hawkeye.location;

public class Location {
    private final Location local;

    public Location(Location local){
        this.local = local;
    }

    public Location getLocation(){
        return local;
    }
    public double getAccuracy(){return local.getAccuracy();}
}
