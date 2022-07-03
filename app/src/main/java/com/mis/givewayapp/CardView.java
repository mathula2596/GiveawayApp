package com.mis.givewayapp;

public class CardView {
    //model class

    private String title;
    private String description;
    private String price;
    private String user;
    private String date;
    private String image;
    private String documentId;
    private String location;

    public CardView(String title, String description, String price, String user, String date,
                    String image,String documentId,String location) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.user = user;
        this.date = date;
        this.image = image;
        this.documentId = documentId;
        this.location = location;

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    public String getImage() {
        return image;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getLocation() {
        return location;
    }
}
