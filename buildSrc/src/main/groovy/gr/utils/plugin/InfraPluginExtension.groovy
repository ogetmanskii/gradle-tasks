package gr.utils.plugin

import gr.utils.ClosureUtils
import gr.utils.spec.RemoteHostSpec
import gr.utils.spec.ServiceSpec
import gr.utils.task.DownServiceTask
import gr.utils.task.HealthcheckServiceTask
import gr.utils.task.UpServiceTask
import org.gradle.api.Project

import javax.inject.Inject

class InfraPluginExtension {

    final Project project

    @Inject
    InfraPluginExtension(Project project) {
        this.project = project

        project.tasks.register("infraUp", {
            it.group = "infra"
            it.doLast {
                logger.lifecycle("Infrastructure UP")
            }
        })
        project.tasks.register("infraDown", {
            it.group = "infra"
            it.doLast {
                logger.lifecycle("Infrastructure DOWN")
            }
        })
        project.tasks.register("infraHealthcheck", {
            it.group = "infra"
            it.doLast {
                logger.lifecycle("Infrastructure HEALTHY")
            }
        })
    }

    static RemoteHostSpec remote(Closure closure) {
        return ClosureUtils.applyClosure(closure, new RemoteHostSpec())
    }

    ServiceSpec service(@DelegatesTo(ServiceSpec) Closure closure) {
        def serviceSpec = ClosureUtils.applyClosure(closure, new ServiceSpec())
        String name = serviceSpec.name
        def upTask = project.tasks.register(name + "Up", UpServiceTask) {
            it.group = "infra"
            it.service = serviceSpec
        }
        project.tasks.named("infraUp").configure {
            it.dependsOn(upTask)
        }

        if (serviceSpec.imageFullPath != null || serviceSpec.downCommand != null) {
            def downTask = project.tasks.register(name + "Down", DownServiceTask) {
                it.group = "infra"
                it.service = serviceSpec
            }
            project.tasks.named("infraDown").configure {
                it.dependsOn(downTask)
            }
        }
        if (serviceSpec.healthcheck != null) {
            def healthCheckTask = project.tasks.register(name + "Healthcheck", HealthcheckServiceTask) {
                it.group = "infra"
                it.service = serviceSpec
                it.mustRunAfter(upTask)
            }
            project.tasks.named("infraUp").configure {
                it.dependsOn(healthCheckTask)
            }
            project.tasks.named("infraHealthcheck").configure {
                it.dependsOn(healthCheckTask)
            }
        }
        return serviceSpec
    }
}