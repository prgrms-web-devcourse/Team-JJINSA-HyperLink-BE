REPOSITORY="/home/ec2-user"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

sudo chmod +x $JAR_NAME

nohup java -jar -Dspring.profiles.active=dev $JAR_NAME --server.port=5000 \
$JAR_NAME > $REPOSITORY/nohup.out 2>&1 &