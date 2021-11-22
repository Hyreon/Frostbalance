package botmanager;

import botmanager.doomstack.DoomStack;
import botmanager.frostbalance.Frostbalance;
import botmanager.generic.BotBase;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import static botmanager.Utilities.readLines;

public class Server {

    BotBase[] bots;
    
    public Server() {
        List<String> tokens = readLines(new File("data/botmanager_tokens.txt"));
        
        bots = new BotBase[] {
            new Frostbalance(tokens.get(0), "Frostbalance"),
                new DoomStack(tokens.get(1), "DoomStack")
        };

        System.out.println("Bots are now loaded. Send any command to stop the bot.");

        new Scanner(System.in).nextLine();

        System.out.println("Bots are now shutting down...");

        for (BotBase bot : bots) {
            bot.shutdown();
        }

        System.out.println("Good night.");

        System.exit(0);

    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(Server::new);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
