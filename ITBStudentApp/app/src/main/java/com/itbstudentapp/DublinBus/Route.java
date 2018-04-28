package com.itbstudentapp.DublinBus;

public class Route {

    private String routeId;
    private String route_dest;
    private String route_start;

    public Route(String routeId, String route_dest, String route_start)
    {
        this.routeId = routeId;
        this.route_dest = route_dest;
        this.route_start = route_start;
    }

    public String GetRouteId()
    {
        return routeId;
    }

    public String GetRouteDest()
    {
        return  route_dest;
    }

    public String GetRouteStart()
    {
        return route_start;
    }
}
