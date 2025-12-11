package soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "PredictionResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class PredictionResponse implements Serializable {

    @XmlElement(required = true)
    private double prediction;

    @XmlElement(required = true)
    private String status;

    @XmlElement(required = true)
    private double threshold;

    public PredictionResponse() {
    }

    public PredictionResponse(double prediction, String status, double threshold) {
        this.prediction = prediction;
        this.status = status;
        this.threshold = threshold;
    }

    public double getPrediction() {
        return prediction;
    }

    public void setPrediction(double prediction) {
        this.prediction = prediction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
