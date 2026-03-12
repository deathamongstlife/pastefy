package de.interaapps.pastely.cli;

import de.interaapps.pastely.Pastely;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "start",
    mixinStandardHelpOptions = true,
    description = "Automatically migrate the database to the latest version"
)
public class StartServerCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("Start server");
        Pastefy instance = Pastely.getInstance();
        instance.setupServer();
        instance.start();

        // Do not exit
        return 0;
    }
}
