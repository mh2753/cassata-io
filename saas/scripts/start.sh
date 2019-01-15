#!/bin/sh

set -e 

SERVICE_NAME=$1
COMMAND=$2
PATH_TO_JAR=../lib
PATH_TO_CONFIG=../config/
PID_PATH_NAME=/tmp/cassata.io.$SERVICE_NAME.lock

_start_service() { 

    echo "Starting $SERVICE_NAME ..."
    if [ ! -f $PID_PATH_NAME ]; then
        nohup java -jar $PATH_TO_JAR/$SERVICE_NAME.jar server $PATH_TO_CONFIG/service/config.yaml 2>> stdout.txt>> stdout.txt &
        echo $! > $PID_PATH_NAME
        echo "$SERVICE_NAME started ..."
    else
        echo "$SERVICE_NAME is already running ..."
    fi
}

_start_worker() { 

    echo "Starting $SERVICE_NAME ..."
    if [ ! -f $PID_PATH_NAME ]; then
        nohup java -jar $PATH_TO_JAR/$SERVICE_NAME.jar $PATH_TO_CONFIG/worker/config.yaml 2>> stdout.txt>> stdout.txt &
        echo $! > $PID_PATH_NAME
        echo "$SERVICE_NAME started ..."
    else
        echo "$SERVICE_NAME is already running ..."
    fi
}

_start() { 

    if [ ! -f $PATH_TO_JAR/$SERVICE_NAME.jar ]; then 
        echo "Unable to find $SERVICE_NAME.jar in $PATH_TO_JAR/ Exiting" 
        exit 1
    fi

    if [ "$SERVICE" = "service" ]; then 
        _start_service
    else 
        _start_worker
    fi
}

_stop() { 

        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
}

_main() { 
    case $COMMAND in
        start) _start
            ;;
        stop) _stop
            ;;
        restart)
            _stop
            _start
            ;;
    esac
}

#Set the path to java                                                
if [ -x "$JAVA_HOME/bin/java" ]; then                                     
    JAVA="$JAVA_HOME/bin/java"                                              
else                                                                      
    set +e                                                                  
    JAVA=`which java`                                                       
    set -e                                                                  
fi                                                                        

if [ ! -x "$JAVA" ]; then                                                 
    echo "could not find java; set JAVA_HOME or ensure java is in
    PATH"     
    exit 1                                                                  
fi                                                                        

if [[ ($SERVICE_NAME = "service" || $SERVICE_NAME = "worker") && ($COMMAND = "start" || $COMMAND = "stop") ]]; then 
    _main
else
    echo "Usage start.sh service|worker start|stop"
    exit 1;
fi

