
AUTOMAKE_OPTIONS = foreign
SUBDIRS = include src 

EXTRA_DIST = AUTHORS LICENSE NOTICE README

data/keystore.ks: data/keytool-responses.txt
	rm -f $@
	$(KEYTOOL) -genkey -keystore $@ -storepass testks -alias spep -keypass testkey -keyalg rsa -keysize 512 < data/keytool-responses.txt

schema: ../saml2cpp/schema
	mkdir -p schema
	cp ../saml2cpp/schema/*.xsd schema/

test: all schema data/keystore.ks
	src/spepcpptest$(EXEEXT)

debug: all schema data/keystore.ks
	gdb src/spepcpptest$(EXEEXT)
