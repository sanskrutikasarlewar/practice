pipeline {
    agent {
        label 'tomcat-server'
    }
    stages{
        stage('code-pull'){
            steps {
                git credentialsId: 'ssh-key', url: 'git@github.com:mohit-decoder/student-ui.git'
            }
        }
        stage('code-build'){
            steps {
                sh 'sudo apt update -y'
                sh 'sudo apt install maven -y'
                sh 'sudo mvn clean package'
            }
        } 
        stage('s3-upload'){
            steps{
                withAWS(credentials: 'mohit', region: 'us-west-2')  {
                sh 'sudo apt update -y'
                sh 'curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" '
                sh 'sudo apt install unzip -y'
                sh 'unzip awscliv2.zip'
                sh 'sudo ./aws/install'
                sh 'aws s3 ls'
                sh 'aws s3 mb s3://artifact11-bucket-builder --region us-west-2'
                sh 'sudo mv /home/ubuntu/workspace/tommy-pipeline/target/studentapp-2.2-SNAPSHOT.war /tmp/student-${BUILD_ID}.war'
                sh 'aws s3 cp /tmp/student-${BUILD_ID}.war  s3://artifact11-bucket-builder'
                }
            }
        }   
        stage('Tomcat-download'){
            steps {
                sh 'sudo wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.70/bin/apache-tomcat-9.0.70.zip -P /opt/'
                sh 'sudo unzip /opt/apache-tomcat-9.0.70.zip -C .'
                
            }
        }
        stage('copying-artifact'){
            steps {
                withAWS(credentials: 'mohit', region: 'us-west-2') {
                 sh '''
                    aws s3 cp s3://artifact11-bucket-builder/student-${BUILD_ID}.war .
                    sudo cp -rv student-${BUILD_ID}.war studentapp.war
                    sudo cp -rv studentapp.war /opt/apache-tomcat-9.0.70/webapps/
                    sudo sh /opt/apache-tomcat-9.0.70/bin/startup.sh
                   
                        '''
                }  
            }                
        }
