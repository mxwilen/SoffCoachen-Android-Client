package com.example.soffcoachen_android.models;

public class Team {
    private final String name;
    private final String abr;

    public Team(String name, String abr) {
        this.name = name;
        this.abr = abr;
    }
    public String getName() {
        return name;
    }

    public String getAbr() {
        return abr;
    }
}
