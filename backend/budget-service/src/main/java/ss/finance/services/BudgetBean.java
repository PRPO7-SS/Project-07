package ss.finance.services;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.types.ObjectId;

import ss.finance.entities.Budget;
import ss.finance.repositories.BudgetRepository;

@ApplicationScoped
public class BudgetBean {

    @Inject
    private BudgetRepository budgetRepository;

    private static final Logger logger = Logger.getLogger(BudgetBean.class.getName());

    public void addBudget(Budget budget) {
        logger.info("Adding new budget: " + budget);
        budgetRepository.addBudget(budget);
    }

    public List<Budget> getBudgetsByUserId(ObjectId userId) {
        logger.info("Fetching budgets for userId: " + userId);
        return budgetRepository.getBudgetsByUserId(userId);
    }

    public void updateBudget(ObjectId userId, String categoryName, double newLimit) {
        logger.info("Updating budget for userId: " + userId + ", category: " + categoryName + ", newLimit: " + newLimit);
        Budget budget = budgetRepository.getBudgetByUserIdAndCategory(userId, categoryName);
        if (budget != null) {
            budgetRepository.updateMonthlyLimit(budget.getId(), newLimit);
        } else {
            logger.warning("No budget found for userId: " + userId + ", category: " + categoryName);
            throw new IllegalArgumentException("Budget not found for the given user and category");
        }
    }

    public boolean deleteBudget(ObjectId userId, String categoryName) {
        logger.info("Attempting to delete budget for userId: " + userId + ", category: " + categoryName);
        return budgetRepository.deleteBudget(userId, categoryName);
    }
}