package ss.finance.entities;

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

public class SavingsGoal {
    private String _id;
    private String userId;
    private String goalName;
    private Integer targetAmount;
    private Integer currentAmount;
    private Date startDate;
    private Date deadline;

    public SavingsGoal() {

    }

    public String getId(){ return _id; }

    public void setId(String id){ this._id = id; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId.toHexString();
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public Integer getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Integer targetAmount) {
        this.targetAmount = targetAmount;
    }

    public Integer getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Integer currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
}
