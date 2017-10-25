pipeline {
    agent any
    
    options {
        skipDefaultCheckout()  // The default checkout doesn't init submodules.
    }
    
    stages {
        stage('Clone sources') {
            steps {
                // Need to jump through some extra hoops because of the submodules.
                // See https://stackoverflow.com/questions/42290133.
                checkout([$class: 'GitSCM',
                          branches: [[name: '*/master']],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [[$class: 'SubmoduleOption',
                                        disableSubmodules: false,
                                        parentCredentials: false,
                                        recursiveSubmodules: true,
                                        reference: '',
                                        trackingSubmodules: false]],
                          submoduleCfg: [],
                          userRemoteConfigs: [[url: 'https://github.com/cwardgar/nexus-IaC.git']]
                ])
            }
        }
    
        stage('Test') {
            steps {
                withCredentials([string(credentialsId: 'nexus-iac-vault-password', variable: 'VAULT_PASSWORD')]) {
                    ansiColor('xterm') {
                        sh "$WORKSPACE/scripts/test_in_docker.sh"
                    }
                }
            }
        }
        
        stage('Prepare and run Terraform') {
            steps {
                withCredentials([string(credentialsId: 'nexus-iac-vault-password', variable: 'VAULT_PASSWORD')]) {
                    ansiColor('xterm') {
                        sh "$WORKSPACE/scripts/destroy_and_recreate_prod.sh"
                    }
                }
            }
        }
    }
    
    post {
        always {
            deleteDir()
        }
        changed {
            // The 'when' directive is only supported within a 'stage' directive. Therefore,
            // to get conditional execution, we must resort to scripted pipeline code.
            // See https://jenkins.io/doc/book/pipeline/syntax/#when
            // See https://jenkins.io/doc/book/pipeline/syntax/#script
            script {
                if (currentBuild.result == 'SUCCESS') {
                    mail to: "cwardgar@ucar.edu",
                         subject: "Jenkins build is back to normal: ${currentBuild.fullDisplayName}",
                         body: "See <${currentBuild.absoluteUrl}>"
                }
            }
        }
        failure {
            mail to: "cwardgar@ucar.edu",
                 subject: "Build failed in Jenkins: ${currentBuild.fullDisplayName}",
                 body: "See <${currentBuild.absoluteUrl}>"
        }
    }
}
