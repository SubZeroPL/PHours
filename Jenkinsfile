pipeline {
    agent any
    options {
        timestamps()
    }
    stages {
        stage('SCM checkout') {
            options {
                skipDefaultCheckout()
            }
            steps {
                echo 'Checkout'
                checkout scm 'GitSCM'
            }
        }
        stage('Build') {
            steps {
                echo 'Build'
            }
        }
        stage('Tests') {
            steps {
                echo 'Tests'
            }
        }
        stage('Publish artifacts') {
            steps {
                echo 'Publish'
            }
        }
    }
}