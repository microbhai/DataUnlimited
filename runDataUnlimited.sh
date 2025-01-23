param=""
for i in $*
do
param=$param" "$i
done
echo $param
cd target
java -Xdock:icon=../images/icon.jpg -cp .:.. -Xmx12g -XX:+HeapDumpOnOutOfMemoryError -jar DataUnlimited-1.0.0-SNAPSHOT.jar $param
