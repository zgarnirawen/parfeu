/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.decision;

public final class Actions {
    
    // ========== INSTANCES SINGLETON ==========
    public static final Action ACCEPT = AcceptAction.getInstance();
    public static final Action DROP = DropAction.getInstance();
    public static final Action ALERT = AlertAction.getInstance();
    public static final Action LOG = LogAction.getInstance();
    
    // Constructeur privé (classe utilitaire non instanciable)
    private Actions() {
        throw new AssertionError("Classe utilitaire non instanciable");
    }
    
    /**
     * Retourne l'action correspondant à un nom.
     * Utile pour désérialisation ou parsing.
     * 
     * @param actionName nom de l'action ("ACCEPT", "DROP", etc.)
     * @return instance de l'action
     * @throws IllegalArgumentException si le nom est invalide
     */
    public static Action fromString(String actionName) {
        return switch (actionName.toUpperCase()) {
            case "ACCEPT" -> ACCEPT;
            case "DROP" -> DROP;
            case "ALERT" -> ALERT;
            case "LOG" -> LOG;
            default -> throw new IllegalArgumentException("Action inconnue : " + actionName);
        };
    }
    
    /**
     * Vérifie si une chaîne correspond à une action valide.
     * 
     * @param actionName nom à vérifier
     * @return true si valide
     */
    public static boolean isValidAction(String actionName) {
        if (actionName == null) return false;
        String upper = actionName.toUpperCase();
        return upper.equals("ACCEPT") || upper.equals("DROP") || 
               upper.equals("ALERT") || upper.equals("LOG");
    }
    
    /**
     * Retourne toutes les actions disponibles.
     * 
     * @return tableau de toutes les actions
     */
    public static Action[] values() {
        return new Action[] { ACCEPT, DROP, ALERT, LOG };
    }
}
