package ss.finance.entities;

import java.util.Date;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Investment {
    private String _id;
    private ObjectId userId;
    private String type;
    private String name;
    private Double amount;
    private Double quantity;
    private Date purchaseDate;
    private Double currentPrice;
    private Double currentValue;
    private Double lastTransactionAmount;
    private String lastTransactonType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date timestamp; // Dodamo timestamp


    public Investment() {

    }

    public Investment(ObjectId userId, String type, String name, Double amount, Double quantity, Date purchaseDate) {
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
    }

    public Double getlastTransactionAmount() {
        return lastTransactionAmount;
    }

    public void setlastTransactionAmount(Double lastTransactionAmount) {
        this.lastTransactionAmount = lastTransactionAmount;
    }

    public String getlastTransactonType() {
        return lastTransactonType;
    }

    public void setlastTransactonType(String lastTransactonType) {
        this.lastTransactonType = lastTransactonType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }


    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double value) {
        this.currentValue = value;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double value) {
        this.currentPrice = value;
    }


}
