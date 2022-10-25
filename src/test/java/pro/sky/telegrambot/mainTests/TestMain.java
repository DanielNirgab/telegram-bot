package pro.sky.telegrambot.mainTests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMain {
    public static void main(String[] args) {
        String message = "01.01.2022 20:00 Сделать домашнюю работу";

        Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(message);

        if (matcher.matches()) {
            String data = matcher.group(1);
            String task = matcher.group(3);

            System.out.println(data + " test " + task);
        }
    }
}
