/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.decision;

public final class LogAction implements Action {
    
    private static final LogAction INSTANCE = new LogAction();
    
    private LogAction() {}
    
    public static LogAction getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getDescription() {
        return "Accepté avec log";
    }
    
    @Override
    public String toString() {
        return "LOG";
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LogAction;
    }
    
    @Override
    public int hashCode() {
        return LogAction.class.hashCode();
    }
}
