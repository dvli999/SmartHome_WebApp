package soap;

import javax.xml.ws.Endpoint;
import soap.service.EnergyManagementService;
import soap.service.DeviceManagementService;

/**
 * SOAP Service Publisher (Java 8 compatible)
 * Publishes all SOAP web services using JAX-WS 2.x
 */
public class SoapServicePublisher {

    private static final String ENERGY_SERVICE_URL = "http://localhost:8089/soap/EnergyManagementService";
    private static final String DEVICE_SERVICE_URL = "http://localhost:8089/soap/DeviceManagementService";

    private static Endpoint energyEndpoint;
    private static Endpoint deviceEndpoint;

    public static void publishServices() {
        try {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║           SOAP Services - Starting...                     ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            // Publish Energy Management Service
            System.out.print("Publishing Energy Management Service... ");
            energyEndpoint = Endpoint.publish(ENERGY_SERVICE_URL, new EnergyManagementService());
            System.out.println("✓");
            System.out.println("   WSDL: " + ENERGY_SERVICE_URL + "?wsdl");

            // Publish Device Management Service
            System.out.print("Publishing Device Management Service... ");
            deviceEndpoint = Endpoint.publish(DEVICE_SERVICE_URL, new DeviceManagementService());
            System.out.println("✓");
            System.out.println("   WSDL: " + DEVICE_SERVICE_URL + "?wsdl");

            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║           ✓ SOAP Services Ready!                          ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            System.out.println("SOAP Endpoints:");
            System.out.println("  • Energy Management: " + ENERGY_SERVICE_URL);
            System.out.println("  • Device Management: " + DEVICE_SERVICE_URL);
            System.out.println("\nWSDL Documents:");
            System.out.println("  • " + ENERGY_SERVICE_URL + "?wsdl");
            System.out.println("  • " + DEVICE_SERVICE_URL + "?wsdl");
            System.out.println();

        } catch (Exception e) {
            System.err.println("\n✗ Failed to publish SOAP services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stopServices() {
        System.out.println("\nStopping SOAP services...");

        if (energyEndpoint != null && energyEndpoint.isPublished()) {
            energyEndpoint.stop();
            System.out.println("  ✓ Energy Management Service stopped");
        }

        if (deviceEndpoint != null && deviceEndpoint.isPublished()) {
            deviceEndpoint.stop();
            System.out.println("  ✓ Device Management Service stopped");
        }
    }

    public static boolean isRunning() {
        return (energyEndpoint != null && energyEndpoint.isPublished()) ||
                (deviceEndpoint != null && deviceEndpoint.isPublished());
    }
}