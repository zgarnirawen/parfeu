/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.initialPacketFiltering;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;

/**
 * Filtrage basé sur le protocole (TCP, UDP, ICMP…).
 */
public class ProtocolFilter extends AbstractFilter {

    private final String protocol; // protocole autorisé

    public ProtocolFilter(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public boolean accept(Packet packet) {
        return protocol.equalsIgnoreCase(packet.getProtocol());
    }
}
