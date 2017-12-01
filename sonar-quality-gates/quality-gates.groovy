@Grapes(
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
)
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON


def env = build.getEnvironment()
def http = new groovyx.net.http.HTTPBuilder(env.serverUrl)

def getTaskStatus(http, env) {
    def _basicAuth = env.SONAR_AUTH_TOKEN+':'
    def _basicAuthEncoded = _basicAuth.bytes.encodeBase64().toString()

    http.request(GET, JSON) {
        uri.path = '/api/ce/task'
        uri.query = [ id: env.ceTaskId ]
        headers.'Accept' = 'application/json'
        headers.'Authorization' = 'Basic ' + _basicAuthEncoded

        response.success = { resp, json ->
            assert resp.status == 200

            return json.task.status
        }
    }
}

while ( getTaskStatus(http, env) == "PENDING" || getTaskStatus(http, env) == "IN_PROGRESS" ) {
    println "Waiting for SonarQube results"
    sleep(1000)
}

def status = getTaskStatus(http, env)

if ((status == "FAILED") || (status == "CANCELED")) {
    throw("SonarQube Task-Status is: ${status}")
}

def basicAuth = env.SONAR_AUTH_TOKEN+':'
def basicAuthEncoded = basicAuth.bytes.encodeBase64().toString()

http.request(GET, JSON) {
    uri.path = '/api/ce/task'
    uri.query = [ id: env.ceTaskId ]
    headers.'Accept' = 'application/json'
    headers.'Authorization' = 'Basic ' + basicAuthEncoded

    response.success = { resp, json ->
        assert resp.status == 200

        qgClient = new groovyx.net.http.HTTPBuilder(env.serverUrl)
        qgClient.request(GET, JSON) {
            uri.path = '/api/qualitygates/project_status'
            uri.query = [ analysisId: json.task.analysisId ]
            headers.'Accept' = 'application/json'
            headers.'Authorization' = 'Basic ' + basicAuthEncoded

            response.success = { qresp, qualitygate ->
                assert qresp.status == 200

                println qualitygate

                assert qualitygate.projectStatus.status != 'WARN'  : "SonarQube Reported a WARNING"
                assert qualitygate.projectStatus.status != 'ERROR' : "SonarQube Reported an ERROR"

                println "SonarQube Results is: ${qualitygate.projectStatus.status}"
            }
        }
    }
}
