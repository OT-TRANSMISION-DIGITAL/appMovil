package com.example.transmisiondigital.models;

public class Calendar {
    public String date;
    public String status;
    public String folio;
    public String hour;
    public String id;

    public Calendar(String date, String status, String folio, String hour, String id) {
        this.date = date;
        this.status = status;
        this.folio = folio;
        this.hour = hour;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
