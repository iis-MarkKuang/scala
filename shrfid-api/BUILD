target(name='shrfid-api',
  dependencies=[
    'shrfid/shrfid-api/src/main/scala'
  ]
)

target(name='tests',
  dependencies=[
    'shrfid/shrfid-api/src/test/scala'
  ]
)

jvm_binary(
  name='bin',
  basename='shrfid-shrfid-api',
  main='com.shrfid.api.ShrfidApiServerMain',
  dependencies=[
    ':shrfid-api'
  ],
  excludes=[
    exclude('org.slf4j', 'slf4j-jdk14'),
    exclude('log4j', 'log4j')
  ]
)
