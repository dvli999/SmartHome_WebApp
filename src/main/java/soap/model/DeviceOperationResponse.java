package soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "DeviceOperationResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceOperationResponse implements Serializable {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement(required = true)
    private String message;

    @XmlElement
    private DeviceInfo device;

    public DeviceOperationResponse() {
    }

    public DeviceOperationResponse(boolean success, String message, DeviceInfo device) {
        this.success = success;
        this.message = message;
        this.device = device;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DeviceInfo getDevice() {
        return device;
    }

    public void setDevice(DeviceInfo device) {
        this.device = device;
    }
}
