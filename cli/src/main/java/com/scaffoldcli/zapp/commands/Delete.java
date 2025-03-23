package com.scaffoldcli.zapp.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scaffoldcli.zapp.ServerAccess.ServerAccessHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Delete {
    private static final Scanner scanner = new Scanner(System.in);

    public void run() {
        String scaffId = getScaffId();

        ServerAccessHandler.deleteScaffServerRequest(scaffId);

        System.out.println("Scaffold 'for Id: "+ scaffId+"' has been deleted.");
    }

    private static String getScaffId() {
        System.out.print("Enter scaffold Id to delete: ");
        String name = "";
        while (name.isEmpty()) {
            name = scanner.nextLine();
        }
        return name;
    }
}
