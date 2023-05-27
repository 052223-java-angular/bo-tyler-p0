package com.revature.app;

import com.revature.app.services.RouterService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import com.revature.app.utils.Session;

public class App {
  public static void main(String args[]) throws ClassNotFoundException, IOException, SQLException {
    Scanner scanner = new Scanner(System.in);
    RouterService router = new RouterService(new Session("", "", ""));
    router.navigate("/home", scanner);
    scanner.close();
  }
}
