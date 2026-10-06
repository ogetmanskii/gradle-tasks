package gr.utils.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class InfraPlugin implements Plugin<Project> {
    @Override
    void apply(Project p) {
        p.extensions.add("infra", InfraPluginExtension)
    }
}
