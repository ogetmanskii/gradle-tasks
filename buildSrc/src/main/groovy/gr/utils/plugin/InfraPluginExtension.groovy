package gr.utils.plugin

import gr.utils.ClosureUtils
import gr.utils.spec.RemoteHostSpec
import gr.utils.spec.ServiceSpec
import gr.utils.task.DownServiceTask
import gr.utils.task.HealthcheckServiceTask
import gr.utils.task.UpServiceTask
import org.gradle.api.Project
import org.gradle.api.Task

import javax.inject.Inject

class InfraPluginExtension {

    final Project project

    static final String TASK_GROUP = "infra"

    @Inject
    InfraPluginExtension(Project project) {
        this.project = project

        project.tasks.register("infraUp", { Task task ->
            task.group = TASK_GROUP
            task.doLast {
                logger.lifecycle("Infrastructure UP")
            }
        })
        project.tasks.register("infraDown", { Task task ->
            task.group = TASK_GROUP
            task.doLast {
                logger.lifecycle("Infrastructure DOWN")
            }
        })
        project.tasks.register("infraHealthcheck", { Task task ->
            task.group = TASK_GROUP
            task.doLast {
                logger.lifecycle("Infrastructure HEALTHY")
            }
        })
    }

    void loadProperties(String propertiesFile) {
        Properties props = new Properties()
        new File(propertiesFile).withInputStream {
            props.load(it)
            props.each {
                project["ext"][it.key as String] = it.value
            }
        }
    }

    static RemoteHostSpec remote(Closure closure) {
        return ClosureUtils.applyClosure(closure, new RemoteHostSpec())
    }

    ServiceSpec service(@DelegatesTo(ServiceSpec) Closure closure) {
        def serviceSpec = ClosureUtils.applyClosure(closure, new ServiceSpec())
        serviceSpec.validate()

        String name = serviceSpec.name
        def upTask = project.tasks.register(name + "Up", UpServiceTask) {
            it.group = TASK_GROUP
            it.service = serviceSpec
        }
        project.tasks.named("infraUp").configure {
            it.dependsOn(upTask)
        }

        if (serviceSpec.imageFullPath != null || serviceSpec.downCommand != null) {
            def downTask = project.tasks.register(name + "Down", DownServiceTask) {
                it.group = TASK_GROUP
                it.service = serviceSpec
            }
            project.tasks.named("infraDown").configure {
                it.dependsOn(downTask)
            }
        }
        if (serviceSpec.healthcheck != null) {
            def healthCheckTask = project.tasks.register(name + "Healthcheck", HealthcheckServiceTask) {
                it.group = TASK_GROUP
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