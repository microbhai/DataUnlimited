echo off
set param=
for %%x in (%*) do call set "param=%%param%% %%x"
cd target 
java -cp .;.. -Xmx6g -XX:+HeapDumpOnOutOfMemoryError -Dpolyglot.engine.WarnInterpreterOnly=false -jar DataUnlimited-1.0.0-SNAPSHOT.jar %param%
cd ..

