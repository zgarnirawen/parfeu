/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.initialPacketFiltering;

import com.mycompany.parfeu.Model.Mahran.generator.Packet;


public abstract class AbstractFilter {

    /**
     * Vérifie si le paquet est accepté par ce filtre.
     *
     * @param packet paquet à vérifier
     * @return true si accepté, false si refusé
     */
    public abstract boolean accept(Packet packet);

}
