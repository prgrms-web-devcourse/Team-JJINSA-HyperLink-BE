REPOSITORY="/home/ec2-user/app"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

sudo chmod +x $JAR_NAME

nohup java -jar -Dspring.profiles.active=set1 $JAR_NAME  \
$JAR_NAME > $REPOSITORY/nohup.out 2>&1 &