package net.kahowell.kube.diagram.model;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.HashMap;

@Data
@RegisterForReflection
public class Route implements KubernetesObject {
    private V1ObjectMeta metadata;
    private String apiVersion;
    private String kind;
    private RouteSpec spec;
    private RouteStatus status;

    @Data
    @RegisterForReflection
    public static class RouteSpec {
        Object alternateBackends;
        String host;
        String path;
        HashMap<String, Object> port;
        String subdomain;
        Object tls;
        Object to;
        String wildcardPolicy;
    }

    @Data
    @RegisterForReflection
    public static class RouteStatus {
        Object ingress;
    }
}
