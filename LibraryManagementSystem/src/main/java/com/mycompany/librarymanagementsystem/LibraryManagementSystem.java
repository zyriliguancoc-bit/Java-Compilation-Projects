package com.mycompany.librarymanagementsystem;
import java.util.ArrayList;
import java.util.Scanner;


public class LibraryManagementSystem {
    public static void main(String[] args) {
ArrayList<String> books = new ArrayList<>();
        ArrayList<Boolean> isBorrowed = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\nLibrary Management System");
            System.out.println("1. Add Book");
            System.out.println("2. Display Books");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: // Add Book
                    System.out.print("Enter book title: ");
                    books.add(scanner.nextLine());
                    isBorrowed.add(false);
                    System.out.println("Book added successfully!");
                    break;

                case 2: // Display Books
                    if (books.isEmpty()) {
                        System.out.println("No books available.");
                    } else {
                        System.out.println("\nList of Books:");
                        for (int i = 0; i < books.size(); i++) {
                            System.out.println((i + 1) + ". " + books.get(i) + 
                                (isBorrowed.get(i) ? " (Borrowed)" : " (Available)"));
                        }
                    }
                    break;

                case 3: // Borrow Book
                    if (books.isEmpty()) {
                        System.out.println("No books available to borrow.");
                        break;
                    }
                    System.out.print("Enter book number to borrow: ");
                    int borrowIndex = scanner.nextInt() - 1;
                    if (borrowIndex >= 0 && borrowIndex < books.size()) {
                        if (!isBorrowed.get(borrowIndex)) {
                            isBorrowed.set(borrowIndex, true);
                            System.out.println("You borrowed: " + books.get(borrowIndex));
                        } else {
                            System.out.println("Book is already borrowed.");
                        }
                    } else {
                        System.out.println("Invalid book number.");
                    }
                    break;

                case 4: // Return Book
                    if (books.isEmpty()) {
                        System.out.println("No books available to return.");
                        break;
                    }
                    System.out.print("Enter book number to return: ");
                    int returnIndex = scanner.nextInt() - 1;
                    if (returnIndex >= 0 && returnIndex < books.size()) {
                        if (isBorrowed.get(returnIndex)) {
                            isBorrowed.set(returnIndex, false);
                            System.out.println("You returned: " + books.get(returnIndex));
                        } else {
                            System.out.println("This book was not borrowed.");
                        }
                    } else {
                        System.out.println("Invalid book number.");
                    }
                    break;

                case 5:
                    System.out.println("Exiting... Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        } while (choice != 5);
        scanner.close();
    }
}