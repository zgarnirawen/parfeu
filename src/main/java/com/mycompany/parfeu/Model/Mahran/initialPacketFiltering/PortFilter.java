/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.initialPacketFiltering;

import com.mycompany.parfeu.Model.Mahran.generator.Packet;

/**
 * Filtrage basé sur le port source ou destination.
 */
public class PortFilter extends AbstractFilter {

    private final Integer srcPort;  // null = ignore
    private final Integer destPort; // null = ignore

    public PortFilter(Integer srcPort, Integer destPort) {
        this.srcPort = srcPort;
        this.destPort = destPort;
    }

    @Override
    public boolean accept(Packet packet) {
        if (srcPort != null && !srcPort.equals(packet.getSrcPort())) {
            return false;
        }
        if (destPort != null && !destPort.equals(packet.getDestPort())) {
            return false;
        }
        return true;
    }
}


