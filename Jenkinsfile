pipeline {
    agent {
        kubernetes {
            yaml '''
              apiVersion: v1
              kind: Pod
              metadata:
                name: esthesis-common
                namespace: jenkins
              spec:
                affinity:
                  podAntiAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 50
                      podAffinityTerm:
                        labelSelector:
                          matchExpressions:
                          - key: jenkins/jenkins-jenkins-agent
                            operator: In
                            values:
                            - "true"
                        topologyKey: kubernetes.io/hostname
                securityContext:
                  runAsUser: 0
                  runAsGroup: 0
                  fsGroup: 0
                containers:
                - name: esthesis-common-builder
                  image: eddevopsd2/ubuntu-dind:docker24-mvn3.9.6-jdk21-node18.16-go1.23-buildx-helm
                  volumeMounts:
                  - name: maven
                    mountPath: /root/.m2/
                    subPath: esthesis-common
                  - name: sonar-scanner
                    mountPath: /root/sonar-scanner
                  tty: true
                  securityContext:
                    privileged: true
                    runAsUser: 0
                imagePullSecrets:
                - name: regcred
                volumes:
                - name: maven
                  persistentVolumeClaim:
                    claimName: maven-nfs-pvc
                - name: sonar-scanner
                  persistentVolumeClaim:
                    claimName: sonar-scanner-nfs-pvc
            '''
            workspaceVolume persistentVolumeClaimWorkspaceVolume(claimName: 'workspace-nfs-pvc', readOnly: false)
        }
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 3, unit: 'HOURS')
    }
    stages {
        stage ('Clone Common and Bom Repositories') {
            steps {
                container (name: 'esthesis-common-builder') {
                    withCredentials([usernamePassword(credentialsId: 'Jenkins-Github-token',
                    usernameVariable: 'Username',
                    passwordVariable: 'Password')]){
                        sh '''
                            git config --global user.email "devops-d2@eurodyn.com"
                            git config --global user.name "$Username"
                            git clone https://$Password@github.com/esthesis-iot/esthesis-bom
                        '''
                    }
                }
            }
        }
        stage('Build Bom') {
            steps {
                container (name: 'esthesis-common-builder') {
                    sh '''
                        cd esthesis-bom
                        mvn clean install
                    '''
                }
            }
        }
        stage('Build Common') {
            steps {
                container (name: 'esthesis-common-builder') {
                    sh '''
                        mvn clean install
                    '''
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                container (name: 'esthesis-common-builder') {
                    withSonarQubeEnv('sonar') {
                        sh '''
                            /root/sonar-scanner/sonar-scanner/bin/sonar-scanner -Dsonar.projectVersion="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)" -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_GLOBAL_KEY} -Dsonar.working.directory="/tmp"
                        '''
                    }
                }
            }
        }
        stage('Build Server') {
            steps {
                container (name: 'esthesis-common-builder') {
                    sh '''
                        mvn clean install -Pcyclonedx-bom
                    '''
                }
            }
        }
        stage('Post Dependency-Track Analysis for device') {
            steps{
                container (name: 'esthesis-common-builder') {
                    sh '''
                        echo '{"project": "66912788-4da1-42b4-ace4-9f7c467b9b77", "bom": "'"$(cat target/bom.xml | base64 -w 0)"'"}' > payload.json
                    '''
                    sh '''
                        curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                    '''
                }
            }
        }
    }
    post {
        changed {
            emailext subject: '$DEFAULT_SUBJECT',
                body: '$DEFAULT_CONTENT',
                to: '12133724.eurodynlu.onmicrosoft.com@emea.teams.ms'
        }
    }
}