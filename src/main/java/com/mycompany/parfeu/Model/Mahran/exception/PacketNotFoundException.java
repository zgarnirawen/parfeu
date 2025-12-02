/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.exception;

/**
 * Exception levée lorsqu’un paquet réseau recherché n’existe pas.
 */
public final class PacketNotFoundException extends Exception {

    public PacketNotFoundException() {
        super("Paquet non trouvé.");
    }

    public PacketNotFoundException(String message) {
        super(message);
    }

    public PacketNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketNotFoundException(Throwable cause) {
        super(cause);
    }
}
