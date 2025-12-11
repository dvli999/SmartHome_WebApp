package soap.service;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import soap.model.*;
import web.MongoDBManager;
import corba.SmartHome.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import java.rmi.Naming;
import java.util.*;

/**
 * Main Energy Management SOAP Web Service (Java 8 compatible)
 * Exposes all SmartHome functionality via SOAP/WSDL
 */
@WebService(
        name = "EnergyManagementService",
        serviceName = "EnergyManagementService",
        portName = "EnergyManagementPort",
        targetNamespace = "http://soap.smarthome.com/"
)
@SOAPBinding(
        style = SOAPBinding.Style.DOCUMENT,
        use = SOAPBinding.Use.LITERAL,
        parameterStyle = SOAPBinding.ParameterStyle.WRAPPED
)
public class EnergyManagementService {

    private static final MongoDBManager databaseManager = new MongoDBManager();
    private static ORB orb;
    private static Temps tempsService;
    private static double threshold = 70.0;
    private static final int CORBA_PORT = 1050;
    private static final int RMI_PORT = 1100;

    static {
        try {
            databaseManager.init();
            initCORBA();
        } catch (Exception e) {
            System.err.println("Failed to initialize services: " + e.getMessage());
        }
    }

    @WebMethod(operationName = "getRealtimeData")
    public EnergyData getRealtimeData() {
        try {
            Map<String, Object> latestRecord = databaseManager.getLatestEnergyRecord();
            if (latestRecord == null) return null;
            return mapToEnergyData(latestRecord);
        } catch (Exception e) {
            System.err.println("Error getting realtime data: " + e.getMessage());
            return null;
        }
    }

    @WebMethod(operationName = "getEnergyHistory")
    public EnergyHistory getEnergyHistory(@WebParam(name = "limit") int limit) {
        try {
            List<Map<String, Object>> historyData = databaseManager.getEnergyHistory();
            EnergyHistory history = new EnergyHistory();
            history.setThreshold(threshold);

            List<EnergyData> records = new ArrayList<EnergyData>();
            int count = 0;
            int maxLimit = limit > 0 ? limit : 100;

            for (Map<String, Object> record : historyData) {
                if (count >= maxLimit) break;
                records.add(mapToEnergyData(record));
                count++;
            }

            history.setRecords(records);
            return history;
        } catch (Exception e) {
            System.err.println("Error getting history: " + e.getMessage());
            return new EnergyHistory();
        }
    }

    @WebMethod(operationName = "predictEnergyConsumption")
    public PredictionResponse predictEnergyConsumption(
            @WebParam(name = "heure") int heure,
            @WebParam(name = "jour") int jour,
            @WebParam(name = "weekend") int weekend) {
        try {
            String prediction = callMLPrediction(heure, jour, weekend);
            double predictedConsumption = parsePrediction(prediction);
            String status = predictedConsumption > threshold ? "ELEVEE" : "NORMAL";
            return new PredictionResponse(predictedConsumption, status, threshold);
        } catch (Exception e) {
            System.err.println("Prediction error: " + e.getMessage());
            return new PredictionResponse(0.0, "ERROR", threshold);
        }
    }

    @WebMethod(operationName = "getCurrentTimeData")
    public TimeData getCurrentTimeData() {
        try {
            if (tempsService == null) initCORBA();
            TimeData timeData = new TimeData();
            timeData.setHeure(tempsService.getHeure());
            timeData.setJour(tempsService.getJour());
            timeData.setWeekend(tempsService.getWeekend());
            return timeData;
        } catch (Exception e) {
            System.err.println("CORBA error: " + e.getMessage());
            return new TimeData();
        }
    }

    @WebMethod(operationName = "shutdownAllDevices")
    public SoapResponse shutdownAllDevices() {
        try {
            rmi.AppareilInterface appareil = (rmi.AppareilInterface)
                    Naming.lookup("rmi://localhost:" + RMI_PORT + "/AppareilService");
            appareil.eteindre();
            return new SoapResponse(true, "All devices shut down successfully via RMI");
        } catch (Exception e) {
            SoapResponse response = new SoapResponse(false, "Failed to shutdown devices");
            response.setErrorDetails(e.getMessage());
            return response;
        }
    }

    @WebMethod(operationName = "updateThreshold")
    public SoapResponse updateThreshold(@WebParam(name = "newThreshold") double newThreshold) {
        try {
            if (newThreshold <= 0) {
                return new SoapResponse(false, "Threshold must be positive");
            }
            threshold = newThreshold;
            return new SoapResponse(true, "Threshold updated to " + newThreshold);
        } catch (Exception e) {
            SoapResponse response = new SoapResponse(false, "Failed to update threshold");
            response.setErrorDetails(e.getMessage());
            return response;
        }
    }

    @WebMethod(operationName = "getThreshold")
    public double getThreshold() {
        return threshold;
    }

    @WebMethod(operationName = "getServiceStatus")
    public ServiceStatus getServiceStatus() {
        ServiceStatus status = new ServiceStatus();
        status.setCorba(tempsService != null ? "connected" : "disconnected");
        status.setRmi(checkRMI() ? "connected" : "disconnected");
        status.setMl("active");
        status.setNameService("running");
        status.setWebServer("running");
        status.setSoap("running");
        return status;
    }

    @WebMethod(operationName = "saveEnergyRecord")
    public SoapResponse saveEnergyRecord(@WebParam(name = "energyData") EnergyData energyData) {
        try {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("timestamp", energyData.getTimestamp());
            data.put("heure", energyData.getHeure());
            data.put("jour", energyData.getJour());
            data.put("weekend", energyData.getWeekend());
            data.put("actual", energyData.getActual());
            data.put("predicted", energyData.getPredicted());
            data.put("status", energyData.getStatus());
            databaseManager.saveEnergyRecord(data);
            return new SoapResponse(true, "Energy record saved successfully");
        } catch (Exception e) {
            SoapResponse response = new SoapResponse(false, "Failed to save record");
            response.setErrorDetails(e.getMessage());
            return response;
        }
    }

    // Helper methods
    private static void initCORBA() {
        try {
            orb = ORB.init(new String[]{"-ORBInitialPort", String.valueOf(CORBA_PORT),
                    "-ORBInitialHost", "127.0.0.1"}, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            tempsService = TempsHelper.narrow(ncRef.resolve_str("TempsService"));
            System.out.println("SOAP Service: CORBA connection established");
        } catch (Exception e) {
            System.err.println("SOAP Service: CORBA connection failed - " + e.getMessage());
        }
    }

    private EnergyData mapToEnergyData(Map<String, Object> record) {
        EnergyData data = new EnergyData();
        data.setTimestamp(((Number) record.get("timestamp")).longValue());
        data.setHeure(((Number) record.get("heure")).intValue());
        data.setJour(((Number) record.get("jour")).intValue());
        data.setWeekend(((Number) record.get("weekend")).intValue());
        data.setActual(((Number) record.get("actual")).doubleValue());
        data.setPredicted(((Number) record.get("predicted")).doubleValue());
        data.setStatus((String) record.get("status"));
        return data;
    }

    private String callMLPrediction(int heure, int jour, int weekend) {
        try {
            String pythonExecutablePath = "python";
            String projectDir = System.getProperty("user.dir");
            String scriptPath = projectDir + "/ml/predict_ml.py";

            ProcessBuilder pb = new ProcessBuilder(pythonExecutablePath, scriptPath,
                    String.valueOf(heure), String.valueOf(jour), String.valueOf(weekend));
            pb.redirectErrorStream(true);
            Process p = pb.start();

            StringBuilder output = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } finally {
                reader.close();
            }

            p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            if (p.exitValue() == 0) return output.toString();
        } catch (Exception e) {
            System.err.println("ML prediction error: " + e.getMessage());
        }
        return simulatePrediction(heure, jour, weekend);
    }

    private String simulatePrediction(int heure, int jour, int weekend) {
        double base = 35.0;
        if (0 <= heure && heure < 6) base -= 10;
        else if (6 <= heure && heure < 9) base += 15;
        else if (9 <= heure && heure < 17) base += 5;
        else if (17 <= heure && heure < 22) base += 25;
        if (weekend == 1 && 9 <= heure && heure < 17) base += 10;
        base += (jour % 3) * 2;
        return "Predicted energy consumption: " + String.format("%.1f", base) + " kWh";
    }

    private double parsePrediction(String output) {
        try {
            String[] lines = output.split(System.lineSeparator());
            for (String line : lines) {
                if (line.contains("Predicted energy consumption:")) {
                    String numericStr = line.replaceAll("[^0-9.]", "");
                    return Double.parseDouble(numericStr);
                }
            }
            return 45.0;
        } catch (Exception e) {
            return 45.0;
        }
    }

    private boolean checkRMI() {
        try {
            return Naming.lookup("rmi://localhost:" + RMI_PORT + "/AppareilService") != null;
        } catch (Exception e) {
            return false;
        }
    }
}