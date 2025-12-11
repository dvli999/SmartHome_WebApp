package soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "TimeData")
@XmlAccessorType(XmlAccessType.FIELD)
public class TimeData implements Serializable {

    @XmlElement(required = true)
    private int heure;

    @XmlElement(required = true)
    private int jour;

    @XmlElement(required = true)
    private int weekend;

    public TimeData() {
    }

    public int getHeure() {
        return heure;
    }

    public void setHeure(int heure) {
        this.heure = heure;
    }

    public int getJour() {
        return jour;
    }

    public void setJour(int jour) {
        this.jour = jour;
    }

    public int getWeekend() {
        return weekend;
    }

    public void setWeekend(int weekend) {
        this.weekend = weekend;
    }
}
