package de.interaapps.pastely.cli;

import de.interaapps.pastely.Pastely;
import de.interaapps.pastely.helper.elastic.ElasticMigrator;
import org.javawebstack.orm.ORM;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "automigrateelastic",
        mixinStandardHelpOptions = true,
        description = "Automatically migrate elastic to the latest version"
)
public class AutoMigrateElasticCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("Automigrate");
        new ElasticMigrator(Pastely.getInstance(), Pastely.getInstance().getElasticsearchClient()).migrateAll();
        System.out.println("Automigrated");
        return 0;
    }
}