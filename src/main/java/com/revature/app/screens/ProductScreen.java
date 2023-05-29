package com.revature.app.screens;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.AllArgsConstructor;

import com.revature.app.services.CartService;
import com.revature.app.utils.Session;
import com.revature.app.utils.StringHelper;
import com.revature.app.models.Cart;
import com.revature.app.models.CartProduct;
import com.revature.app.models.Product;

@AllArgsConstructor
public class ProductScreen implements IScreen {
    private final CartService cartService;
    private final Product product;
    private final Session session;
    private static final Logger logger = LogManager.getLogger(ProductScreen.class);

    @Override
    public void start(Scanner scan) {
        String input = "";
        String message = "";
        int minimumQuantity = 1;
        int maximumQuantity = 20;
        String PRODUCT_ADDED_TO_CART_SUCCESS_MSG = "Item added to cart succesfully.";
        String PRODUCT_DELETED_FROM_CART_SUCCESS_MSG = "Item removed from cart succcessfully.";
        String INVALID_OPTION_MSG = "Invalid option!";

        main: {
            while (true) {
                logger.info("Navigated to ProductScreen");
                Cart cart = cartService.getCartFromUserId(session.getId());
                clearScreen();
                System.out.println("------------------- PRODUCT DETAILS --------------------");
                System.out.println("Name: " + product.getName());
                System.out.println("Category: " + product.getCategory());
                System.out.println("Price: $" + product.getPrice());
                System.out.println("Description:");
                wrapAndDisplay(product.getDescription());
                System.out.println("\n------------------------------------------------------");

                if (!productExistsInCart(product.getId(), cart)) {
                    System.out.println("[r] Review this product - [a] Add to cart");
                } else {
                    System.out.println("[r] Review this product - [d] Delete from cart");
                }

                if (!message.isEmpty()) {
                    System.out.println("\n" + message);
                }

                System.out.print("\nEnter (x to cancel): ");
                input = scan.nextLine();

                switch (input.toLowerCase()) {
                    case "a":
                        String addToCartMessage = "";
                        int quantity = 1;
                        while (true) {
                            if (!addToCartMessage.isEmpty()) {
                                System.out.println("\n" + addToCartMessage);
                            }
                            System.out.print("Enter quantity between " + minimumQuantity + " and " + maximumQuantity
                                    + " (enter for " + minimumQuantity + ", x to cancel): ");
                            input = scan.nextLine();
                            if (input.equalsIgnoreCase("x")) {
                                break;
                            }

                            if (input.isBlank()) {
                                cartService.addProductToCart(cart.getId(), product.getId(), quantity);
                                logger.info("Added " + product.getName() + " to " + session.getUsername() + "'s cart");
                                message = PRODUCT_ADDED_TO_CART_SUCCESS_MSG;
                                break;
                            }

                            if (!StringHelper.isNumeric(input)) {
                                logger.warn("Invalid input on ProductScreen!");
                                addToCartMessage = INVALID_OPTION_MSG;
                                continue;
                            }

                            double inputDouble = Double.parseDouble(input);

                            if (!StringHelper.isInteger(inputDouble)) {
                                logger.warn("Invalid input on ProductScreen!");
                                addToCartMessage = INVALID_OPTION_MSG;
                                continue;
                            }

                            if (inputDouble < minimumQuantity || inputDouble > maximumQuantity) {
                                logger.warn("Invalid input on ProductScreen!");
                                addToCartMessage = "Quantity out of range!";
                                continue;
                            }

                            quantity = (int) inputDouble;

                            cartService.addProductToCart(cart.getId(), product.getId(), quantity);
                            logger.info("Added " + product.getName() + " to " + session.getUsername() + "'s cart");
                            message = PRODUCT_ADDED_TO_CART_SUCCESS_MSG;
                            break;
                        }
                        break;
                    case "d":
                        String deleteFromCartMessage = "";
                        deleteFromCart: {
                            while (true) {
                                if (!deleteFromCartMessage.isEmpty()) {
                                    System.out.println("\n" + deleteFromCartMessage);
                                }

                                System.out.print("Are you sure you want to remove this item from your cart? (y/n): ");

                                input = scan.nextLine();

                                switch (input.toLowerCase()) {
                                    case "y":
                                        cartService.removeProductFromCart(cart.getId(), product.getId());
                                        logger.info("Removed " + product.getName() + " from " + session.getUsername()
                                                + "'s cart");
                                        message = PRODUCT_DELETED_FROM_CART_SUCCESS_MSG;
                                        break deleteFromCart;
                                    case "n":
                                        break deleteFromCart;
                                    default:
                                        logger.warn("Invalid input on ProductScreen!");
                                        deleteFromCartMessage = INVALID_OPTION_MSG;
                                        break;
                                }
                            }
                        }
                        break;
                    case "x":
                        break main;
                    default:
                        message = INVALID_OPTION_MSG;
                        continue;
                }
            }
        }
    }

    // method for wrapping description text in a next and orderly way
    public static void wrapAndDisplay(String text) {
        int maxLineLength = 60; // Maximum line length for description

        // Wrap the text
        StringBuilder wrappedText = new StringBuilder();
        int currentIndex = 0;
        while (currentIndex < text.length()) {
            if (currentIndex + maxLineLength < text.length()) {
                // Find the last space within the line length
                int lastSpaceIndex = text.lastIndexOf(' ', currentIndex + maxLineLength);

                if (lastSpaceIndex != -1 && lastSpaceIndex > currentIndex) {
                    wrappedText.append(text, currentIndex, lastSpaceIndex);
                    wrappedText.append(System.lineSeparator());
                    currentIndex = lastSpaceIndex + 1;
                } else {
                    // If no space is found, break the line at the line length
                    wrappedText.append(text, currentIndex, currentIndex + maxLineLength);
                    wrappedText.append(System.lineSeparator());
                    currentIndex += maxLineLength;
                }
            } else {
                // Add the remaining part of the text
                wrappedText.append(text.substring(currentIndex));
                currentIndex = text.length();
            }
        }

        System.out.print(wrappedText);
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private boolean productExistsInCart(String productId, Cart cart) {
        for (CartProduct cartProduct : cart.getCartProducts()) {
            if (cartProduct.getProduct().getId().equals(productId)) {
                return true;
            }
        }
        return false;
    }
}
