#!/bin/bash
BASE_PATH=/home/ec2-user/app
CHECK_PROFILE=/api/v0.1/profile
CHECK_HEALTH=/api/v0.1/actuator/health

MY_IP=$(curl -s ifconfig.me)
if [ $MY_IP == 54.180.58.103 ]
then
  MY_DOMAIN=test.hyper-link.store
elif [ $MY_IP == 54.180.208.136 ]
then
  MY_DOMAIN=api.hyper-link.store
fi

sudo cp $BASE_PATH/zip/*.jar $BASE_PATH/

BUILD_PATH=$(ls $BASE_PATH/*.jar | tail -n 1)
JAR_NAME=$(basename $BUILD_PATH)
echo "> build 파일명: $JAR_NAME"

echo "> build 파일 복사"
DEPLOY_PATH=$BASE_PATH/jar/

sudo cp $JAR_NAME $DEPLOY_PATH

echo "> 현재 구동중인 Set 확인(curl -s https://$MY_DOMAIN$CHECK_PROFILE)"
CURRENT_PROFILE=$(curl -s https://$MY_DOMAIN$CHECK_PROFILE)
echo "> $CURRENT_PROFILE"

# 쉬고 있는 set 찾기: set1이 사용중이면 set2가 쉬고 있고, 반대면 set1이 쉬고 있음
if [ $CURRENT_PROFILE == set1 ]
then
  IDLE_PROFILE=set2
  IDLE_PORT=8001
elif [ $CURRENT_PROFILE == set2 ]
then
  IDLE_PROFILE=set1
  IDLE_PORT=8000
else
  echo "> 일치하는 Profile이 없습니다. Profile: $CURRENT_PROFILE"
  echo "> set1을 할당합니다. IDLE_PROFILE: set1"
  IDLE_PROFILE=set1
  IDLE_PORT=8000
fi
echo "> IDLE_PROFLE : $IDLE_PROFILE"
echo "> application.jar 교체"
IDLE_APPLICATION=$IDLE_PROFILE-server-0.0.1-SNAPSHOT.jar
IDLE_APPLICATION_PATH=$DEPLOY_PATH$IDLE_APPLICATION

sudo ln -Tfs $DEPLOY_PATH$JAR_NAME $IDLE_APPLICATION_PATH

echo "> $IDLE_PROFILE 에서 구동중인 애플리케이션 pid 확인"
IDLE_PID=$(pgrep -f $IDLE_APPLICATION)

if [ -z $IDLE_PID ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> kill -15 $IDLE_PID"
  sudo kill -15 $IDLE_PID
  sleep 5
fi

echo "> $IDLE_PROFILE 배포"
sudo nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE $IDLE_APPLICATION_PATH &

echo "> $IDLE_PROFILE 10초 후 Health check 시작"
echo "> curl -s http://$MY_DOMAIN:$IDLE_PORT$CHECK_HEALTH "
sleep 10

for retry_count in {1..10}
do
  response=$(curl -s http://$MY_DOMAIN:$IDLE_PORT$CHECK_HEALTH)
  up_count=$(echo $response | grep 'UP' | wc -l)

  if [ $up_count -ge 1 ]
  then # $up_count >= 1 ("UP" 문자열이 있는지 검증)
      echo "> Health check 성공"
      break
  else
      echo "> Health check의 응답을 알 수 없거나 혹은 status가 UP이 아닙니다."
      echo "> Health check: ${response}"
  fi

  if [ $retry_count -eq 10 ]
  then
    echo "> Health check 실패. "
    echo "> Nginx에 연결하지 않고 배포를 종료합니다."
    exit 1
  fi

  echo "> Health check 연결 실패. 재시도..."
  sleep 10
done

/home/ec2-user/app/switch.sh