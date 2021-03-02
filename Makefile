# Makefile for Unix
# edit this file accroding to your system
# 'make all' will build 'jsch-xxxx.jar'

SHELL  := /usr/local/bin/bash
PATH   := /usr/local/bin:/opt/bin:${PATH}
JAR    := jar
JAVAC  := javac5
JLIB   := /opt/lib/java
CP     := ${JLIB}/jzlib.jar
MINVER := 1.5

SRCROOT  := src/main/java
JFLAGS   := ${PRESET_JFLAGS} -source ${MINVER} -target ${MINVER} -encoding UTF-8 -Xlint:deprecation -Xlint:unchecked
JFLAGSCP := ${JFLAGS} -classpath ${CP}
VER      := 0.1.55a2
FNAME    := jsch-${VER}

SOURCES := $(shell cd ${SRCROOT} && find . -name '*.java')
OBJECTS := $(subst .java,*.class,${SOURCES})

EXAMPLES:= $(shell cd examples && find . -name '*.java')

all:
	${MAKE} compile
	${MAKE} jar
	${MAKE} compile-examples

compile:
	cd src/main/java && \
	${JAVAC} ${JFLAGSCP} ${SOURCES}

jar:
	cd src/main/java && \
	${JAR} cf ../../../${FNAME}.jar ${OBJECTS}

compile-examples: ${FNAME}.jar
compile-examples: CP := ${CP}:../${FNAME}.jar
compile-examples: JFLAGSCP := ${JFLAGS} -classpath ${CP}
compile-examples:
	cd examples && \
	${JAVAC} ${JFLAGSCP} ${EXAMPLES}

all-debug:
	${MAKE} PRESET_JFLAGS=-g all
