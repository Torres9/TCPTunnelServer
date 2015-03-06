cd $(dirname $0)
mvn clean
mvn package
sudo cp target/TCPTunnelServer.war /usr/share/tomcat8/webapps/ROOT.war
sudo rm /usr/share/tomcat8/webapps/ROOT
sudo service tomcat8 restart
