package ss.finance.entities;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ss.finance.serializer.ObjectIdSerializer;

public class Budget {
    @JsonSerialize(using=ObjectIdSerializer.class)
    private ObjectId id;
    @JsonSerialize(using=ObjectIdSerializer.class)
    private ObjectId userId;
    @JsonProperty("category") // Povezava s ključem "category" v JSON
    private String categoryName;
    private double monthlyLimit;

    // Constructors
    public Budget() {}

    public Budget(ObjectId userId, String categoryName, double monthlyLimit) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.monthlyLimit = monthlyLimit;
    }

    // Getters and Setters
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

    @JsonProperty("category") // Serializacija in deserializacija za "category"
    public String getCategory() {
        return categoryName;
    }

    @JsonProperty("category") // Povezava za deserializacijo
    public void setCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", userId=" + userId +
                ", categoryName='" + categoryName + '\'' +
                ", monthlyLimit=" + monthlyLimit +
                '}';
    }
}