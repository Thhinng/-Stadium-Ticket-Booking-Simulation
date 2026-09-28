/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author MY PC
 */
public class TransactionLog {
    private String logId;
    private String userId;
    private String action;
    private String timestamp;
    private String description;

    public TransactionLog(String logId, String userId, String action, String timestamp, String description) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
    public void saveLog(){
        System.out.println("Log saved: " + logId + "| User: " + userId + "| Action: " + action);
    }
            
            
}
