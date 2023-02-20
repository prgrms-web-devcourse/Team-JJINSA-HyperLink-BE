REPOSITORY="/home/ec2-user/app"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

cd /home/ec2-user
cp application.yml $REPOSITORY

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

sudo chmod +x $JAR_NAME

nohup java -jar -Dspring.profiles.active=set1 $JAR_NAME  \
$JAR_NAME > $REPOSITORY/nohup.out 2>&1 &