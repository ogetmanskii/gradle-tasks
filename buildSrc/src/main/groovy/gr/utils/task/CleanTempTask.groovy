package gr.utils.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths

abstract class CleanTempTask extends DefaultTask {

    @TaskAction
    void execute() {
        def tempDir = System.getenv('TEMP') ?: System.getenv('TMP') ?: '/tmp'
        def tempPath = Paths.get(tempDir)

        logger.lifecycle "Clean folder: ${tempPath}"

        int deletedFiles = 0
        int deletedDirs = 0
        int skippedItems = 0
        int errorsCount = 0

        def deleteRecursively
        deleteRecursively = { Path path ->
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                return
            }
            try {
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    def children
                    try {
                        children = Files.list(path).toList()
                    } catch (Exception e) {
                        logger.info "[SKIPPED] Could not read: ${path} (${e.message})"
                        skippedItems++
                        return
                    }
                    children.each { child ->
                        deleteRecursively(child)
                    }
                    try {
                        Files.delete(path)
                        deletedDirs++
                    } catch (Exception e) {
                        logger.info "[SKIPPED] Could not delete: ${path} (${e.message})"
                        skippedItems++
                    }
                } else {
                    try {
                        Files.delete(path)
                        deletedFiles++
                    } catch (Exception e) {
                        logger.info "[SKIPPED] Could not delete: ${path} (${e.message})"
                        skippedItems++
                    }
                }
            } catch (Exception e) {
                logger.info "[ERROR] ${path}: ${e.message}"
                errorsCount++
            }
        }

        if (!Files.exists(tempPath)) {
            logger.info "Folder does not exist: ${tempPath}"
            return
        }

        try {
            Files.list(tempPath).forEach { item ->
                deleteRecursively(item)
            }
        } catch (Exception e) {
            logger.info "[ERROR] Could not read TEMP: ${e.message}"
            return
        }

        logger.lifecycle "Deleted files:   ${deletedFiles}"
        logger.lifecycle "Deleted folders: ${deletedDirs}"
        logger.lifecycle "Skipped:         ${skippedItems}"
        logger.lifecycle "Errors:          ${errorsCount}"
    }
}
