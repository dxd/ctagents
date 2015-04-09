#!/bin/bash
# Script to launch the CTAPL project.
# Author: Bart Kamphorst (Bart.Kamphorst@phil.uu.nl)
# October 2008.

######################### Section Variable declarations ##############
HOME="/Users/dxd/git/ct"
CTROOT="./WebCT"
CTJARPATH="$CTROOT/dist"									# Path to ct3.jar.
CTJAR="$CTJARPATH/ct3.jar"									# The actual jar.
APLROOT="./n-2apl"
APLJARPATH="$APLROOT"										# Path to the 2APL jar.
APLJAR="$APLROOT/release/2apl.jar"							# The 2APL jar file.
APLMASDIR="./examples/ct"
APLSERVER="java -jar ./release/2apl.jar -nojade ./examples/ct/ct.mas"
CTSERVER="ant -f $CTROOT/build.xml runserver" 			# Takes a <port> as argument.
CTADMIN="java -jar $CTJAR -a"  #  -Dconfigfile=$CONFIG" 	# Takes $CONFIG as input (<).
CTCONFIG="$ROOT/lib/adminconfig/GG.txt" 					# Path to  config file.
CTCLIENT="java -jar $CTJAR -c ctgui.original.GUI"			#  --pin 10  --client_hostip $2
DATE="`/bin/date +%Y%m%d-%H:%M`"
TOMCAT="/Users/dxd/Downloads/apache-tomcat-7.0.57/bin"
TOMCATRUN="./startup.sh start -Dcatalina.base=/Users/dxd/Documents/workspace2/.metadata/.plugins/org.eclipse.wst.server.core/tmp0 -Dcatalina.home=/Library/Tomcat -Dwtp.deploy=/Users/dxd/Documents/workspace2 -Djava.endorsed.dirs=/Library/Tomcat/endorsed"
SAFARI="/usr/bin/open -a Safari http://localhost:8080/WebCT/login.html?pin=20"
######################### Section Functions #########################

usage_exit() {
 echo "Usage: `basename $0` <arguments>"
 echo " Start ctserver  : -s <port> " 
 echo " Start ctadmin   : -A <relative path to config file> "
 echo " Start ctclient  : -c <pin> " 
 echo " Start ctagent   : -a <type> "
 echo " Start aplserver : -2 "
 echo " Stop ctserver   : -k "
 exit 1
}

log_error() {
 ME="$(basename $0)"
 echo "$ME: $DATE: $1" 
 logger -p daemon.err "$ME: $DATE $1"
}

error_exit() {
 ME="$(basename $0)"
 echo "ERROR EXIT $DATE: $ME: $1"
 exit 1
}

pidof() {
#ps -avx | grep $1 | cut -d ' ' -f 1
ps | grep -i $1 | awk '{print '\$1'}'
}

check_sanity() {
 	[ -d $CTROOT ] || error_exit "$CTROOT does not exist."
 	[ -d $APLROOT ] || error_exit "$APLROOT does not exist."
 	[ -f $CTJAR ] || error_exit "Could not locate $CTJAR."
 	[ -f $APLJAR ] || error_exit "Could not locate $APLJAR."
 	export PATH="$PATH:$CTROOT:$APLROOT"
 	echo "PATH is set to: $PATH"
 	if [ -f $(which java) ] ; then  
  		echo "Java is present: $(which java)"
  	else error_exit "No java present."
 	fi
	if [ ! -d $CTROOT/logsh ] ; then 
		echo "The logs directory in $CTROOT does not exist. Trying to create it..."
		if $(mkdir -p $CTROOT/logsh/CTserverTC $CTROOT/logsh/CTserver $CTROOT/logsh/CTClient $CTROOT/logsh/CTAdmin $CTROOT/logsh/CTAgent) ; then
			echo "...succesfully created the logs directories in $CTROOT."
		else 
			error_exit "Failed to create the logs directories. Make sure you have the right permissions."
		fi
	fi
	if [ ! -d $APLROOT/logsh ] ; then
		echo "The logs directory in $APLROOT does not exist. Trying to create it..."
		if $(mkdir -p $APLROOT/logsh/APLServer) ; then
			echo "...succesfully created the logs directories in $APLROOT."
		else
			error_exit "Failed to create the logs directories. Make sure you have the right permissions."
		fi
	fi
}

start_ctserver() {
  #   kill `pidof $CTJAR` 2>/dev/null
 	if (cd $CTROOT) ; then
	 	echo "cd to $CTROOT succeeded."
	fi
 	if [ -f $CTROOT/build.xml ] ; then
		echo "Trying to start the CT server..."
  		$CTSERVER >> $CTROOT/logsh/CTserver/CTserverlog$DATE.log 2>&1 &
  		echo "... CT server has been started."
  		export SERVER_PID=$!
  		echo "The server has process ID: $SERVER_PID."
 	else 
		log_error "$DATE: Could not locate $CTJAR." && error_exit "Could not start the CT server."
fi
}

start_ctclient() {
# 	cd $CTJARPATH && echo "cd to $CTJARPATH succeeded."
 	if [ -f $CTJAR ] ; then
  		$CTCLIENT --pin $1 >> $CTROOT/logsh/CTClient/CTclient$1log$DATE.log 2>&1 & 	# --client_hostip $2
  		echo "Client $1 has been started."
 	else 
		log_error "$DATE: Could not locate $CTJAR." && error_exit "Could not start the CT client."
	fi
}

start_ctadmin() {
 	cd $CTROOT && echo "cd to $CTROOT succeeded."
 	export CTCONFIG="$CTROOT/lib/adminconfig/$1"
  	if [ -f $CTJAR ] ; then
  		$CTADMIN < $CTCONFIG >> $CTROOT/logsh/CTAdmin/CTadminlog$DATE.log 2>&1 &
  		echo "Admin loaded configuration file."
 	else 
		log_error "$DATE: Could not locate $CTJAR." && error_exit "Could not start admin."
fi
}

start_ctagent() {
 	cd $CTROOT && echo "cd to $CTROOT succeeded."
 	AGENT=$1
 	eval \$"${AGENT}" >> $CTROOT/logsh/CTAgent/CT$AGENT$DATE.log 2>&1 & 
 	echo "$AGENT agent has been started."
}

stop_ctserver() {
	echo "Trying to bring down the CT server..."
	if (kill `pidof $CTJAR` 2>/dev/null) ; then
		echo "... the server has been stopped."
        sleep 1
 	else {
 		log_error "$DATE: The CT server was not running."
		error_exit "Could not stop the server, for it was not running."
		}
    fi

}

start_aplserver() {
    kill `pidof 2apl.jar` 2>/dev/null
    sleep 1
	echo "Trying to start the 2APL main container..."
	# if [ -f $APLMASDIR/ct.mas ] ; then
			cd $APLROOT && echo "Succesfull cd to $APLROOT."
			$APLSERVER >> ./logsh/APLServer/aplserverlog$DATE.log 2>&1 & #-mas $APLMASDIR/$1
			echo "...the 2APL main container has been started." # " with the specified mas file."
            cd $HOME
	#	else 
	#		error_exit "No mas file was found at $APLMASDIR/"
	# fi
	
}

start_tomcat() {
    kill `pidof bootstrap` 2>/dev/null
    sleep 1
    cd $TOMCAT
    pwd
    $TOMCATRUN >> $HOME/WebCT/logsh/CTserverTC/CTserverlog$DATE.log 2>&1 &
    echo "tomcat has started."

}

start_safari() {
    kill `pidof safari` 2>/dev/null
    sleep 5
    $SAFARI
}

######################### Section getopts ######################

# The script needs arguments

[ -z "$1" ] && usage_exit

# Check if the environment is sane.

check_sanity

# Parse the commandline arguments

 while getopts ":a:c:s:A:k2t" Option

# Initial declaration.
# a, c, s, A, k, and t are the flags expected.
# The : after some flags shows it will have an option passed with it.
# Use $OPTARG

do
case $Option in
a ) start_ctagent $OPTARG ;;
c ) start_ctclient $OPTARG ;;
s ) start_ctserver $OPTARG ;;
A ) start_ctadmin $OPTARG ;;
k ) stop_ctserver;start_aplserver;start_ctserver ;;
t ) echo blaat ;;
2 ) start_aplserver ;;
* ) usage_exit ;;
esac
done
shift $(($OPTIND - 1))

######################### Section TODO #########################
# TODO:
# Wrapperscript schrijven
# Mechanisme bedenken om games te managen

######################### Extra info ###########################
# runtime.jar = ct3.jar
# ct3.jar -s = server
# ct3.jar -a = admin (-Dconfigfile=../CT.txt)
# ct3.jar -c ctgui.original.GUI --pin 10 = runclient1
# ct3.jar -c ctgui.original.GUI --pin 20 = runclient2
# ct3.jar -c ctgui.example.interruptible.GUI --pin 10 --client_localport 8010

# ct3.jar Output:
# Arguments
# =========
# Frontend takes the following arguments: 
# -s                        : Start a server.
# --server_localport <port> : Port to start server on.
# -c <class name>           : Start a GUI client.
# --pin                     : Specify a GUI client pin(required for GUI client.
# --client_localport <port> : Specify a local port for the client to listen on.
# --client_hostip <ip>      : Specify a remote host's IP to connect to.
# -a                        : Start an command line admin client.

# Usage
# =====
# Standard Usage:
# Standalone Server: java -jar ct3.jar -s
# Standalone Client: java -jar ct3.jar -c <gui class name> --pin <pin number>
# Standalone Admin:  java -jar ct3.jar -a
