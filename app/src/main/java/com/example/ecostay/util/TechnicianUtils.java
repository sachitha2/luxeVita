package com.example.ecostay.util;

import java.util.HashMap;
import java.util.Map;

public final class TechnicianUtils {

    private static final Map<String, String> TECHNICIANS = new HashMap<>();

    static {
        TECHNICIANS.put("Smartphone", "Kasun Perera");
        TECHNICIANS.put("Laptop", "Nimal Fernando");
        TECHNICIANS.put("Television", "Saman Silva");
        TECHNICIANS.put("Air Conditioner", "Ruwan Jayasinghe");
        TECHNICIANS.put("Refrigerator", "Chamara De Silva");
        TECHNICIANS.put("Washing Machine", "Prasad Kumara");
    }

    public static final String[] STATUS_FLOW = {
            "Received",
            "Technician Assigned",
            "Under Repair",
            "Ready for Pickup",
            "Completed"
    };

    private TechnicianUtils() {
    }

    public static String getTechnicianForDeviceType(String deviceType) {
        String name = TECHNICIANS.get(deviceType);
        return name != null ? name : "TechCare Specialist";
    }

    public static String getNextStatus(String currentStatus) {
        for (int i = 0; i < STATUS_FLOW.length - 1; i++) {
            if (STATUS_FLOW[i].equals(currentStatus)) {
                return STATUS_FLOW[i + 1];
            }
        }
        return currentStatus;
    }

    public static int getStatusIndex(String status) {
        for (int i = 0; i < STATUS_FLOW.length; i++) {
            if (STATUS_FLOW[i].equals(status)) {
                return i;
            }
        }
        return 0;
    }

    public static boolean canAdvanceStatus(String currentStatus) {
        return getStatusIndex(currentStatus) < STATUS_FLOW.length - 1;
    }
}
