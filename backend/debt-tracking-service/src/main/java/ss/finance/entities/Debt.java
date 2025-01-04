package ss.finance.entities;

import java.util.Date;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ss.finance.serializer.ObjectIdSerializer;

public class Debt {

    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId userId;
    private String creditor; // Ime upnika
    private String description; // Opis dolga
    private double amount; // Znesek dolga
    @JsonProperty("isPaid") // Serializacija/deserializacija za JSON
    private boolean isPaid; // Ali je dolg plačan
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date deadline; // Datum zapadlosti dolga

    // Konstruktorji
    public Debt() {
    }

    public Debt(ObjectId userId, String creditor, String description, double amount, boolean isPaid, Date deadline) {
        this.userId = userId;
        this.creditor = creditor;
        this.description = description;
        this.amount = amount;
        this.isPaid = isPaid;
        this.deadline = deadline;
    }

    // Getterji in setterji
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getCreditor() {
        return creditor;
    }

    public void setCreditor(String creditor) {
        this.creditor = creditor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @JsonProperty("isPaid")
    public boolean getIsPaid() {
        return isPaid;
    }

    @JsonProperty("isPaid")
    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "Debt{" +
                "id=" + id +
                ", userId=" + userId +
                ", creditor='" + creditor + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", isPaid=" + isPaid +
                ", deadline=" + deadline +
                '}';
    }
}