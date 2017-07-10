package com.example.android.shopping;

/**
 * Created by Jonathan on 6/13/2017.
 */
// TODO: 7/9/2017 implement 
public class InventoryItem {
    String name;
    String url;

    public InventoryItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
