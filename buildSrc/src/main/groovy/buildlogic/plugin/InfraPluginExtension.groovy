package buildlogic.plugin

import buildlogic.utils.ClosureUtils
import buildlogic.spec.RemoteHostSpec
import buildlogic.spec.ServiceSpec
import buildlogic.task.service.DownServiceTask
import buildlogic.task.service.HealthcheckServiceTask
import buildlogic.task.service.UpServiceTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

class InfraPluginExtension {

    private static final String TASK_GROUP = "infra"
    private final Project project
    private TaskProvider<Task> infraUpTask
    private TaskProvider<Task> infraDownTask
    private TaskProvider<Task> infraHealthcheckTask

    @Inject
    InfraPluginExtension(Project project) {
        this.project = project
    }

    void loadProperties(String propertiesFile) {
        def thisProject = project
        Properties props = new Properties()
        new File(propertiesFile).withInputStream {
            props.load(it)
            props.each {
                thisProject["ext"][it.key as String] = it.value
            }
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    RemoteHostSpec remote(Closure closure) {
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
        if (serviceSpec.isRequired()) {
            getInfraUpTask().configure {
                it.dependsOn(upTask)
            }
        }

        if (serviceSpec.imageFullPath != null || serviceSpec.downCommand != null) {
            def downTask = project.tasks.register(name + "Down", DownServiceTask) {
                it.group = TASK_GROUP
                it.service = serviceSpec
            }
            getInfraDownTask().configure {
                it.dependsOn(downTask)
            }
        }
        if (serviceSpec.healthcheck != null) {
            def healthCheckTask = project.tasks.register(name + "Healthcheck", HealthcheckServiceTask) {
                it.group = TASK_GROUP
                it.service = serviceSpec
                it.mustRunAfter(upTask)
            }
            if (serviceSpec.isRequired()) {
                getInfraUpTask().configure {
                    it.dependsOn(healthCheckTask)
                }
                getInfraHealthcheckTask().configure {
                    it.dependsOn(healthCheckTask)
                }
            }
        }
        return serviceSpec
    }

    private TaskProvider<Task> getInfraUpTask() {
        if (infraUpTask == null) {
            infraUpTask = project.tasks.register("infraUp", { Task task ->
                task.group = TASK_GROUP
                task.doLast {
                    logger.lifecycle("Infrastructure UP")
                }
            })
        }
        return infraUpTask
    }

    private TaskProvider<Task> getInfraDownTask() {
        if (infraDownTask == null) {
            infraDownTask = project.tasks.register("infraDown", { Task task ->
                task.group = TASK_GROUP
                task.doLast {
                    logger.lifecycle("Infrastructure DOWN")
                }
            })
        }
        return infraDownTask
    }

    private TaskProvider<Task> getInfraHealthcheckTask() {
        if (infraHealthcheckTask == null) {
            infraHealthcheckTask = project.tasks.register("infraHealthcheck", { Task task ->
                task.group = TASK_GROUP
                task.doLast {
                    logger.lifecycle("Infrastructure HEALTHY")
                }
            })
        }
        return infraHealthcheckTask
    }
}