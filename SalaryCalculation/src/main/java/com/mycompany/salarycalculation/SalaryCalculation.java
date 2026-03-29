package com.mycompany.salarycalculation;
import java.util.*;

public class SalaryCalculation {

    static String name;
    static String position;
    static String month;
    
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        employeesDetails();
        
        System.out.print("Enter Rendered Hours: ");
        int renderedHours = input.nextInt();

        
        int salary = salaryCalc(position, renderedHours);

        paySlip(name, position, month, renderedHours, salary);
         
    }

    // One method to handle all employee inputs and process the salary
    static void employeesDetails() {
        Scanner input = new Scanner(System.in);

        System.out.print("Enter your name: ");
        name = input.nextLine();

        System.out.print("Enter your position (Manager, Office Clerk, Janitor): ");
        position = input.nextLine();

        System.out.print("Month of: ");
        month = input.nextLine();
        return;
    }
    
    static int salaryCalc(String position, int renderedHours){
        int ratePerHours = 0;    
        if (position.equalsIgnoreCase("Manager")){
            ratePerHours = 500;
        } else if (position.equalsIgnoreCase(" Office Clerk")) {
            ratePerHours = 300;
        } else if (position.equalsIgnoreCase(" Janitor")) {
            ratePerHours = 100;
        } else {
            System.out.println("You entered invalid Position. Please enter again!");
        } 
        int salary = renderedHours * ratePerHours;
        
        return salary;
    }
    
    static void paySlip(String name, String position, String month, int renderedHours, int salary){
        System.out.println("\n---Payroll---");
        System.out.println("\nFor the Month of: " + month);
        System.out.println("Name: " + name);
        System.out.println("Position: " + position);
        System.out.println("Rendered Hours: " + renderedHours);
        System.out.println("Your Salary: " + salary);
        return;
    }
}
