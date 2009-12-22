
includedirs = ['include']
libdirs = []

platform = ARGUMENTS.get('OS', Platform().name)
libdirs += Split(ARGUMENTS.get('libdirs', ''))
includedirs += Split(ARGUMENTS.get('includedirs', ''))

env = Environment(CPPPATH=includedirs, LIBPATH=libdirs, PLATFORM=platform)
env.ParseFlags('-Optimized')
if platform == 'win32':
	env.MergeFlags('-DWIN32 -D_WINDOWS -D_WIN32_WINNT=0x0501')
	env.MergeFlags({'CCFLAGS':Split('/MD /TP /EHsc /W0 /nologo /Ox')})
	env['LINKCOM'] = [env['LINKCOM'], 'mt.exe -nologo -manifest ${TARGET}.manifest -outputresource:${TARGET};#1']
	env['SHLINKCOM'] = [env['SHLINKCOM'], 'mt.exe -nologo -manifest ${TARGET}.manifest -outputresource:${TARGET};#2']

env.VariantDir('build', 'src', duplicate=0)

Return('env')
