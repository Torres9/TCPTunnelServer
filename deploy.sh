cd $(dirname $0)
sudo rm -f /usr/share/tomcat8/logs/TCPTunnelServer.log
sudo cp target/TCPTunnelServer.war /usr/share/tomcat8/webapps/ROOT.war
sudo rm -rf /usr/share/tomcat8/webapps/ROOT
sudo service tomcat8 restart
