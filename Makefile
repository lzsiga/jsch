# Makefile for Unix
# edit this file accroding to your system
# 'make all' will build 'jsch-xxxx.jar'

SHELL  := /usr/local/bin/bash
PATH   := /usr/local/bin:/opt/bin:${PATH}
JAR    := jar
JAVAC  := javac8
JLIB   := /opt/lib/java
CP     := ${JLIB}/jzlib.jar
MINVER := 1.5

SRCROOT:= src/main/java
JFLAGS := -source ${MINVER} -target ${MINVER} -encoding UTF-8 -classpath ${CP}
VER    := 0.1.55a
FNAME  := jsch-${VER}

SOURCES := $(shell cd ${SRCROOT} && find . -name '*.java')
OBJECTS := $(subst .java,*.class,${SOURCES})

all:
	cd src/main/java && \
	${JAVAC} ${JFLAGS} ${SOURCES} && \
	${JAR} cf ../../../${FNAME}.jar ${OBJECTS}

all-verbose: JFLAGS += -Xlint:deprecation -Xlint:unchecked
all-verbose: all
