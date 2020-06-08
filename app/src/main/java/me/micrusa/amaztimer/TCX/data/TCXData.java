package me.micrusa.amaztimer.TCX.data;

import java.util.ArrayList;

import me.micrusa.amaztimer.TCX.data.Lap;

public class TCXData {

    private ArrayList<Lap> laps;

    public void addLap(Lap lap){
        laps.add(lap);
    }

    public ArrayList<Lap> getLaps(){
        return this.laps;
    }

    public String getTime(){
        return this.laps.get(0).getStartTime();
    }

    public String getSportName(){
        return "Other";
    }

}
