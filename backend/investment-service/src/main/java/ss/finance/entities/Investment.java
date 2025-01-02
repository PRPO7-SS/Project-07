package ss.finance.entities;

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

public class Investment {
    private String _id;
    private ObjectId userId;
    private String type;
    private String name;
    private Integer amount;
    private Integer quantity;
    private Date purchaseDate;
    private Integer currentValue;

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Integer getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }
}
