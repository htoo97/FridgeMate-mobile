package com.fridgemate.yangliu.fridgemate.current_contents;

public class RecipeItem{

    private String itemName;

    private String imageUri;

    private String itemLink;

    RecipeItem(String name, String uri, String link) {
        imageUri = uri;
        itemName = name;
        itemLink = link;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getItemLink() {
        return itemLink;
    }

    public void setItemLink(String itemLink) {
        this.itemLink = itemLink;
    }
}