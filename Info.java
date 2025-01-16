package com.example.cet343assignment;

public class Info {

    private String name;
    private String desc;
    private String date;
    private String price;
    private String imageURL;
    private String key;
    private String status;
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getDate() {
        return date;
    }

    public String getPrice() {
        return price;
    }

    public String getImageURL() {
        return imageURL;
    }
    public Info(String name, String desc, String date, String price, String imageURL) {

        this.name = name;
        this.desc = desc;
        this.date = date;
        this.price = price;
        this.imageURL = imageURL;

    }

    public Info(String Name, double latitude, double longitude){
        this.name = Name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public Info() {
        // Default constructor required for Firebase deserialization
    }


}
