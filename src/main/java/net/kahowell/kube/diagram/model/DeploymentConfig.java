package net.kahowell.kube.diagram.model;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class DeploymentConfig implements KubernetesObject {
    V1ObjectMeta metadata;
    String apiVersion;
    String kind;
    DeploymentConfigSpec spec;
    DeploymentConfigStatus status;

    @Data
    @RegisterForReflection
    public static class DeploymentConfigSpec {
        int minReadySeconds;
        boolean paused;
        int replicas;
        int revisionHistoryLimit;
        Object selector;
        Object strategy;
        V1PodTemplateSpec template;
        boolean test;
        Object triggers;
    }

    @Data
    @RegisterForReflection
    public static class DeploymentConfigStatus {
        int availableReplicas;
        Object conditions;
        Object details;
        int latestVersion;
        int observedGeneration;
        int readyReplicas;
        int replicas;
        int unavailableReplicas;
        int updatedReplicas;
    }
}
