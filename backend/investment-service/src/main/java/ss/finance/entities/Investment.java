package ss.finance.entities;

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

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

    public Investment() {

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
