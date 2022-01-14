package net.kahowell.kube.diagram.model;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Yaml;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map.Entry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
@RegisterForReflection
public class Template implements KubernetesObject {

    V1ObjectMeta metadata;

    String apiVersion;

    String kind;

    Object labels;

    List<HashMap<String, Object>> parameters;

    List<HashMap<String, Object>> objects;

    public List<KubernetesObject> getKubernetesObjects() {
        return objects.stream().map(Yaml::dump).map(this::load).collect(Collectors.toList());
    }

    private KubernetesObject load(String yaml) {
        // attempt to substitute the parameters into the YAML as possible.
        Map<String, String> parameterMap = parameters.stream()
                .collect(Collectors.toMap(v -> (String) v.get("name"), this::getValue));
        for (Entry<String, String> param : parameterMap.entrySet()) {
            yaml = yaml.replaceAll("\\$\\{\\{" + param.getKey() + "}}", param.getValue());
            yaml = yaml.replaceAll("\\$\\{" + param.getKey() + "}", param.getValue());
        }
        try {
            return (KubernetesObject) Yaml.load(yaml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getValue(Map<String, Object> param) {
        String effectiveValue = param.getOrDefault("value", "null").toString();
        // if the value starts with @, escape the string
        if (effectiveValue.startsWith("@")) {
            return "'" + effectiveValue + "'";
        }
        return effectiveValue;
    }

}
