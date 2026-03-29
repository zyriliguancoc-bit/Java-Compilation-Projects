public class Method {

    public static void main(String[] args) throws Exception {
      Hello();
      myName("Zyrille Francis Liguan");
      myAge(19);
      myCourse("BSIT");
      myAgeNextYear(20);

    }
    
    
    static void Hello() {
        System.out.println("Hello! ");
    }
    
    static void myName( String name){
        System.out.println("I am " + name);
    }
    
    static void myAge(int Age){
        System.out.println("I am a " + Age + " year old");
    }
    
    static int myAgeNextYear(int Age1){
        System.out.println("I will be " + Age1 + " year old next year.");
        return Age1 + 1;
    }

    static void myCourse(String course){
        System.out.println("I am a " + course + " Student.");
    }
}