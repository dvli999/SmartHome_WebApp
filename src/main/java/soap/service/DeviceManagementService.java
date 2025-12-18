package soap.service;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;

import soap.model.DeviceInfo;
import soap.model.DeviceListResponse;
import soap.model.DeviceOperationResponse;

import web.MongoDBManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.rmi.Naming;

/**
 * Device Management SOAP Web Service (Java 8 compatible) - MongoDB-backed
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

    private static final int RMI_PORT = 1100;

    // Single shared DB manager for this SOAP service
    private final MongoDBManager db;

    public DeviceManagementService() {
        db = new MongoDBManager();
        db.init();
        db.seedDefaultDevicesIfEmpty();
    }

    private static DeviceInfo mapToDeviceInfo(Map<String, Object> doc) {
        String name = (String) doc.get("name");
        Object bc = doc.get("baseConsumption");
        double baseConsumption = (bc instanceof Number) ? ((Number) bc).doubleValue() : 0.0;
        Object on = doc.get("isOn");
        boolean isOn = (on instanceof Boolean) ? (Boolean) on : false;
        return new DeviceInfo(name, baseConsumption, isOn);
    }

    @WebMethod(operationName = "getAllDevices")
    public DeviceListResponse getAllDevices() {
        List<Map<String, Object>> docs = db.getAllDevices();
        List<DeviceInfo> list = new ArrayList<DeviceInfo>();
        for (Map<String, Object> doc : docs) {
            list.add(mapToDeviceInfo(doc));
        }
        DeviceListResponse response = new DeviceListResponse();
        response.setDevices(list);
        response.setTotalCount(list.size());
        return response;
    }

    @WebMethod(operationName = "getDeviceByName")
    public DeviceInfo getDeviceByName(@WebParam(name = "deviceName") String deviceName) {
        List<Map<String, Object>> all = db.getAllDevices();
        for (Map<String, Object> doc : all) {
            String name = (String) doc.get("name");
            if (name != null && name.equalsIgnoreCase(deviceName)) {
                return mapToDeviceInfo(doc);
            }
        }
        return null;
    }

    @WebMethod(operationName = "toggleDevice")
    public DeviceOperationResponse toggleDevice(@WebParam(name = "deviceName") String deviceName) {
        boolean ok = db.toggleDevicePower(deviceName);
        if (ok) {
            DeviceInfo device = getDeviceByName(deviceName);
            return new DeviceOperationResponse(true,
                    deviceName + " toggled successfully", device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "turnOnDevice")
    public DeviceOperationResponse turnOnDevice(@WebParam(name = "deviceName") String deviceName) {
        boolean ok = db.setDevicePower(deviceName, true);
        if (ok) {
            DeviceInfo device = getDeviceByName(deviceName);
            return new DeviceOperationResponse(true, deviceName + " turned ON", device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "turnOffDevice")
    public DeviceOperationResponse turnOffDevice(@WebParam(name = "deviceName") String deviceName) {
        boolean ok = db.setDevicePower(deviceName, false);
        if (ok) {
            DeviceInfo device = getDeviceByName(deviceName);
            return new DeviceOperationResponse(true, deviceName + " turned OFF", device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "shutdownAllDevicesRMI")
    public DeviceOperationResponse shutdownAllDevicesRMI() {
        try {
            rmi.AppareilInterface appareil = (rmi.AppareilInterface)
                    Naming.lookup("rmi://localhost:" + RMI_PORT + "/AppareilService");
            appareil.eteindre();

            // Turn off all devices in DB
            List<Map<String, Object>> all = db.getAllDevices();
            for (Map<String, Object> doc : all) {
                String name = (String) doc.get("name");
                if (name != null) db.setDevicePower(name, false);
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
        return db.getTotalConsumptionForOnDevices();
    }

    @WebMethod(operationName = "getDevicesByStatus")
    public DeviceListResponse getDevicesByStatus(@WebParam(name = "isOn") boolean isOn) {
        List<Map<String, Object>> docs = db.getDevicesByStatus(isOn);
        List<DeviceInfo> list = new ArrayList<DeviceInfo>();
        for (Map<String, Object> doc : docs) {
            list.add(mapToDeviceInfo(doc));
        }
        DeviceListResponse response = new DeviceListResponse();
        response.setDevices(list);
        response.setTotalCount(list.size());
        return response;
    }

    @WebMethod(operationName = "addDevice")
    public DeviceOperationResponse addDevice(
            @WebParam(name = "name") String name,
            @WebParam(name = "baseConsumption") double baseConsumption,
            @WebParam(name = "isOn") boolean isOn) {

        boolean inserted = db.insertDevice(name, baseConsumption, isOn);
        if (!inserted) {
            return new DeviceOperationResponse(false, "Device already exists: " + name, null);
        }
        return new DeviceOperationResponse(true, "Device added successfully: " + name,
                new DeviceInfo(name, baseConsumption, isOn));
    }

    @WebMethod(operationName = "removeDevice")
    public DeviceOperationResponse removeDevice(@WebParam(name = "deviceName") String deviceName) {
        boolean ok = db.removeDevice(deviceName);
        if (ok) {
            return new DeviceOperationResponse(true, "Device removed: " + deviceName, null);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }

    @WebMethod(operationName = "updateDeviceConsumption")
    public DeviceOperationResponse updateDeviceConsumption(
            @WebParam(name = "deviceName") String deviceName,
            @WebParam(name = "newConsumption") double newConsumption) {

        boolean ok = db.updateDeviceConsumption(deviceName, newConsumption);
        if (ok) {
            DeviceInfo device = getDeviceByName(deviceName);
            return new DeviceOperationResponse(true,
                    "Consumption updated for " + deviceName, device);
        }
        return new DeviceOperationResponse(false, "Device not found: " + deviceName, null);
    }
}
