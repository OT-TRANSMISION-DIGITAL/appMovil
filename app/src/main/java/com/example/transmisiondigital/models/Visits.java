package com.example.transmisiondigital.models;

import java.util.Date;

public class Visits {
    public String date;
    public String status;
    public String folio;
    public String hour;
    public String idVisit;

    public Visits(String date, String status, String folio, String hour, String idVisit) {
        this.date = date;
        this.status = status;
        this.folio = folio;
        this.hour = hour;
        this.idVisit = idVisit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getIdVisit() {
        return idVisit;
    }

    public void setIdVisit(String idVisit) {
        this.idVisit = idVisit;
    }
}
