import common_pb2
import socket
import time
import struct
import pipe_pb2
import global_pb2

def request_ping():

    cm = global_pb2.GlobalMessage()
    cm.globalHeader.cluster_id = 1
    cm.globalHeader.time = 1000
    #cm.globalHeader.destination_id = 5

    #cm.globalHeader = cmsg
    #cgheader = cm.GlobalHeader()
    #cgheader = cmsg
    cm.ping = True
    print "request_ping() executing"

    pingr = cm.SerializeToString()

    packed_len = struct.pack('>L', len(pingr))
    # Sending Ping Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # host = socket.gethostname() # Testing on own computer
    # port = 5570 # Public Port
    s.connect(("localhost", 4268))
    # Prepending the length field and sending
    s.sendall(packed_len+pingr)
    s.close()




if __name__ == '__main__':
    print "main() executing"
    request_ping()