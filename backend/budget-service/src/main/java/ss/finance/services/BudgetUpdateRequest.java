package ss.finance.services;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.types.ObjectId;

import ss.finance.entities.Budget;
import ss.finance.repositories.BudgetRepository;

public class BudgetUpdateRequest {
    private double newLimit;

    public double getNewLimit() {
        return newLimit;
    }

    public void setNewLimit(double newLimit) {
        this.newLimit = newLimit;
    }
}