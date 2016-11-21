#!/bin/bash
#
# creates the python classes for our .proto
#

project_base="/Users/rahilvora/Desktop/nettyNewGlobal/fluffy"
#rm ${project_base}/src1/comm_pb2.py
protoc -I=${project_base}/resources --python_out=./src ${project_base}/resources/pipe.proto
