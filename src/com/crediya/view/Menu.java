package com.crediya.view;

import com.crediya.exception.CrediYaException;
import com.crediya.util.InputHelper;
import java.util.List;

/**
 * Template Method pattern: this class owns the menu loop (show options, read choice,
 * handle errors). Each concrete menu only defines its title, its options and what each option does.
 */
public abstract class Menu {

    protected abstract String title();
    protected abstract List<String> options();
    protected abstract void execute(int option);

    protected String backLabel() { return "Back"; }

    public final void run() {
        int choice;
        do {
            System.out.println("\n========== " + title() + " ==========");
            List<String> options = options();
            for (int i = 0; i < options.size(); i++) System.out.printf("%d. %s%n", i + 1, options.get(i));
            System.out.println("0. " + backLabel());
            choice = InputHelper.readInt("Option: ", 0, options.size());
            if (choice != 0) {
                try {
                    execute(choice);
                } catch (CrediYaException e) {
                    System.out.println("\nERROR: " + e.getMessage());
                }
                InputHelper.pause();
            }
        } while (choice != 0);
    }
}
