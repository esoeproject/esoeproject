
includedirs = ['include']
libdirs = []

libdirs_arg = ARGUMENTS.get('libdirs')
if libdirs_arg:
	libdirs += Split(libdirs_arg)

includedirs_arg = ARGUMENTS.get('includedirs')
if includedirs_arg:
	includedirs += Split(includedirs_arg)

env = Environment(CPPPATH=includedirs, LIBPATH=libdirs)

env.VariantDir('build', 'src', duplicate=0)
env.Repository('src')

Return('env')
