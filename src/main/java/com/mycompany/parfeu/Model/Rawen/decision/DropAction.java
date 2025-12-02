/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.decision;
public final class DropAction implements Action {
    
    private static final DropAction INSTANCE = new DropAction();
    
    private DropAction() {}
    
    public static DropAction getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getDescription() {
        return "Bloqué";
    }
    
    // getSeverity() et getSymbol() utilisent les defaults
    
    @Override
    public String toString() {
        return "DROP";
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof DropAction;
    }
    
    @Override
    public int hashCode() {
        return DropAction.class.hashCode();
    }
}
