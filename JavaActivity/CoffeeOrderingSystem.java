import java.util.Scanner;

public class CoffeeOrderingSystem{
    public static void main(String[] args) throws Exception{
        Scanner input = new Scanner(System.in);
        displayMenu();
       
    
       System.out.println("Enter drink type (Espresso/Latte): ");
       String mydrink = input.nextLine();
       
      
       System.out.println("Enter size (Small/Medium/Large): ");
       String mysize = input.nextLine();

       System.out.println("Add Extra? (yes/no): ");
       String mychoice = input.nextLine();

       System.out.println("Add milk? (yes/no): ");
       String mychoiceee = input.nextLine();
    
    }

    static void displayMenu(){
        System.out.println("Menu:");
        System.out.println("1. Espresso - Small: 100 | Medium: 120 | Large: 140");
        System.out.println("2. Latte - Small: 110 | Medium: 130 | Large: 150");
    }


}
