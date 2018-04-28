package com.itbstudentapp.DublinBus;


public class BusTimeInfo {

    private String bus_dest;
    private String bus_time;

    public BusTimeInfo(String bus_dest, String bus_time)
    {
        this.bus_dest = bus_dest;
        this.bus_time = bus_time;
    }


    public String getBus_dest() {
        return bus_dest;
    }

    public String getBus_time() {
        return bus_time;
    }
}


