cd $(dirname $0)
#BASE_DIR=/opt/tomcat8
BASE_DIR=/usr/share/tomcat8
sudo rm -f $BASE_DIR/logs/TCPTunnelServer.log
sudo cp target/TCPTunnelServer.war $BASE_DIR/webapps/ROOT.war
sudo rm -rf $BASE_DIR/webapps/ROOT
sudo service tomcat8 restart
