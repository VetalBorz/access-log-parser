import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

    //получаем первое число
    System.out.println("Введите число:");
    int firstNumber = new Scanner(System.in).nextInt();

    //получаем второе число
    System.out.println("Введите число:");
    int secondNumber = new Scanner(System.in).nextInt();

     //Вычесляем
     int sum = firstNumber + secondNumber;
     int difference = secondNumber - firstNumber;
     int product = firstNumber * secondNumber;
     double quotient = (double) firstNumber / secondNumber;

     //Результат
    System.out.println("Сумма: "+sum);
    System.out.println("Разность: "+difference);
    System.out.println("Произведение: "+product);
    System.out.println("Частное: "+quotient);


    }
}