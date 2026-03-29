import java.util.*;

public class TwoDArray {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int [][] grades = new int[3][3];
        
        grades[0][0] = 100;
        grades[0][1] = 99;
        grades[0][2] = 100;
        grades[1][0] = 98;
        grades[1][1] = 97;
        grades[1][2] = 99;
        grades[2][0] = 99;
        grades[2][1] = 98;
        grades[2][2] = 99;
        
        System.out.println("Subject: DSA");
        String[] rowName = {"Zyrille", "Jesse  ", "Luke   " };

        for (int i = 0; i < grades.length; i++){
            System.out.print(rowName[i] + ": ");
            for (int j = 0; j < grades[i].length; j++){
                System.out.print(grades[i][j] + "   ");
            }
            System.out.println();
        }
    }
}
