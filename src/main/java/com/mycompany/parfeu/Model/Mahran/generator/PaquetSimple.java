/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.generator;

import java.time.LocalDateTime;


public final class PaquetSimple extends Packet {

    public PaquetSimple(String srcIP,
                        String destIP,
                        int srcPort,
                        int destPort,
                        String protocol,
                        String payload,
                        LocalDateTime timestamp) {
        super(srcIP, destIP, srcPort, destPort, protocol, payload, timestamp);
    }

    public PaquetSimple(String srcIP, String destIP, int srcPort, int destPort, String protocol, String payload) {
        this(srcIP, destIP, srcPort, destPort, protocol, payload, null);
    }

    @Override
    public byte[] serialize() {
        return super.serialize();
    }

    @Override
    public String toString() {
        return "PaquetSimple" + super.toString();
    }
}
