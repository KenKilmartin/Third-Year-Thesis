package com.itbstudentapp.DublinBus;

import android.os.AsyncTask;
import android.util.Log;

import com.itbstudentapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DublinBusRouteFinder extends AsyncTask<Void, Void, Void>{

    private RouteChoice routeChoice;

    public DublinBusRouteFinder(RouteChoice routeChoice)
    {
        this.routeChoice = routeChoice;
        stopIdsInArea = routeChoice.getResources().getStringArray(R.array.dublin_bus_stops);
    }

    private String[] stopIdsInArea;// // stops in area

    private String routes[]; // array of routes

    @Override
    protected Void doInBackground(Void... voids) {
        routes =  getAvailibleRouteNumbers(); // gets routes
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void voids) {
        super.onPostExecute(voids);
        routeChoice.display(routes);
    }

    public String[] getRoutes()
    {
        return  this.routes;
    } // get routes

    private String[] getAvailibleRouteNumbers()
    {
        ArrayList<String> availableRoutes = new ArrayList<String>(); // we are never sure of what the size is so an arraylist suits
        String[] routes = null;

        try
        {
            for(int i = 0; i < stopIdsInArea.length;i++)
            {
                String json = getJsonStringOfDetails(stopIdsInArea[i]); // gets json string
                String[] routesArray = DecodeJson(json); // needs to be decoded
                checkForConflicts(availableRoutes, routesArray); // loop to make sure we arent listing routes twice
            }


            Object[] objects = availableRoutes.toArray(); // convert to array
            routes = new String[objects.length];

            for(int x = 0; x < objects.length; x++)
            {
                routes[x] = objects[x].toString(); // casting
            }

        } catch (IOException exception)
        {
            Log.e("Error getting routes", "getAvailibleRouteNumbers: Error finding results");
        }

        return  routes;
    }

    private ArrayList<String> checkForConflicts(ArrayList<String> list, String[] currentRoutes)
    {
        if(list.size() == 0)
        {
            for(int i = 0; i < currentRoutes.length; i++)
            {
                list.add(currentRoutes[i]); // first time case
            }
        }

        for(int i = 0; i < currentRoutes.length; i++)
        {
            boolean isFound = false;
            for(int x = 0; x < list.size(); x++)
            {
                if(currentRoutes[i].equals(list.get(x)))
                {
                    isFound = true; // if we found, leave to the next go
                    break;
                }
            }

            if(!isFound)
            {
                list.add(currentRoutes[i]); // else add
            }
        }
        return list;
    }

    private String getJsonStringOfDetails(String stopID) throws IOException
    {
        HttpURLConnection connection;
        URL bus = new URL("https://data.dublinked.ie/cgi-bin/rtpi/busstopinformation?stopid=" + stopID);
        connection = (HttpURLConnection) bus.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine = in.readLine();

        if(in.readLine() != null)
        {
            Log.e("Reading error", "getJsonStringOfDetails: To much information, has the server changed the output?");
            return null;
        }

        in.close();
        return inputLine;
    }

    private String RemoveChars(String routes)
    {
        String s = "";
        for(int i = 0; i < routes.length(); i++)
        {
            if(routes.charAt(i) == '[' || routes.charAt(i) == ']' || routes.charAt(i) == '{' || routes.charAt(i) == '}' || routes.charAt(i) == '"')
            {
                continue;
            } // this removes padding around
            s+= routes.charAt(i);
        }

        return s;
    }

    private String[] DecodeJson(String routeInformation)
    {
        String[] split = routeInformation.split(":");
        String routes = RemoveChars(split[21]);

        return routes.split(",");

    }

}
