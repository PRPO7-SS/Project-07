package ss.finance.services;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ss.finance.serializer.ObjectIdSerializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentMessage {
    @JsonSerialize(using=ObjectIdSerializer.class)
    private String userId;
    private String type; // "income" ali "expense"
    private Double amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE MMM dd HH:mm:ss zzz yyyy", locale = "en")
    private Date timestamp; // Dodamo timestamp


    // Getters in setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}