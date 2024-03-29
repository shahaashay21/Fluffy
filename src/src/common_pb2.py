# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: common.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf.internal import enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='common.proto',
  package='',
  serialized_pb=_b('\n\x0c\x63ommon.proto\"\x95\x01\n\x06Header\x12\x0f\n\x07node_id\x18\x01 \x02(\x05\x12\x0c\n\x04time\x18\x02 \x02(\x03\x12\x15\n\x0bsource_host\x18\x03 \x01(\t:\x00\x12\x1a\n\x10\x64\x65stination_host\x18\x04 \x01(\t:\x00\x12\x0e\n\x06source\x18\x07 \x01(\x05\x12\x13\n\x0b\x64\x65stination\x18\x08 \x01(\x05\x12\x14\n\x08max_hops\x18\n \x01(\x05:\x02-1\"6\n\x07\x46\x61ilure\x12\n\n\x02id\x18\x01 \x02(\x05\x12\x0e\n\x06ref_id\x18\x02 \x01(\x05\x12\x0f\n\x07message\x18\x03 \x01(\t\"P\n\x04\x46ile\x12\x0f\n\x07\x63hunkId\x18\x01 \x01(\x05\x12\x0c\n\x04\x64\x61ta\x18\x02 \x01(\x0c\x12\x10\n\x08\x66ilename\x18\x03 \x02(\t\x12\x17\n\x0ftotalNoOfChunks\x18\x05 \x01(\x05\"u\n\x07Request\x12\x11\n\trequestId\x18\x04 \x02(\t\x12!\n\x0brequestType\x18\x01 \x02(\x0e\x32\x0c.RequestType\x12\x12\n\x08\x66ileName\x18\x02 \x01(\tH\x00\x12\x15\n\x04\x66ile\x18\x03 \x01(\x0b\x32\x05.FileH\x00\x42\t\n\x07payload\"\xa4\x01\n\x08Response\x12\x11\n\trequestId\x18\x06 \x02(\t\x12!\n\x0brequestType\x18\x01 \x02(\x0e\x32\x0c.RequestType\x12\x0f\n\x07success\x18\x02 \x01(\x08\x12\x1b\n\x07\x66\x61ilure\x18\x03 \x01(\x0b\x32\x08.FailureH\x00\x12\x15\n\x04\x66ile\x18\x04 \x01(\x0b\x32\x05.FileH\x00\x12\x12\n\x08\x66ileName\x18\x05 \x01(\tH\x00\x42\t\n\x07payload\")\n\x06Update\x12\x0c\n\x04\x66ile\x18\x02 \x02(\x0c\x12\x11\n\tfile_name\x18\x03 \x02(\t\"p\n\x0e\x43ommandMessage\x12\x17\n\x06header\x18\x01 \x02(\x0b\x32\x07.Header\x12\x0e\n\x04ping\x18\x03 \x01(\x08H\x00\x12\x11\n\x07message\x18\x04 \x01(\tH\x00\x12\x17\n\x03\x65rr\x18\x05 \x01(\x0b\x32\x08.FailureH\x00\x42\t\n\x07payload*:\n\x0bRequestType\x12\x08\n\x04READ\x10\x01\x12\t\n\x05WRITE\x10\x02\x12\n\n\x06UPDATE\x10\x03\x12\n\n\x06\x44\x45LETE\x10\x04\x42\x0f\n\x0bpipe.commonH\x01')
)
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

_REQUESTTYPE = _descriptor.EnumDescriptor(
  name='RequestType',
  full_name='RequestType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='READ', index=0, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='WRITE', index=1, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='UPDATE', index=2, number=3,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='DELETE', index=3, number=4,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=749,
  serialized_end=807,
)
_sym_db.RegisterEnumDescriptor(_REQUESTTYPE)

RequestType = enum_type_wrapper.EnumTypeWrapper(_REQUESTTYPE)
READ = 1
WRITE = 2
UPDATE = 3
DELETE = 4



_HEADER = _descriptor.Descriptor(
  name='Header',
  full_name='Header',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='node_id', full_name='Header.node_id', index=0,
      number=1, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='time', full_name='Header.time', index=1,
      number=2, type=3, cpp_type=2, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='source_host', full_name='Header.source_host', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=True, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='destination_host', full_name='Header.destination_host', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=True, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='source', full_name='Header.source', index=4,
      number=7, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='destination', full_name='Header.destination', index=5,
      number=8, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='max_hops', full_name='Header.max_hops', index=6,
      number=10, type=5, cpp_type=1, label=1,
      has_default_value=True, default_value=-1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=17,
  serialized_end=166,
)


_FAILURE = _descriptor.Descriptor(
  name='Failure',
  full_name='Failure',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='Failure.id', index=0,
      number=1, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ref_id', full_name='Failure.ref_id', index=1,
      number=2, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='message', full_name='Failure.message', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=168,
  serialized_end=222,
)


_FILE = _descriptor.Descriptor(
  name='File',
  full_name='File',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='chunkId', full_name='File.chunkId', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='data', full_name='File.data', index=1,
      number=2, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='filename', full_name='File.filename', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='totalNoOfChunks', full_name='File.totalNoOfChunks', index=3,
      number=5, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=224,
  serialized_end=304,
)


_REQUEST = _descriptor.Descriptor(
  name='Request',
  full_name='Request',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='requestId', full_name='Request.requestId', index=0,
      number=4, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='requestType', full_name='Request.requestType', index=1,
      number=1, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='fileName', full_name='Request.fileName', index=2,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file', full_name='Request.file', index=3,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
    _descriptor.OneofDescriptor(
      name='payload', full_name='Request.payload',
      index=0, containing_type=None, fields=[]),
  ],
  serialized_start=306,
  serialized_end=423,
)


_RESPONSE = _descriptor.Descriptor(
  name='Response',
  full_name='Response',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='requestId', full_name='Response.requestId', index=0,
      number=6, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='requestType', full_name='Response.requestType', index=1,
      number=1, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='success', full_name='Response.success', index=2,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='failure', full_name='Response.failure', index=3,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file', full_name='Response.file', index=4,
      number=4, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='fileName', full_name='Response.fileName', index=5,
      number=5, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
    _descriptor.OneofDescriptor(
      name='payload', full_name='Response.payload',
      index=0, containing_type=None, fields=[]),
  ],
  serialized_start=426,
  serialized_end=590,
)


_UPDATE = _descriptor.Descriptor(
  name='Update',
  full_name='Update',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='file', full_name='Update.file', index=0,
      number=2, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_name', full_name='Update.file_name', index=1,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=592,
  serialized_end=633,
)


_COMMANDMESSAGE = _descriptor.Descriptor(
  name='CommandMessage',
  full_name='CommandMessage',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='header', full_name='CommandMessage.header', index=0,
      number=1, type=11, cpp_type=10, label=2,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ping', full_name='CommandMessage.ping', index=1,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='message', full_name='CommandMessage.message', index=2,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='err', full_name='CommandMessage.err', index=3,
      number=5, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
    _descriptor.OneofDescriptor(
      name='payload', full_name='CommandMessage.payload',
      index=0, containing_type=None, fields=[]),
  ],
  serialized_start=635,
  serialized_end=747,
)

_REQUEST.fields_by_name['requestType'].enum_type = _REQUESTTYPE
_REQUEST.fields_by_name['file'].message_type = _FILE
_REQUEST.oneofs_by_name['payload'].fields.append(
  _REQUEST.fields_by_name['fileName'])
_REQUEST.fields_by_name['fileName'].containing_oneof = _REQUEST.oneofs_by_name['payload']
_REQUEST.oneofs_by_name['payload'].fields.append(
  _REQUEST.fields_by_name['file'])
_REQUEST.fields_by_name['file'].containing_oneof = _REQUEST.oneofs_by_name['payload']
_RESPONSE.fields_by_name['requestType'].enum_type = _REQUESTTYPE
_RESPONSE.fields_by_name['failure'].message_type = _FAILURE
_RESPONSE.fields_by_name['file'].message_type = _FILE
_RESPONSE.oneofs_by_name['payload'].fields.append(
  _RESPONSE.fields_by_name['failure'])
_RESPONSE.fields_by_name['failure'].containing_oneof = _RESPONSE.oneofs_by_name['payload']
_RESPONSE.oneofs_by_name['payload'].fields.append(
  _RESPONSE.fields_by_name['file'])
_RESPONSE.fields_by_name['file'].containing_oneof = _RESPONSE.oneofs_by_name['payload']
_RESPONSE.oneofs_by_name['payload'].fields.append(
  _RESPONSE.fields_by_name['fileName'])
_RESPONSE.fields_by_name['fileName'].containing_oneof = _RESPONSE.oneofs_by_name['payload']
_COMMANDMESSAGE.fields_by_name['header'].message_type = _HEADER
_COMMANDMESSAGE.fields_by_name['err'].message_type = _FAILURE
_COMMANDMESSAGE.oneofs_by_name['payload'].fields.append(
  _COMMANDMESSAGE.fields_by_name['ping'])
_COMMANDMESSAGE.fields_by_name['ping'].containing_oneof = _COMMANDMESSAGE.oneofs_by_name['payload']
_COMMANDMESSAGE.oneofs_by_name['payload'].fields.append(
  _COMMANDMESSAGE.fields_by_name['message'])
_COMMANDMESSAGE.fields_by_name['message'].containing_oneof = _COMMANDMESSAGE.oneofs_by_name['payload']
_COMMANDMESSAGE.oneofs_by_name['payload'].fields.append(
  _COMMANDMESSAGE.fields_by_name['err'])
_COMMANDMESSAGE.fields_by_name['err'].containing_oneof = _COMMANDMESSAGE.oneofs_by_name['payload']
DESCRIPTOR.message_types_by_name['Header'] = _HEADER
DESCRIPTOR.message_types_by_name['Failure'] = _FAILURE
DESCRIPTOR.message_types_by_name['File'] = _FILE
DESCRIPTOR.message_types_by_name['Request'] = _REQUEST
DESCRIPTOR.message_types_by_name['Response'] = _RESPONSE
DESCRIPTOR.message_types_by_name['Update'] = _UPDATE
DESCRIPTOR.message_types_by_name['CommandMessage'] = _COMMANDMESSAGE
DESCRIPTOR.enum_types_by_name['RequestType'] = _REQUESTTYPE

Header = _reflection.GeneratedProtocolMessageType('Header', (_message.Message,), dict(
  DESCRIPTOR = _HEADER,
  __module__ = 'common_pb2'
  # @@protoc_insertion_point(class_scope:Header)
  ))
_sym_db.RegisterMessage(Header)

Failure = _reflection.GeneratedProtocolMessageType('Failure', (_message.Message,), dict(
  DESCRIPTOR = _FAILURE,
  __module__ = 'common_pb2'
  # @@protoc_insertion_point(class_scope:Failure)
  ))
_sym_db.RegisterMessage(Failure)

File = _reflection.GeneratedProtocolMessageType('File', (_message.Message,), dict(
  DESCRIPTOR = _FILE,
  __module__ = 'common_pb2'
  # @@protoc_insertion_point(class_scope:File)
  ))
_sym_db.RegisterMessage(File)

Request = _reflection.GeneratedProtocolMessageType('Request', (_message.Message,), dict(
  DESCRIPTOR = _REQUEST,
  __module__ = 'common_pb2'
  # @@protoc_insertion_point(class_scope:Request)
  ))
_sym_db.RegisterMessage(Request)

Response = _reflection.GeneratedProtocolMessageType('Response', (_message.Message,), dict(
  DESCRIPTOR = _RESPONSE,
  __module__ = 'common_pb2'
  # @@protoc_insertion_point(class_scope:Response)
  ))
_sym_db.RegisterMessage(Response)

Update = _reflection.GeneratedProtocolMessageType('Update', (_message.Message,), dict(
  DESCRIPTOR = _UPDATE,
  __module__ = 'common_pb2'
  # @@protoc_insertion_point(class_scope:Update)
  ))
_sym_db.RegisterMessage(Update)

CommandMessage = _reflection.GeneratedProtocolMessageType('CommandMessage', (_message.Message,), dict(
  DESCRIPTOR = _COMMANDMESSAGE,
  __module__ = 'common_pb2'
  # @@protoc_insertion_point(class_scope:CommandMessage)
  ))
_sym_db.RegisterMessage(CommandMessage)


DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), _b('\n\013pipe.commonH\001'))
# @@protoc_insertion_point(module_scope)
