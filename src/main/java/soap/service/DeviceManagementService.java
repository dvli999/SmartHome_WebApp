package soap.service;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import soap.model.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.rmi.Naming;

/**
 * Device Management SOAP Web Service (Java 8 compatible)
 */
@WebService(
        name = "DeviceManagementService",
        serviceName = "DeviceManagementService",
        portName = "DeviceManagementPort",
        targetNamespace = "http://soap.smarthome.com/"
)
@SOAPBinding(
        style = SOAPBinding.Style.DOCUMENT,
        use = SOAPBinding.Use.LITERAL,
        parameterStyle = SOAPBinding.ParameterStyle.WRAPPED
)
public class DeviceManagementService {

    private static final List<DeviceInfo> devices = new CopyOnWriteArrayList<DeviceInfo>();
    private static final int RMI_PORT = 1100;

    static {
        devices.add(new DeviceInfo("Heating System", 25.0, true));
        devices.add(new DeviceInfo("Air Conditioner", 30.0, false));
        devices.add(new DeviceInfo("Water Heater", 15.0, true));
        devices.add(new DeviceInfo("Lighting Grid", 10.0, true));
        devices.add(new DeviceInfo("Entertainment System", 5.0, false));
    }

    @WebMethod(operationName = "getAllDevices")
    public DeviceListResponse getAllDevices() {
        DeviceListResponse response = new DeviceListResponse();
        response.setDevices(new ArrayList<DeviceInfo>(devices));
        response.setTotalCount(devices.size());
        return response;
    }

    @WebMethod(operationName = "getDeviceByName")
    public DeviceInfo getDeviceByName(@WebParam(name = "deviceName") String deviceName) {
        for (DeviceInfo d : devices) {
            if (d.getName().equalsIgnoreCase(deviceName)) {
                return d;
            }
        }
        return null;
    }

    @WebMethod(operationName = "toggleDevice")
    public DeviceOperationResponse toggleDevice(@WebParam(name = "deviceName") String deviceName) {
        DeviceInfo device = null;
        for (DeviceInfo d : devices) {
            if (d.getName().equalsIgnoreCase(deviceName)) {
                device = d;
                break;
            }
        }

        if (device != null) {
            device.setOn(!device.isOn());
            return new DeviceOperationResponse(true,
                    device.getName() + " is now " + (device.isOn() ? "ON" : "OFF"), device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "turnOnDevice")
    public DeviceOperationResponse turnOnDevice(@WebParam(name = "deviceName") String deviceName) {
        DeviceInfo device = null;
        for (DeviceInfo d : devices) {
            if (d.getName().equalsIgnoreCase(deviceName)) {
                device = d;
                break;
            }
        }

        if (device != null) {
            device.setOn(true);
            return new DeviceOperationResponse(true, device.getName() + " turned ON", device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "turnOffDevice")
    public DeviceOperationResponse turnOffDevice(@WebParam(name = "deviceName") String deviceName) {
        DeviceInfo device = null;
        for (DeviceInfo d : devices) {
            if (d.getName().equalsIgnoreCase(deviceName)) {
                device = d;
                break;
            }
        }

        if (device != null) {
            device.setOn(false);
            return new DeviceOperationResponse(true, device.getName() + " turned OFF", device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "shutdownAllDevicesRMI")
    public DeviceOperationResponse shutdownAllDevicesRMI() {
        try {
            rmi.AppareilInterface appareil = (rmi.AppareilInterface)
                    Naming.lookup("rmi://localhost:" + RMI_PORT + "/AppareilService");
            appareil.eteindre();

            for (DeviceInfo device : devices) {
                device.setOn(false);
            }
            return new DeviceOperationResponse(true,
                    "All devices shut down via RMI successfully", null);
        } catch (Exception e) {
            return new DeviceOperationResponse(false,
                    "RMI shutdown failed: " + e.getMessage(), null);
        }
    }

    @WebMethod(operationName = "getTotalConsumption")
    public double getTotalConsumption() {
        double total = 0.0;
        for (DeviceInfo device : devices) {
            if (device.isOn()) {
                total += device.getBaseConsumption();
            }
        }
        return total;
    }

    @WebMethod(operationName = "getDevicesByStatus")
    public DeviceListResponse getDevicesByStatus(@WebParam(name = "isOn") boolean isOn) {
        List<DeviceInfo> filteredDevices = new ArrayList<DeviceInfo>();
        for (DeviceInfo d : devices) {
            if (d.isOn() == isOn) {
                filteredDevices.add(d);
            }
        }

        DeviceListResponse response = new DeviceListResponse();
        response.setDevices(filteredDevices);
        response.setTotalCount(filteredDevices.size());
        return response;
    }

    @WebMethod(operationName = "addDevice")
    public DeviceOperationResponse addDevice(
            @WebParam(name = "name") String name,
            @WebParam(name = "baseConsumption") double baseConsumption,
            @WebParam(name = "isOn") boolean isOn) {

        boolean exists = false;
        for (DeviceInfo d : devices) {
            if (d.getName().equalsIgnoreCase(name)) {
                exists = true;
                break;
            }
        }

        if (exists) {
            return new DeviceOperationResponse(false, "Device already exists: " + name, null);
        }

        DeviceInfo newDevice = new DeviceInfo(name, baseConsumption, isOn);
        devices.add(newDevice);
        return new DeviceOperationResponse(true, "Device added successfully: " + name, newDevice);
    }

    @WebMethod(operationName = "removeDevice")
    public DeviceOperationResponse removeDevice(@WebParam(name = "deviceName") String deviceName) {
        DeviceInfo toRemove = null;
        for (DeviceInfo d : devices) {
            if (d.getName().equalsIgnoreCase(deviceName)) {
                toRemove = d;
                break;
            }
        }

        if (toRemove != null) {
            devices.remove(toRemove);
            return new DeviceOperationResponse(true, "Device removed: " + deviceName, null);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "updateDeviceConsumption")
    public DeviceOperationResponse updateDeviceConsumption(
            @WebParam(name = "deviceName") String deviceName,
            @WebParam(name = "newConsumption") double newConsumption) {

        DeviceInfo device = null;
        for (DeviceInfo d : devices) {
            if (d.getName().equalsIgnoreCase(deviceName)) {
                device = d;
                break;
            }
        }

        if (device != null) {
            device.setBaseConsumption(newConsumption);
            return new DeviceOperationResponse(true,
                    "Consumption updated for " + device.getName(), device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }
}