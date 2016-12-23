package de.lindenvalley.paymillexample.model;

import java.io.Serializable;

public class Purchase implements Serializable {
    private float price;

    public Purchase(float price) {
        this.price = price;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
