package com.example.android.shopping;

/**
 * Created by Jonathan on 6/13/2017.
 */
// TODO: 7/9/2017 implement 
class InventoryItem {
    String name;
    String url;

    InventoryItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }
}
