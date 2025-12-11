package soap.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Device Model - JAXB annotated
 */
@XmlRootElement(name = "Device")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceInfo implements Serializable {

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private double baseConsumption;

    @XmlElement(required = true)
    private boolean isOn;

    public DeviceInfo() {}

    public DeviceInfo(String name, double baseConsumption, boolean isOn) {
        this.name = name;
        this.baseConsumption = baseConsumption;
        this.isOn = isOn;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBaseConsumption() { return baseConsumption; }
    public void setBaseConsumption(double baseConsumption) {
        this.baseConsumption = baseConsumption;
    }

    public boolean isOn() { return isOn; }
    public void setOn(boolean on) { isOn = on; }

    @XmlRootElement(name = "PredictionRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    static
    class PredictionRequest implements Serializable {

        @XmlElement(required = true)
        private int heure;

        @XmlElement(required = true)
        private int jour;

        @XmlElement(required = true)
        private int weekend;

        public PredictionRequest() {}

        public PredictionRequest(int heure, int jour, int weekend) {
            this.heure = heure;
            this.jour = jour;
            this.weekend = weekend;
        }

        public int getHeure() { return heure; }
        public void setHeure(int heure) { this.heure = heure; }

        public int getJour() { return jour; }
        public void setJour(int jour) { this.jour = jour; }

        public int getWeekend() { return weekend; }
        public void setWeekend(int weekend) { this.weekend = weekend; }
    }
}