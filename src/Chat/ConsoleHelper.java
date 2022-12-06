package Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// вспомогательный класс, для чтения или записи в консоль.
public class ConsoleHelper {
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));



    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static String readString() throws Exception{
        String message = "";
        try {
             message = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            message = readString();
        }
        return message;
    }

    public static int readInt(){
        int i = 0;
        try {
            i = Integer.parseInt(readString());
        } catch (Exception e) {
            System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            i = readInt();
        }
        return i;
    }


}
