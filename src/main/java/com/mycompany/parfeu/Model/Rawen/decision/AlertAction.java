/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.decision;
public final class AlertAction implements Action {
    
    private static final AlertAction INSTANCE = new AlertAction();
    
    private AlertAction() {}
    
    public static AlertAction getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getDescription() {
        return "Alerte générée";
    }
    
    @Override
    public String toString() {
        return "ALERT";
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AlertAction;
    }
    
    @Override
    public int hashCode() {
        return AlertAction.class.hashCode();
    }}