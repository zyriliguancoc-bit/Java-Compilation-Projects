package com.mycompany.coffeeorderingsystem;
import java.util.Scanner;
public class CoffeeOrderingSystem {

    public static void main(String[] args) throws Exception {
            Scanner input = new Scanner(System.in);

        displayMenu();

        System.out.println("Enter drink type (Espresso/Latte): ");
        String drink = input.nextLine();

        System.out.println("Enter size (Small/Medium/Large): ");
        String size = input.nextLine();

        int drinkCost = calculateDrinkCost(drink, size);

        System.out.println("Add extra shot? (yes/no): ");
        String extraShot = input.nextLine();

        System.out.println("Add milk? (yes/no): ");
        String milk = input.nextLine();

        int addOnCost = calculateAddOns(extraShot, milk);
        int total = calculateTotalBill(drinkCost, addOnCost);

        printReceipt(drink, size, extraShot, milk, total);

        input.close();
    }

    static void displayMenu() {
        System.out.println("Menu:");
        System.out.println("Espresso - Small: 100 | Medium: 120 | Large: 140");
        System.out.println("Latte    - Small: 110 | Medium: 130 | Large: 150\n");
    }

    static int calculateDrinkCost(String drink, String size) {
        int cost = 0;

        if (drink == ("Espresso")) {
            if (size == ("Small")) {
                cost = 100;
            } else if (size == ("Medium")) {
                cost = 120;
            } else if (size == ("Large")) {
                cost = 140;
            }
        } else if (drink == ("Latte")) {
            if (size == ("Small")) {
                cost = 110;
            } else if (size == ("Medium")) {
                cost = 130;
            } else if (size == ("Large")) {
                cost = 150;
            }
        }

        return cost;
    }

    static int calculateAddOns(String shot, String milk) {
        int addOn = 0;

        if (shot == ("yes")) {
            addOn += 20;
        } else {
            addOn += 0;
        }

        if (milk == ("yes")) {
            addOn += 10;
        } else {
            addOn += 0;
        }

        return addOn;
    }

    static int calculateTotalBill(int drinkCost, int addOnCost) {
        return drinkCost + addOnCost;
    }

    static void printReceipt(String drink, String size, String shot, String milk, int total) {
        System.out.println("\n---- Receipt ----");
        System.out.println("Drink: " + capitalize(drink) + " (" + capitalize(size) + ")");
        System.out.print("Add-ons: ");

        if (shot == ("yes") && milk == ("yes")) {
            System.out.print("Extra Shot Milk");
        } else if (shot == ("yes")) {
            System.out.print("Extra Shot");
        } else if (milk == ("yes")) {
            System.out.print("Milk");
        } else {
            System.out.print("None");
        }

        System.out.println("\nTotal: " + total + " PHP");
        System.out.println("------------------");
    }

    static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        } else {
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
    
    }
}
