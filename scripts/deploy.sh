#!/bin/bash
BASE_PATH=/home/ec2-user/app
CHECK_PROFILE=/profile
CHECK_HEALTH=/actuator/health

sudo cp $BASE_PATH/zip/*.jar $BASE_PATH/

BUILD_PATH=$(ls $BASE_PATH/*.jar | tail -n 1)
JAR_NAME=$(basename $BUILD_PATH)
echo "> build 파일명: $JAR_NAME"


echo "> application.jar 교체"
IDLE_APPLICATION=server-0.0.1-SNAPSHOT.jar
IDLE_APPLICATION_PATH=$BASE_PATH/$IDLE_APPLICATION

echo "> 구동중인 애플리케이션 pid 확인"
IDLE_PID=$(pgrep -f $IDLE_APPLICATION)

echo "> kill -15 $IDLE_PID"
sudo kill -15 $IDLE_PID
sleep 5

echo "> 새로운 jar 배포"
sudo nohup java -jar $IDLE_APPLICATION_PATH >> /home/ec2-user/app/hyperlink.log 2>/home/ec2-user/app/hyperlink_error.log &

echo "> Nginx Reload"
sudo service nginx reload