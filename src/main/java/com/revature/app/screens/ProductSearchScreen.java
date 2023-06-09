package com.revature.app.screens;

import java.util.List;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.app.services.ProductService;
import com.revature.app.services.RouterService;
import com.revature.app.utils.Session;
import com.revature.app.utils.StringHelper;
import com.revature.app.models.Product;

import lombok.AllArgsConstructor;

/**
 * The ProductSearchScreen class handles what information is presented to the
 * user when it
 * is navigated to. It implements the IScreen interface.
 */
@AllArgsConstructor
public class ProductSearchScreen implements IScreen {
    private static final Logger logger = LogManager.getLogger(ProductSearchScreen.class);
    private final RouterService routerService;
    private final ProductService productService;
    private Session session;

    // Overrides default IScreen start method
    @Override
    public void start(Scanner scan) {
        String message = "";
        String input = "";

        exit: {
            // while loop to handle displaying information to the user
            while (true) {
                clearScreen();
                System.out.println("Product Search (press x to go back at any time)");
                // option to leave screen
                if (!message.isBlank()) {
                    System.out.println(message);
                }

                System.out.println("\nChooose a search option below:");
                System.out.println("[1] by name 🔤");
                System.out.println("[2] by category 📁");
                System.out.println("[3] by price range 💰");

                System.out.print("\nEnter: ");
                input = scan.nextLine();
                // checks in place to lead user to their requested search function, also will
                // check if input
                // is valid and gives them an option to leave
                switch (input.toLowerCase()) {
                    case "1":
                        logger.info("Search products by name");
                        searchProductsByName(scan, session);
                        break;
                    case "2":
                        logger.info("Search products by category");
                        searchProductsByCategory(scan, session);
                        break;
                    case "3":
                        logger.info("Search products by price range");
                        searchProductsByPriceRange(scan, session);
                        break;
                    case "x":
                        logger.info("Exiting ProductSearchScreen");
                        message = "";
                        clearScreen();
                        routerService.navigate("/menu", scan);
                        break exit;
                    default:
                        logger.warn("Invalid input on ProductSearchScreen!");
                        message = "Invalid option!";
                        break;
                }
            }
        }
    }

    // Method for the user to search for a product by it's name
    public void searchProductsByName(Scanner scan, Session session) {
        String searchString = "";
        String message = "";
        // new menu for the user to search by Product name
        while (true) {
            clearScreen();

            System.out.println("Product Name 🔤 Search\n");
            if (!message.isEmpty()) {
                System.out.println(message);
            }

            System.out.print("Enter a search term (x to go back): ");

            searchString = scan.nextLine();

            if (searchString.equalsIgnoreCase("x")) {
                break;
            }

            List<Product> products = productService.findProductsByName(searchString);
            if (products.size() > 0) {
                printProducts(products);
                message = "";
            } else {
                message = "No results found";
                continue;
            }

            chooseProduct(scan, products, session);
        }
    }

    // option for the user to search for product by category
    public void searchProductsByCategory(Scanner scan, Session session) {
        String message = "";
        String input = "";

        List<String> categories = productService.findAllProductCategories();
        // new menu for the user to search by category
        while (true) {
            clearScreen();

            System.out.println("Product Category 📁 Search");
            if (!message.isEmpty()) {
                System.out.println(message);
            }

            System.out.println();

            for (int i = 0; i < categories.size(); i++) {
                System.out.println("[" + (i + 1) + "]" + " " + categories.get(i));
            }

            System.out.print("\nEnter a category (x to go back): ");

            input = scan.nextLine();

            if (input.equalsIgnoreCase("x")) {
                break;
            }

            if (!StringHelper.isNumeric(input)) {
                message = "Invalid option!";
                continue;
            }

            double inputDouble = Double.parseDouble(input);

            if (!StringHelper.isInteger(inputDouble)) {
                message = "Invalid option!";
                continue;
            }

            if (inputDouble > categories.size() || inputDouble < 1) {
                message = "Invalid option!";
                continue;
            }

            String category = categories.get((int) inputDouble - 1);
            List<Product> products = productService.findProductsByCategory(category);
            printProducts(products);
            chooseProduct(scan, products, session);
        }
    }

    // option for the user to search for product by a certain range of prices
    public void searchProductsByPriceRange(Scanner scan, Session session) {
        String message = "";
        String lowerLimit = "";
        String upperLimit = "";

        exit: {
            // a new price range menu for the user to input their range
            while (true) {
                clearScreen();

                System.out.println("Product Price Range 💰 Search");
                if (!message.isEmpty()) {
                    System.out.println(message);
                }

                while (!StringHelper.isNumeric(lowerLimit)) {
                    System.out.print("\nEnter lower limit (x to go back): ");

                    lowerLimit = getPriceString(scan);

                    if (lowerLimit.equalsIgnoreCase("x")) {
                        break exit;
                    }
                }

                while (!StringHelper.isNumeric(upperLimit)) {
                    System.out.print("Enter upper limit (x to go back): ");

                    upperLimit = getPriceString(scan);

                    if (upperLimit.equalsIgnoreCase("x")) {
                        break exit;
                    }
                }

                Double lowerLimitDouble = Double.parseDouble(lowerLimit);
                Double upperLimitDouble = Double.parseDouble(upperLimit);

                if (lowerLimitDouble > upperLimitDouble) {
                    message = "Upper limit must be greater than lower limit!";
                    lowerLimit = "";
                    upperLimit = "";
                    continue;
                }

                List<Product> products = productService.findProductsByPriceRange(lowerLimitDouble, upperLimitDouble);
                if (products.size() > 0) {
                    message = "";
                    printProducts(products);
                } else {
                    message = "No results found";
                    lowerLimit = "";
                    upperLimit = "";
                    continue;
                }

                String input = chooseProduct(scan, products, session);
                if (input.equalsIgnoreCase("x")) {
                    lowerLimit = "";
                    upperLimit = "";
                    continue;
                }
            }
        }
    }

    // Method to check the inputted prices
    private String getPriceString(Scanner scan) {
        String input = "";

        while (true) {
            input = scan.nextLine();
            if (input.equalsIgnoreCase("x")) {
                return "x";
            }

            if (!StringHelper.isNumeric(input)) {
                System.out.println("Invalid input!");
                return "";
            }

            double inputDouble = Double.parseDouble(input);

            if (inputDouble < 0) {
                System.out.println("Price cannot be negative!");
                return "";
            }

            if (input.length() > 15) {
                System.out.println("Price is too large!");
                return "";
            }

            return input;
        }
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // metho to price the products to the screen
    private void printProducts(List<Product> products) {
        System.out.println("\nResults:");
        System.out.printf("%5s %40s %15s %10s\n", "", "Name", "Category", "Price");
        System.out.println("--------------------------------------------------------------------------");
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            System.out.printf("%5s %40s %15s %10s\n", "[" + (i + 1) + "]", product.getName(),
                    product.getCategory(),
                    "$" + product.getPrice());
        }
    }

    // method to choose the product
    private String chooseProduct(Scanner scan, List<Product> products, Session session) {
        String input = "";
        while (true) {
            System.out.print("\nChoose a product (x to go back): ");
            input = scan.nextLine();

            if (StringHelper.isNumeric(input)) {
                double inputDouble = Double.parseDouble(input);

                if (!StringHelper.isInteger(inputDouble)) {
                    System.out.println("Invalid option!");
                    continue;
                }

                if (inputDouble > products.size() || inputDouble < 1) {
                    System.out.println("Invalid option!");
                    continue;
                }

                Product product = products.get((int) (inputDouble - 1));
                logger.info("Navigating to ProductScreen");
                routerService.navigate("/product", scan, product);
                return "";
            } else {
                if (input.equalsIgnoreCase("x")) {
                    return "x";
                } else {
                    logger.warn("Invalid input on Product Search Screen!");
                    System.out.println("Invalid option!");
                    continue;
                }
            }
        }
    }
}
