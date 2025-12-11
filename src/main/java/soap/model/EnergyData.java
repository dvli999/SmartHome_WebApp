package soap.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Energy Data Record - JAXB annotated model (Java 8 compatible)
 */
@XmlRootElement(name = "EnergyData")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnergyData implements Serializable {

    @XmlElement(required = true)
    private long timestamp;

    @XmlElement(required = true)
    private int heure;

    @XmlElement(required = true)
    private int jour;

    @XmlElement(required = true)
    private int weekend;

    @XmlElement(required = true)
    private double actual;

    @XmlElement(required = true)
    private double predicted;

    @XmlElement(required = true)
    private String status;

    public EnergyData() {}

    public EnergyData(long timestamp, int heure, int jour, int weekend,
                      double actual, double predicted, String status) {
        this.timestamp = timestamp;
        this.heure = heure;
        this.jour = jour;
        this.weekend = weekend;
        this.actual = actual;
        this.predicted = predicted;
        this.status = status;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getHeure() { return heure; }
    public void setHeure(int heure) { this.heure = heure; }

    public int getJour() { return jour; }
    public void setJour(int jour) { this.jour = jour; }

    public int getWeekend() { return weekend; }
    public void setWeekend(int weekend) { this.weekend = weekend; }

    public double getActual() { return actual; }
    public void setActual(double actual) { this.actual = actual; }

    public double getPredicted() { return predicted; }
    public void setPredicted(double predicted) { this.predicted = predicted; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}