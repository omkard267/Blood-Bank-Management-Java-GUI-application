import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BloodBankGUI extends JFrame {
    private Map<String, Integer> inventory;
    private Map<String, Integer> hospitalDemands;
    private Map<String, Integer> hospitalCosts;

    public BloodBankGUI() {
        inventory = new HashMap<>();
        hospitalDemands = new HashMap<>();
        hospitalCosts = new HashMap<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Blood Bank Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        getContentPane().setBackground(Color.BLACK); // Set background color to black

        JToolBar toolBar = new JToolBar();
        JButton addDonationButton = new JButton("Add Donation");
        JButton distributeButton = new JButton("Distribute");
        JButton calculateProfitButton = new JButton("Calculate Profit");
        JButton viewTotalStockButton = new JButton("View Total Stock");
        JButton viewTotalDonatedButton = new JButton("View Total Donated");
        JButton addHospitalButton = new JButton("Add Hospital Demand");
        JButton viewHospitalDemandListButton = new JButton("View Hospital Demand List");
        toolBar.add(addDonationButton);
        toolBar.add(distributeButton);
        toolBar.add(calculateProfitButton);
        toolBar.add(viewTotalStockButton);
        toolBar.add(viewTotalDonatedButton);
        toolBar.add(addHospitalButton);
        toolBar.add(viewHospitalDemandListButton);

        addDonationButton.addActionListener(e -> {
            String bloodType = JOptionPane.showInputDialog("Enter Blood Type:");
            String quantityStr = JOptionPane.showInputDialog("Enter Quantity:");
            if (bloodType != null && quantityStr != null) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (inventory.containsKey(bloodType)) {
                        inventory.put(bloodType, inventory.get(bloodType) + quantity);
                    } else {
                        inventory.put(bloodType, quantity);
                    }
                    JOptionPane.showMessageDialog(null, "Donation added successfully");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid quantity. Please enter a valid number.");
                }
            }
        });

        distributeButton.addActionListener(e -> {
            String hospital = JOptionPane.showInputDialog("Enter Hospital Name:");
            String bloodType = JOptionPane.showInputDialog("Enter Blood Type:");
            String quantityStr = JOptionPane.showInputDialog("Enter Quantity:");
            if (hospital != null && bloodType != null && quantityStr != null) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (inventory.containsKey(bloodType) && inventory.get(bloodType) >= quantity) {
                        inventory.put(bloodType, inventory.get(bloodType) - quantity);
                        if (hospitalDemands.containsKey(hospital)) {
                            int demand = hospitalDemands.get(hospital);
                            if (demand >= quantity) {
                                demand -= quantity;
                                hospitalDemands.put(hospital, demand);
                            } else {
                                JOptionPane.showMessageDialog(null, "Hospital demand exceeded. Cannot distribute.");
                                inventory.put(bloodType, inventory.get(bloodType) + quantity); // Undo inventory deduction
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Hospital demand not found. Cannot distribute.");
                            inventory.put(bloodType, inventory.get(bloodType) + quantity); // Undo inventory deduction
                        }
                        JOptionPane.showMessageDialog(null, "Blood distributed successfully");
                    } else {
                        boolean compatibleBloodFound = distributeCompatibleBlood(bloodType, quantity, hospital);
                        if (!compatibleBloodFound) {
                            JOptionPane.showMessageDialog(null, "Insufficient quantity of blood available");
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid quantity. Please enter a valid number.");
                }
            }
        });

        calculateProfitButton.addActionListener(e -> {
            int totalCost = 0;
            for (Map.Entry<String, Integer> entry : hospitalDemands.entrySet()) {
                totalCost += entry.getValue() * hospitalCosts.getOrDefault(entry.getKey(), 0);
            }
            JOptionPane.showMessageDialog(null, "Total Cost of Distribution: " + totalCost);
        });

        viewTotalStockButton.addActionListener(e -> {
            StringBuilder stock = new StringBuilder();
            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                stock.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            JOptionPane.showMessageDialog(null, "Total Stock:\n" + stock.toString());
        });

        viewTotalDonatedButton.addActionListener(e -> {
            StringBuilder totalDonated = new StringBuilder("Total Blood Donated:\n");
            for (Map.Entry<String, Integer> entry : hospitalDemands.entrySet()) {
                totalDonated.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            JOptionPane.showMessageDialog(null, totalDonated.toString());
        });

        addHospitalButton.addActionListener(e -> {
            String hospital = JOptionPane.showInputDialog("Enter Hospital Name:");
            String demandStr = JOptionPane.showInputDialog("Enter Blood Demand Quantity:");
            String costStr = JOptionPane.showInputDialog("Enter Cost per Unit:");
            if (hospital != null && demandStr != null && costStr != null) {
                try {
                    int demand = Integer.parseInt(demandStr);
                    int cost = Integer.parseInt(costStr);
                    hospitalDemands.put(hospital, demand);
                    hospitalCosts.put(hospital, cost);
                    JOptionPane.showMessageDialog(null, "Hospital demand added successfully");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid demand or cost. Please enter valid numbers.");
                }
            }
        });

        viewHospitalDemandListButton.addActionListener(e -> {
            StringBuilder demandList = new StringBuilder("Hospital Demand List:\n");
            for (Map.Entry<String, Integer> entry : hospitalDemands.entrySet()) {
                int remainingQuantity = entry.getValue();
                for (Map.Entry<String, Integer> distribution : inventory.entrySet()) {
                    if (hospitalDemands.containsKey(entry.getKey()) && distribution.getKey().equals(entry.getKey())) {
                        remainingQuantity -= distribution.getValue();
                    }
                }
                demandList.append(entry.getKey()).append(": ").append(remainingQuantity).append("\n");
            }
            JOptionPane.showMessageDialog(null, demandList.toString());
        });

        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);

        setVisible(true);
    }

    private boolean distributeCompatibleBlood(String bloodType, int quantity, String hospital) {
        int totalAvailableQuantity = inventory.getOrDefault(bloodType, 0);
        switch (bloodType) {
            case "A+":
                totalAvailableQuantity += inventory.getOrDefault("A-", 0);
                totalAvailableQuantity += inventory.getOrDefault("O+", 0);
                totalAvailableQuantity += inventory.getOrDefault("O-", 0);
                break;
            case "A-":
                totalAvailableQuantity += inventory.getOrDefault("O-", 0);
                break;
            case "B+":
                totalAvailableQuantity += inventory.getOrDefault("B-", 0);
                totalAvailableQuantity += inventory.getOrDefault("O+", 0);
                totalAvailableQuantity += inventory.getOrDefault("O-", 0);
                break;
            case "B-":
                totalAvailableQuantity += inventory.getOrDefault("O-", 0);
                break;
            case "AB+":
                totalAvailableQuantity += inventory.getOrDefault("AB-", 0);
                totalAvailableQuantity += inventory.getOrDefault("A+", 0);
                totalAvailableQuantity += inventory.getOrDefault("A-", 0);
                totalAvailableQuantity += inventory.getOrDefault("B+", 0);
                totalAvailableQuantity += inventory.getOrDefault("B-", 0);
                totalAvailableQuantity += inventory.getOrDefault("O+", 0);
                totalAvailableQuantity += inventory.getOrDefault("O-", 0);
                break;
            case "AB-":
                totalAvailableQuantity += inventory.getOrDefault("A-", 0);
                totalAvailableQuantity += inventory.getOrDefault("B-", 0);
                totalAvailableQuantity += inventory.getOrDefault("O-", 0);
                break;
            case "O+":
                totalAvailableQuantity += inventory.getOrDefault("O-", 0);
                break;
            case "O-":
                break;
        }

        if (totalAvailableQuantity >= quantity) {
            // Distribute from compatible blood types
            if (inventory.containsKey(bloodType) && inventory.get(bloodType) >= quantity) {
                inventory.put(bloodType, inventory.get(bloodType) - quantity);
            } else {
                int remainingQuantity = quantity;
                if (inventory.containsKey(bloodType)) {
                    remainingQuantity -= inventory.get(bloodType);
                    inventory.put(bloodType, 0);
                }
                switch (bloodType) {  
                    case "A+":
                        if (remainingQuantity > 0) {
                            int aMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("A-", 0));
                            inventory.put("A-", inventory.get("A-") - aMinusQuantity);
                            remainingQuantity -= aMinusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int oPlusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O+", 0));
                            inventory.put("O+", inventory.get("O+") - oPlusQuantity);
                            remainingQuantity -= oPlusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int oMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O-", 0));
                            inventory.put("O-", inventory.get("O-") - oMinusQuantity);
                            remainingQuantity -= oMinusQuantity;
                        }
                        break;
                    case "A-":
                        if (remainingQuantity > 0) {
                            int oMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O-", 0));
                            inventory.put("O-", inventory.get("O-") - oMinusQuantity);
                            remainingQuantity -= oMinusQuantity;
                        }
                        break;
                    case "B+":
                        if (remainingQuantity > 0) {
                            int bMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("B-", 0));
                            inventory.put("B-", inventory.get("B-") - bMinusQuantity);
                            remainingQuantity -= bMinusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int oPlusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O+", 0));
                            inventory.put("O+", inventory.get("O+") - oPlusQuantity);
                            remainingQuantity -= oPlusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int oMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O-", 0));
                            inventory.put("O-", inventory.get("O-") - oMinusQuantity);
                            remainingQuantity -= oMinusQuantity;
                        }
                        break;
                    case "B-":
                        if (remainingQuantity > 0) {
                            int oMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O-", 0));
                            inventory.put("O-", inventory.get("O-") - oMinusQuantity);
                            remainingQuantity -= oMinusQuantity;
                        }
                        break;
                    case "AB+":
                        if (remainingQuantity > 0) {
                            int abMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("AB-", 0));
                            inventory.put("AB-", inventory.get("AB-") - abMinusQuantity);
                            remainingQuantity -= abMinusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int aPlusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("A+", 0));
                            inventory.put("A+", inventory.get("A+") - aPlusQuantity);
                            remainingQuantity -= aPlusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int aMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("A-", 0));
                            inventory.put("A-", inventory.get("A-") - aMinusQuantity);
                            remainingQuantity -= aMinusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int bPlusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("B+", 0));
                            inventory.put("B+", inventory.get("B+") - bPlusQuantity);
                            remainingQuantity -= bPlusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int bMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("B-", 0));
                            inventory.put("B-", inventory.get("B-") - bMinusQuantity);
                            remainingQuantity -= bMinusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int oPlusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O+", 0));
                            inventory.put("O+", inventory.get("O+") - oPlusQuantity);
                            remainingQuantity -= oPlusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int oMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O-", 0));
                            inventory.put("O-", inventory.get("O-") - oMinusQuantity);
                            remainingQuantity -= oMinusQuantity;
                        }
                        break;
                    case "AB-":
                        if (remainingQuantity > 0) {
                            int aMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("A-", 0));
                            inventory.put("A-", inventory.get("A-") - aMinusQuantity);
                            remainingQuantity -= aMinusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int bMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("B-", 0));
                            inventory.put("B-", inventory.get("B-") - bMinusQuantity);
                            remainingQuantity -= bMinusQuantity;
                        }
                        if (remainingQuantity > 0) {
                            int oMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O-", 0));
                            inventory.put("O-", inventory.get("O-") - oMinusQuantity);
                            remainingQuantity -= oMinusQuantity;
                        }
                        break;
                    case "O+":
                        if (remainingQuantity > 0) {
                            int oMinusQuantity = Math.min(remainingQuantity, inventory.getOrDefault("O-", 0));
                            inventory.put("O-", inventory.get("O-") - oMinusQuantity);
                            remainingQuantity -= oMinusQuantity;
                        }
                        break;
                    case "O-":
                        break;
                }
            }

            // Update hospital demand
            if (hospitalDemands.containsKey(hospital)) {
                int demand = hospitalDemands.get(hospital);
                if (demand >= quantity) {
                    demand -= quantity;
                    hospitalDemands.put(hospital, demand);
                } else {
                    JOptionPane.showMessageDialog(null, "Hospital demand exceeded. Cannot distribute.");
                    // Undo inventory deduction
                    inventory.put(bloodType, inventory.getOrDefault(bloodType, 0) + quantity);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Hospital demand not found. Cannot distribute.");
                // Undo inventory deduction
                inventory.put(bloodType, inventory.getOrDefault(bloodType, 0) + quantity);
            }
            JOptionPane.showMessageDialog(null, "Blood distributed successfully: " + bloodType);
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        new BloodBankGUI();
    }
}



