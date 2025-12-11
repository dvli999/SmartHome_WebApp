package soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "EnergyHistory")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnergyHistory implements Serializable {

    @XmlElement(name = "record")
    private List<EnergyData> records;

    @XmlElement(required = true)
    private double threshold;

    public EnergyHistory() {
        records = new ArrayList<EnergyData>();
    }

    public List<EnergyData> getRecords() { return records; }
    public void setRecords(List<EnergyData> records) { this.records = records; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
}
