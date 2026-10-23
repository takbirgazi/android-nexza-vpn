package com.nexzatech.nexzavpn.data.model;

public class Server {

    private final String id;
    private final String name;
    private final String country;

    public Server(String id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }
}