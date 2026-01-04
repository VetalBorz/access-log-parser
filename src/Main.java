import java.io.BufferedReader;
import java.io.FileReader;


class LineTooLongException extends RuntimeException {
    public LineTooLongException(String message) {
        super(message);
    }
}


public class Main {
    public static void main(String[] args) {
        String path = "access.log";

        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader reader = new BufferedReader(fileReader);

            String line;
            int totalLines = 0;
            int minLength = Integer.MAX_VALUE;
            int maxLength = 0;

            while ((line = reader.readLine()) != null) {
                int length = line.length();

                // Проверка на максимальную длину
                if (length > 1024) {
                    throw new LineTooLongException(
                            "Строка #" + (totalLines + 1) + " превышает 1024 символа. Длина: " + length
                    );
                }

                totalLines++;
                if (length < minLength) minLength = length;
                if (length > maxLength) maxLength = length;
            }

            reader.close();
            fileReader.close();

            System.out.println("Количество строк: " + totalLines);
            System.out.println("Минимальная длина: " + (minLength == Integer.MAX_VALUE ? 0 : minLength));
            System.out.println("Максимальная длина: " + maxLength);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
