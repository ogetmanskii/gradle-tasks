package gr.utils.spec

import gr.utils.task.TaskUtils

class RemoteHostSpec {

    Object host
    Object port
    Object user
    Object password

    void host(Object v) {
        host = v
    }

    void port(Object v) {
        port = v
    }

    void user(Object v) {
        user = v
    }

    void password(Object v) {
        password = v
    }

    String getHost() {
        return TaskUtils.getString(host, null)
    }

    Integer getPort() {
        return TaskUtils.getInteger(port, 22)
    }

    String getUser() {
        return TaskUtils.getString(user, null)
    }

    String getPassword() {
        return TaskUtils.getString(password, null)
    }
}
