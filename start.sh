#!/bin/bash

function build()
{
	cd /usr/src/demo-apps/cf-helloworld-server;
	mvn clean install;
}

function buildclient()
{
	cd /usr/src/demo-apps/cf-helloworld-client;
	mvn clean install;
}

function start()
{
	cd /usr/src/demo-apps/run;
	java -jar cf-helloworld-server-1.1.0-SNAPSHOT.jar
}

function default()
{
	buildclient;
}

echo "### starting californium"
if [ "$1" == "" ]
then
	echo "### run default"
	default
elif [ "$1" == "start" ]
then
	echo "### run start"
	start
elif [ "$1" == "build" ]
then
	echo "### run build"
	build
fi 