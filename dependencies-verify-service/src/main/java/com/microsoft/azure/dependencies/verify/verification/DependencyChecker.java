package com.microsoft.azure.dependencies.verify.verification;

import com.microsoft.azure.dependencies.verify.common.Utils;
import com.microsoft.azure.dependencies.verify.exception.VerificationResolveException;
import com.microsoft.azure.dependencies.verify.pom.ModelResolver;
import com.microsoft.azure.dependencies.verify.pom.Project;
import com.microsoft.azure.dependencies.verify.pom.SimplePom;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DependencyChecker {

    private final SimplePom pom;

    private final List<CheckResult> checkResults = new ArrayList<>();

    private final ModelResolver resolver = new ModelResolver();

    public DependencyChecker(@NonNull Project project) {
        this.pom = SimplePom.fromProject(project);
    }

    private boolean dependencyMatches(@NonNull Dependency target, @NonNull Dependency reference) {
        if (!target.getGroupId().equals(reference.getGroupId())) {
            return false;
        } else if (!target.getArtifactId().equals(reference.getArtifactId())) {
            return false;
        } else {
            return true;
        }
    }

    private String getPropertyName(@NonNull String placeHolder) {
        return placeHolder.replace("${", "").replace("}", "");
    }

    private boolean isPlaceHolderProperty(@NonNull String value) {
        return value.startsWith("${") && value.endsWith("}");
    }

    private void resolveDependencyParentVersion(@NonNull Dependency dependency, @NonNull Model model) {
        Model parentModel = this.resolver.resolve(SimplePom.fromParent(model.getParent()));
        Optional<Dependency> optional = parentModel.getDependencies().stream()
                .filter(d -> dependencyMatches(d, dependency)).findFirst();

        if (optional.isPresent() && optional.get().getVersion() != null) {
            dependency.setVersion(resolveProperty(optional.get().getVersion(), model));
        } else {
            resolveDependencyInternal(dependency, parentModel);
        }
    }

    private void resolveDependencyManagementVersion(@NonNull Dependency dependency, @NonNull Model model) {
        for (Dependency d : model.getDependencyManagement().getDependencies()) {
            if ("import".equals(d.getScope()) && "pom".equals(d.getType())) {
                resolveDependencyInternal(d, model);

                Model dependMode = this.resolver.resolve(SimplePom.fromDependency(d));
                Optional<Dependency> optional = dependMode.getDependencyManagement().getDependencies().stream()
                        .filter(p -> dependencyMatches(p, dependency)).findFirst();

                if (optional.isPresent()) {
                    dependency.setVersion(resolveProperty(optional.get().getVersion(), dependMode));
                } else {
                    resolveDependencyInternal(dependency, dependMode);
                }
            } else if (dependencyMatches(dependency, d)) {
                dependency.setVersion(resolveProperty(d.getVersion(), model));
            }

            if (isDependencyResolved(dependency)) {
                return;
            }
        }
    }

    private boolean isDependencyResolved(@NonNull Dependency dependency) {
        if (isPlaceHolderProperty(dependency.getGroupId()) || isPlaceHolderProperty(dependency.getGroupId())) {
            return false;
        } else if (dependency.getVersion() == null) {
            return false;
        } else {
            return !isPlaceHolderProperty(dependency.getVersion());
        }
    }

    private String resolveProjectGroupId(@NonNull Model model) {
        String groupId = model.getGroupId();

        if (StringUtils.hasText(groupId)) {
            return groupId;
        } else {
            return model.getParent().getGroupId();
        }
    }

    private String resolveProjectVersion(@NonNull Model model) {
        String version = model.getVersion();

        if (StringUtils.hasText(version)) {
            return version;
        } else {
            return model.getParent().getVersion();
        }
    }

    private String resolveProperty(@NonNull String property, @NonNull Model model) {
        if (!isPlaceHolderProperty(property)) {
            return property;
        }

        Model currentModel = model;
        String name = getPropertyName(property);

        while (true) {
            Properties properties = currentModel.getProperties();

            if (properties.containsKey(name)) {
                return properties.getProperty(name);
            } else if (currentModel.getParent() != null) {
                currentModel = this.resolver.resolve(SimplePom.fromParent(currentModel.getParent()));
            } else if (name.equals("project.version")) {
                return resolveProjectVersion(model);
            } else if (name.equals("project.groupId")) {
                return resolveProjectGroupId(model);
            } else {
                break;
            }
        }

        throw new VerificationResolveException("Failed to resolve property: " + property);
    }

    private void resolveDependencyProperties(@NonNull Dependency dependency, @NonNull Model model) {
        if (isDependencyResolved(dependency)) {
            return;
        }

        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();

        if (isPlaceHolderProperty(groupId)) {
            dependency.setGroupId(resolveProperty(groupId, model));
        }

        if (isPlaceHolderProperty(artifactId)) {
            dependency.setArtifactId(resolveProperty(artifactId, model));
        }

        if (version != null && isPlaceHolderProperty(version)) {
            dependency.setVersion(resolveProperty(version, model));
        }
    }

    private void resolveDependencyVersion(@NonNull Dependency dependency, @NonNull Model model) {
        if (isDependencyResolved(dependency)) {
            return;
        }

        if (model.getDependencyManagement() != null) {
            resolveDependencyManagementVersion(dependency, model);
        }

        if (model.getParent() != null && !isDependencyResolved(dependency)) {
            resolveDependencyParentVersion(dependency, model);
        }
    }

    private void resolveDependencyInternal(@NonNull Dependency dependency, @NonNull Model model) {
        resolveDependencyProperties(dependency, model);
        resolveDependencyVersion(dependency, model);
    }

    private void resolveDependency(@NonNull Dependency dependency, @NonNull Model model) {
        if (isDependencyResolved(dependency)) {
            return;
        }

        resolveDependencyInternal(dependency, model);

        Assert.isTrue(isDependencyResolved(dependency), "dependency should be resolved.");

        log.debug("resolve dependency {}", dependency.toString());
    }

    private boolean isCompileScope(@NonNull Dependency dependency, final int level) {
        boolean isCompile = dependency.getScope() == null || dependency.getScope().equals("compile");

        if (isCompile && "true".equals(dependency.getOptional()) && level > 0) {
            return false;
        } else {
            return isCompile;
        }
    }

    private List<SimplePom> buildDependencyPoms(@NonNull SimplePom simplePom, @NonNull Map<String, SimplePom> pomsMap,
                                                final int level) {
        List<SimplePom> poms = new ArrayList<>();
        Model model = this.resolver.resolve(simplePom);

        log.debug("build {}", simplePom.toString());

        model.getDependencies().stream().filter(d -> isCompileScope(d, level)).forEach(d -> {

            resolveDependency(d, model);

            SimplePom pom = SimplePom.fromDependency(d);

            pomsMap.compute(pom.signature(), (s, p) -> {
                if (p == null) {
                    poms.add(pom);
                    return pom;
                } else if (p.getVersion().equals(pom.getVersion())) {
                    return p;
                } else {
                    this.checkResults.add(CheckResult.from(p, pom.getVersion()));
                    poms.add(p);
                    return p;
                }
            });
        });

        simplePom.getDependencies().addAll(poms);

        return poms;
    }

    private List<SimplePom> buildDependencyTreeLevel(@NonNull List<SimplePom> poms,
                                                     @NonNull Map<String, SimplePom> pomsMap,
                                                     final int level) {
        log.info("== Build Dependency Tree Level [{}] ==", level);

        List<SimplePom> simplePoms = poms.stream()
                .map(p -> buildDependencyPoms(p, pomsMap, level))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return Utils.toDistinctList(simplePoms);
    }

    private void buildDependencyTree(@NonNull Map<String, SimplePom> pomsMap) {
        int level = 0;
        List<SimplePom> poms = Collections.singletonList(this.pom);

        while (!poms.isEmpty()) {
            poms = buildDependencyTreeLevel(poms, pomsMap, level++);
        }
    }

    public List<CheckResult> check() {
        this.checkResults.clear();

        buildDependencyTree(new HashMap<>());

        return this.checkResults;
    }
}
