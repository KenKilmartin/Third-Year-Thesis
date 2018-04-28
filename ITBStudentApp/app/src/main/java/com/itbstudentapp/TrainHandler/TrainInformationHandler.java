package com.itbstudentapp.TrainHandler;

import android.os.AsyncTask;

import com.itbstudentapp.Interfaces.OnThreadComplete;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class TrainInformationHandler extends AsyncTask{

    private ArrayList<Train> trainInfo;
    private OnThreadComplete threadComplete;

    public TrainInformationHandler(OnThreadComplete threadComplete)
    {
        trainInfo = new ArrayList<>();
        this.threadComplete = threadComplete;
    }

    public String getXmlStringOfDetails() throws IOException
    {
        URL trainXML = new URL("http://api.irishrail.ie/realtime/realtime.asmx/getStationDataByCodeXML_WithNumMins?StationCode=CMINE&NumMins=60");

        // reads the xml that has been recieved and parses it down
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(trainXML.openStream()));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("objStationData");

            for(int i = 0; i < nodeList.getLength(); i++)
            {
                NodeList node =  nodeList.item(i).getChildNodes();

                String destination = null, direction = null, traincode = null, status = null;
                int duein = 0, late = 0;

                for(int z = 0; z < node.getLength(); z++)
                {
                    if(node.item(z).getNodeName().equalsIgnoreCase("destination"))
                    {
                        destination = node.item(z).getTextContent();
                    } else if(node.item(z).getNodeName().equalsIgnoreCase("duein"))
                    {
                        duein = Integer.parseInt(node.item(z).getTextContent());
                    } else if(node.item(z).getNodeName().equalsIgnoreCase("direction"))
                    {
                        direction = node.item(z).getTextContent();
                    } else if(node.item(z).getNodeName().equalsIgnoreCase("Status"))
                    {
                        status = node.item(z).getTextContent();
                    } else if(node.item(z).getNodeName().equalsIgnoreCase("Traincode"))
                    {
                        traincode = node.item(z).getTextContent();
                    }else if(node.item(z).getNodeName().equalsIgnoreCase("late"))
                    {
                        late = Math.abs(Integer.parseInt(node.item(z).getTextContent()));
                    }
                }

                trainInfo.add(new Train(traincode, destination, direction, duein, late, status));
            }

        } catch (SAXException | ParserConfigurationException e){

        }
        return null;
    }

    @Override
    protected Object doInBackground(Object[] objects)
    {
        try {
            getXmlStringOfDetails();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        threadComplete.onThreadCompleteCall();
    }

    public ArrayList<Train> getTrainInfo()
    {
        return this.trainInfo;
    }
}
