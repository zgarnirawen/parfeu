/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.decision;

import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Resultat d'une decision du pare-feu pour un paquet analyse.
 * Classe immuable (final) contenant toutes les informations sur la decision prise.
 * 
 * Concepts Java utilises :
 * - Final class (immutable)
 * - Collections immuables
 * - Objects.requireNonNull pour validation
 * 
 * @author ZGARNI
 */
public final class DecisionResult {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Packet packet;
    private final List<DetectionSignal> signals;
    private final int totalScore;
    private final Action action;
    private final String reason;
    private final LocalDateTime timestamp;

    public DecisionResult(Packet packet, 
                         List<DetectionSignal> signals, 
                         int totalScore, 
                         Action action, 
                         String reason) {
        this.packet = Objects.requireNonNull(packet, "packet ne peut pas etre null");
        this.signals = Collections.unmodifiableList(
            Objects.requireNonNull(signals, "signals ne peut pas etre null")
        );
        this.totalScore = totalScore;
        this.action = Objects.requireNonNull(action, "action ne peut pas etre null");
        this.reason = Objects.requireNonNull(reason, "reason ne peut pas etre null");
        this.timestamp = LocalDateTime.now();
    }

    public Packet getPacket() { 
        return packet; 
    }
    
    public List<DetectionSignal> getSignals() { 
        return signals;
    }
    
    public int getTotalScore() { 
        return totalScore; 
    }
    
    public Action getAction() { 
        return action; 
    }
    
    public String getReason() { 
        return reason; 
    }
    
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    public boolean isBlocked() {
        return action instanceof DropAction;
    }

    public boolean isAccepted() {
        return action instanceof AcceptAction || action instanceof LogAction;
    }

    public boolean needsAlert() {
        return action instanceof AlertAction || action instanceof DropAction;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s Action=%s, Score=%d, Signaux=%d, Raison=%s",
                timestamp.format(FORMATTER),
                action.getSymbol(),
                action,
                totalScore,
                signals.size(),
                reason);
    }

    public String getDetailedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("     DECISION DU PARE-FEU\n");
        sb.append("========================================\n\n");
        
        sb.append("Timestamp : ").append(timestamp.format(FORMATTER)).append("\n");
        sb.append("Action    : ").append(action.getSymbol()).append(" ")
          .append(action.getDescription()).append("\n");
        sb.append("Score     : ").append(totalScore).append("/10\n");
        sb.append("Raison    : ").append(reason).append("\n\n");
        
        sb.append("Paquet analyse :\n");
        sb.append("   ").append(packet.summary()).append("\n\n");
        
        if (!signals.isEmpty()) {
            sb.append("Signaux detectes (").append(signals.size()).append(") :\n");
            for (int i = 0; i < signals.size(); i++) {
                DetectionSignal signal = signals.get(i);
                sb.append(String.format("   %d. [Score: %d] %s\n", 
                    i + 1, 
                    signal.getScore(), 
                    signal.getDescription()));
            }
        } else {
            sb.append("Aucun signal de menace detecte\n");
        }
        
        sb.append("\n========================================\n");
        return sb.toString();
    }

    public String toCSV() {
        return String.format("%s,%s,%s,%s,%d,%d,%d,%s",
            timestamp.format(FORMATTER),
            packet.getSrcIP(),
            packet.getDestIP(),
            packet.getProtocol(),
            packet.getDestPort(),
            totalScore,
            signals.size(),
            action.toString()
        );
    }

    public static String getCSVHeader() {
        return "Timestamp,Source IP,Destination IP,Protocol,Port,Score,Signals Count,Action";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionResult that = (DecisionResult) o;
        return totalScore == that.totalScore &&
                Objects.equals(packet, that.packet) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packet, totalScore, timestamp);
    }
}