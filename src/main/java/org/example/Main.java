package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // два режима тестирования
        runParsingSession("desktop", new Dimension(1920, 1080));
        Thread.sleep(1000);
        runParsingSession("mobile", new Dimension(375, 800));
    }

    private static void runParsingSession(String mode, Dimension windowSize) throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().setSize(windowSize);

        try {
            System.out.println("running " + mode + " version");

            driver.get("https://www.wildberries.ru/");
            Thread.sleep(3000);

            //поиск по слову транспортир
            WebElement searchInput = driver.findElement(By.id("searchInput"));
            searchInput.sendKeys("транспортир");
            searchInput.sendKeys(Keys.ENTER);

            Thread.sleep(3000);
            System.out.println("Открыта страница с результатами поиска по слову \"транспортир\"");

            //сортировка в зависимости от режима
            if (isMobileVersion(driver)) {
                driver.findElement(By.cssSelector("button.sorter-mobile__btn")).click();
                Thread.sleep(1000);
                driver.findElement(By.cssSelector("li.popup-sorting__item[data-sorting-value='priceup']")).click();
            } else {
                driver.findElement(By.cssSelector("button.dropdown-filter__btn")).click();
                Thread.sleep(1000);
                List<WebElement> options = driver.findElements(By.cssSelector("li.j-catalog-sort .radio-with-text__text"));
                for (WebElement option : options) {
                    if (option.getText().contains("По возрастанию цены")) {
                        option.click();
                        break;
                    }
                }
            }

            System.out.println("Товары отсортированы по возрастанию цены");
            Thread.sleep(3000);

            List<WebElement> cards = driver.findElements(By.cssSelector("div.product-card__wrapper"));
            List<String[]> products = new ArrayList<>();

            //парсинг карточек товаров
            for (int i = 0; i < 10; i++) {
                WebElement card = cards.get(i);

                String title = card.findElement(By.cssSelector("span.product-card__name")).getText();

                String price = card.findElement(By.cssSelector("ins.price__lower-price")).getText();

                products.add(new String[]{title, price});
            }

            //вывод результата в файл соответствующий режиму
            try (PrintWriter writer = new PrintWriter(new FileWriter("results_" + mode +".csv"))) {
                writer.println("Title, price");
                for (String[] product : products)
                    writer.println(product[0] + ", " + product[1]);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Done with the " + mode + " version");
            driver.quit();
        }
    }

    //проверка того, какой режим тестирования запущен
    private static boolean isMobileVersion(WebDriver driver) {
        return !driver.findElements(By.cssSelector("button.sorter-mobile__btn")).isEmpty();
    }
}
