package ss.finance.services;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.types.ObjectId;

import ss.finance.entities.Debt;
import ss.finance.repositories.DebtRepository;

@ApplicationScoped
public class DebtBean {

    @Inject
    private DebtRepository debtRepository;

    private static final Logger logger = Logger.getLogger(DebtBean.class.getName());

    public void addDebt(Debt debt) {
        logger.info("Adding new debt: " + debt);
        debtRepository.addDebt(debt);
    }

    public List<Debt> getDebtsByUserId(ObjectId userId) {
        logger.info("Fetching debts for userId: " + userId);
        return debtRepository.getDebtsByUserId(userId);
    }

    public boolean updateDebt(ObjectId debtId, Debt updatedDebt) {
        if (updatedDebt == null) {
            logger.warning("Attempted to update debt with null object.");
            return false;
        }
        logger.info("Updating debt with ID: " + debtId);
        return debtRepository.updateDebt(debtId, updatedDebt);
    }

    public boolean deleteDebt(ObjectId debtId) {
        logger.info("Deleting debt with ID: " + debtId);
        return debtRepository.deleteDebt(debtId);
    }

    public boolean markAsPaid(ObjectId debtId) {
        logger.info("Marking debt with ID " + debtId + " as paid.");
        List<Debt> debts = debtRepository.getDebtsByUserId(debtId);
        if (debts.isEmpty()) {
            logger.warning("Debt not found with ID: " + debtId);
            return false;
        }
        debtRepository.markAsPaid(debtId);
        return true;
    }

    public Debt getDebtById(ObjectId debtObjectId) {
        if (debtObjectId == null) {
            logger.warning("Attempted to fetch debt with null ID.");
            return null;
        }
        logger.info("Fetching debt with ID: " + debtObjectId);
        return debtRepository.getDebtById(debtObjectId);
    }
}