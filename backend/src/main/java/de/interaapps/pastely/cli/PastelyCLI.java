package de.interaapps.pastely.cli;

import picocli.CommandLine;

@CommandLine.Command(
    name = "pastely",
    mixinStandardHelpOptions = true,
    version = "Pastely CLI",
    description = "Command line interface for Pastely",
        subcommands = {
            AutoMigrateCommand.class,
            StartServerCommand.class,
            AutoMigrateElasticCommand.class,
            SyncToElasticCommand.class,
            SyncToMinioCommand.class,
            TestingCommand.class
        }
)
public class PastelyCLI implements Runnable {
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
