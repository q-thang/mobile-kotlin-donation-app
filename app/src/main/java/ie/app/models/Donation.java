package ie.app.models;

import java.util.Date;

public class Donation {
    public String id;
    public int amount;
    public String paymenttype;
    public int upvotes;
    public String date;
    public Donation (String id, int amount, String method, int upvotes, String date)
    {
        this.id = id;
        this.amount = amount;
        this.paymenttype = method;
        this.upvotes = upvotes;
        this.date = date;
    }
    public Donation ()
    {
        this.amount = 0;
        this.paymenttype = "";
        this.upvotes = 0;
    }
    public String toString()
    {
        return id + ", " + amount + ", " + paymenttype + ", " + upvotes;
    }
}

