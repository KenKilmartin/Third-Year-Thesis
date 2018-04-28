package com.itbstudentapp.DublinBus;

public class Stop {

    private String stop_name;
    private String stop_number;
    private String longatude;
    private String latitude;

    public  Stop(String stop_name, String stop_number, String longatude, String latitude)
    {
        this.stop_name = stop_name;
        this.stop_number = stop_number;
        this.longatude = longatude;
        this.latitude = latitude;
    }

    public String getStop_name() {
        return stop_name;
    }

    public String getStop_number() {
        return stop_number;
    }

    public String getLongatude() { return longatude; }

    public String getLatitude() {return latitude;}
}
