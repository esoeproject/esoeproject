# ESOE management startup script
#
# Andre Zitelli 2008.
#
# chkconfig: 345 90 03
# description: Starts up the entire ESOE platform

ulimit -n 4096
LIMIT=`su esoe -c "ulimit -n"`

echo "the value of ulimit -n for esoe $LIMIT"
logger -p user.info -t S80esoe "the value of ulimit -n for esoe $LIMIT"

########################################################################
# User configurable properties. I is highly recommended that you do NOT
# edit anything other than these two lines.

# ESOE base installation directory
BASEDIR=/usr/local/esoe

# Full path to JDK home directory
JAVA_HOME=/usr/java/latest

# Full path to ESOE configuration file
ESOE_CONFIG="$BASEDIR/data"

ESOE_HEAP_SIZE="-Xms512m -Xmx1280m -XX:PermSize=80m -XX:MaxPermSize=80m" \

########################################################################
# End user configurable options

ESOE_OPTS="-Desoe.home=$BASE_DIR" \
ESOE_OPTS="$ESOE_OPTS -Desoe.data=$ESOE_CONFIG/core" \
ESOE_OPTS="$ESOE_OPTS -Desoemanager.data=$ESOE_CONFIG/manager" \
ESOE_OPTS="$ESOE_OPTS -Dspep.data=$ESOE_CONFIG/spep" \
ESOE_OPTS="$ESOE_OPTS -Dopeniddeleg.data=$ESOE_CONFIG/openiddeleg" \
ESOE_OPTS="$ESOE_OPTS -Dshibdeleg.data=$ESOE_CONFIG/shibdeleg"

JVM_OPTS="-server -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=50"
#JVM_OPTS="-server -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:ParallelGCThreads=2"

# YourKit profiler integration
#
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/site/esoe/yjp-7.0.1/bin/linux-x86-32

TRUE="true"
FALSE="false"

# Script operation options. Values are are read from esoemanage.options file. These
# are defaults if not defined in said file.

# start up options
esoe_app_enabled=$TRUE

# shutdown options
esoe_app_stop=$TRUE


# TODO implement file based options
function processOptionsFile
{
        echo "File override is not currently implemented"

}

function processStartAnswers
{
	echo -n "Do you wish to start the ESOE web application? [Y/N] "
        read answer

        if [ "$answer" == 'Y' -o "$answer" == 'y' ]
        then
                esoe_app_enabled=$TRUE
        else
                esoe_app_enabled=$FALSE
        fi
}


function processStopAnswers
{

        echo -n "Do you wish to stop the ESOE web application? [Y/N] "
        read answer

        if [ "$answer" == 'Y' -o "$answer" == 'y' ]
        then
                esoe_app_stop=$TRUE
        else
                esoe_app_stop=$FALSE
        fi


}

function esoeApp
{
        if [ "$1" == 'start' ]
        then

                if [ "$esoe_app_enabled" == $TRUE ]
                then

                        if [ -z "`ps auxww | grep -v grep | grep catalina.home=$ESOE_HOME/tomcat`" ]
                        then
                                export JAVA_OPTS="$ESOE_HEAP_SIZE $JVM_OPTS $ESOE_OPTS"

                                echo "Starting ESOE web environment..."

                                cd $BASEDIR

                                # now invoke Tomcat
                                su  esoe -c $BASEDIR/tomcat/bin/startup.sh
                        else
                                echo "ESOE web environment is already running."
                        fi
                fi

        else
                if [ ! -z "`ps auxww | grep -v grep | grep catalina.home=$ESOE_HOME`" ]
                then

                        echo "Stopping ESOE web environment..."

                        su  esoe -c $BASEDIR/tomcat/bin/shutdown.sh
                else
                        echo "ESOE web enviroment is not running."
                fi
        fi

}


function start
{

        if [ ! -z "$interactive" ]
        then
                processStartAnswers
        fi

        if [ "$esoe_app_enabled" == $TRUE ]
        then
                esoeApp start
        fi
}


function stop
{

        if [ ! -z "$interactive" ]
        then
                processStopAnswers
        fi


        if [ "$esoe_app_stop" == $TRUE ]
        then
                esoeApp stop
        fi

}

# Export env vars. This will be set before any options are processed.
export JAVA_OPTS JAVA_HOME

function usage
{
        echo "usage:"
        echo ""
        echo -e "$0 ([-l] | (start | stop | restart)  [-i] [-f default_override_file] ) \n\n"
        echo "Options:"
        echo -e "\t-l\t\tList Default settings. Mutually exclusive with all other options."
        echo -e "\t-f file.\tSpecifies a file to override default settings. These settings are applied if interactive mode is not used."
        echo -e "\t-i\t\tInteractive. The user will be prompted for various options during script operation."
        echo ""

        exit 0;
}


[ "$#" -lt 1 ] && usage

for i in "$@"
do
        case $i in


        'start')

                starting='yes'  ;;

        'stop')

                stopping='yes' ;;

        'restart')

                restarting='yes' ;;

        '-f')
                shift
                properties_file=$1

                if [ -z $properties_file -o ! -r $properties_file ]
                then
                        echo "ERROR: Given properties file '$properties_file' is not readable."
                        usage
                fi

                ;;

        '-i')
                interactive='yes' ;;

        '-l')
                echo "Default settings:"
                echo -e "\tStart ESOE application on startup/restart: $esoe_app_enabled"
                echo -e "\tStop ESOE application on shutdown/restart: $esoe_app_stop\n"

                echo -n ""

                ;;
        *)
                usage
                ;;
        esac
done

if [ ! -z "$starting" ]
then
        start

elif [ ! -z "$stopping" ]
then
        stop

elif [ ! -z "$restarting" ]
then
        stop
        sleep 2
        start
fi
