#!/bin/bash

MY_IP=$(curl -s ifconfig.me)
if [ $MY_IP == 52.79.110.182 ]
then
  MY_DOMAIN=test.hyper-link.store
elif [ $MY_IP == 3.36.209.200 ]
then
  MY_DOMAIN=api.hyper-link.store
fi

CHECK_PROFILE=/profile
echo "> 현재 구동중인 Port 확인"
CURRENT_PROFILE=$(curl -s https://$MY_DOMAIN$CHECK_PROFILE)

# 쉬고 있는 set 찾기: set1이 사용중이면 set2가 쉬고 있고, 반대면 set1이 쉬고 있음
if [ $CURRENT_PROFILE == set1 ]
then
  IDLE_PORT=8001
elif [ $CURRENT_PROFILE == set2 ]
then
  IDLE_PORT=8000
else
  echo "> 일치하는 Profile이 없습니다. Profile: $CURRENT_PROFILE"
  echo "> 8000을 할당합니다."
  IDLE_PORT=8000
fi

echo "> 전환할 Port: $IDLE_PORT"
echo "> Port 전환"
echo "set \$service_url http://127.0.0.1:$IDLE_PORT;" | sudo tee /etc/nginx/conf.d/service-url.inc

PROXY_PORT=$(curl -s http://$MY_DOMAIN$CHECK_PROFILE)
echo "> Nginx Current Proxy Port: $PROXY_PORT"

echo "> Nginx Reload"
sudo service nginx reload