/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.generator;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Sélectionne aléatoirement des paquets depuis des fichiers CSV.
 * Permet à l'utilisateur de choisir entre paquets sains ou malicieux.
 * 
 * Format CSV attendu :
 * srcIP,destIP,srcPort,destPort,protocol,payload,attackType
 * 
 * @author ZGARNI
 */
public class PacketSelector {
    
    private static final String BENIGN_FILE = "paquets_sains.csv";
    private static final String MALICIOUS_FILE = "paquets_malicieux.csv";
    
    private final Random random;
    private List<String[]> benignPackets;
    private List<String[]> maliciousPackets;
    
    public PacketSelector() {
        this.random = new Random();
        this.benignPackets = new ArrayList<>();
        this.maliciousPackets = new ArrayList<>();
    }
    
    /**
     * Charge les fichiers CSV en mémoire au démarrage.
     * @throws Exception si les fichiers n'existent pas
     */
    public void loadPacketFiles() throws Exception {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📂 CHARGEMENT DES FICHIERS DE PAQUETS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Charger paquets sains
        benignPackets = loadCSVFile(BENIGN_FILE);
        System.out.println("✓ " + benignPackets.size() + " paquets sains chargés");
        
        // Charger paquets malicieux
        maliciousPackets = loadCSVFile(MALICIOUS_FILE);
        System.out.println("✓ " + maliciousPackets.size() + " paquets malicieux chargés");
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if (benignPackets.isEmpty()) {
            throw new Exception("❌ Aucun paquet sain trouvé dans " + BENIGN_FILE);
        }
        if (maliciousPackets.isEmpty()) {
            throw new Exception("❌ Aucun paquet malicieux trouvé dans " + MALICIOUS_FILE);
        }
    }
    
    /**
     * Charge un fichier CSV en mémoire.
     * @param filename nom du fichier CSV
     * @return liste de lignes parsées
     */
    private List<String[]> loadCSVFile(String filename) throws Exception {
        Path path = Paths.get(filename);
        
        if (!Files.exists(path)) {
            throw new Exception("Fichier introuvable : " + filename);
        }
        
        List<String[]> packets = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(path.toFile()),
                    StandardCharsets.UTF_8
                )
             )) {
            
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Ignorer l'en-tête
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                // Ignorer lignes vides
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Parser la ligne CSV
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) { // Au moins 6 colonnes requises
                    packets.add(parts);
                }
            }
        }
        
        return packets;
    }
    
    /**
     * Sélectionne un paquet aléatoire selon le choix de l'utilisateur.
     * 
     * @param isMalicious true pour paquet malicieux, false pour sain
     * @return paquet sélectionné aléatoirement
     */
    public Packet selectRandomPacket(boolean isMalicious) {
        if (isMalicious) {
            return selectRandomMaliciousPacket();
        } else {
            return selectRandomBenignPacket();
        }
    }
    
    /**
     * Sélectionne aléatoirement un paquet sain.
     */
    private PaquetSimple selectRandomBenignPacket() {
        if (benignPackets.isEmpty()) {
            throw new IllegalStateException("Aucun paquet sain disponible");
        }
        
        // Sélection aléatoire
        String[] data = benignPackets.get(random.nextInt(benignPackets.size()));
        
        return new PaquetSimple(
            data[0].trim(),                          // srcIP
            data[1].trim(),                          // destIP
            Integer.parseInt(data[2].trim()),        // srcPort
            Integer.parseInt(data[3].trim()),        // destPort
            data[4].trim(),                          // protocol
            data[5].trim()                           // payload
        );
    }
    
    /**
     * Sélectionne aléatoirement un paquet malicieux.
     */
    private PaquetMalicieux selectRandomMaliciousPacket() {
        if (maliciousPackets.isEmpty()) {
            throw new IllegalStateException("Aucun paquet malicieux disponible");
        }
        
        // Sélection aléatoire
        String[] data = maliciousPackets.get(random.nextInt(maliciousPackets.size()));
        
        String attackType = data.length > 6 ? data[6].trim() : "UNKNOWN";
        
        return new PaquetMalicieux(
            data[0].trim(),                          // srcIP
            data[1].trim(),                          // destIP
            Integer.parseInt(data[2].trim()),        // srcPort
            Integer.parseInt(data[3].trim()),        // destPort
            data[4].trim(),                          // protocol
            data[5].trim(),                          // payload
            attackType                               // typeAttaque
        );
    }
    
    /**
     * Sélectionne plusieurs paquets aléatoires.
     * 
     * @param count nombre de paquets à générer
     * @param maliciousPercentage pourcentage de paquets malicieux (0-100)
     * @return liste de paquets
     */
    public List<Packet> selectRandomPackets(int count, int maliciousPercentage) {
        List<Packet> packets = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // Décider si ce paquet sera malicieux
            boolean isMalicious = random.nextInt(100) < maliciousPercentage;
            packets.add(selectRandomPacket(isMalicious));
        }
        
        return packets;
    }
    
    /**
     * Permet à l'utilisateur de choisir interactivement.
     * @param scanner scanner pour lire l'entrée utilisateur
     * @return paquet sélectionné
     */
    public Packet userSelectPacket(java.util.Scanner scanner) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║     SÉLECTION D'UN PAQUET                            ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("\nQuel type de paquet voulez-vous tester ?");
        System.out.println("  1. Paquet SAIN (légitime)");
        System.out.println("  2. Paquet MALICIEUX (attaque)");
        System.out.print("\nVotre choix (1 ou 2) : ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consommer la ligne
        
        Packet packet;
        if (choice == 2) {
            packet = selectRandomMaliciousPacket();
            System.out.println("\n🔴 Paquet MALICIEUX sélectionné :");
        } else {
            packet = selectRandomBenignPacket();
            System.out.println("\n🟢 Paquet SAIN sélectionné :");
        }
        
        System.out.println("   " + packet.summary());
        
        return packet;
    }
    
    /**
     * Permet à l'utilisateur de sélectionner plusieurs paquets.
     */
    public List<Packet> userSelectMultiplePackets(java.util.Scanner scanner) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║     SÉLECTION DE PLUSIEURS PAQUETS                   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        
        System.out.print("\nCombien de paquets voulez-vous générer ? ");
        int count = scanner.nextInt();
        
        System.out.print("Pourcentage de paquets malicieux (0-100) : ");
        int percentage = scanner.nextInt();
        scanner.nextLine(); // Consommer la ligne
        
        List<Packet> packets = selectRandomPackets(count, percentage);
        
        long maliciousCount = packets.stream()
            .filter(p -> p instanceof PaquetMalicieux)
            .count();
        long benignCount = packets.size() - maliciousCount;
        
        System.out.println("\n✓ " + packets.size() + " paquets générés :");
        System.out.println("   🟢 Sains      : " + benignCount);
        System.out.println("   🔴 Malicieux  : " + maliciousCount);
        
        return packets;
    }
    
    /**
     * Affiche les statistiques des fichiers chargés.
     */
    public void printStatistics() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📊 STATISTIQUES DES FICHIERS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Paquets sains disponibles     : " + benignPackets.size());
        System.out.println("Paquets malicieux disponibles : " + maliciousPackets.size());
        System.out.println("Total paquets                  : " + (benignPackets.size() + maliciousPackets.size()));
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
    
    // Getters
    public int getBenignPacketsCount() {
        return benignPackets.size();
    }
    
    public int getMaliciousPacketsCount() {
        return maliciousPackets.size();
    }
}