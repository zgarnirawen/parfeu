/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.initialPacketFiltering;

import com.mycompany.parfeu.Model.Mahran.generator.Packet;

/**
 * Filtrage basé sur l'adresse IP source ou destination.
 */
public class IPFilter extends AbstractFilter {

    private final String srcIP;   // null = ignore
    private final String destIP;  // null = ignore

    public IPFilter(String srcIP, String destIP) {
        this.srcIP = srcIP;
        this.destIP = destIP;
    }

    @Override
    public boolean accept(Packet packet) {
        if (srcIP != null && !srcIP.equals(packet.getSrcIP())) {
            return false;
        }
        if (destIP != null && !destIP.equals(packet.getDestIP())) {
            return false;
        }
        return true;
    }
}
