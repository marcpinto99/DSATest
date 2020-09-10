#!/usr/bin/env bash

# uncomment the next line if you want to remove the existing bin directory (if it exists) before building your project
rm -rf bin

mkdir bin
# My java files are in com/example/client, com/example/server, and com/example/main_code.
# The main function is located in com/example/main_code/main.java

#cp -R com/ bin/



# compile the .java files.
# -d is used to specify the destination directory for the .class files
# bin must be in the classpath because I'm compiling from outside of bin
#I need to set -cp to be bin/ because I'm building from outside of bin.
javac -target 8 -source 8 -sourcepath src -d bin -cp bin/ -cp .:json-java.jar src/com/example/app/MyApp.java


# copy the run.sh script from the root of the project into the bin directory
cp run.sh bin/
cp json-java.jar bin/


#delete line
cp knownhosts.json bin/


echo Done!

exit 0