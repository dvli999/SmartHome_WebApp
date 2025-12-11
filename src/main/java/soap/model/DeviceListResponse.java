package soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "DeviceListResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceListResponse implements Serializable {

    @XmlElement(name = "device")
    private List<DeviceInfo> devices;

    @XmlElement(required = true)
    private int totalCount;

    public DeviceListResponse() {
        devices = new ArrayList<DeviceInfo>();
    }

    public List<DeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
        this.devices = devices;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
