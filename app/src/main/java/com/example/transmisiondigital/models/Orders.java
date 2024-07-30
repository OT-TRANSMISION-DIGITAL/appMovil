package com.example.transmisiondigital.models;

import java.util.Date;

public class Orders {
    public String date;
    public String status;
    public String folio;
    public String hour;
    public String idOrder;

    public Orders(String date, String status, String folio, String hour, String idOrder) {
        this.date = date;
        this.status = status;
        this.folio = folio;
        this.hour = hour;
        this.idOrder = idOrder;
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

    public String getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
    }
}
