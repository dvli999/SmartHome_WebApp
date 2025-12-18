package web;

import com.sun.net.httpserver.*;
import org.json.*;
import corba.SmartHome.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import soap.SoapServicePublisher;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.util.*;
import java.util.concurrent.*;

public class WebServer {

    private static ORB orb;
    private static Temps tempsService;
    private static final int WEB_PORT = 8088;
    private static final int SOAP_PORT = 8089;
    private static final int CORBA_PORT = 1050;
    private static final int RMI_PORT = 1100;

    private static Process orbdProcess;
    private static Thread corbaServerThread;
    private static Thread rmiServerThread;
    private static HttpServer httpServer;

    // Shared DB manager
    private static final MongoDBManager databaseManager = new MongoDBManager();

    private static final List<Map<String, Object>> notifications = new CopyOnWriteArrayList<>();
    private static double threshold = 70.0;
    private static boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         Smart Energy System - Starting...                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                cleanup();
            }
        }));

        try {
            databaseManager.init();
            databaseManager.seedDefaultDevicesIfEmpty();

            startORBD();
            startCORBAServer();
            startRMIServer();
            initCORBA(args);

            startSOAPServices();
            startHTTPServer();
            startBackgroundCollector();

            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✓ System Ready!                              ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println("\n🌐 Dashboard URL: http://localhost:" + WEB_PORT);
            System.out.println("🌐 SOAP Services: http://localhost:" + SOAP_PORT + "/soap/");
            System.out.println("\nServices running:");
            System.out.println("  ✓ Database Service (MongoDB)");
            System.out.println("  ✓ CORBA Name Service (port " + CORBA_PORT + ")");
            System.out.println("  ✓ CORBA Server");
            System.out.println("  ✓ RMI Server (port " + RMI_PORT + ")");
            System.out.println("  ✓ REST Web Server (port " + WEB_PORT + ")");
            System.out.println("  ✓ SOAP Web Services (port " + SOAP_PORT + ")");
            System.out.println("  ✓ ML Service\n");
            System.out.println("SOAP WSDL URLs:");
            System.out.println("  • http://localhost:" + SOAP_PORT + "/soap/EnergyManagementService?wsdl");
            System.out.println("  • http://localhost:" + SOAP_PORT + "/soap/DeviceManagementService?wsdl");
            System.out.println("\nPress Ctrl+C to stop all services\n");

            openBrowser("http://localhost:" + WEB_PORT);

            while (isRunning) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println("Failed to start system: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            System.exit(1);
        }
    }

    /* ------------------- SOAP START / STOP ------------------- */

    private static void startSOAPServices() {
        System.out.print("Starting SOAP Web Services... ");
        try {
            Thread soapThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SoapServicePublisher.publishServices();
                    } catch (Exception e) {
                        System.err.println("SOAP service error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            soapThread.setDaemon(false);
            soapThread.start();
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            System.out.println("✓");
        } catch (Exception e) {
            System.out.println("✗");
            System.err.println("Warning: Could not start SOAP services: " + e.getMessage());
        }
    }

    private static void stopSOAPServices() {
        try {
            SoapServicePublisher.stopServices();
        } catch (Exception e) {
            System.err.println("Error stopping SOAP services: " + e.getMessage());
        }
    }

    /* ------------------- HTTP Server / Handlers ------------------- */

    private static void startHTTPServer() throws Exception {
        System.out.print("Starting Web Server... ");
        httpServer = HttpServer.create(new InetSocketAddress(WEB_PORT), 0);
        httpServer.createContext("/", new DashboardHandler());
        httpServer.createContext("/api/status", new StatusHandler());
        httpServer.createContext("/api/realtime", new RealtimeHandler());
        httpServer.createContext("/api/history", new HistoryHandler());
        httpServer.createContext("/api/predict", new PredictHandler());
        httpServer.createContext("/api/device", new DeviceHandler());
        httpServer.createContext("/api/notifications", new NotificationsHandler());
        httpServer.createContext("/api/threshold", new ThresholdHandler());
        httpServer.createContext("/api/devices", new DevicesHandler());
        httpServer.createContext("/api/soap-info", new SoapInfoHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(10));
        httpServer.start();
        System.out.println("✓");
    }

    static class SoapInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            JSONObject soapInfo = new JSONObject();
            soapInfo.put("enabled", true);
            soapInfo.put("port", SOAP_PORT);
            JSONArray services = new JSONArray();
            JSONObject energyService = new JSONObject();
            energyService.put("name", "EnergyManagementService");
            energyService.put("endpoint", "http://localhost:" + SOAP_PORT + "/soap/EnergyManagementService");
            energyService.put("wsdl", "http://localhost:" + SOAP_PORT + "/soap/EnergyManagementService?wsdl");
            services.put(energyService);
            JSONObject deviceService = new JSONObject();
            deviceService.put("name", "DeviceManagementService");
            deviceService.put("endpoint", "http://localhost:" + SOAP_PORT + "/soap/DeviceManagementService");
            deviceService.put("wsdl", "http://localhost:" + SOAP_PORT + "/soap/DeviceManagementService?wsdl");
            services.put(deviceService);
            soapInfo.put("services", services);
            sendJSON(exchange, soapInfo.toString());
        }
    }

    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                String html = getDashboardHTML();
                exchange.getResponseHeaders().add("Content-Type", "text/html");
                byte[] response = html.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                try { os.write(response); } finally { os.close(); }
            } else {
                sendError(exchange, "Not Found");
            }
        }
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            JSONObject status = new JSONObject();
            status.put("corba", tempsService != null ? "connected" : "disconnected");
            status.put("ml", "active");
            status.put("rmi", checkRMI() ? "connected" : "disconnected");
            status.put("nameService", orbdProcess != null && orbdProcess.isAlive() ? "running" : "stopped");
            status.put("webServer", "running");
            sendJSON(exchange, status.toString());
        }
    }

    static class RealtimeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            try {
                Map<String, Object> latestRecord = databaseManager.getLatestEnergyRecord();
                if (latestRecord == null) {
                    sendError(exchange, "No data available yet.");
                    return;
                }
                sendJSON(exchange, new JSONObject(latestRecord).toString());
            } catch (Exception e) {
                sendError(exchange, "Failed to get realtime data: " + e.getMessage());
            }
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            List<Map<String, Object>> history = databaseManager.getEnergyHistory();
            JSONObject response = new JSONObject();
            response.put("history", new JSONArray(history));
            response.put("threshold", threshold);
            sendJSON(exchange, response.toString());
        }
    }

    static class PredictHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            if (!"POST".equals(exchange.getRequestMethod())) { sendError(exchange, "Method not allowed"); return; }
            try {
                String body = new String(readAllBytesFromStream(exchange.getRequestBody()), StandardCharsets.UTF_8);
                JSONObject request = new JSONObject(body);
                int heure = request.getInt("heure");
                int jour = request.getInt("jour");
                int weekend = request.getInt("weekend");
                String prediction = callMLPrediction(heure, jour, weekend);
                double predictedConsumption = parsePrediction(prediction);
                JSONObject response = new JSONObject();
                response.put("prediction", predictedConsumption);
                response.put("status", predictedConsumption > threshold ? "ELEVEE" : "NORMAL");
                response.put("threshold", threshold);
                sendJSON(exchange, response.toString());
            } catch (Exception e) {
                sendError(exchange, "Prediction failed: " + e.getMessage());
            }
        }
    }

    static class DeviceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            if (!"POST".equals(exchange.getRequestMethod())) { sendError(exchange, "Method not allowed"); return; }

            String path = exchange.getRequestURI().getPath();
            String action = path.substring(path.lastIndexOf('/') + 1);

            try {
                rmi.AppareilInterface appareil = (rmi.AppareilInterface)
                        Naming.lookup("rmi://localhost:" + RMI_PORT + "/AppareilService");

                if ("shutdown-all".equals(action)) {
                    appareil.eteindre();

                    // turn off all devices in MongoDB
                    List<Map<String, Object>> all = databaseManager.getAllDevices();
                    for (Map<String, Object> doc : all) {
                        String name = (String) doc.get("name");
                        if (name != null) databaseManager.setDevicePower(name, false);
                    }

                    addNotification("warning", "Manual shutdown command executed for all devices.", "RMI Service");
                }

                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("message", "Device command '" + action + "' executed via RMI");
                sendJSON(exchange, response.toString());
            } catch (Exception e) {
                sendError(exchange, "RMI call failed: " + e.getMessage());
            }
        }
    }

    /**
     * REST endpoint used by the webpage to show devices.
     * Now reads from MongoDB 'devices' collection, which is also used by SOAP.
     */
    static class DevicesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            List<Map<String, Object>> all = databaseManager.getAllDevices();
            JSONArray deviceArray = new JSONArray();
            for (Map<String, Object> doc : all) {
                JSONObject json = new JSONObject();
                json.put("name", doc.get("name"));

                Object bc = doc.get("baseConsumption");
                double baseConsumption = (bc instanceof Number) ? ((Number) bc).doubleValue() : 0.0;

                Object on = doc.get("isOn");
                boolean isOn = (on instanceof Boolean) ? (Boolean) on : false;

                // Keep frontend field names same as before
                json.put("consumption", baseConsumption);
                json.put("isOn", isOn);
                deviceArray.put(json);
            }
            sendJSON(exchange, deviceArray.toString());
        }
    }

    static class NotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            sendJSON(exchange, new JSONArray(notifications).toString());
        }
    }

    static class ThresholdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            if (!"POST".equals(exchange.getRequestMethod())) { sendError(exchange, "Method not allowed"); return; }
            try {
                String body = new String(readAllBytesFromStream(exchange.getRequestBody()), StandardCharsets.UTF_8);
                JSONObject request = new JSONObject(body);
                threshold = request.getDouble("threshold");
                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("threshold", threshold);
                sendJSON(exchange, response.toString());
            } catch (Exception e) {
                sendError(exchange, "Failed to update threshold: " + e.getMessage());
            }
        }
    }

    /* ------------------- Background Collector ------------------- */

    private static void startBackgroundCollector() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    collectEnergyData();
                } catch (Exception e) { /* silently ignore */ }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private static void collectEnergyData() {
        try {
            if (tempsService == null) { initCORBA(new String[]{}); return; }

            // compute currentActualConsumption from MongoDB devices
            double currentActualConsumption = 0.0;

            List<Map<String, Object>> allDevices = databaseManager.getAllDevices();
            for (Map<String, Object> doc : allDevices) {
                Object on = doc.get("isOn");
                boolean isOn = (on instanceof Boolean) ? (Boolean) on : false;

                Object bc = doc.get("baseConsumption");
                double baseConsumption = (bc instanceof Number) ? ((Number) bc).doubleValue() : 0.0;

                if (isOn) currentActualConsumption += baseConsumption;
            }

            // noise
            currentActualConsumption += (Math.random() * 4 - 2);

            int heure = tempsService.getHeure();
            int jour = tempsService.getJour();
            int weekend = tempsService.getWeekend();
            String prediction = callMLPrediction(heure, jour, weekend);
            double predictedConsumption = parsePrediction(prediction);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("timestamp", System.currentTimeMillis());
            data.put("heure", heure);
            data.put("jour", jour);
            data.put("weekend", weekend);
            data.put("actual", Math.round(currentActualConsumption * 10.0) / 10.0);
            data.put("predicted", Math.round(predictedConsumption * 10.0) / 10.0);
            data.put("status", currentActualConsumption > threshold ? "ELEVEE" : "NORMAL");
            databaseManager.saveEnergyRecord(data);

            if (currentActualConsumption > threshold) {
                addNotification("alert", "High consumption detected: " + data.get("actual") + " kWh", "System Alert");
                try {
                    rmi.AppareilInterface appareil = (rmi.AppareilInterface)
                            Naming.lookup("rmi://localhost:" + RMI_PORT + "/AppareilService");
                    appareil.eteindre();

                    // turn off all devices in MongoDB
                    List<Map<String, Object>> all = databaseManager.getAllDevices();
                    for (Map<String, Object> doc : all) {
                        String name = (String) doc.get("name");
                        if (name != null) {
                            databaseManager.setDevicePower(name, false);
                        }
                    }

                } catch (Exception e) { /* silently ignore */ }
            }
        } catch (Exception e) { /* silently ignore */ }
    }

    private static void addNotification(String type, String message, String source) {
        Map<String, Object> notif = new HashMap<String, Object>();
        notif.put("id", System.currentTimeMillis());
        notif.put("type", type);
        notif.put("message", message);
        notif.put("time", "just now");
        notif.put("source", source);
        notif.put("read", false);
        notifications.add(0, notif);
        if (notifications.size() > 50) {
            notifications.remove(notifications.size() - 1);
        }
    }

    private static void cleanup() {
        System.out.println("\nStopping services gracefully...");
        isRunning = false;
        if (httpServer != null) {
            httpServer.stop(1);
            System.out.println("  ✓ Web Server stopped");
        }
        stopSOAPServices();
        databaseManager.close();
        if (orbdProcess != null && orbdProcess.isAlive()) {
            orbdProcess.destroy();
            System.out.println("  ✓ CORBA Name Service process terminated");
        }
        System.out.println("✓ Cleanup complete");
    }

    /* ------------------- CORBA / RMI / ORBD Helpers ------------------- */

    private static void startORBD() {
        try {
            System.out.print("Starting CORBA Name Service... ");
            if (isPortInUse(CORBA_PORT)) {
                System.out.println("✓ (already running)");
                return;
            }
            ProcessBuilder pb = new ProcessBuilder("orbd", "-ORBInitialPort", String.valueOf(CORBA_PORT), "-ORBInitialHost", "localhost");
            pb.redirectErrorStream(true);
            orbdProcess = pb.start();
            Thread.sleep(2000);
            if (orbdProcess.isAlive()) { System.out.println("✓"); } else { throw new Exception("ORBD failed to start."); }
        } catch (Exception e) {
            System.out.println("✗");
            System.err.println("Warning: Could not start ORBD automatically. Please start it manually.");
        }
    }

    private static void startCORBAServer() {
        System.out.print("Starting CORBA Server... ");
        corbaServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    corba.TempsImpl.main(new String[]{
                            "-ORBInitialPort", String.valueOf(CORBA_PORT),
                            "-ORBInitialHost", "127.0.0.1",
                            "-ORBServerHost", "127.0.0.1"
                    });
                } catch (Exception e) { System.err.println("CORBA Server error: " + e.getMessage()); }
            }
        });
        corbaServerThread.setDaemon(true);
        corbaServerThread.start();
        try { Thread.sleep(3000); System.out.println("✓"); } catch (InterruptedException e) { System.out.println("✗"); }
    }

    private static void startRMIServer() {
        System.out.print("Starting RMI Server... ");
        rmiServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try { rmi.AppareilImpl.main(new String[]{}); } catch (Exception e) { System.err.println("RMI Server error: " + e.getMessage()); }
            }
        });
        rmiServerThread.setDaemon(true);
        rmiServerThread.start();
        try { Thread.sleep(3000); System.out.println("✓"); } catch (InterruptedException e) { System.out.println("✗"); }
    }

    private static void initCORBA(String[] args) {
        System.out.print("Connecting to CORBA services... ");
        try {
            orb = ORB.init(new String[]{"-ORBInitialPort", String.valueOf(CORBA_PORT), "-ORBInitialHost", "127.0.0.1"}, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            tempsService = TempsHelper.narrow(ncRef.resolve_str("TempsService"));
            System.out.println("✓");
        } catch (Exception e) {
            System.out.println("✗");
            System.err.println("Warning: CORBA connection failed. Will retry automatically.");
        }
    }

    /* ------------------- ML Integration (unchanged) ------------------- */

    private static String callMLPrediction(int heure, int jour, int weekend) {
        String pythonExecutablePath = "C:\\Users\\Ahmed\\.conda\\envs\\AhmedMBarekMLTP\\python.exe";
        if (new File(pythonExecutablePath).exists()) {
            String result = tryPythonCommand(pythonExecutablePath, heure, jour, weekend);
            if (result != null) return result;
        }
        String result = tryPythonCommand("python3", heure, jour, weekend);
        if (result != null) return result;
        result = tryPythonCommand("python", heure, jour, weekend);
        if (result != null) return result;
        System.err.println("Warning: Python script execution failed. Falling back to Java simulation.");
        return simulatePrediction(heure, jour, weekend);
    }

    private static String tryPythonCommand(String command, int heure, int jour, int weekend) {
        try {
            String projectDir = System.getProperty("user.dir");
            String scriptPath = new File(projectDir, "ml/predict_ml.py").getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(command, scriptPath, String.valueOf(heure), String.valueOf(jour), String.valueOf(weekend));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            reader.close();
            if (!p.waitFor(5, TimeUnit.SECONDS)) { p.destroyForcibly(); return null; }
            if (p.exitValue() != 0) { return null; }
            return output.toString();
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private static String simulatePrediction(int heure, int jour, int weekend) {
        double base = 35.0;
        if (0 <= heure && heure < 6) base -= 10;
        else if (6 <= heure && heure < 9) base += 15;
        else if (9 <= heure && heure < 17) base += 5;
        else if (17 <= heure && heure < 22) base += 25;
        if (weekend == 1 && 9 <= heure && heure < 17) base += 10;
        base += (jour % 3) * 2;
        return "Predicted energy consumption: " + String.format("%.1f", base) + " kWh";
    }

    private static double parsePrediction(String output) {
        try {
            String[] lines = output.split(System.lineSeparator());
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains("Predicted energy consumption:")) {
                    String num = line.replaceAll("[^0-9.]", "");
                    if (num.length() > 0) {
                        return Double.parseDouble(num);
                    }
                }
            }
            return 45.0;
        } catch (Exception e) {
            return 45.0;
        }
    }

    /* ------------------- Helpers ------------------- */

    private static byte[] readAllBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private static void setCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJSON(HttpExchange exchange, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        try { os.write(response); } finally { os.close(); }
    }

    private static void sendError(HttpExchange exchange, String message) throws IOException {
        JSONObject error = new JSONObject();
        error.put("error", message);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] response = error.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(500, response.length);
        OutputStream os = exchange.getResponseBody();
        try { os.write(response); } finally { os.close(); }
    }

    private static boolean checkRMI() {
        try {
            return Naming.lookup("rmi://localhost:" + RMI_PORT + "/AppareilService") != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getDashboardHTML() {
        InputStream is = WebServer.class.getResourceAsStream("/web/dashboard.html");
        if (is == null) {
            System.err.println("Warning: dashboard.html not found in resources. Falling back to embedded version.");
            return getEmbeddedDashboard();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        } catch (Exception e) {
            System.err.println("Warning: Could not read dashboard.html resource. " + e.getMessage());
            return getEmbeddedDashboard();
        } finally {
            if (reader != null) try { reader.close(); } catch (IOException ignored) {}
            try { is.close(); } catch (IOException ignored) {}
        }
    }

    private static String getEmbeddedDashboard() {
        return "<!DOCTYPE html><html><head><title>Smart Energy System</title></head><body><h1>System is running...</h1><p>Could not load dashboard.html from resources.</p></body></html>";
    }

    private static boolean isPortInUse(int port) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            return false;
        } catch (IOException e) {
            return true;
        } finally {
            if (socket != null) try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private static void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if (os.contains("mac")) Runtime.getRuntime().exec("open " + url);
            else if (os.contains("nix") || os.contains("nux")) Runtime.getRuntime().exec("xdg-open " + url);
        } catch (Exception e) { /* silently ignore */ }
    }
}
