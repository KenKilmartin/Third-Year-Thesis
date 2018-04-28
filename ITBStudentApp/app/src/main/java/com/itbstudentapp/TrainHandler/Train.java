package com.itbstudentapp.TrainHandler;


public class Train
{
    private String trainCode;
    private String destination;
    private String direction;
    private int duein;
    private int late;
    private String status;

    public Train(String trainCode, String destination, String direction, int duein, int late, String status)
    {
        this.trainCode = trainCode;
        this.destination = destination;
        this.direction = direction;
        this.duein = duein;
        this.late = late;
        this.status = status;
    }

    public String getTrainCode() {
        return trainCode;
    }

    public String getDestination() {
        return destination;
    }

    public String getDirection() {
        return direction;
    }

    public int getDuein() {
        return duein;
    }

    public int getLate() {
        return late;
    }

    public String getStatus() {
        return status;
    }
}
