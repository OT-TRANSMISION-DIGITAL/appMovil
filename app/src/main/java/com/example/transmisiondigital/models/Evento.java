package com.example.transmisiondigital.models;

public class Evento {
    private String message;
    private int tecnico_id;

    public Evento(String message, int tecnico_id) {
        this.message = message;
        this.tecnico_id = tecnico_id;
    }

    public String getMessage() {
        return message;
    }

    public int getTecnicoId() {
        return tecnico_id;
    }
}
