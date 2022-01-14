package net.kahowell.kube.diagram.cli;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.util.ModelMapper;
import io.kubernetes.client.util.Yaml;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Location;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.annotations.RegisterForReflection;
import net.kahowell.kube.diagram.model.DeploymentConfig;
import net.kahowell.kube.diagram.model.Route;
import net.kahowell.kube.diagram.model.Template;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.inject.Inject;
import java.io.*;
import java.util.Objects;

@QuarkusMain
@TopCommand
@Command(name = "kube-diagram", mixinStandardHelpOptions = true)
@RegisterForReflection(targets = String.class) // so we can use replace/replaceAll in templates
public class KubeDiagram implements Runnable, QuarkusApplication {

    private static final Logger log = LoggerFactory.getLogger(KubeDiagram.class);

    @Location("header.puml")
    io.quarkus.qute.Template header;

    @Location("footer.puml")
    io.quarkus.qute.Template footer;

    @Inject
    Engine templateEngine;

    @ConfigProperty(name = "quarkus.application.name")
    String appName;

    @ConfigProperty(name = "quarkus.application.version")
    String version;

    @Inject
    CommandLine.IFactory factory;

    @CommandLine.Option(names = "-f", description = "Path to file with k8s objects. Can be specified multiple times.", required = true)
    File[] paths;

    @CommandLine.Option(names = "-o", description = "Path to output file. (defaults to stdout).", converter = PrintStreamConverter.class)
    PrintStream output = System.out;

    private void setupMappings() {
        ModelMapper.addModelMap("template.openshift.io", "v1", "Template", "templates", Template.class, null);
        ModelMapper.addModelMap("", "v1", "Template", "templates", Template.class, null);
        ModelMapper.addModelMap("apps.openshift.io", "v1", "DeploymentConfig", "deploymentConfigs", DeploymentConfig.class, null);
        ModelMapper.addModelMap("", "v1", "DeploymentConfig", "deploymentConfigs", DeploymentConfig.class, null);
        ModelMapper.addModelMap("route.openshift.io", "v1", "Route", "routes", Route.class, null);
        ModelMapper.addModelMap("", "v1", "Route", "routes", Route.class, null);
    }

    public void processObject(KubernetesObject object) {
        if (object instanceof Template) {
            Template template = (Template) object;
            for (KubernetesObject subobject : template.getKubernetesObjects()) {
                processObject(subobject);
            }
        }
        else {
            String fullName = object.getApiVersion() + "/" + object.getKind();
            io.quarkus.qute.Template template = templateEngine.getTemplate(fullName + ".puml");
            // fallback to unqualified name
            if (template == null) {
                template = templateEngine.getTemplate(object.getKind() + ".puml");
            }
            if (template != null) {
                output.println(template.data("item", object).render());
            }
            else {
                log.warn("No template for {}", fullName);
            }
        }
    }

    @Override
    public void run() {
        setupMappings();
        try {
            output.println(header.data("version", version).render());
            for (File path : paths) {
                processFile(path);
            }
            output.println(footer.render());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processFile(File path) throws IOException {
        if (path.isDirectory()) {
            File[] children = path.listFiles();
            if (children != null) {
                for (File child : children) {
                    processFile(child);
                }
            }
            return;
        }
        var result = (KubernetesObject) Yaml.load(path);
        processObject(result);
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory)
                .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                .execute(args);
    }
}
