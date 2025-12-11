package soap.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


@XmlRootElement(name = "Response")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoapResponse implements Serializable {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement
    private String message;

    @XmlElement
    private String errorDetails;

    public SoapResponse() {}

    public SoapResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
}


